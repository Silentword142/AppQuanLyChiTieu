package com.vinaspends.mai;

import android.app.Notification;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Bước 1 của bộ phân tích thông báo:
 *  - Chỉ nhận thông báo từ các ứng dụng tài chính (ngân hàng, ví điện tử, thẻ/tài chính)
 *    hoặc SMS từ brandname ngân hàng / ví.
 *  - Chặn ngay thông báo từ app chat, mạng xã hội, mua sắm, gọi xe, OTP thuần, hoặc tin không có tiền.
 * Bước 2 (hiểu nội dung & tự động thêm giao dịch) do mai.js / Gemini thực hiện.
 */
public class MaiNotificationListener extends NotificationListenerService {

    static final Set<String> FINANCE_PKGS = new HashSet<String>(Arrays.asList(
            // Ngân hàng Việt Nam & Quốc tế
            "com.VCB", "com.vcb", "com.vietcombank.digibank",
            "vn.com.techcombank.bb.app", "com.fastacash.tcb",
            "com.mbmobile", "com.mbbank.mobile",
            "com.vnpay.bidv", "com.bidv.smartbanking",
            "com.vietinbank.ipay",
            "com.vnpay.Agribank3g", "com.agribank.mobile",
            "mobile.acb.com.vn", "com.acb.one",
            "com.tpb.mb.gprsandroid", "com.tpbank.mb",
            "com.vnpay.vpbankonline", "com.vpbank.neo", "com.vpb.neo",
            "com.sacombank.ewallet", "com.sacombank.mbanking",
            "com.vib.myvib2", "com.vib.myvib",
            "vn.shb.mbanking", "vn.shb.saha.mbanking",
            "com.shinhan.global.vn.bank",
            "com.vnpay.hdbank", "com.hdbank.mobilebanking",
            "vn.com.ocb.awe",
            "vn.com.msb.smartBanking", "com.msb.smartBanking",
            "vn.com.seabank.mb1",
            "vn.com.lienvietpostbank",
            "com.vnpay.eximbank",
            "com.namabank.app",
            "com.bab.retailUAT", "com.bacabank.mobilebanking",
            "com.pvcombank.retail",
            "com.vnpay.abb", "com.abbank.mobile", "vn.abbank.ditto",
            "com.bvbank.digimi", "vn.banvietbank.digimi",
            "com.vnpay.klb", "com.kienlongbank.kienlongbank",
            "com.ncb.izimobile", "vn.ncb.izimobile",
            "com.vnpay.vietbank", "com.vnpay.vab", "com.vnpay.baoviet",
            "com.vnpay.oceanbank", "com.oceanbank.mbv",
            "com.dongabank.ebanking", "vn.vikki.app", "vn.vikki.bank",
            "xyz.be.cake", "io.lifestyle.plus",
            "com.ubank.app", "vn.vpbank.ubank",
            "com.liobank.app", "vn.ocb.liobank",
            "vn.tnex.app", "com.msb.tnex",
            "vn.kasikornbank.kplus", "com.kasikornbank.kplus.vn",
            "com.hsbc.hsbcvietnam", "vn.hsbc.mobilebanking",
            "com.sc.mobilebanking.vn",
            "com.uob.mighty.vn", "com.uob.tmrw.vn",
            "com.woori.won.vn", "com.cimb.octo.vn", "com.publicbank.pbvn",
            // Ví điện tử & cổng thanh toán
            "com.mservice.momotransfer", "com.mservice.momowallet",
            "vn.com.vng.zalopay",
            "com.beeasy.shopee.vn", "com.shopeepay.vn", "com.airpay.vn.merchant",
            "com.viettel.vtt.vn.emoneycustomer", "com.bplus.vtpay",
            "com.vnptpay.android", "vn.vnpt.vnptpay", "vn.vnpt.epay",
            "com.vnpay.app", "vn.vnpay.wallet",
            "vn.payoo.payoo", "com.fpt.foxpay", "vn.ninepay.wallet", "com.vtc.vtcpay",
            "com.google.android.apps.walletnfcrel", "com.samsung.android.spay",
            // Tài chính tiêu dùng / Trả sau / Thẻ tín dụng
            "vn.fecredit.app", "vn.homecredit.app", "com.homecredit.app",
            "com.shinhanfinance.ishinhan", "vn.hdsaison.app", "com.miraeasset.mytouch",
            "vn.kredivo.app", "vn.fundiin.app"
    ));

    static final Set<String> SMS_PKGS = new HashSet<String>(Arrays.asList(
            "com.google.android.apps.messaging", "com.samsung.android.messaging",
            "com.android.mms", "com.android.messaging", "com.oneplus.mms", "com.miui.mms",
            "com.coloros.mms", "com.oplus.mms", "com.vivo.message", "com.huawei.message", "com.asus.message"
    ));

