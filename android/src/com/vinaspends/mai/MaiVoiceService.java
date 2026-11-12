package com.vinaspends.mai;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Nghe "Mai ơi" cả khi khoá màn hình (dịch vụ chạy nền loại "micro", luôn có thông báo "Mai đang nghe").
 *
 * Luồng: CHỜ GỌI (nghe liên tục, bắt "Mai ơi" ngay từ kết quả tạm thời)
 *   -> bật sáng màn hình + hiện giao diện "Mai đang nghe" (kể cả trên màn hình khoá), rung nhẹ
 *   -> NGHE LỆNH (phần còn lại của câu, hoặc câu kế tiếp nếu anh chỉ gọi "Mai ơi")
 *   -> đưa câu vào hàng đợi (MaiStore, packageName = "voice") để trang web gửi Gemini và ghi sổ
 *   -> BẬN cho tới khi trang web báo xong (Mai đọc to kết quả) rồi quay lại CHỜ GỌI.
 */
public class MaiVoiceService extends Service {
    static final String ACTION_START = "com.vinaspends.mai.VOICE_START";
    static final String ACTION_STOP = "com.vinaspends.mai.VOICE_STOP";
    private static final String CHANNEL = "mai_voice";
    private static final String CHANNEL_WAKE = "mai_voice_wake";
    private static final int NOTI_ID = 7301;
    private static final int NOTI_WAKE_ID = 7302;
    private static final int S_WAKE = 0, S_COMMAND = 1, S_BUSY = 2;

    private static volatile MaiVoiceService running;
    /** Đang trong một lượt "Mai ơi" (từ lúc nghe thấy tiếng gọi tới lúc ghi xong). */
    static volatile boolean lockWakeActive;

    private final Handler ui = new Handler(Looper.getMainLooper());
    private SpeechRecognizer recognizer;
    private PowerManager.WakeLock cpuLock, screenLock;
    private boolean stopped;
    private int paused;          // app đang tự dùng micro (bấm nút nói) -> tạm nhường
    private int state = S_WAKE;
    private boolean wakeSeen;    // đã nghe thấy "Mai ơi" trong lượt nghe hiện tại
    private long commandUntil;
    private int failStreak;
    private String lastPartial = "";

    static boolean isRunning() { return running != null; }

    static void start(Context c) {
        Intent i = new Intent(c, MaiVoiceService.class).setAction(ACTION_START);
        if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(i); else c.startService(i);
    }

    static void stop(Context c) {
        c.startService(new Intent(c, MaiVoiceService.class).setAction(ACTION_STOP));
    }

    /** App đang tự dùng micro -> dịch vụ tạm dừng nghe. */
    static void pause() {
        final MaiVoiceService s = running;
        if (s == null) return;
        s.ui.post(new Runnable() { @Override public void run() { s.paused++; s.cancelListening(); } });
    }

    static void resume() {
        final MaiVoiceService s = running;
        if (s == null) return;
        s.ui.postDelayed(new Runnable() { @Override public void run() { if (s.paused > 0) { s.paused--; if (s.paused == 0) s.listenSoon(400); } } }, 600);
    }

