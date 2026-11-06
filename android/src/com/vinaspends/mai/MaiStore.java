package com.vinaspends.mai;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Hàng đợi thông báo giao dịch chờ Mai xử lý. Lưu trong SharedPreferences nên vẫn còn
 * khi app bị tắt hoặc điện thoại khởi động lại; app mở lên sẽ lấy ra và ghi sổ.
 */
final class MaiStore {
    private static final String PREFS = "mai_queue";
    private static final String KEY = "items";
    private static final String KEY_RECENT = "recent";
    private static final int MAX_ITEMS = 300;

    private MaiStore() {}

    static synchronized boolean add(Context ctx, JSONObject item) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        try {
            // Một số app đăng lại cùng thông báo nhiều lần (cập nhật, nhóm thông báo) -> bỏ trùng 15 phút
            String sig = item.optString("packageName") + "|" + item.optString("title") + "|" + item.optString("text");
            long now = System.currentTimeMillis();
            JSONArray recent = new JSONArray(sp.getString(KEY_RECENT, "[]"));
            JSONArray keep = new JSONArray();
            for (int i = 0; i < recent.length(); i++) {
                JSONObject r = recent.getJSONObject(i);
                if (now - r.optLong("t") > 15 * 60 * 1000L) continue;
                if (sig.equals(r.optString("s"))) return false;
                keep.put(r);
            }
            JSONObject mark = new JSONObject();
            mark.put("s", sig);
            mark.put("t", now);
            keep.put(mark);

            JSONArray arr = new JSONArray(sp.getString(KEY, "[]"));
            arr.put(item);
            JSONArray trimmed = arr;
            if (arr.length() > MAX_ITEMS) {
                trimmed = new JSONArray();
                for (int i = arr.length() - MAX_ITEMS; i < arr.length(); i++) trimmed.put(arr.get(i));
            }
            sp.edit().putString(KEY, trimmed.toString()).putString(KEY_RECENT, keep.toString()).apply();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static synchronized String drain(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String s = sp.getString(KEY, "[]");
        sp.edit().putString(KEY, "[]").apply();
        return s;
    }

    static synchronized int size(Context ctx) {
        try {
            return new JSONArray(ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]")).length();
        } catch (Exception e) {
            return 0;
        }
    }
}
