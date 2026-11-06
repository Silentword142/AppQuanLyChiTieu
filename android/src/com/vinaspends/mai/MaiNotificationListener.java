package com.vinaspends.mai;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Nghe thông báo của app ngân hàng / ví / tin nhắn SMS. Chỉ giữ lại thông báo có dạng
 * biến động số dư; mọi thứ khác bị bỏ qua ngay và không được lưu.
 * Việc hiểu nội dung (số tiền, thu/chi, hạng mục...) do Mai (mai.js) làm, hoàn toàn offline.
 */
public class MaiNotificationListener extends NotificationListenerService {

    static final Set<String> KNOWN = new HashSet<String>(Arrays.asList(
            // Ngân hàng
            "com.VCB", "vn.com.techcombank.bb.app", "com.mbmobile", "com.vnpay.bidv", "com.vietinbank.ipay",
            "com.vnpay.Agribank3g", "mobile.acb.com.vn", "com.tpb.mb.gprsandroid", "com.vnpay.vpbankonline",
            "com.vpbank.neo", "com.sacombank.ewallet", "com.vib.myvib2", "vn.shb.mbanking", "vn.shb.saha.mbanking",
            "com.shinhan.global.vn.bank", "com.vnpay.hdbank", "vn.com.ocb.awe", "vn.com.msb.smartBanking",
            "vn.com.seabank.mb1", "vn.com.lienvietpostbank", "com.vnpay.eximbank", "com.namabank.app",
            "com.pvcombank.retail", "xyz.be.cake", "io.lifestyle.plus",
            // Ví điện tử
            "com.mservice.momotransfer", "vn.com.vng.zalopay", "com.beeasy.shopee.vn",
            "com.viettel.vtt.vn.emoneycustomer", "com.bplus.vtpay",
            // Tin nhắn SMS
            "com.google.android.apps.messaging", "com.samsung.android.messaging", "com.android.mms",
            "com.android.messaging", "com.oneplus.mms", "com.miui.mms"
    ));

    // Có số tiền dạng 1,234,000 kèm dấu +/- hoặc đơn vị tiền
    private static final Pattern MONEY = Pattern.compile(
            "([+\\-−]\\s?(VND\\s?)?\\d{1,3}([.,]\\d{3})+)|(\\d{1,3}([.,]\\d{3})+\\s?(VND|VNĐ|vnd|vnđ|đ|₫|d\\b))|((VND|₫)\\s?\\d{1,3}([.,]\\d{3})+)");

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            if (sbn == null || getPackageName().equals(sbn.getPackageName())) return;
            Notification n = sbn.getNotification();
            if (n == null) return;
            if ((n.flags & Notification.FLAG_ONGOING_EVENT) != 0) return;
            if ((n.flags & Notification.FLAG_GROUP_SUMMARY) != 0) return;
            Bundle ex = n.extras;
            if (ex == null) return;

            String title = str(ex.getCharSequence(Notification.EXTRA_TITLE));
            String text = str(ex.getCharSequence(Notification.EXTRA_TEXT));
            String big = str(ex.getCharSequence(Notification.EXTRA_BIG_TEXT));
            if (big.length() > text.length()) text = big;
            CharSequence[] lines = ex.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
            if (lines != null && lines.length > 0 && text.length() < 20) {
                StringBuilder sb = new StringBuilder();
                for (CharSequence l : lines) { if (sb.length() > 0) sb.append('\n'); sb.append(l); }
                text = sb.toString();
            }
            if (text.trim().isEmpty()) return;

            String pkg = sbn.getPackageName();
            boolean known = KNOWN.contains(pkg);
            boolean money = MONEY.matcher(title + "\n" + text).find();
            if (!money) return;                       // không có số tiền -> không phải biến động số dư
            if (!known && !looksLikeBank(title + " " + text)) return;

            JSONObject o = new JSONObject();
            o.put("packageName", pkg);
            o.put("title", title);
            o.put("text", text.length() > 1500 ? text.substring(0, 1500) : text);
            o.put("postTime", sbn.getPostTime());
            if (MaiStore.add(this, o)) MainActivity.notifyNewItems();
        } catch (Throwable ignored) {
        }
    }

    private static boolean looksLikeBank(String s) {
        String l = s.toLowerCase();
        return l.contains("số dư") || l.contains("so du") || l.contains("sd:") || l.contains("tk ")
                || l.contains("tài khoản") || l.contains("giao dịch") || l.contains("biến động")
                || l.contains("thanh toán") || l.contains("nhận tiền") || l.contains("chuyển tiền");
    }

    private static String str(CharSequence cs) {
        return cs == null ? "" : cs.toString();
    }
}