    /** Trang web đã xử lý xong câu nói (và Mai đã đọc xong) -> nghe tiếp. */
    static void lockDone() {
        lockWakeActive = false;
        final MaiVoiceService s = running;
        if (s == null) return;
        s.ui.post(new Runnable() { @Override public void run() { s.finishTurn(600); } });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : ACTION_START;
        if (ACTION_STOP.equals(action)) {
            shutdown();
            return START_NOT_STICKY;
        }
        Notification n = buildNotification();
        try {
            if (Build.VERSION.SDK_INT >= 29) startForeground(NOTI_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
            else startForeground(NOTI_ID, n);
        } catch (Exception e) {
            // Android không cho bật micro nền lúc này (vd. app không ở màn hình) -> thôi
            stopSelf();
            return START_NOT_STICKY;
        }
        running = this;
        stopped = false;
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (cpuLock == null) {
            cpuLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Mai:voice");
            cpuLock.setReferenceCounted(false);
        }
        if (!cpuLock.isHeld()) cpuLock.acquire();
        if (state != S_BUSY) listenSoon(300);
        return START_STICKY;
    }

    private int piFlags() { return Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT; }

    private Notification buildNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26 && nm.getNotificationChannel(CHANNEL) == null) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Mai nghe giọng nói", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Hiện khi Mai đang nghe \"Mai ơi\" (kể cả khi khoá màn hình)");
            ch.setShowBadge(false);
            nm.createNotificationChannel(ch);
        }
        PendingIntent open = PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP), piFlags());
        PendingIntent stopPi = PendingIntent.getService(this, 2, new Intent(this, MaiVoiceService.class).setAction(ACTION_STOP), piFlags());
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, CHANNEL) : new Notification.Builder(this);
        b.setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Mai đang nghe")
            .setContentText("Nói \"Mai ơi\" rồi kể khoản thu chi, kể cả khi khoá màn hình")
            .setOngoing(true)
            .setContentIntent(open)
            .addAction(new Notification.Action.Builder(0, "Tắt", stopPi).build());
        if (Build.VERSION.SDK_INT >= 21) b.setVisibility(Notification.VISIBILITY_PUBLIC);
        return b.build();
    }

    // ---------- Vòng nghe ----------
    private void listenSoon(long delay) {
        ui.removeCallbacks(listenRunnable);
        if (!stopped) ui.postDelayed(listenRunnable, delay);
    }

    private final Runnable listenRunnable = new Runnable() {
        @Override public void run() { startListening(); }
    };

    /** Bộ nhận giọng nói đôi khi "im lặng" không trả về gì -> sau 25 giây thì tạo lại. */
    private final Runnable watchdog = new Runnable() {
        @Override public void run() {
            if (stopped || paused > 0 || state == S_BUSY) return;
            resetRecognizer();
            if (state == S_COMMAND && System.currentTimeMillis() > commandUntil) { cancelTurn("Mai chưa nghe rõ"); return; }
            listenSoon(200);
        }
    };

    private final Runnable busyTimeout = new Runnable() {
        @Override public void run() { lockWakeActive = false; finishTurn(300); MainActivity.lockTimedOut(); }
    };

    private void cancelListening() {
        ui.removeCallbacks(listenRunnable);
        ui.removeCallbacks(watchdog);
        if (recognizer != null) { try { recognizer.cancel(); } catch (Exception ignored) { } }
    }

    private void resetRecognizer() {
        if (recognizer != null) { try { recognizer.destroy(); } catch (Exception ignored) { } }
        recognizer = null;
    }

    private void startListening() {
        if (stopped || paused > 0 || state == S_BUSY) return;
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { listenSoon(30000); return; }
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(listener);
        }
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "vi-VN");
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        // lấy nhiều cách nghe: chỉ cần 1 cách nghe ra "Mai ơi" là đủ
        i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        if (Build.VERSION.SDK_INT >= 33) {
            // gợi ý cho bộ nhận giọng nói những cụm hay gặp -> nghe "Mai ơi" chuẩn hơn
            ArrayList<String> bias = new ArrayList<String>();
            bias.add("Mai ơi"); bias.add("Mai ơi anh chi"); bias.add("Mai ơi nãy anh chi hết"); bias.add("tiền mặt"); bias.add("nghìn"); bias.add("triệu");
            i.putStringArrayListExtra(RecognizerIntent.EXTRA_BIASING_STRINGS, bias);
        }
        // đang nghe lệnh: cho ngập ngừng lâu hơn mới coi là nói xong
        long silence = state == S_COMMAND || wakeSeen ? 2500L : 1800L;
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, silence);
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, silence);
        lastPartial = "";
        try {
            recognizer.cancel();
            recognizer.startListening(i);
            armWatchdog();
        } catch (Exception e) {
            resetRecognizer();
            listenSoon(1500);
        }
    }

    private void armWatchdog() {
        ui.removeCallbacks(watchdog);
        ui.postDelayed(watchdog, 25000);
    }

    private final RecognitionListener listener = new RecognitionListener() {
        @Override public void onReadyForSpeech(Bundle b) { armWatchdog(); }
        @Override public void onBeginningOfSpeech() { armWatchdog(); }
        @Override public void onRmsChanged(float v) { }
        @Override public void onBufferReceived(byte[] b) { }
        @Override public void onEndOfSpeech() { armWatchdog(); }
        @Override public void onEvent(int t, Bundle b) { }

        @Override public void onPartialResults(Bundle b) {
            if (stopped || state == S_BUSY) return;
            armWatchdog();
            ArrayList<String> r = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (r == null || r.isEmpty()) return;
            String best = r.get(0) == null ? "" : r.get(0).trim();
            if (best.isEmpty()) return;
            lastPartial = best;
            if (state == S_WAKE) {
                int w = -1;
                for (String s : r) { w = wakeEnd(s); if (w >= 0) { best = s; break; } }
                if (w >= 0 && !wakeSeen) { wakeSeen = true; onWake(); }
                if (wakeSeen) MainActivity.voiceJs(event("lock_partial", w >= 0 ? best.substring(w).trim() : best));
            } else {
                MainActivity.voiceJs(event("lock_partial", best));
            }
        }

        @Override public void onResults(Bundle b) {
            if (stopped || state == S_BUSY) return;
            ui.removeCallbacks(watchdog);
            failStreak = 0;
            ArrayList<String> r = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            ArrayList<String> alts = new ArrayList<String>();
            if (r != null) for (String s : r) if (s != null && !s.trim().isEmpty()) alts.add(s.trim());
            if (alts.isEmpty() && !lastPartial.isEmpty()) alts.add(lastPartial);
            handleFinal(alts);
        }

        @Override public void onError(int error) {
            if (stopped || state == S_BUSY) return;
            ui.removeCallbacks(watchdog);
            if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) { shutdown(); return; }
            boolean quiet = error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT;
            // máy bận / mất kết nối tới dịch vụ nhận giọng nói -> tạo lại cho sạch
            if (!quiet) resetRecognizer();
            failStreak = quiet ? 0 : failStreak + 1;
            long wait = quiet ? 100 : Math.min(20000, 600L * failStreak);

            if (state == S_WAKE && wakeSeen) {
                // đã nghe "Mai ơi" (kết quả tạm) nhưng câu bị đứt -> dùng phần đã nghe, hoặc nghe lệnh tiếp
                ArrayList<String> alts = new ArrayList<String>();
                if (!lastPartial.isEmpty()) alts.add(lastPartial);
                handleFinal(alts);
                return;
            }
            if (state == S_COMMAND) {
                if (System.currentTimeMillis() < commandUntil) { listenSoon(wait); return; }
                cancelTurn(error == SpeechRecognizer.ERROR_NETWORK || error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT ? "Nhận giọng nói cần có mạng" : "Mai chưa nghe rõ");
                return;
            }
            listenSoon(wait);
        }
    };

    private void handleFinal(ArrayList<String> alts) {
        if (state == S_COMMAND) {
            if (alts.isEmpty()) {
                if (System.currentTimeMillis() < commandUntil) listenSoon(100); else cancelTurn("Mai chưa nghe rõ");
                return;
            }
            // anh có thể gọi lại "Mai ơi" ở đầu câu lệnh -> bỏ đi
            ArrayList<String> clean = new ArrayList<String>();
            for (String s : alts) { int w = wakeEnd(s); String t = w >= 0 ? s.substring(w).trim() : s; if (!t.isEmpty()) clean.add(t); }
            if (clean.isEmpty()) { commandUntil = System.currentTimeMillis() + 9000; listenSoon(100); return; }
            deliver(clean);
            return;
        }
        // CHỜ GỌI
        ArrayList<String> rests = new ArrayList<String>();
        boolean found = false;
        for (String s : alts) {
            int w = wakeEnd(s);
            if (w >= 0) { found = true; String t = s.substring(w).trim(); if (!t.isEmpty()) rests.add(t); }
        }
        if (!found && !wakeSeen) { listenSoon(100); return; }
        if (!found) {
            // "Mai ơi" có trong kết quả tạm nhưng kết quả cuối viết khác -> lấy cả câu, Gemini tự lọc
            for (String s : alts) rests.add(s);
        }
        if (!wakeSeen) { wakeSeen = true; onWake(); }
        String best = rests.isEmpty() ? "" : rests.get(0);
        boolean hasDigit = best.matches(".*\\d.*");
        if (best.isEmpty() || (!hasDigit && best.split("\\s+").length < 3)) {
            // mới chỉ gọi "Mai ơi" -> nghe câu kế tiếp
            state = S_COMMAND;
            commandUntil = System.currentTimeMillis() + 10000;
            MainActivity.voiceJs(event("lock_partial", ""));
            listenSoon(80);
            return;
        }
        deliver(rests);
    }

    // ---------- Nghe thấy "Mai ơi" ----------
    private void onWake() {
        lockWakeActive = true;
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        try {
            if (screenLock == null) {
                screenLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Mai:screen");
                screenLock.setReferenceCounted(false);
            }
            screenLock.acquire(30000);
        } catch (Exception ignored) { }
        try {
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (v != null) v.vibrate(60);
        } catch (Exception ignored) { }

        boolean locked = false;
        try { locked = ((KeyguardManager) getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked(); } catch (Exception ignored) { }
        // 1) app còn sống: cho phép hiện trên màn hình khoá + báo trang web hiện "Mai đang nghe"
        MainActivity.onLockWake(locked);
        // 2) đưa app lên trước (được phép khi app đang hiện / có quyền hiện trên ứng dụng khác)
        Intent open = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).putExtra("voice_wake", true);
        try { startActivity(open); } catch (Exception ignored) { }
        // 3) thông báo toàn màn hình: Android tự mở app khi máy đang khoá / tắt màn hình
        try {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 26 && nm.getNotificationChannel(CHANNEL_WAKE) == null) {
                NotificationChannel ch = new NotificationChannel(CHANNEL_WAKE, "Mai trả lời khi gọi \"Mai ơi\"", NotificationManager.IMPORTANCE_HIGH);
                ch.setDescription("Bật sáng màn hình và hiện \"Mai đang nghe\" khi anh gọi Mai");
                ch.setSound(null, null);
                ch.enableVibration(false);
                ch.setShowBadge(false);
                ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                nm.createNotificationChannel(ch);
            }
            PendingIntent full = PendingIntent.getActivity(this, 3, open, piFlags());
            Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, CHANNEL_WAKE) : new Notification.Builder(this);
            b.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Mai đang nghe…")
                .setContentText("Anh kể khoản thu chi đi ạ")
                .setCategory(Notification.CATEGORY_CALL)
                .setAutoCancel(true)
                .setContentIntent(full)
                .setFullScreenIntent(full, true);
            if (Build.VERSION.SDK_INT < 26) b.setPriority(Notification.PRIORITY_MAX);
            if (Build.VERSION.SDK_INT >= 21) b.setVisibility(Notification.VISIBILITY_PUBLIC);
            nm.notify(NOTI_WAKE_ID, b.build());
        } catch (Exception ignored) { }
    }

    private void deliver(ArrayList<String> alts) {
        String command = alts.get(0);
        try {
            JSONObject o = new JSONObject();
            o.put("packageName", "voice");
            o.put("title", "v" + System.currentTimeMillis()); // khác nhau mỗi lần để hàng đợi không coi là tin trùng
            o.put("text", command);
            JSONArray a = new JSONArray();
            for (int k = 1; k < alts.size() && k < 4; k++) a.put(alts.get(k));
            o.put("alts", a);
            o.put("postTime", System.currentTimeMillis());
            MaiStore.add(this, o);
        } catch (Exception ignored) { }
        // chờ trang web xử lý + Mai đọc xong rồi mới nghe tiếp (không nghe nhầm chính giọng Mai)
        state = S_BUSY;
        wakeSeen = false;
        cancelListening();
        ui.removeCallbacks(busyTimeout);
        ui.postDelayed(busyTimeout, 45000);
        MainActivity.voiceJs(event("lock_thinking", command));
        MainActivity.deliverVoice();
    }

    /** Gọi "Mai ơi" nhưng không nghe được lệnh. */
    private void cancelTurn(String why) {
        state = S_BUSY;
        wakeSeen = false;
        cancelListening();
        MainActivity.voiceJs(event("lock_cancel", why));
        // trang web hiện lỗi vài giây rồi gọi lockDone(); phòng khi trang web không chạy
        ui.removeCallbacks(busyTimeout);
        ui.postDelayed(busyTimeout, 6000);
    }

    private void finishTurn(long delay) {
        ui.removeCallbacks(busyTimeout);
        state = S_WAKE;
        wakeSeen = false;
        try { ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(NOTI_WAKE_ID); } catch (Exception ignored) { }
        try { if (screenLock != null && screenLock.isHeld()) screenLock.release(); } catch (Exception ignored) { }
        listenSoon(delay);
    }

    private static String event(String type, String text) {
        try {
            JSONObject o = new JSONObject();
            o.put("type", type);
            o.put("text", text == null ? "" : text);
            return o.toString();
        } catch (Exception e) { return "{}"; }
    }

    // ---------- Nhận ra tiếng gọi "Mai ơi" ----------
    /** Bỏ dấu, GIỮ NGUYÊN độ dài chuỗi (mỗi ký tự gốc -> đúng 1 ký tự) để vị trí khớp với câu gốc. */
    static String fold(String s) {
        if (s == null) return "";
        String nfc = Normalizer.normalize(s, Normalizer.Form.NFC);
        StringBuilder sb = new StringBuilder(nfc.length());
        for (int k = 0; k < nfc.length(); k++) {
            char c = nfc.charAt(k);
            if (c == 'đ' || c == 'Đ') { sb.append('d'); continue; }
            String d = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD);
            char base = d.isEmpty() ? c : d.charAt(0);
            sb.append(Character.toLowerCase(base));
        }
        return sb.toString();
    }

    // Máy hay nghe "Mai ơi" thành: mai ơi, mai ôi, may ơi, mây ơi, mài ơi, my ơi, mai ui, mai à, mai ạ, mai ới, mai hỡi...
    private static final Pattern WAKE = Pattern.compile(
        "(?<![a-z0-9])(?:mai+|may|mei|mae|my|nai)[\\s,.!-]*(?:oi+|ui|oy|uoi|hoi|a)(?![a-z0-9])[\\s,.!:;-]*");

    /** Vị trí ngay sau tiếng gọi "Mai ơi" (cuối cùng) trong câu, -1 nếu câu không gọi Mai. */
    static int wakeEnd(String raw) {
        if (raw == null) return -1;
        String nfc = Normalizer.normalize(raw, Normalizer.Form.NFC);
        Matcher m = WAKE.matcher(fold(nfc));
        int end = -1;
        while (m.find()) end = m.end();
        if (end < 0) return -1;
        // raw có thể không ở dạng NFC -> quy đổi vị trí tương đối an toàn
        return nfc.length() == raw.length() ? end : Math.min(raw.length(), end + (raw.length() - nfc.length()));
    }

    private void shutdown() {
        stopped = true;
        running = null;
        lockWakeActive = false;
        cancelListening();
        ui.removeCallbacks(busyTimeout);
        resetRecognizer();
        if (cpuLock != null && cpuLock.isHeld()) cpuLock.release();
        try { if (screenLock != null && screenLock.isHeld()) screenLock.release(); } catch (Exception ignored) { }
        try { ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(NOTI_WAKE_ID); } catch (Exception ignored) { }
        try { stopForeground(true); } catch (Exception ignored) { }
        stopSelf();
        MainActivity.voiceServiceChanged();
    }

    @Override
    public void onDestroy() {
        stopped = true;
        running = null;
        lockWakeActive = false;
        cancelListening();
        ui.removeCallbacks(busyTimeout);
        resetRecognizer();
        if (cpuLock != null && cpuLock.isHeld()) cpuLock.release();
        try { if (screenLock != null && screenLock.isHeld()) screenLock.release(); } catch (Exception ignored) { }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