    // Các app chắc chắn KHÔNG phải ứng dụng tài chính -> chặn ngay để không nhầm tin nhắn chat / đơn hàng
    static final Set<String> NON_FINANCE_PKGS = new HashSet<String>(Arrays.asList(
            "com.zing.zalo", "com.facebook.orca", "com.facebook.katana", "com.facebook.lite",
            "org.telegram.messenger", "org.telegram.messenger.web",
            "com.whatsapp", "com.whatsapp.w4b", "com.viber.voip", "jp.naver.line.android",
            "com.discord", "com.Slack", "com.microsoft.teams",
            "com.instagram.android", "com.ss.android.ugc.trill", "com.zhiliaoapp.musically",
            "com.snapchat.android", "com.twitter.android",
            "com.shopee.vn", "com.lazada.android", "vn.tiki.app.tikiandroid", "com.sendo.buyer",
            "com.grabtaxi.passenger", "com.gojek.app", "xyz.be.rider", "com.be.app", "com.deliverynow",
            "com.google.android.gm", "com.microsoft.office.outlook",
            "com.android.chrome", "com.google.android.youtube"
    ));

    private static final Pattern FINANCE_PKG_RE = Pattern.compile(
            "(vnpay|vietcombank|techcombank|vpbank|tpbank|sacombank|vietinbank|agribank|hdbank|bidv|seabank|shinhan|kienlong|bvbank|liobank|viettelmoney|vtpay|vnptpay|vnptmoney|zalopay|shopeepay|momo|fecredit|homecredit|kredivo|hdsaison|mirae|fundiin|hsbc|woori|cimb|ubank|tnex|mbanking|smartbanking|digibank|ebanking|ewallet|mobilebanking)"
    );

    // Có số tiền: 1,234,567(.00) hoặc 50000 VND / 500đ / +50,000 / ngoại tệ USD, EUR...
    private static final Pattern MONEY = Pattern.compile(
            "(?i)(?:[+\\-−]\\s?(?:VND|VNĐ|₫|USD|\\$)?\\s?(?:\\d{1,3}(?:[.,]\\d{3})+(?:[.,]\\d{1,2})?|\\d{3,}(?:[.,]\\d{1,2})?))"
                    + "|(?:(?:\\d{1,3}(?:[.,]\\d{3})+(?:[.,]\\d{1,2})?|\\d+(?:[.,]\\d{1,2})?)\\s?(?:VND|VNĐ|đ|₫|dong|đồng|USD|EUR|SGD|JPY|KRW|GBP|AUD|THB|CNY)\\b)"
                    + "|(?:(?:^|[\\s:|(])\\d+(?:[.,]\\d{3})*\\s?d\\b)"
                    + "|(?:(?:VND|VNĐ|₫|USD|EUR|SGD|JPY|\\$)\\s?(?:\\d{1,3}(?:[.,]\\d{3})+(?:[.,]\\d{1,2})?|\\d+(?:[.,]\\d{1,2})?))"
    );

    private static final Pattern BANK_BRAND_RE = Pattern.compile(
            "(?i)\\b(vietcombank|vcb|techcombank|tcb|mbbank|mb bank|mb\\s*:|tk\\s*mb|bidv|vietinbank|agribank|acb|tpbank|tpb|vpbank|vpb|sacombank|stb|vib|shb|hdbank|hdb|ocb|msb|seabank|lpbank|lienvietpostbank|eximbank|nam a ?bank|bac a ?bank|pvcombank|abbank|bvbank|ban viet|kienlongbank|klb|ncb|baoviet ?bank|vietbank|vietabank|mbv|vikki|shinhan|hsbc|standard chartered|uob|woori|cimb|public bank|cake|timo|ubank|liobank|tnex|kbank|k plus|momo|zalopay|shopeepay|viettel ?money|viettelpay|vnpt ?money|vnpt ?pay|vnpay|payoo|foxpay|9pay|vtcpay|fe ?credit|home ?credit|hd ?saison|mirae|kredivo|fundiin)\\b"
    );

    private static final Pattern STRICT_BANK_MSG_RE = Pattern.compile(
            "(?i)(\\b(sd\\s*tk|so du\\s*tk|số dư\\s*tk|bien dong so du|biến động số dư|the tin dung|thẻ tín dụng|han muc kha dung|hạn mức khả dụng|du no hien tai|dư nợ|ghi co|ghi no|tai khoan.{0,20}so du|số dư:|so du:|sd:|sdc:)\\b)"
    );

    private static final Pattern PHONE_TITLE_RE = Pattern.compile("^\\+?\\d[\\d\\s.\\-()]{6,16}\\d$");

    private static final Pattern PURE_OTP_RE = Pattern.compile(
            "(?i)\\b(otp|ma xac thuc|mã xác thực|ma xac nhan|mã xác nhận|smart ?otp|passcode|verification code)\\b"
    );

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

            String pkg = sbn.getPackageName();
            if (isNonFinancePkg(pkg)) return;

