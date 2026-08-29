package com.example.service

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FinanceNotificationListener : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var repository: FinanceRepository

    companion object {
        private val processedNotifications = java.util.concurrent.ConcurrentHashMap<String, Long>()
    }

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(applicationContext)
        repository = FinanceRepository(database.transactionDao(), database.bankAccountDao(), database.debtDao(), applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = (sbn.packageName ?: "").lowercase()
        val extras = sbn.notification.extras
        
        // 1. Defensively retrieve and combine all available text fields
        val titleOriginal = (extras.getString("android.title") 
            ?: extras.getCharSequence("android.title")?.toString() 
            ?: "").trim()
        val title = titleOriginal.lowercase()
        
        val textOriginal = (extras.getCharSequence("android.text") 
            ?: extras.getCharSequence("android.bigText") 
            ?: extras.getCharSequence("android.infoText") 
            ?: extras.getCharSequence("android.subText") 
            ?: "").toString().trim()
        val textLower = textOriginal.lowercase()
        
        // Build unified content representing the full notification text
        val combinedOriginal = if (titleOriginal.isNotEmpty() && textOriginal.isNotEmpty()) {
            if (textOriginal.contains(titleOriginal, ignoreCase = true)) {
                textOriginal
            } else {
                "$titleOriginal | $textOriginal"
            }
        } else if (titleOriginal.isNotEmpty()) {
            titleOriginal
        } else {
            textOriginal
        }
        
        val rawTextOriginal = combinedOriginal
        val text = combinedOriginal.lowercase()

        Log.d("VinaSpendsListener", "Nhận được thông báo từ: $packageName | Title: $title | Text: $text")

        // 2. Check if package belongs to popular Vietnamese banking/e-wallet or SMS/messaging apps
        val isPackageMatch = packageName.contains("vietcombank") || 
                              packageName.contains("tcbmobile") || 
                              packageName.contains("mbmobile") || 
                              packageName.contains("mservice.momo") ||
                              packageName.contains("vnpay") ||
                              packageName.contains("viettelpay") ||
                              packageName.contains("vpbank") ||
                              packageName.contains("tpbank") ||
                              packageName.contains("acb") ||
                              packageName.contains("sacombank") ||
                              packageName.contains("zalopay") ||
                              packageName.contains("bidv") ||
                              packageName.contains("shb") ||
                              packageName.contains("vib") ||
                              packageName.contains("hsbc") ||
                              packageName.contains("scb") ||
                              packageName.contains("cake") ||
                              packageName.contains("timo") ||
                              packageName.contains("shinhan") ||
                              packageName.contains("ocb") ||
                              packageName.contains("hdb") ||
                              packageName.contains("agri") ||
                              packageName.contains("vietin") ||
                              packageName.contains("sms") || 
                              packageName.contains("mms") || 
                              packageName.contains("messaging") ||
                              packageName.contains("message") ||
                              packageName.contains("bank") ||
                              packageName.contains("wallet") ||
                              packageName.contains("pay") ||
                              packageName.contains("technisys") ||
                              packageName.contains("techcombank") ||
                              packageName.contains("zalo") ||
                              packageName.contains("viber") ||
                              packageName.contains("telegram") ||
                              packageName.contains("gmail") ||
                              packageName.contains("outlook") ||
                              packageName.contains("mail")

        // 3. Robust financial and credit card keywords check
        val hasFinancialKeywords = (text.contains("tk") || text.contains("giao dich") || text.contains("giao dịch") ||
                                    text.contains("so du") || text.contains("số dư") || text.contains("so tk") || text.contains("số tk") ||
                                    text.contains("biến động") || text.contains("bien dong") || text.contains("chuyen khoan") ||
                                    text.contains("chuyển khoản") || text.contains("thanh toan") || text.contains("thanh toán") ||
                                    text.contains("rut tien") || text.contains("rút tiền") || text.contains("nhan tien") ||
                                    text.contains("nhận tiền") || text.contains("chi tiêu") || text.contains("chi tieu") ||
                                    text.contains("thẻ và tài khoản") || text.contains("thẻ tín dụng") || text.contains("the tin dung") ||
                                    text.contains("thẻ visa") || text.contains("the visa") || text.contains("hạn mức") || text.contains("han muc") ||
                                    text.contains("thẻ") || text.contains("the") || text.contains("card")) && 
                                   (text.contains("vnd") || text.contains("đ") || text.contains("dđ") || text.contains("d") ||
                                    text.contains("+") || text.contains("-") || text.contains("trừ") || text.contains("tru"))

        val isSmsApp = packageName.contains("sms") || 
                       packageName.contains("mms") || 
                       packageName.contains("messaging") ||
                       packageName.contains("message")

        val hasAccountIndicator = text.contains("tk") || text.contains("tài khoản") || text.contains("tai khoan") ||
                                  text.contains("so du tk") || text.contains("số dư tk") || text.contains("so tk") || text.contains("số tk") ||
                                  text.contains("thẻ") || text.contains("the") || text.contains("card") || text.contains("acc") || text.contains("account") ||
                                  text.contains("trích nợ") || text.contains("trich no") || text.contains("thụ hưởng") || text.contains("thu huong") ||
                                  text.contains(Regex("\\b[0-9]{3,}[x*]{2,}[0-9]*\\b")) ||
                                  text.contains(Regex("\\b[x*]{2,}[0-9]{3,}\\b")) ||
                                  text.contains(Regex("\\b[0-9]{4,20}\\b"))

        // 4. Bulletproof Fallback: if the notification content is unmistakably a financial/card transaction,
        // we parse it regardless of the origin app (even if package check is not triggered).
        val hasStrongFinancialSignatures = 
            (text.contains("biến động số dư") || text.contains("bien dong so du") ||
             text.contains("số dư tài khoản") || text.contains("so du tai khoan") ||
             text.contains("hạn mức khả dụng") || text.contains("han muc kha dung") ||
             text.contains("thẻ tín dụng") || text.contains("the tin dung") ||
             text.contains("chi tiêu qua thẻ") || text.contains("chi tieu qua the") ||
             text.contains("thanh toán thẻ") || text.contains("thanh toan the") ||
             text.contains("giao dịch thẻ") || text.contains("giao dich the") ||
             text.contains("trừ tiền") || text.contains("tru tien") ||
             text.contains("phát sinh giao dịch") || text.contains("phat sinh giao dich") ||
             text.contains("gd the") || text.contains("gd thẻ")) &&
            (text.contains("vnd") || text.contains("đ") || text.contains("dđ") || text.contains("d") ||
             text.contains("+") || text.contains("-") || text.contains("trừ") || text.contains("tru"))

        val isTargetApp = if (isSmsApp) {
            (isPackageMatch && hasFinancialKeywords && hasAccountIndicator) || hasStrongFinancialSignatures
        } else {
            (isPackageMatch && hasFinancialKeywords) || hasStrongFinancialSignatures
        }

        // Filter out promotional/advertising messages unless they contain clear indicators of real transaction
        val isPromoOrAd = text.contains("khuyen mai") || text.contains("khuyến mãi") || text.contains("khuyến mại") ||
                          text.contains("uu dai") || text.contains("ưu đãi") ||
                          text.contains("quang cao") || text.contains("quảng cáo") ||
                          text.contains("voucher") || text.contains("quà tặng") || text.contains("qua tang") ||
                          text.contains("quà 0đ") || text.contains("nhập mã") || text.contains("nhap ma") ||
                          text.contains("hoàn tiền lên tới") || text.contains("hoan tien len toi") ||
                          text.contains("vinh danh") || text.contains("nhan dip") || text.contains("nhân dịp") ||
                          text.contains("nhận tới") || text.contains("nhan toi") || text.contains("tặng ngay") ||
                          text.contains("tang ngay") || text.contains("tặng voucher") || text.contains("tang voucher") ||
                          text.contains("quay số") || text.contains("quay so") || text.contains("trúng thưởng") || text.contains("trung thuong") ||
                          text.contains("nhận quà") || text.contains("nhan qua") || text.contains("gói quà") || text.contains("goi qua") ||
                          text.contains("điểm thưởng") || text.contains("diem thuong") || text.contains("bốc thăm") || text.contains("xu momo") ||
                          text.contains("vay nhanh") || text.contains("vay siêu tốc") || text.contains("mở thẻ nhận") || text.contains("chiêu đãi") ||
                          text.contains("deal hời") || text.contains("deal hot") || text.contains("deal soc") || text.contains("deal sốc")

        val hasClearBalanceIndicator = text.contains("biến động số dư") || text.contains("bien dong so du") ||
                                       text.contains("so du tk") || text.contains("số dư tk") ||
                                       text.contains("giao dich khoang") || text.contains("giao dịch khoảng") ||
                                       text.contains("phat sinh") || text.contains("phát sinh") ||
                                       text.contains("tk chu tai khoan") || text.contains("tk chủ tài khoản") ||
                                       text.contains("da thanh toan") || text.contains("đã thanh toán") ||
                                       text.contains("giao dịch thành công") || text.contains("giao dich thanh cong") ||
                                       text.contains("so du vi") || text.contains("số dư ví") ||
                                       text.contains("kính gửi") || text.contains("kinh gui") ||
                                       text.contains("chi tiêu") || text.contains("chi tieu") ||
                                       text.contains("hạn mức khả dụng") || text.contains("han muc kha dung") ||
                                       text.contains("giao dịch thẻ") || text.contains("giao dich the")

        val shouldSkip = isPromoOrAd && !hasClearBalanceIndicator

        if (isTargetApp && !shouldSkip && rawTextOriginal.isNotBlank()) {
            val notificationText = "$packageName | $title | $rawTextOriginal".lowercase()
            val currentTime = System.currentTimeMillis()

            // Remove entries processed over 30 seconds ago
            processedNotifications.entries.removeIf { currentTime - it.value > 30000 }

            if (processedNotifications.containsKey(notificationText)) {
                Log.d("VinaSpendsListener", "Deduplicated active notification: $notificationText")
                return
            }
            processedNotifications[notificationText] = currentTime

            scope.launch {
                // Parse automatically using our Gemini AI model config and personal key if set
                val prefs = applicationContext.getSharedPreferences("vinaspends_prefs", android.content.Context.MODE_PRIVATE)
                val personalKey = prefs.getString("custom_gemini_api_key", "") ?: ""
                val prefBank = getBankNameFromPackage(packageName)
                val parsed = repository.parseBankText("$title - $rawTextOriginal", personalKey.ifEmpty { null }, prefBank)
                if (parsed != null) {
                    if (!parsed.isValidTransaction) {
                        Log.d("VinaSpendsListener", "Bỏ qua thông báo quảng cáo/khuyến mãi/tin nhắn không phải gd thực tế: ${parsed.note}")
                        return@launch
                    }
                    val trimmedCategory = parsed.category.trim()
                    if (trimmedCategory.isNotEmpty()) {
                        val defaultCategories = listOf(
                            "Ăn uống", "Di chuyển", "Mua sắm", "Giải trí", 
                            "Lương", "Nhà cửa", "Sức khỏe", "Học tập", 
                            "Hóa đơn", "Đầu tư", "Tiết kiệm", "Du lịch", 
                            "Làm đẹp", "Quà tặng", "Khác"
                        )
                        if (!defaultCategories.contains(trimmedCategory)) {
                            val prefs = applicationContext.getSharedPreferences("vinaspends_prefs", android.content.Context.MODE_PRIVATE)
                            val savedSet = prefs.getStringSet("custom_categories", emptySet()) ?: emptySet()
                            if (!savedSet.contains(trimmedCategory)) {
                                val updatedSet = savedSet.toMutableSet()
                                updatedSet.add(trimmedCategory)
                                prefs.edit().putStringSet("custom_categories", updatedSet).apply()
                            }
                        }
                    }

                    val tx = com.example.data.model.TransactionEntity(
                        amount = parsed.amount,
                        type = parsed.type,
                        category = trimmedCategory,
                        note = "${parsed.note} (Tự động đọc thông báo)",
                        bankName = parsed.bankName,
                        timestamp = currentTime,
                        rawSms = rawTextOriginal,
                        accountNo = parsed.accountNo
                    )
                    val insertedAccount = repository.insertTransaction(tx)
                    if (insertedAccount != null) {
                        Log.d("VinaSpendsListener", "Đã tự động thêm chi tiêu từ thông báo: ${parsed.amount} - ${parsed.note}")
                        
                        // Broadcast event back to UI to play ripple sounds / toast alerts if app is foreground
                        val intent = Intent("com.example.vinaspends.TRANSACTION_ADDED")
                        intent.putExtra("amount", parsed.amount)
                        intent.putExtra("bankName", insertedAccount.name)
                        intent.putExtra("type", parsed.type)
                        sendBroadcast(intent)
                    } else {
                        Log.d("VinaSpendsListener", "Bỏ qua thông báo: Không tìm thấy ví/tài khoản ngân hàng tương ứng được bạn thêm trong danh sách.")
                    }
                }
            }
        }
    }

    private fun getBankNameFromPackage(packageName: String): String? {
        val pkg = packageName.lowercase()
        return when {
            pkg.contains("vietcombank") -> "Vietcombank"
            pkg.contains("tcbmobile") || pkg.contains("techcombank") || pkg.contains("technisys") -> "Techcombank"
            pkg.contains("mbmobile") || pkg.contains("mbbank") -> "MB Bank"
            pkg.contains("tpbank") -> "TPBank"
            pkg.contains("vpbank") -> "VPBank"
            pkg.contains("bidv") -> "BIDV"
            pkg.contains("acb") -> "ACB"
            pkg.contains("vietinbank") || pkg.contains("vietin") -> "VietinBank"
            pkg.contains("sacombank") -> "Sacombank"
            pkg.contains("momo") -> "Ví MoMo"
            pkg.contains("zalopay") -> "Ví ZaloPay"
            pkg.contains("shopeepay") -> "Ví ShopeePay"
            pkg.contains("viettelpay") || pkg.contains("viettel money") || pkg.contains("viettelmoney") -> "Ví Viettel Money"
            pkg.contains("agribank") -> "Agribank"
            pkg.contains("vib") -> "VIB"
            pkg.contains("cake") -> "Cake by VPBank"
            pkg.contains("timo") -> "Timo"
            pkg.contains("shinhan") -> "Shinhan Bank"
            pkg.contains("ocb") -> "OCB"
            pkg.contains("hdb") || pkg.contains("hdbank") -> "HDBank"
            pkg.contains("shb") -> "SHB"
            pkg.contains("hsbc") -> "HSBC"
            pkg.contains("scb") -> "SCB"
            else -> null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
