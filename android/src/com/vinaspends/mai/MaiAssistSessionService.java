package com.vinaspends.mai;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.service.voice.VoiceInteractionSessionService;

/**
 * Phiên làm việc của Trợ lý ảo Mai:
 * Khi anh giữ nút nguồn / vuốt góc màn hình / giữ phím Home,
 * Android gọi vào đây -> mở ngay giao diện "Mai đang nghe" (kể cả trên màn hình khoá).
 */
public class MaiAssistSessionService extends VoiceInteractionSessionService {
    @Override
    public VoiceInteractionSession onNewSession(Bundle args) {
        return new MaiSession(this);
    }

    private static class MaiSession extends VoiceInteractionSession {
        MaiSession(Context context) {
            super(context);
        }

        @Override
        public void onShow(Bundle args, int showFlags) {
            super.onShow(args, showFlags);
            Intent i = new Intent(getContext(), MainActivity.class)
                    .setAction(Intent.ACTION_ASSIST)
                    .putExtra("assist_launch", true)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            try {
                getContext().startActivity(i);
            } catch (Exception ignored) {
            }
            finish();
        }
    }
}