            String title = str(ex.getCharSequence(Notification.EXTRA_TITLE)).trim();
            String sub = str(ex.getCharSequence(Notification.EXTRA_SUB_TEXT)).trim();
            String text = extractBody(ex, title);
            if (!sub.isEmpty() && !title.contains(sub) && !text.contains(sub)) {
                text = text.isEmpty() ? sub : (sub + "\n" + text);
            }
            if (text.trim().isEmpty()) return;

            String combined = (title + "\n" + text).trim();
            if (!isFinancialNotification(pkg, title, combined)) return;
            if (!MONEY.matcher(combined).find()) return;
            if (PURE_OTP_RE.matcher(combined).find() && !STRICT_BANK_MSG_RE.matcher(combined).find()) return;

            JSONObject o = new JSONObject();
            o.put("packageName", pkg);
            o.put("title", title);
            o.put("text", text.length() > 1500 ? text.substring(0, 1500) : text);
            o.put("postTime", sbn.getPostTime());
            if (MaiStore.add(this, o)) MainActivity.notifyNewItems();
        } catch (Throwable ignored) {
        }
    }

    static boolean isNonFinancePkg(String pkg) {
        if (pkg == null) return true;
        String p = pkg.toLowerCase(Locale.ROOT);
        if (NON_FINANCE_PKGS.contains(pkg) || NON_FINANCE_PKGS.contains(p)) return true;
        return p.startsWith("com.zing.zalo") || p.startsWith("com.facebook.")
                || p.startsWith("org.telegram.") || p.startsWith("com.whatsapp");
    }

    static boolean isFinanceAppPkg(String pkg) {
        if (pkg == null || isNonFinancePkg(pkg)) return false;
        if (FINANCE_PKGS.contains(pkg)) return true;
        String p = pkg.toLowerCase(Locale.ROOT);
        for (String k : FINANCE_PKGS) if (k.toLowerCase(Locale.ROOT).equals(p)) return true;
        return FINANCE_PKG_RE.matcher(p).find();
    }

    static boolean isFinancialNotification(String pkg, String title, String combined) {
        if (isFinanceAppPkg(pkg)) return true;
        if (pkg != null && SMS_PKGS.contains(pkg.toLowerCase(Locale.ROOT))) {
            // Chặn SMS từ số điện thoại cá nhân để không nhầm tin nhắn hỏi vay / đòi nợ
            String cleanTitle = title == null ? "" : title.trim();
            if (!cleanTitle.isEmpty() && PHONE_TITLE_RE.matcher(cleanTitle).matches()) return false;
            return BANK_BRAND_RE.matcher(cleanTitle).find()
                    || cleanTitle.equalsIgnoreCase("MB")
                    || STRICT_BANK_MSG_RE.matcher(combined).find()
                    || BANK_BRAND_RE.matcher(combined).find();
        }
        // App khác không nằm trong danh sách chat/mua sắm: chỉ nhận khi có tên ngân hàng/ví + dấu hiệu tài chính rõ ràng
        return BANK_BRAND_RE.matcher(combined).find() && STRICT_BANK_MSG_RE.matcher(combined).find();
    }

    private static String extractBody(Bundle ex, String title) {
        String text = str(ex.getCharSequence(Notification.EXTRA_TEXT)).trim();
        String big = str(ex.getCharSequence(Notification.EXTRA_BIG_TEXT)).trim();
        if (big.length() > text.length()) text = big;

        CharSequence[] lines = ex.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
        if (lines != null && lines.length > 0) {
            StringBuilder sb = new StringBuilder();
            int limit = Math.min(lines.length, 10);
            for (int i = 0; i < limit; i++) {
                String line = str(lines[i]).trim();
                if (line.isEmpty() || text.contains(line)) continue;
                if (sb.length() > 0) sb.append('\n');
                sb.append(line);
            }
            if (sb.length() > 0) {
                text = text.length() < 20 ? sb.toString() : (text + "\n" + sb.toString());
            }
        }

        if (text.isEmpty()) {
            try {
                Parcelable[] msgs = ex.getParcelableArray("android.messages");
                if (msgs != null && msgs.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (Parcelable p : msgs) {
                        if (!(p instanceof Bundle)) continue;
                        Bundle mb = (Bundle) p;
                        String sender = str(mb.getCharSequence("sender")).trim();
                        String msgText = str(mb.getCharSequence("text")).trim();
                        if (msgText.isEmpty()) continue;
                        if (sb.length() > 0) sb.append('\n');
                        if (!sender.isEmpty() && !sender.equals(title)) sb.append(sender).append(": ");
                        sb.append(msgText);
                    }
                    text = sb.toString().trim();
                }
            } catch (Throwable ignored) {
            }
        }
        return text;
    }

    private static String str(CharSequence cs) {
        return cs == null ? "" : cs.toString();
    }
}

