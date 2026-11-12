package com.vinaspends.mai;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

/**
 * Ô "Gọi Mai" trên bảng Cài đặt nhanh (Quick Settings):
 * Vuốt thanh thông báo xuống (kể cả khi khoá máy), bấm là Mai bật lên nghe ngay lập tức.
 * Hoàn toàn không tốn 1% pin nào ở chế độ chờ.
 */
public class MaiTileService extends TileService {
    @Override
    public void onClick() {
        super.onClick();
        Intent intent = new Intent(this, MainActivity.class)
                .setAction(Intent.ACTION_ASSIST)
                .putExtra("assist_launch", true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            if (Build.VERSION.SDK_INT >= 34) {
                PendingIntent pi = PendingIntent.getActivity(
                        this,
                        888,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                );
                startActivityAndCollapse(pi);
            } else {
                startActivityAndCollapse(intent);
            }
        } catch (Exception e) {
            try {
                startActivity(intent);
            } catch (Exception ignored) {
            }
        }
    }
}
