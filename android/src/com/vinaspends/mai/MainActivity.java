package com.vinaspends.mai;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.os.PowerManager;
import android.view.WindowManager;
import android.content.ClipData;
import android.content.pm.PackageManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    // Gốc ảo để WebView có origin https cố định (localStorage bền, không phụ thuộc file://)
    static final String HOST = "appassets.androidplatform.net";
    static final String START_URL = "https://" + HOST + "/index.html";
    private static final int REQ_FILE = 41;
    private static final int REQ_SAVE = 42;
    private static final int REQ_MIC = 43;
    private SpeechRecognizer recognizer;
    private boolean pendingVoiceStart;

    private static volatile MainActivity live;
    private static volatile MainActivity current; // còn sống (kể cả khi khoá màn hình / chạy nền)
    private static final int REQ_MIC_LOCK = 44;
    private boolean lockShown; // đang được phép hiện trên màn hình khoá (lượt "Mai ơi")
    private WebView web;
    private ValueCallback<Uri[]> fileCallback;
    private String pendingSave;
    private TextToSpeech tts;
    private boolean ttsReady;
    private final Handler ui = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#F2E6CC"));
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setTextZoom(100);
        s.setUserAgentString(s.getUserAgentString() + " VinaSpendsApp/2.0");

        web.addJavascriptInterface(new Bridge(), "MaiBridge");
        web.setWebViewClient(new AssetClient());
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                String[] types = p.getAcceptTypes();
                String type = (types != null && types.length > 0 && types[0] != null && !types[0].isEmpty()) ? types[0] : "*/*";
                if (type.startsWith(".")) type = "*/*";
                i.setType(type);
                if (p.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE) i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                try {
                    startActivityForResult(Intent.createChooser(i, "Chọn tệp"), REQ_FILE);
                } catch (ActivityNotFoundException e) {
                    fileCallback = null;
                    return false;
                }
                return true;
            }
        });

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(new Locale("vi", "VN"));
                    ttsReady = true;
                }
            }
        });

        current = this;
        if (MaiVoiceService.lockWakeActive) applyLockShow(true);
        handleShare(getIntent());
        web.loadUrl(START_URL);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getBooleanExtra("voice_wake", false) && MaiVoiceService.lockWakeActive) applyLockShow(true);
        handleShare(intent);
        callJs("window.onMaiResume && window.onMaiResume()");
    }

    // Chia sẻ tin nhắn SMS / văn bản vào VinaSpends -> đưa cho Mai xử lý
    private void handleShare(Intent intent) {
        if (intent == null || !Intent.ACTION_SEND.equals(intent.getAction())) return;
        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (text == null || text.trim().isEmpty()) return;
        try {
            JSONObject o = new JSONObject();
            o.put("packageName", "share");
            o.put("title", "");
            o.put("text", text);
            o.put("postTime", System.currentTimeMillis());
            MaiStore.add(this, o);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        live = this;
        callJs("window.onMaiResume && window.onMaiResume()");
        // báo trang web: app vừa được mở lên (vd. "Hey Google, mở Mai") -> Mai có thể nghe luôn
        callJs("window.onMaiAppOpen && window.onMaiAppOpen()");
    }

    @Override
    protected void onPause() {
        live = null;
        stopVoice();
        callJs("window.onMaiVoice && window.onMaiVoice({type:'paused'})");
        super.onPause();
    }

    // ---------- Nhận giọng nói (SpeechRecognizer của Android, tiếng Việt) ----------
    private void voiceEvent(String type, String text, int code) {
        try {
            JSONObject o = new JSONObject();
            o.put("type", type);
            if (text != null) o.put("text", text);
            if (code != 0) o.put("code", code);
            callJs("window.onMaiVoice && window.onMaiVoice(" + o.toString() + ")");
        } catch (Exception ignored) {
        }
    }

    private boolean hasMic() {
        return Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void startVoice() {
        if (!hasMic()) {
            pendingVoiceStart = true;
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQ_MIC);
            return;
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { voiceEvent("error", "unavailable", -1); return; }
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle b) { voiceEvent("ready", null, 0); }
                @Override public void onBeginningOfSpeech() { voiceEvent("speech", null, 0); }
                @Override public void onRmsChanged(float v) { }
                @Override public void onBufferReceived(byte[] b) { }
                @Override public void onEndOfSpeech() { voiceEvent("end", null, 0); }
                @Override public void onError(int error) { MaiVoiceService.resume(); voiceEvent("error", null, error); }
                @Override public void onResults(Bundle b) {
                    MaiVoiceService.resume();
                    ArrayList<String> r = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    try {
                        JSONObject o = new JSONObject();
                        o.put("type", "final");
                        o.put("text", r != null && !r.isEmpty() ? r.get(0) : "");
                        org.json.JSONArray alts = new org.json.JSONArray();
                        if (r != null) for (int k = 1; k < r.size() && k < 4; k++) alts.put(r.get(k));
                        o.put("alts", alts);
                        callJs("window.onMaiVoice && window.onMaiVoice(" + o.toString() + ")");
                    } catch (Exception e) { voiceEvent("final", r != null && !r.isEmpty() ? r.get(0) : "", 0); }
                }
                @Override public void onPartialResults(Bundle b) {
                    ArrayList<String> r = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (r != null && !r.isEmpty()) voiceEvent("partial", r.get(0), 0);
                }
                @Override public void onEvent(int t, Bundle b) { }
            });
        }
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "vi-VN");
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 4);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        if (Build.VERSION.SDK_INT >= 33) {
            ArrayList<String> bias = new ArrayList<String>();
            bias.add("Mai ơi"); bias.add("tiền mặt"); bias.add("nghìn"); bias.add("triệu");
            i.putStringArrayListExtra(RecognizerIntent.EXTRA_BIASING_STRINGS, bias);
        }
        // im lặng 2,5 giây mới coi là nói xong (câu dài có ngập ngừng)
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2500L);
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L);
        try {
            MaiVoiceService.pause();
            recognizer.cancel();
            recognizer.startListening(i);
            voiceEvent("start", null, 0);
        } catch (Exception e) {
            voiceEvent("error", e.getMessage(), -2);
        }
    }

    private void stopVoice() {
        if (recognizer != null) {
            try { recognizer.cancel(); } catch (Exception ignored) { }
            MaiVoiceService.resume();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQ_MIC_LOCK) {
            boolean okLock = results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED;
            if (okLock) startLockVoice(); else voiceEvent("perm_denied", null, 0);
            return;
        }
        if (requestCode != REQ_MIC) return;
        boolean ok = results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED;
        voiceEvent(ok ? "perm_ok" : "perm_denied", null, 0);
        if (ok && pendingVoiceStart) startVoice();
        pendingVoiceStart = false;
    }

    @Override
    protected void onDestroy() {
        if (current == this) current = null;
        if (tts != null) tts.shutdown();
        if (recognizer != null) { try { recognizer.destroy(); } catch (Exception ignored) { } }
        super.onDestroy();
    }

    // Vuốt cạnh màn hình / nút Back: để app web tự xử lý (đóng cửa sổ, về mục trước).
    // App không thoát bằng vuốt; chỉ thoát nếu trang web chưa tải được.
    @Override
    public void onBackPressed() {
        if (web == null) { super.onBackPressed(); return; }
        web.evaluateJavascript("(function(){try{return window.onAndroidBack?(window.onAndroidBack()?'1':'0'):'x'}catch(e){return 'x'}})()",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String v) {
                        if (v != null && v.contains("x")) {
                            if (web.canGoBack()) web.goBack();
                            else MainActivity.super.onBackPressed();
                        }
                    }
                });
    }

    // ---------- Lượt "Mai ơi": bật sáng màn hình, hiện giao diện đang nghe trên màn hình khoá ----------
    private void applyLockShow(boolean on) {
        lockShown = on;
        try {
            if (Build.VERSION.SDK_INT >= 27) {
                setShowWhenLocked(on);
                setTurnScreenOn(on);
            } else if (on) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            }
            if (on) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Exception ignored) {
        }
    }

    private boolean keyguardLocked() {
        try { return ((KeyguardManager) getSystemService(KEYGUARD_SERVICE)).isKeyguardLocked(); } catch (Exception e) { return false; }
    }

    /** Dịch vụ vừa nghe thấy "Mai ơi". */
    static void onLockWake(final boolean locked) {
        final MainActivity a = current;
        if (a == null) return;
        a.ui.post(new Runnable() {
            @Override public void run() {
                a.applyLockShow(true);
                a.stopVoiceQuiet();
                if (a.web != null) a.web.evaluateJavascript("window.onMaiVoice && window.onMaiVoice({type:'lock_wake',locked:" + locked + "})", null);
            }
        });
    }

    static void voiceJs(String json) {
        final MainActivity a = current;
        if (a != null) a.callJs("window.onMaiVoice && window.onMaiVoice(" + json + ")");
    }

    /** Trang web không báo xong kịp -> tự thu dọn. */
    static void lockTimedOut() {
        final MainActivity a = current;
        if (a == null) return;
        a.ui.post(new Runnable() { @Override public void run() { a.endLockShow(); a.callJs("window.onMaiVoice && window.onMaiVoice({type:'lock_end'})"); } });
    }

    private void endLockShow() {
        if (!lockShown) return;
        boolean locked = keyguardLocked();
        applyLockShow(false);
        // máy vẫn đang khoá -> ẩn app đi, không để sổ chi tiêu nằm trên màn hình khoá
        if (locked) { try { moveTaskToBack(true); } catch (Exception ignored) { } }
    }

    private void stopVoiceQuiet() {
        if (recognizer != null) { try { recognizer.cancel(); } catch (Exception ignored) { } }
    }

    /** Dịch vụ nghe "Mai ơi" vừa nhận một câu -> nếu app còn sống thì xử lý ngay. */
    static boolean deliverVoice() {
        final MainActivity a = current;
        if (a == null) return false;
        a.callJs("window.onMaiResume && window.onMaiResume()");
        return true;
    }

    static void voiceServiceChanged() {
        final MainActivity a = current;
        if (a != null) a.callJs("window.onMaiVoiceService && window.onMaiVoiceService(" + MaiVoiceService.isRunning() + ")");
    }

    private void startLockVoice() {
        if (!hasMic()) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQ_MIC_LOCK);
            return;
        }
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, REQ_MIC_LOCK + 1);
        }
        try { MaiVoiceService.start(this); } catch (Exception e) { voiceEvent("lock_error", e.getMessage(), 0); }
        ui.postDelayed(new Runnable() { @Override public void run() { voiceServiceChanged(); } }, 800);
    }

    /** Gọi từ dịch vụ nghe thông báo khi có giao dịch mới mà app đang mở. */
    static void notifyNewItems() {
        final MainActivity a = live;
        if (a != null) a.callJs("window.onMaiResume && window.onMaiResume()");
    }

    private void callJs(final String js) {
        ui.post(new Runnable() {
            @Override
            public void run() {
                if (web != null) web.evaluateJavascript(js, null);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_FILE && fileCallback != null) {
            Uri[] res = null;
            if (resultCode == RESULT_OK && data != null) {
                ClipData clip = data.getClipData();
                if (clip != null && clip.getItemCount() > 0) {
                    res = new Uri[clip.getItemCount()];
                    for (int k = 0; k < clip.getItemCount(); k++) res[k] = clip.getItemAt(k).getUri();
                } else if (data.getData() != null) res = new Uri[]{data.getData()};
            }
            fileCallback.onReceiveValue(res);
            fileCallback = null;
        } else if (requestCode == REQ_SAVE) {
            boolean ok = false;
            if (resultCode == RESULT_OK && data != null && data.getData() != null && pendingSave != null) {
                try {
                    OutputStream os = getContentResolver().openOutputStream(data.getData());
                    os.write(pendingSave.getBytes("UTF-8"));
                    os.close();
                    ok = true;
                } catch (Exception ignored) {
                }
            }
            pendingSave = null;
            callJs("window.onMaiFileSaved && window.onMaiFileSaved(" + ok + ")");
        }
    }

    private boolean hasListenerAccess() {
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (flat == null) return false;
        ComponentName me = new ComponentName(this, MaiNotificationListener.class);
        for (String part : flat.split(":")) {
            ComponentName cn = ComponentName.unflattenFromString(part);
            if (me.equals(cn)) return true;
        }
        return false;
    }

    /** Cầu nối JavaScript: window.MaiBridge.* */
    private class Bridge {
        @JavascriptInterface public boolean isAndroid() { return true; }
        @JavascriptInterface public boolean hasNotificationAccess() { return hasListenerAccess(); }
        /** Bản Lite không khai báo dịch vụ đọc thông báo. */
        @JavascriptInterface public boolean canListen() {
            try {
                getPackageManager().getServiceInfo(new ComponentName(MainActivity.this, MaiNotificationListener.class), 0);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        @JavascriptInterface public String drainQueue() { return MaiStore.drain(MainActivity.this); }
        @JavascriptInterface public int queueSize() { return MaiStore.size(MainActivity.this); }

        @JavascriptInterface public void openNotificationAccessSettings() {
            ui.post(new Runnable() {
                @Override public void run() {
                    // Android 11+: mở thẳng trang bật quyền của riêng VinaSpends
                    if (Build.VERSION.SDK_INT >= 30) {
                        try {
                            Intent d = new Intent("android.settings.NOTIFICATION_LISTENER_DETAIL_SETTINGS");
                            d.putExtra("android.provider.extra.NOTIFICATION_LISTENER_COMPONENT_NAME",
                                    new ComponentName(MainActivity.this, MaiNotificationListener.class).flattenToString());
                            startActivity(d);
                            return;
                        } catch (Exception ignored) {
                        }
                    }
                    try {
                        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    } catch (Exception e) {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                    }
                }
            });
        }

        /** Trang "Thông tin ứng dụng" – nơi có menu ⋮ "Cho phép chế độ cài đặt bị hạn chế" (Android 13+). */
        @JavascriptInterface public void openAppInfo() {
            ui.post(new Runnable() {
                @Override public void run() {
                    try {
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
                    } catch (Exception e) {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                    }
                }
            });
        }
        @JavascriptInterface public int sdk() { return Build.VERSION.SDK_INT; }
        @JavascriptInterface public boolean canVoice() { return SpeechRecognizer.isRecognitionAvailable(MainActivity.this); }
        @JavascriptInterface public boolean hasMicPermission() { return hasMic(); }
        @JavascriptInterface public void startVoice() { ui.post(new Runnable() { @Override public void run() { MainActivity.this.startVoice(); } }); }
        @JavascriptInterface public boolean isLockVoice() { return MaiVoiceService.isRunning(); }
        @JavascriptInterface public boolean isLockWake() { return MaiVoiceService.lockWakeActive; }
        @JavascriptInterface public boolean isLocked() { return keyguardLocked(); }
        /** Trang web xử lý xong lượt "Mai ơi" (đã ghi / báo lỗi xong). */
        @JavascriptInterface public void lockDone() {
            ui.post(new Runnable() { @Override public void run() { MaiVoiceService.lockDone(); endLockShow(); } });
        }
        @JavascriptInterface public boolean batteryUnrestricted() {
            try { return Build.VERSION.SDK_INT < 23 || ((PowerManager) getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName()); } catch (Exception e) { return true; }
        }
        @JavascriptInterface public void requestBatteryUnrestricted() {
            ui.post(new Runnable() { @Override public void run() {
                try { startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName()))); }
                catch (Exception e) { try { startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)); } catch (Exception ignored) { } }
            } });
        }
        @JavascriptInterface public boolean canFullScreen() {
            try { return Build.VERSION.SDK_INT < 34 || ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).canUseFullScreenIntent(); } catch (Exception e) { return true; }
        }
        @JavascriptInterface public void openFullScreenSettings() {
            ui.post(new Runnable() { @Override public void run() {
                try { startActivity(new Intent("android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT", Uri.parse("package:" + getPackageName()))); }
                catch (Exception e) { try { startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()))); } catch (Exception ignored) { } }
            } });
        }
        @JavascriptInterface public void startLockVoice() { ui.post(new Runnable() { @Override public void run() { MainActivity.this.startLockVoice(); } }); }
        @JavascriptInterface public void stopLockVoice() { ui.post(new Runnable() { @Override public void run() { MaiVoiceService.stop(MainActivity.this); ui.postDelayed(new Runnable() { @Override public void run() { voiceServiceChanged(); } }, 500); } }); }
        @JavascriptInterface public void stopVoice() { ui.post(new Runnable() { @Override public void run() { MainActivity.this.stopVoice(); } }); }

        @JavascriptInterface public void speak(String text) {
            if (ttsReady && text != null) tts.speak(text, TextToSpeech.QUEUE_ADD, null, "mai" + System.nanoTime());
        }

        @JavascriptInterface public void setStatusBarColor(final String hex, final boolean darkIcons) {
            ui.post(new Runnable() {
                @Override public void run() {
                    try {
                        Window w = getWindow();
                        int c = Color.parseColor(hex);
                        w.setStatusBarColor(c);
                        w.setNavigationBarColor(c);
                        web.setBackgroundColor(c);
                        View d = w.getDecorView();
                        int f = d.getSystemUiVisibility();
                        if (darkIcons) f |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; else f &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                        if (Build.VERSION.SDK_INT >= 26) {
                            if (darkIcons) f |= 0x00000010; else f &= ~0x00000010; // SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                        }
                        d.setSystemUiVisibility(f);
                    } catch (Exception ignored) {
                    }
                }
            });
        }

        /** Lưu file sao lưu (JSON) ra nơi người dùng chọn. */
        @JavascriptInterface public void saveFile(final String name, final String mime, final String content) {
            pendingSave = content;
            ui.post(new Runnable() {
                @Override public void run() {
                    Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType(mime == null || mime.isEmpty() ? "application/json" : mime);
                    i.putExtra(Intent.EXTRA_TITLE, name);
                    try {
                        startActivityForResult(i, REQ_SAVE);
                    } catch (Exception e) {
                        pendingSave = null;
                    }
                }
            });
        }

        @JavascriptInterface public void openUrl(final String url) {
            ui.post(new Runnable() {
                @Override public void run() {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception ignored) {}
                }
            });
        }
    }

    /** Phục vụ dist/www (đóng gói trong assets/www) dưới origin https ảo, chạy offline 100%. */
    private class AssetClient extends WebViewClient {
        private final Map<String, String> mime = new HashMap<String, String>();

        AssetClient() {
            mime.put("html", "text/html");
            mime.put("js", "application/javascript");
            mime.put("css", "text/css");
            mime.put("json", "application/json");
            mime.put("woff2", "font/woff2");
            mime.put("png", "image/png");
            mime.put("webp", "image/webp");
            mime.put("svg", "image/svg+xml");
            mime.put("wasm", "application/wasm");
            mime.put("gz", "application/octet-stream");
            mime.put("mjs", "text/javascript");
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
            Uri u = req.getUrl();
            if (!HOST.equals(u.getHost())) return null;
            String path = u.getPath();
            if (path == null || path.equals("/") || path.isEmpty()) path = "/index.html";
            if (path.contains("..")) return null;
            String ext = path.substring(path.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
            String type = mime.containsKey(ext) ? mime.get(ext) : "application/octet-stream";
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Access-Control-Allow-Origin", "*");
            headers.put("Cache-Control", "no-cache");
            try {
                InputStream in = getAssets().open("www" + path);
                String enc = type.startsWith("text/") || type.equals("application/javascript") ? "UTF-8" : null;
                return new WebResourceResponse(type, enc, 200, "OK", headers, in);
            } catch (Exception e) {
                return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", headers, null);
            }
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri u = Uri.parse(url);
            if (HOST.equals(u.getHost())) return false;
            String scheme = u.getScheme();
            if ("http".equals(scheme) || "https".equals(scheme) || "mailto".equals(scheme) || "tel".equals(scheme)) {
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) {}
                return true;
            }
            return true;
        }
    }
}
