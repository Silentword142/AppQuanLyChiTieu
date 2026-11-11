package com.vinaspends.mai;

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
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import org.json.JSONObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Nghe "Mai ơi" cả khi khoá màn hình (dịch vụ chạy nền loại "micro", luôn có thông báo "Mai đang nghe").
 * Câu nói sau "Mai ơi" được đưa vào hàng đợi (MaiStore, packageName = "voice"); trang web của app
 * gửi cho Gemini để hiểu và tự ghi giao dịch, rồi Mai đọc to kết quả.
 */
public class MaiVoiceService extends Service {
    static final String ACTION_START = "com.vinaspends.mai.VOICE_START";
    static final String ACTION_STOP = "com.vinaspends.mai.VOICE_STOP";
    private static final String CHANNEL = "mai_voice";
    private static final int NOTI_ID = 7301;

    private static volatile MaiVoiceService running;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private boolean ttsReady;
    private PowerManager.WakeLock wakeLock;
    private boolean stopped;
    private int paused;           // app đang dùng micro (bấm nút nói) -> tạm nhường
    private boolean waitingCommand; // vừa nghe "Mai ơi" -> câu tiếp theo là lệnh
    private long commandUntil;
    private int failStreak;

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
        s.ui.postDelayed(new Runnable() { @Override public void run() { if (s.paused > 0) s.paused--; s.listenSoon(400); } }, 600);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) { tts.setLanguage(new Locale("vi", "VN")); ttsReady = true; }
            }
        });
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
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Mai:voice");
            wakeLock.setReferenceCounted(false);
        }
        if (!wakeLock.isHeld()) wakeLock.acquire();
        listenSoon(300);
        return START_STICKY;
    }

    private Notification buildNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26 && nm.getNotificationChannel(CHANNEL) == null) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Mai nghe giọng nói", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Hiện khi Mai đang nghe \"Mai ơi\" (kể cả khi khoá màn hình)");
            ch.setShowBadge(false);
            nm.createNotificationChannel(ch);
        }
        int piFlags = Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0;
        PendingIntent open = PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP), piFlags);
        PendingIntent stopPi = PendingIntent.getService(this, 2, new Intent(this, MaiVoiceService.class).setAction(ACTION_STOP), piFlags);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, CHANNEL) : new Notification.Builder(this);
        b.setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Mai đang nghe")
            .setContentText("Nói \"Mai ơi, …\" để ghi khoản thu chi, kể cả khi khoá màn hình")
            .setOngoing(true)
            .setContentIntent(open)
            .addAction(new Notification.Action.Builder(0, "Tắt", stopPi).build());
        if (Build.VERSION.SDK_INT >= 21) b.setVisibility(Notification.VISIBILITY_PUBLIC);
        return b.build();
    }

    private void listenSoon(long delay) {
        ui.removeCallbacks(listenRunnable);
        if (!stopped) ui.postDelayed(listenRunnable, delay);
    }

    private final Runnable listenRunnable = new Runnable() {
        @Override public void run() { startListening(); }
    };

    private void cancelListening() {
        ui.removeCallbacks(listenRunnable);
        if (recognizer != null) { try { recognizer.cancel(); } catch (Exception ignored) { } }
    }

    private void startListening() {
        if (stopped || paused > 0) return;
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { listenSoon(30000); return; }
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle b) { }
                @Override public void onBeginningOfSpeech() { }
                @Override public void onRmsChanged(float v) { }
                @Override public void onBufferReceived(byte[] b) { }
                @Override public void onEndOfSpeech() { }
                @Override public void onPartialResults(Bundle b) { }
                @Override public void onEvent(int t, Bundle b) { }
                @Override public void onError(int error) {
                    if (stopped) return;
                    if (waitingCommand && System.currentTimeMillis() > commandUntil) waitingCommand = false;
                    if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) { shutdown(); return; }
                    boolean quiet = error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT;
                    failStreak = quiet ? 0 : failStreak + 1;
                    // không nói gì -> nghe lại ngay; lỗi mạng / máy bận -> chờ lâu dần
                    listenSoon(quiet ? 150 : Math.min(30000, 800L * failStreak));
                }
                @Override public void onResults(Bundle b) {
                    failStreak = 0;
                    ArrayList<String> r = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    handleText(r != null && !r.isEmpty() ? r.get(0) : "");
                }
            });
        }
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "vi-VN");
        i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L);
        try {
            recognizer.cancel();
            recognizer.startListening(i);
        } catch (Exception e) {
            listenSoon(2000);
        }
    }

    static String fold(String s) {
        String n = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).trim();
    }

    /** "mai oi nay anh chi 45k" -> độ dài phần "mai oi" ở đầu câu (theo chữ gốc), -1 nếu không gọi Mai. */
    static int wakeLength(String raw) {
        String f = fold(raw);
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(?:(?:e|nay|alo|hey)\\s+)?(?:mai|may)\\s*(?:oi|a|oi\\s+oi)?\\b[\\s,.!:]*").matcher(f);
        if (!m.find()) return -1;
        // bắt buộc có "ơi" / "à" sau "Mai" để không bắt nhầm câu thường có chữ "mai" (vd. "mai đi chợ")
        if (!m.group().matches(".*(?:mai|may)\\s*(?:oi|a)\\b.*")) return -1;
        return Math.min(raw.length(), m.end());
    }

    private void handleText(String raw) {
        String text = raw == null ? "" : raw.trim();
        if (waitingCommand && System.currentTimeMillis() <= commandUntil) {
            waitingCommand = false;
            if (!text.isEmpty()) deliver(text);
            listenSoon(1500);
            return;
        }
        waitingCommand = false;
        int w = wakeLength(text);
        if (w < 0) { listenSoon(150); return; }
        String rest = text.substring(w).trim();
        if (rest.isEmpty()) {
            // chỉ gọi "Mai ơi" -> trả lời "Dạ" rồi nghe câu tiếp theo
            say("Dạ");
            waitingCommand = true;
            commandUntil = System.currentTimeMillis() + 12000;
            listenSoon(900);
            return;
        }
        deliver(rest);
        listenSoon(2500);
    }

    private void deliver(String command) {
        try {
            JSONObject o = new JSONObject();
            o.put("packageName", "voice");
            o.put("title", "");
            o.put("text", command);
            o.put("postTime", System.currentTimeMillis());
            MaiStore.add(this, o);
        } catch (Exception ignored) {
        }
        // App còn chạy (kể cả đang khoá màn hình) -> xử lý ngay; nếu app đã bị tắt hẳn thì giữ lại, mở app là ghi
        if (!MainActivity.deliverVoice()) say("Dạ, Mai nhớ rồi. Anh mở app để Mai ghi nhé");
    }

    private void say(String text) {
        if (ttsReady) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mai");
    }

    private void shutdown() {
        stopped = true;
        running = null;
        cancelListening();
        if (recognizer != null) { try { recognizer.destroy(); } catch (Exception ignored) { } recognizer = null; }
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        try { stopForeground(true); } catch (Exception ignored) { }
        stopSelf();
        MainActivity.voiceServiceChanged();
    }

    @Override
    public void onDestroy() {
        stopped = true;
        running = null;
        cancelListening();
        if (recognizer != null) { try { recognizer.destroy(); } catch (Exception ignored) { } }
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
