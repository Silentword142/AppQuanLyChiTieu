package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.example.data.api.ParsedTransaction
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object LocalOcrParser {

    private const val TAG = "LocalOcrParser"

    /**
     * Recognizes text from a local Uri using on-device Google ML Kit Text Recognition.
     */
    suspend fun recognizeTextFromUri(context: Context, uri: Uri): String = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    Log.d(TAG, "OCR Success from Uri: Recognized text: \n$text")
                    continuation.resume(text)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "OCR Failure from Uri", e)
                    continuation.resumeWithException(e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "OCR Exception from Uri", e)
            continuation.resumeWithException(e)
        }
    }

    /**
     * Recognizes text from a local Bitmap using on-device Google ML Kit Text Recognition.
     */
    suspend fun recognizeTextFromBitmap(bitmap: Bitmap): String = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    Log.d(TAG, "OCR Success from Bitmap: Recognized text: \n$text")
                    continuation.resume(text)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "OCR Failure from Bitmap", e)
                    continuation.resumeWithException(e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "OCR Exception from Bitmap", e)
            continuation.resumeWithException(e)
        }
    }

    /**
     * Parses Vietnamese banking transactions from raw recognized OCR text on-device.
     */
    fun parseOcrText(rawText: String, preferredBank: String? = null): ParsedTransaction {
        if (rawText.isBlank()) {
            return ParsedTransaction(
                amount = 0.0,
                type = "EXPENSE",
                bankName = "Tiền mặt",
                note = "Văn bản trống",
                category = "Khác",
                isValidTransaction = false
            )
        }

        // Normalize accents to Standard NFC Form first to handle decomposed character length discrepancies perfectly
        val rawTextNfc = java.text.Normalizer.normalize(rawText, java.text.Normalizer.Form.NFC)
        val lines = rawTextNfc.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val lowerText = rawTextNfc.lowercase(Locale.ROOT)
        val strippedText = stripAccents(lowerText)

        if (checkIsReminder(lowerText, strippedText)) {
            return ParsedTransaction(
                amount = 0.0,
                type = "EXPENSE",
                bankName = preferredBank?.trim() ?: "Khác",
                note = "Tin nhắn nhắc nhở/thông báo dư nợ (Không phải GD phát sinh)",
                category = "Hóa đơn",
                isValidTransaction = false
            )
        }
        
        var amount = 0.0
        var type = "EXPENSE" // Default to Expense
        var bankName = preferredBank?.trim() ?: ""
        var accountNo = ""
        
        // Keep track of which line indices are classified into standard parts so we can exclude them for the final Note extraction.
        val bankLineIndices = mutableSetOf<Int>()
        val accountLineIndices = mutableSetOf<Int>()
        val amountLineIndices = mutableSetOf<Int>()
        val metadataLineIndices = mutableSetOf<Int>()

        // ==========================================
        // BƯỚC 1: Nhận diện thông báo đó là của app ngân hàng nào trước
        // ==========================================
        for (i in lines.indices) {
            val lineLower = lines[i].lowercase(Locale.ROOT)
            val lineStripped = stripAccents(lineLower)
            
            // If we already have a bankName set from preferredBank, we only search for that specific bank's keywords in the text.
            // This prevents other bank/wallet keywords in the note/body (like "MoMo") from hijacking the bank detection or blocking note extraction.
            val detected = if (bankName.isNotEmpty()) {
                val matchesPreferred = when (bankName) {
                    "Vietcombank" -> lineStripped.contains("vietcombank") || lineStripped.contains("vcb") || lineStripped.contains("digibank")
                    "Techcombank" -> lineStripped.contains("techcombank") || lineStripped.contains("tcb")
                    "MB Bank" -> lineStripped.contains("mbbank") || lineStripped.contains("mb bank") || lineStripped.contains("military bank")
                    "TPBank" -> lineStripped.contains("tpbank") || lineStripped.contains("tpb")
                    "VPBank" -> lineStripped.contains("vpbank") || lineStripped.contains("vpb")
                    "BIDV" -> lineStripped.contains("bidv")
                    "ACB" -> lineStripped.contains("acb")
                    "VietinBank" -> lineStripped.contains("vietinbank") || lineStripped.contains("ctg") || lineStripped.contains("vietin")
                    "Sacombank" -> lineStripped.contains("sacombank") || lineStripped.contains("stb")
                    "Ví MoMo" -> lineStripped.contains("momo") || lineStripped.contains("mo mo")
                    "Ví ZaloPay" -> lineStripped.contains("zalopay") || lineStripped.contains("zalo pay")
                    "Ví ShopeePay" -> lineStripped.contains("shopeepay") || lineStripped.contains("shopee pay")
                    "Ví Viettel Money" -> lineStripped.contains("viettel money") || lineStripped.contains("viettelmoney") || lineStripped.contains("viettelpay") || lineStripped.contains("viettel pay")
                    "Agribank" -> lineStripped.contains("agribank") || lineStripped.contains("vba")
                    "VIB" -> lineStripped.contains("vib")
                    "Cake by VPBank" -> lineStripped.contains("cake")
                    "Timo" -> lineStripped.contains("timo")
                    "Shinhan Bank" -> lineStripped.contains("shinhan")
                    "OCB" -> lineStripped.contains("ocb")
                    "HDBank" -> lineStripped.contains("hdbank") || lineStripped.contains("hdb")
                    "SHB" -> lineStripped.contains("shb")
                    "HSBC" -> lineStripped.contains("hsbc")
                    "SCB" -> lineStripped.contains("scb")
                    else -> false
                }
                if (matchesPreferred) bankName else null
            } else {
                when {
                    lineStripped.contains("vietcombank") || lineStripped.contains("vcb") || lineStripped.contains("digibank") -> "Vietcombank"
                    lineStripped.contains("techcombank") || lineStripped.contains("tcb") -> "Techcombank"
                    lineStripped.contains("mbbank") || lineStripped.contains("mb bank") || lineStripped.contains("military bank") -> "MB Bank"
                    lineStripped.contains("tpbank") || lineStripped.contains("tpb") -> "TPBank"
                    lineStripped.contains("vpbank") || lineStripped.contains("vpb") -> "VPBank"
                    lineStripped.contains("bidv") -> "BIDV"
                    lineStripped.contains("acb") -> "ACB"
                    lineStripped.contains("vietinbank") || lineStripped.contains("ctg") || lineStripped.contains("vietin") -> "VietinBank"
                    lineStripped.contains("sacombank") || lineStripped.contains("stb") -> "Sacombank"
                    lineStripped.contains("momo") || lineStripped.contains("mo mo") -> "Ví MoMo"
                    lineStripped.contains("zalopay") || lineStripped.contains("zalo pay") -> "Ví ZaloPay"
                    lineStripped.contains("shopeepay") || lineStripped.contains("shopee pay") -> "Ví ShopeePay"
                    lineStripped.contains("viettel money") || lineStripped.contains("viettelmoney") || lineStripped.contains("viettelpay") || lineStripped.contains("viettel pay") -> "Ví Viettel Money"
                    lineStripped.contains("agribank") || lineStripped.contains("vba") -> "Agribank"
                    lineStripped.contains("vib") -> "VIB"
                    lineStripped.contains("cake") -> "Cake by VPBank"
                    lineStripped.contains("timo") -> "Timo"
                    lineStripped.contains("shinhan") -> "Shinhan Bank"
                    lineStripped.contains("ocb") -> "OCB"
                    lineStripped.contains("hdbank") || lineStripped.contains("hdb") -> "HDBank"
                    lineStripped.contains("shb") -> "SHB"
                    lineStripped.contains("hsbc") -> "HSBC"
                    lineStripped.contains("scb") -> "SCB"
                    else -> null
                }
            }
            if (detected != null) {
                if (bankName.isEmpty()) {
                    bankName = detected
                }
                bankLineIndices.add(i)
            }
        }
        
        // Fallback bank name check if not found in specific lines
        if (bankName.isEmpty()) {
            bankName = when {
                strippedText.contains("vietcombank") || strippedText.contains("vcb") || strippedText.contains("digibank") -> "Vietcombank"
                strippedText.contains("techcombank") || strippedText.contains("tcb") -> "Techcombank"
                strippedText.contains("mbbank") || strippedText.contains("mb bank") || strippedText.contains("military bank") -> "MB Bank"
                strippedText.contains("tpbank") || strippedText.contains("tpb") -> "TPBank"
                strippedText.contains("vpbank") || strippedText.contains("vpb") -> "VPBank"
                strippedText.contains("bidv") -> "BIDV"
                strippedText.contains("acb") -> "ACB"
                strippedText.contains("vietinbank") || strippedText.contains("ctg") || strippedText.contains("vietin") -> "VietinBank"
                strippedText.contains("sacombank") || strippedText.contains("stb") -> "Sacombank"
                strippedText.contains("momo") || strippedText.contains("mo mo") -> "Ví MoMo"
                strippedText.contains("zalopay") || strippedText.contains("zalo pay") -> "Ví ZaloPay"
                strippedText.contains("shopeepay") || strippedText.contains("shopee pay") -> "Ví ShopeePay"
                strippedText.contains("viettel money") || strippedText.contains("viettelmoney") || strippedText.contains("viettelpay") || strippedText.contains("viettel pay") -> "Ví Viettel Money"
                strippedText.contains("agribank") || strippedText.contains("vba") -> "Agribank"
                strippedText.contains("vib") -> "VIB"
                strippedText.contains("cake") -> "Cake by VPBank"
                strippedText.contains("timo") -> "Timo"
                strippedText.contains("shinhan") -> "Shinhan Bank"
                strippedText.contains("ocb") -> "OCB"
                strippedText.contains("hdbank") || strippedText.contains("hdb") -> "HDBank"
                strippedText.contains("shb") -> "SHB"
                strippedText.contains("hsbc") -> "HSBC"
                strippedText.contains("scb") -> "SCB"
                else -> "Khác"
            }
        }

        // ==========================================
        // BƯỚC 2: Tìm số tài khoản hoặc số thẻ để xác định nguồn tiền
        // ==========================================
        // Look for cards/credit cards first
        for (i in lines.indices) {
            val line = lines[i]
            val lineLower = line.lowercase(Locale.ROOT)
            val lineStripped = stripAccents(lineLower)
            
            val hasCardKeyword = lineStripped.contains(Regex("(th[eẻ]|card|visa|mastercard|jcb|t[ií]n d[uụ]ng|credit)"))
            if (hasCardKeyword) {
                // Look for masked credit card patterns like "4... 2717" or "**** 1234"
                val fancyMaskMatch = Regex("""([0-9]{0,6}[.*xX#\-_\s]{3,25}[0-9]{4})""").find(line)
                if (fancyMaskMatch != null) {
                    val candidate = fancyMaskMatch.groupValues[1].trim()
                    if (candidate.contains(Regex("[.*xX#]"))) {
                        accountNo = candidate.replace(" ", "")
                        accountLineIndices.add(i)
                        break
                    }
                }
                
                val maskedMatch = Regex("([.*xX#]{1,12}\\s*[0-9]{4})\\b").find(line)
                if (maskedMatch != null) {
                    accountNo = maskedMatch.groupValues[1].replace(" ", "")
                    accountLineIndices.add(i)
                    break
                }
                
                val suffixMatch = Regex("(?i)(?:k[eế]t th[uú]c b[aằ]ng|đuôi|duoi|s[oố]|so)[^0-9]*?([0-9]{4})\\b").find(lineStripped)
                if (suffixMatch != null) {
                    accountNo = "*" + suffixMatch.groupValues[1]
                    accountLineIndices.add(i)
                    break
                }
                
                val standalone4DigitsMatch = Regex("\\b([0-9]{4})\\b").findAll(line).map { it.groupValues[1] }
                for (digits in standalone4DigitsMatch) {
                    val num = digits.toIntOrNull()
                    if (num != null && num !in 1995..2035) {
                        val idx = line.indexOf(digits)
                        if (idx > 0 && line[idx - 1] == ':') continue
                        if (idx + 4 < line.length && line[idx + 4] == ':') continue
                        accountNo = "*" + digits
                        accountLineIndices.add(i)
                        break
                    }
                }
                if (accountNo.isNotEmpty()) break
            }
        }

        // If card not found, look for bank account number patterns
        if (accountNo.isEmpty()) {
            val accKeyRegex = Regex("(?i)(t[aả]i kho[aả]n|tk|account|tr[ií]ch n[oợ]|th[uụ] h[uư]ởng|s[oố] tk|so tk|s[oố] tài khoản|so tai khoan)")
            val accountNoRegex = Regex("\\b([0-9]{8,20})\\b")
            for (i in lines.indices) {
                val line = lines[i]
                val lineStripped = stripAccents(line.lowercase(Locale.ROOT))
                if (accKeyRegex.containsMatchIn(lineStripped)) {
                    val match = accountNoRegex.find(line)
                    if (match != null) {
                        accountNo = match.groupValues[1]
                        accountLineIndices.add(i)
                        break
                    }
                }
            }
            if (accountNo.isEmpty()) {
                for (i in lines.indices) {
                    val line = lines[i]
                    val match = accountNoRegex.find(line)
                    if (match != null && !line.contains(Regex("(?i)(ngày|mã|giao dịch|thời gian|time|date|id|ref|nội dung|noi dung|loi nhan)"))) {
                        accountNo = match.groupValues[1]
                        accountLineIndices.add(i)
                        break
                    }
                }
            }
        }

        // ==========================================
        // BƯỚC 3: Tìm số tiền biến động (số tiền giao dịch) và loại GD (In/Out)
        // ==========================================
        var foundAmount = false
        val amountValueRegex = Regex("([+-]?\\s*[0-9]{1,3}(?:[.,][0-9]{3})+)(?:\\s*(?:VND|đ|d|vnd|vnd))?\\b")
        val fallbackSimpleNumberRegex = Regex("\\b([0-9]{4,9})\\s*(?:VND|đ|d|vnd|vnd)\\b", RegexOption.IGNORE_CASE)
        val balanceKeywords = listOf("số dư", "so du", "sodu", "bal", "hạn mức", "han muc", "khả dụng", "kha dung", "với số dư", "thăng dư", "số dư khả dụng")

        // Priority 1: Check lines indicating a clearly annotated dynamic transaction amount
        val amountKeywords = listOf("số tiền", "so tien", "gia tri", "giá trị", "thanh toán", "chuyển khoản", "thanh toan", "chuyen khoan", "tiền chuyển", "tiền gửi")
        for (i in lines.indices) {
            val line = lines[i]
            val lineLower = line.lowercase(Locale.ROOT)
            val lineStripped = stripAccents(lineLower)
            
            if (balanceKeywords.any { lineStripped.contains(it) }) continue
            
            if (amountKeywords.any { lineStripped.contains(it) }) {
                val match = amountValueRegex.find(line) ?: fallbackSimpleNumberRegex.find(line)
                if (match != null) {
                    val rawNumStr = match.groupValues[1]
                    val direction = if (line.contains("+") || lineLower.contains("nhận") || lineLower.contains("cộng")) "INCOME" else if (line.contains("-") || lineLower.contains("trừ") || lineLower.contains("rút")) "EXPENSE" else null
                    if (direction != null) type = direction
                    
                    amount = cleanAmountString(rawNumStr)
                    if (amount > 0) {
                        foundAmount = true
                        amountLineIndices.add(i)
                        break
                    }
                } else {
                    // Try subsequent line if the value is in it
                    if (i + 1 < lines.size) {
                        val nextLine = lines[i + 1]
                        val nextLineLower = nextLine.lowercase(Locale.ROOT)
                        val nextLineStripped = stripAccents(nextLineLower)
                        if (balanceKeywords.none { nextLineStripped.contains(it) }) {
                            val nextMatch = amountValueRegex.find(nextLine) ?: fallbackSimpleNumberRegex.find(nextLine)
                            if (nextMatch != null) {
                                amount = cleanAmountString(nextMatch.groupValues[1])
                                if (amount > 0) {
                                    val combined = "$line $nextLine".lowercase(Locale.ROOT)
                                    if (combined.contains("+") || combined.contains("nhận") || combined.contains("cộng")) {
                                        type = "INCOME"
                                    } else if (combined.contains("-") || combined.contains("trừ") || combined.contains("nợ")) {
                                        type = "EXPENSE"
                                    }
                                    foundAmount = true
                                    amountLineIndices.add(i)
                                    amountLineIndices.add(i + 1)
                                    break
                                }
                            }
                        }
                    }
                }
            }
        }

        // Priority 2: Scan for lines that start explicitly with "+" or "-" or have +/- in front of a number
        if (!foundAmount) {
            for (i in lines.indices) {
                val line = lines[i]
                val lineLower = line.lowercase(Locale.ROOT)
                val lineStripped = stripAccents(lineLower)
                if (balanceKeywords.any { lineStripped.contains(it) }) continue
                
                if (line.trim().startsWith("+") || line.trim().startsWith("-") || line.contains(Regex("[+-]\\s*[0-9]"))) {
                    val match = amountValueRegex.find(line)
                    if (match != null) {
                        amount = cleanAmountString(match.groupValues[1])
                        if (amount > 0) {
                            type = if (line.contains("+")) "INCOME" else "EXPENSE"
                            foundAmount = true
                            amountLineIndices.add(i)
                            break
                        }
                    }
                }
            }
        }

        // Priority 3: Scan any other line for formatted amounts, avoiding balance lines
        if (!foundAmount) {
            for (i in lines.indices) {
                val line = lines[i]
                val lineLower = line.lowercase(Locale.ROOT)
                val lineStripped = stripAccents(lineLower)
                if (balanceKeywords.any { lineStripped.contains(it) }) continue
                
                val match = amountValueRegex.find(line) ?: fallbackSimpleNumberRegex.find(line)
                if (match != null) {
                    amount = cleanAmountString(match.groupValues[1])
                    if (amount > 0) {
                        if (line.contains("+") || lineLower.contains("nhận") || lineLower.contains("cộng")) {
                            type = "INCOME"
                        } else if (line.contains("-") || lineLower.contains("trừ") || lineLower.contains("nợ") || lineLower.contains("rút")) {
                            type = "EXPENSE"
                        }
                        foundAmount = true
                        amountLineIndices.add(i)
                        break
                    }
                }
            }
        }

        // Determine type based on semantic scoring if amount found but type is default
        val incomeKeywords = listOf("+", "nhận", "nộp", "cộng", "tang", "tăng", "có", "đã có", "vào tài khoản", "hoàn tiền", "hoan tien", "thu nhập", "thương", "luong", "lương")
        val expenseKeywords = listOf("-", "trừ", "tru", "chi ", "chi ngoai", "nợ", "thanh toán", "rút", "chuyển", "mua", "giao dịch thẻ phat sinh", "phí")
        
        var incomeScore = 0
        var expenseScore = 0
        incomeKeywords.forEach { if (lowerText.contains(it)) incomeScore++ }
        expenseKeywords.forEach { if (lowerText.contains(it)) expenseScore++ }
        
        if (incomeScore > expenseScore && !lowerText.contains("trừ tài khoản") && !lowerText.contains("bị trừ")) {
            type = "INCOME"
        } else if (expenseScore > incomeScore) {
            type = "EXPENSE"
        }

        // ==========================================
        // BƯỚC 4: Tìm nội dung chuyển khoản là dòng không thuộc các nội dung trên
        // ==========================================
        // Identify other typical UI / metadata lines to classify them as metadataLineIndices
        val strictMetadataLabels = listOf(
            "mã giao dịch", "ma giao dich", "mã gd", "ma gd", "mã chuẩn chi", "ma chuan chi", 
            "mã tham chiếu", "ma tham chieu", "id giao dịch", "id giao dich", "transaction id", 
            "thời gian", "thoi gian", "ngày thực hiện", "ngay thuc hien", 
            "ngày giao dịch", "ngay giao dich", "ngày gd", "ngay gd", "số dư", "so du", "sodu", 
            "hạn mức", "han muc", "phí giao dịch", "phi giao dich", "phí chuyển", "phi chuyen", 
            "phí dịch vụ", "phi dich vu", "tài khoản nguồn", "tai khoan nguon", "tk nguồn", "tk nguon",
            "tài khoản thụ hưởng", "tai khoan thu huong", "tk thụ hưởng", "tk thu huong", 
            "tài khoản nhận", "tai khoan nhan", "người thụ hưởng", "nguoi thu huong", 
            "người nhận", "nguoi nhan", "ngân hàng thụ hưởng", "ngan hang thu huong", 
            "ngân hàng nhận", "ngan hang nhan", "trạng thái", "trang thai", "thành công", "thanh cong",
            "chia sẻ", "chia se", "chụp màn hình", "chup man hinh", "sao chép", "sao chep", "lưu ảnh", "luu anh", "tải ảnh",
            "tài khoản đối ứng", "tai khoan doi ung", "tk nhận", "tk nhan", "tới tài khoản", "toi tai khoan",
            "từ tài khoản", "tu tai khoan", "người chuyển", "nguoi chuyen", "ngân hàng tiếp nhận", "ngan hang tiep nhan",
            "tên người nhận", "ten nguoi nhan", "tên người hưởng", "ten nguoi huong", "số bút toán", "so but toan",
            "bút toán", "but toan", "phương thức", "phuong thuc", "hệ thống", "he thong", "dịch vụ", "dich vu", "vcb digibank"
        )
        val strictMetadataLabelsStripped = strictMetadataLabels.map { stripAccents(it).lowercase(Locale.ROOT) }

        for (i in lines.indices) {
            val line = lines[i]
            val lineLower = line.lowercase(Locale.ROOT)
            val lineStripped = stripAccents(lineLower)
            
            val isMeta = strictMetadataLabelsStripped.any { lineStripped.contains(it) } ||
                         lineLower.contains("sao chép") || 
                         lineLower.contains("copy") ||
                         lineLower.contains("chia sẻ") ||
                         lineLower.contains("lưu ảnh") ||
                         lineLower.contains("hoàn tất") ||
                         lineLower.contains("quay lại") ||
                         line.contains(Regex("^\\s*[0-9]{10,}\\s*$")) || // line containing only a very long number
                         line.contains(Regex("^\\s*[^A-Za-z0-9]+\\s*$")) || // line containing only symbols
                         line.contains(Regex("^[0-9:\\s./-]{10,}$")) // line containing only dates or times
            
            if (isMeta) {
                metadataLineIndices.add(i)
            }
        }

        // Now, find the note candidate line(s). The user says:
        // "cuối cùng là tìm nội dung chuyển khoản là dòng không thuộc các nội dung trên" (the line/text that doesn't belong to any of the above).
        val noteCandidates = mutableListOf<String>()
        for (i in lines.indices) {
            // Check if this line index is not classified under bank, account, amount, or metadata.
            val belongsToNone = !bankLineIndices.contains(i) && 
                                !accountLineIndices.contains(i) && 
                                !amountLineIndices.contains(i) && 
                                !metadataLineIndices.contains(i)
            
            if (belongsToNone) {
                val candidate = lines[i]
                val candidateStripped = stripAccents(candidate.lowercase(Locale.ROOT)).trim()
                
                // Extra defensive filter: make sure the candidate itself doesn't contain account number or exact bank name or amount values
                val containsAcc = accountNo.isNotEmpty() && candidate.contains(accountNo)
                val isOnlyBank = candidateStripped == "vietcombank" || candidateStripped == "techcombank" || candidateStripped == "mbbank" || candidateStripped == "acb"
                
                if (!containsAcc && !isOnlyBank && candidate.isNotBlank() && !isLineAmount(candidate)) {
                    noteCandidates.add(candidate)
                }
            }
        }

        var note = ""
        if (noteCandidates.isNotEmpty()) {
            note = noteCandidates.joinToString(" ").trim()
        }

        // If our "belongs to none" logic didn't return any suitable lines, or the result is too short,
        // we use our strong keywords-based extraction and fallback rules to locate the note.
        if (note.length < 3) {
            val rawNoteKeywords = listOf(
                "nội dung chuyển khoản", "noi dung chuyen khoan", 
                "nội dung giao dịch", "noi dung giao dich", 
                "nội dung thanh toán", "noi dung thanh toan", 
                "lời nhắn chuyển khoản", "loi nhan chuyen khoan",
                "nội dung thực hiện", "noi dung thuc hien",
                "nội dung chi tiết", "noi dung chi tiet",
                "nội dung nhận", "noi dung nhan",
                "nội dung chuyển", "noi dung chuyen",
                "nội dung gd", "noi dung gd", 
                "lời nhắn", "loi nhan", 
                "nội dung ck", "noi dung ck",
                "nội dung", "noi dung", 
                "lý do gd", "ly do gd",
                "lý do", "ly do", 
                "mô tả", "mo ta", 
                "ghi chú", "ghi chu"
            )
            val highPriorityKeywords = rawNoteKeywords.sortedByDescending { it.length }
            var foundMatch = false
            
            for (kw in highPriorityKeywords) {
                for (i in lines.indices) {
                    val line = lines[i]
                    val lineLower = line.lowercase(Locale.ROOT)
                    val idx = findKeywordIndex(lineLower, kw)
                    if (idx != -1) {
                        var potentialNote = line.substring(idx + kw.length).trim()
                        while (potentialNote.isNotEmpty() && (potentialNote.startsWith(":") || potentialNote.startsWith("-") || potentialNote.startsWith(">") || potentialNote.startsWith("/") || potentialNote.startsWith(" "))) {
                            potentialNote = potentialNote.substring(1).trim()
                        }
                        if (potentialNote.length > 2 && !isLineAmount(potentialNote)) {
                            note = potentialNote
                            foundMatch = true
                            break
                        }
                    }
                }
                if (foundMatch) break
            }
            
            if (!foundMatch) {
                val possibleLines = lines.filter { line ->
                    isValidFallbackNote(line) && (accountNo.isEmpty() || !line.contains(accountNo))
                }
                if (possibleLines.isNotEmpty()) {
                    val sortedLines = possibleLines.sortedByDescending { scoreFallbackNote(it) }
                    note = sortedLines.first()
                } else {
                    note = "Chuyển khoản"
                }
            }
        }

        // Clean note from typical prefixes like "ND:", "Nội dung:", "Lý do:", "Nội dung CK:", "Lời nhắn:", etc.
        val prefixesToRemove = listOf(
            "nội dung chuyển khoản", "noi dung chuyen khoan", 
            "nội dung giao dịch", "noi dung giao dich", 
            "nội dung thanh toán", "noi dung thanh toan", 
            "lời nhắn chuyển khoản", "loi nhan chuyen khoan",
            "nội dung thực hiện", "noi dung thuc hien",
            "nội dung chi tiết", "noi dung chi tiet",
            "nội dung nhận", "noi dung nhan",
            "nội dung chuyển", "noi dung chuyen",
            "nội dung gd", "noi dung gd", 
            "lời nhắn", "loi nhan", 
            "nội dung ck", "noi dung ck",
            "nội dung", "noi dung", 
            "lý do gd", "ly do gd",
            "lý do", "ly do", 
            "mô tả", "mo ta", 
            "ghi chú", "ghi chu",
            "ndck:", "ndck", "nd ck", "nd_ck", "nd:", "nd ", "ref:", "memo:", "message:", "comment:"
        )
        
        var cleanedNote = note.trim()
        val cleanedNoteLower = cleanedNote.lowercase(Locale.ROOT)
        for (prefix in prefixesToRemove.sortedByDescending { it.length }) {
            if (cleanedNoteLower.startsWith(prefix)) {
                cleanedNote = cleanedNote.substring(prefix.length).trim()
                // Strip starting punctuation
                while (cleanedNote.isNotEmpty() && (cleanedNote.startsWith(":") || cleanedNote.startsWith("-") || cleanedNote.startsWith(">") || cleanedNote.startsWith("/") || cleanedNote.startsWith(" ") || cleanedNote.startsWith("|") || cleanedNote.startsWith("+"))) {
                    cleanedNote = cleanedNote.substring(1).trim()
                }
                break
            }
        }
        if (cleanedNote.isNotBlank()) {
            note = cleanedNote
        }

        // Clean note from trailing junk
        note = note.replace(Regex("[:\\->\\s|\\+]+$"), "").trim()
        if (note.length > 1) {
            note = note.substring(0, 1).uppercase(Locale.ROOT) + note.substring(1)
        }

        return ParsedTransaction(
            amount = amount,
            type = type,
            bankName = bankName,
            note = note,
            category = guessCategory(note, lowerText),
            accountNo = accountNo.ifEmpty { null },
            isValidTransaction = amount > 0
        )
    }

    private fun cleanAmountString(str: String): Double {
        // Strip out non-numeric characters EXCEPT commas or dots that could be formatting residues
        val cleared = str.replace(Regex("[^0-9,.]"), "")
        if (cleared.isEmpty()) return 0.0
        
        // Handle decimals if found (e.g. 50,000.00 or 150000.00)
        // In VIETNAM, dots are thousands separators, e.g. 150.000đ. Decimals are not used for VND.
        // We will strip both and treat as integer unless it ends with .00 or ,00
        val clean = if (cleared.endsWith(".00") || cleared.endsWith(",00")) {
            cleared.substring(0, cleared.length - 3).replace(Regex("[.,]"), "")
        } else {
            cleared.replace(Regex("[.,]"), "")
        }
        return clean.toDoubleOrNull() ?: 0.0
    }

    private fun guessCategory(note: String, rawLower: String): String {
        val textToMatch = "$note $rawLower".lowercase(Locale.ROOT)
        return when {
            textToMatch.contains(Regex("(?i)(ăn|uống|food|cafe|phở|bún|chè|cơm|coffe|bánh|trà sữa|milktea|nhà hàng|mì|grabfood|shopeefood|starbucks|highlands|kfc|lotteria|pizza|lẩu|nướng|buffet|bia|nhậu|lau|nuong|bún cá|trà|bánh mì|cheers|circle k|ministop|gs25|tạp hóa|tiện lợi|mì cay|gà rán|the coffee house|phúc long|phuclong|gongcha|tocotoco|koilands|boba|bakery|nước ngọt|sinh tố|kem|quán ăn|nha hang|uong)")) -> "Ăn uống"
            
            textToMatch.contains(Regex("(?i)(grab|taxi|xe ôm|be |gojek|xăng|vé xe|tàu hỏa|máy bay|đỗ xe|phí cầu đường|gasoline|phuong trang|xe khach|limousine|nạp thẻ vetc|epass|bay|hang khong|gửi xe|gui xe|xe may|xe ô tô|mai linh|vinataxi|vinasun|xe buýt|bus|subway|hàng không|vietjet|bamboo|vietnam airlines|mua xăng|ve xe)")) -> "Di chuyển"
            
            textToMatch.contains(Regex("(?i)(shopee|lazada|tiki|mua sắm|shopping|áo|quần|uniqlo|hm|con cưng|kidsplaza|mẹ và bé|thời trang|giày|dép|túi xách|điện thoại|văn phòng phẩm|sách|sendo|siêu thị|coopmart|winmart|bhx|bách hóa|supermarket|lotte|aeon|emart|tạp hoá|mĩ phẩm|cosmetic|skincare|son môi|quần áo|phụ kiện|trang sức|đồng hồ|bách hóa xanh|bhx)")) -> "Mua sắm"
            
            textToMatch.contains(Regex("(?i)(net|game|phim|cgv|vé xem phim|karaoke|du lịch|travel|khách sạn|hotel|steam|nạp thẻ|quà tặng|gift|bar|club|pub|massage|spa|làm đẹp|vé số|netflix|spotify|nintendo|playstation|ps5|rap chiếu phim|lotte cinema|bida|bi-a|chơi game|concert|show|triển lãm)")) -> "Giải trí"
            
            textToMatch.contains(Regex("(?i)(lương|salary|bonus|thu nhập|thưởng|tiền lãi|interest|hoàn tiền|refund|co ve|nhan luong|luong|chuyển khoản lương|dividend|cổ tức|nhận lương)")) -> "Lương"
            
            textToMatch.contains(Regex("(?i)(điện|nước|wifi|internet|nhà|phòng|thuê|tiền nhà|chung cư|dịch vụ chung cư|vệ sinh|rác|sửa nhà|nội thất|tiền nước|tiền điện|truyền hình cáp|fpt|viettel wifi|vnpt)")) -> "Nhà cửa"
            
            textToMatch.contains(Regex("(?i)(thuốc|bệnh viện|phòng khám|pharmacity|long châu|bác sĩ|doctor|thuoc|y te|kham|nha khoa|răng|an sinh|hoàn mỹ|tâm anh|khám bệnh|thuốc tây|tiêm ngừa|vaccine|chữa bệnh|bảo hiểm y tế|mắt kính|gym|fitness|yoga)")) -> "Sức khỏe"
            
            textToMatch.contains(Regex("(?i)(học phí|hoc phi|khoa hoc|sách|vở|school|tuyển sinh|tiếng anh|ielts|toefl|udemy|coursera|đại học|trường học|lớp học|dạy kèm|gia sư|thi cử|văn phòng phẩm)")) -> "Học tập"
            
            textToMatch.contains(Regex("(?i)(hóa đơn|hoa don|dien luc|cap nuoc|mạng|cáp|truyền hình|vtv|đóng phí|phí dịch vụ|tiền mạng|lệ phí|phí ngân hàng|phí duy trì)")) -> "Hóa đơn"
            
            else -> "Khác"
        }
    }

    private fun isLineAmount(line: String): Boolean {
        val s = line.trim().lowercase(Locale.ROOT)
        if (s.isEmpty()) return false
        // Replace all digits, common separators (. ,), currency symbols (đ, d, vcb, vnd, vnd, $, usd),
        // mathematical operators (+ -), spaces, and percentage (%)
        val remaining = s.replace(Regex("[0-9.,\\s+\\-đd$]|vnd|usd|%"), "")
        // If the remaining string is empty or contains only non-word chars, it is an amount line
        if (remaining.isEmpty()) return true
        // If remaining is just "vnd" or "đ" or "d" (redundant check) or very few characters like letters but has digits
        if (remaining.length <= 1 && s.any { it.isDigit() }) return true
        return false
    }

    private fun scoreFallbackNote(line: String): Int {
        val sLower = line.lowercase(Locale.ROOT)
        val sStripped = stripAccents(sLower)
        var score = 0
        
        // Lowercase or mixed case is preferred over ALL CAPS names
        val hasLowercase = line.any { it.isLowerCase() }
        if (hasLowercase) {
            score += 15
        }
        
        // Common action or purpose words in transfer descriptions
        val transferKeywords = listOf(
            "chuyen", "chuyển", "ck", "tien", "tiền", "thanh toan", "thanh toán", "mua", "trả", "tra",
            "an ", "ăn ", "an", "uong", "uống", "dong ", "đóng ", "nop ", "nộp ", "gửi", "gui", "nap", "nạp",
            "dong tien", "đóng tiền", "tra no", "trả nợ", "luong", "lương", "phi ", "phí ", "mua sam", "mua sắm",
            "cafe", "phở", "bún", "cơm", "com ", "tiền ăn", "tien an", "chuyển tiền", "chuyen tien", "dong hoc phi"
        )
        val transferKeywordsStripped = transferKeywords.map { stripAccents(it).lowercase(Locale.ROOT) }
        for (kw in transferKeywordsStripped) {
            if (sStripped.contains(kw)) {
                score += 10
            }
        }
        
        // Longer sentences that have multiple words are preferred over a single or double word (which might be a name)
        val wordsCount = line.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        if (wordsCount >= 3) {
            score += 5
        }
        if (wordsCount >= 5) {
            score += 5
        }
        
        // Avoid lines that look like generic Vietnamese names (e.g. starting with NGUYEN, LE, TRAN, PHAM, HOANG, BUI, CAO, VU, VO, DANG, DO, NGO...)
        val uppercaseNameKeywords = listOf("NGUYEN ", "TRAN ", "LE ", "PHAM ", "HOANG ", "BUI ", "VU ", "VO ", "DANG ", "DO ", "NGO ", "CAO ", "THI ")
        val uppercaseNameKeywordsStripped = uppercaseNameKeywords.map { stripAccents(it).uppercase(Locale.ROOT) }
        for (name in uppercaseNameKeywordsStripped) {
            if (stripAccents(line).uppercase(Locale.ROOT).startsWith(name)) {
                score -= 10
            }
        }
        
        return score
    }

    private fun isValidFallbackNote(line: String): Boolean {
        val s = line.trim()
        if (s.length < 3 || s.length > 100) return false
        
        val sLower = s.lowercase(Locale.ROOT)
        val sStripped = stripAccents(sLower)
        
        // Exclude lines that are mostly amount/currency values
        if (isLineAmount(s)) return false
        
        // Exclude lines that look like a bank/e-wallet name entirely
        val bankNames = listOf("vietcombank", "techcombank", "mbbank", "bidv", "acb", "tpbank", "vpbank", "vietinbank", "sacombank", "agribank", "vib", "momo", "zalopay", "shopeepay", "viettelpay", "viettel money")
        val bankNamesStripped = bankNames.map { stripAccents(it).lowercase(Locale.ROOT) }
        if (bankNamesStripped.any { sStripped == it || sStripped.contains(it) || sStripped.contains("ngan hang") }) return false
        
        // Exclude generic banking headers that are exactly or very close to these
        val exactHeaders = listOf(
            "thành công", "thanh cong", "giao dịch thành công", "giao dich thanh cong",
            "giao dịch thành công!", "giao dich thanh cong!", "vcb digibank", "vietcombank",
            "techcombank", "mbbank", "bidv", "tpbank", "vpbank", "agribank", "sacombank", "acb",
            "chuyển tiền thành công", "chuyển khoản thành công", "chuyen tien thanh cong",
            "giao dịch chuyển khoản", "chi tiết giao dịch", "thông tin giao dịch", "biên lai điện tử",
            "biên lai chuyển tiền", "thông báo biến động", "biến động số dư"
        )
        val exactHeadersStripped = exactHeaders.map { stripAccents(it).lowercase(Locale.ROOT) }
        if (exactHeadersStripped.any { sStripped == it || sStripped.startsWith(it + " ") || sStripped.endsWith(" " + it) }) return false
        
        // Exclude typical technical words/labels or metadata lines
        val excludedKeywords = listOf(
            "số dư", "so du", "sodu", "tài khoản nguồn", "tai khoan nguon", "tk nguồn", "tk nguon",
            "tài khoản thụ hưởng", "tai khoan thu huong", "tk thụ hưởng", "tk thu huong", "tài khoản nhận",
            "tài khoản đích", "tk đích", "người thụ hưởng", "nguoi thu huong", "người nhận", "nguoi nhan",
            "số tiền", "so tien", "mã giao dịch", "ma giao dich", "mã gd", "ma gd", "mã tham chiếu",
            "mã chuẩn chi", "thời gian", "thoi gian", "ngày thực hiện", "ngay thuc hien", "ngày giao dịch",
            "ngay giao dich", "phí giao dịch", "phi giao dich", "phí chuyển", "phi chuyen", "hạn mức", "han muc",
            "chụp màn hình", "chup man hinh", "chia sẻ", "chia se", "sao chép", "sao chep", "copy", "tải ảnh", "lưu ảnh",
            "từ tài khoản", "tu tai khoan", "tới tài khoản", "toi tai khoan", "ngân hàng nhận", "ngan hang nhan",
            "bút toán", "but toan", "phương thức", "phuong thuc", "hệ thống", "he thong", "dịch vụ", "dich vu",
            "tên người thụ hưởng", "ten nguoi thu huong", "tên người nhận", "ten nguoi nhan", "ngân hàng thụ hưởng",
            "tài khoản", "tai khoan", "so tài khoản", "so tai khoan", "tk", "người chuyển", "nguoi chuyen"
        )
        val excludedKeywordsStripped = excludedKeywords.map { stripAccents(it).lowercase(Locale.ROOT) }
        if (excludedKeywordsStripped.any { sStripped.contains(it) }) return false
        
        // Ensure it doesn't look like a lone card number, bank reference ID, or date/time
        if (s.contains(Regex("^[0-9\\s]{8,}$"))) return false
        if (s.contains(Regex("^\\d{4,}[\\-\\/.]\\d{2}[\\-\\/.]\\d{2}"))) return false // Date like YYYY/MM/DD
        if (s.contains(Regex("^\\d{2}[\\-\\/.]\\d{2}[\\-\\/.]\\d{4}"))) return false // Date like DD/MM/YYYY
        
        return true
    }

    private fun findKeywordIndex(lineLower: String, kw: String): Int {
        val lineLowerStripped = stripAccents(lineLower)
        val kwStripped = stripAccents(kw)
        
        var startIdx = 0
        while (true) {
            val idx = lineLowerStripped.indexOf(kwStripped, startIdx)
            if (idx == -1) return -1
            
            // Check word boundary before the keyword
            val isBoundaryBefore = idx == 0 || !lineLowerStripped[idx - 1].isLetterOrDigit()
            
            // Check word boundary after the keyword
            val isBoundaryAfter = idx + kwStripped.length >= lineLowerStripped.length || 
                    kwStripped.endsWith(" ") || kwStripped.endsWith(":") || kwStripped.endsWith("_") || 
                    !lineLowerStripped[idx + kwStripped.length].isLetterOrDigit()
            
            if (isBoundaryBefore && isBoundaryAfter) {
                return idx
            }
            startIdx = idx + 1
        }
    }

    private fun checkIsReminder(lowerText: String, strippedText: String): Boolean {
        val reminderPhrases = listOf(
            "nhac nho", "nhắc nhở",
            "vui long thanh toan", "vui lòng thanh toán",
            "de nghi thanh toan", "đề nghị thanh toán",
            "thong bao du no", "thông báo dư nợ",
            "ky sao ke", "kỳ sao kê",
            "sao ke the", "sao kê thẻ",
            "thong bao sao ke", "thông báo sao kê",
            "vui long nop", "vui lòng nộp",
            "vui long nap", "vui lòng nạp",
            "lich thanh toan", "lịch thanh toán",
            "lich tra no", "lịch trả nợ",
            "nhac no", "nhắc nợ"
        )
        
        val hasReminderPhrase = reminderPhrases.any { phrase ->
            lowerText.contains(phrase) || strippedText.contains(stripAccents(phrase))
        }
        
        if (hasReminderPhrase) {
            val completedMarkers = listOf(
                "thành công", "thanh cong",
                "biến động số dư", "bien dong so du",
                "đã thanh toán", "da thanh toan",
                "đã trừ", "da tru",
                "đã nhận", "da nhan",
                "giao dịch thành công", "giao dich thanh cong",
                "đã chuyển", "da chuyen"
            )
            val hasCompletedMarker = completedMarkers.any { marker ->
                lowerText.contains(marker) || strippedText.contains(stripAccents(marker))
            }
            
            if (!hasCompletedMarker) {
                return true
            }
        }
        
        val noticePhrases = listOf(
            "du no den han", "dư nợ đến hạn",
            "han thanh toan", "hạn thanh toán",
            "ngay den han", "ngày đến hạn",
            "han cuoi", "hạn cuối"
        )
        val hasNoticePhrase = noticePhrases.any { phrase ->
            lowerText.contains(phrase) || strippedText.contains(stripAccents(phrase))
        }
        
        if (hasNoticePhrase) {
            val transactionKeywords = listOf(
                "giao dich", "giao dịch",
                "phat sinh", "phát sinh",
                "biến động", "bien dong",
                "thành công", "thanh cong",
                "đã thanh toán", "da thanh toan",
                "đã chuyển", "da chuyen",
                "đã nhận", "da nhan"
            )
            val hasTransaction = transactionKeywords.any { kw ->
                lowerText.contains(kw) || strippedText.contains(stripAccents(kw))
            }
            if (!hasTransaction) {
                return true
            }
        }
        
        return false
    }

    private fun stripAccents(input: String): String {
        val temp = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFC) // convert first to NFC
        val normalized = java.text.Normalizer.normalize(temp, java.text.Normalizer.Form.NFD) // then to NFD for diacritics stripping
        val pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        var out = pattern.matcher(normalized).replaceAll("")
        // Replace non-composing Vietnamese characters manually
        out = out.replace('đ', 'd').replace('Đ', 'D')
        out = out.replace('ư', 'u').replace('Ư', 'U')
        out = out.replace('ơ', 'o').replace('Ơ', 'O')
        out = out.replace('â', 'a').replace('Â', 'A')
        out = out.replace('ê', 'e').replace('Ê', 'E')
        out = out.replace('ô', 'o').replace('Ô', 'O')
        out = out.replace('ă', 'a').replace('Ă', 'A')
        return out
    }

    private fun cleanExtractedNote(note: String): String {
        var clean = note.replace(Regex("[:\\->\\s|\\+]+$"), "").trim()
        if (clean.length > 1) {
            clean = clean.substring(0, 1).uppercase(Locale.ROOT) + clean.substring(1)
        }
        return clean
    }

    private fun extractNoteFromNotificationLine(line: String): String? {
        val s = line.trim()
        if (s.isEmpty()) return null
        
        val sLower = s.lowercase(Locale.ROOT)
        
        // Check if this line looks like a bank transaction notification
        // (must contain at least some banking traits like "số dư", "tài khoản", "tk", "sd", "vcb", "vnd", "giao dịch", "bien dong")
        val bankingTraits = listOf("số dư", "so du", "tk", "tài khoản", "tai khoan", "giao dịch", "giao dich", "biên lai", "bien lai", "vnd", "sd ", "sd:", "gd:", "gd ", "chuyển khoản", "chuyen khoan")
        val isBankingNotification = bankingTraits.any { sLower.contains(it) }
        
        if (!isBankingNotification) return null
        
        // 1. Look for explicit tags first (case insensitive)
        val explicitTags = listOf(
            Regex("(?i)\\b(?:nd|nội dung|noi dung|loi nhan|lời nhắn|ly do|lý do|ndck)[:_\\-\\s]*(.*)$"),
            Regex("(?i)\\b(?:nội dung chuyển khoản|loi nhan chuyen khoan)[:_\\-\\s]*(.*)$")
        )
        for (regex in explicitTags) {
            val match = regex.find(s)
            if (match != null) {
                val candidate = match.groupValues[1].trim()
                if (candidate.length >= 3 && !isLineAmount(candidate)) {
                    return cleanExtractedNote(candidate)
                }
            }
        }
        
        // 2. Fallback: Clean everything else from the line as requested by the user
        var cleaned = s
        
        // Remove bank header at start
        cleaned = cleaned.replace(Regex("(?i)^(?:vietcombank|techcombank|mbbank|bidv|acb|tpbank|vpbank|vietinbank|sacombank|agribank|vib|momo|zalopay)[:\\s\\-\\|]*"), "")
        
        // Remove account number patterns (e.g. TK 0123456789 or tài khoản ... or tk *2014)
        cleaned = cleaned.replace(Regex("(?i)\\b(?:tk|tài khoản|tai khoan|số tk|so tk|acc|account)[:\\s\\-\\|]*[0-9*]{4,16}\\b"), "")
        
        // Remove balance patterns (e.g. SD: 1,500,000 VND or số dư: +10.000đ)
        cleaned = cleaned.replace(Regex("(?i)\\b(?:sd|số dư|so du|sodu|bal|balance)[:\\s\\-\\|]*[+-]?\\s*[0-9]{1,3}(?:[.,][0-9]{3})+(?:\\s*(?:VND|đ|d|vnd|usd|\\$))?\\b"), "")
        
        // Remove amount patterns with currency prefix before or after digits
        cleaned = cleaned.replace(Regex("(?i)\\b(?:VND|đ|d|vnd|usd|\\$)\\s*[+-]?\\s*[0-9]{1,3}(?:[.,][0-9]{3})+\\b"), "")
        cleaned = cleaned.replace(Regex("(?i)[+-]?\\s*[0-9]{1,3}(?:[.,][0-9]{3})+(?:\\s*(?:VND|đ|d|vnd|usd|\\$))\\b"), "")
        cleaned = cleaned.replace(Regex("(?i)[+-]\\s*[0-9]{1,3}(?:[.,][0-9]{3})+\\b"), "")
        
        // Remove standalone pure numbers (4 to 16 digits)
        cleaned = cleaned.replace(Regex("\\b[0-9]{4,16}\\b"), "")
        
        // Remove date/time patterns
        cleaned = cleaned.replace(Regex("(?i)\\b(?:luc|lúc|ngay|ngày|vào|vao)[:\\s\\-\\|]*\\d{2}[\\-/:.]\\d{2}(?:[\\-/:.]\\d{4})?\\s*(?:\\d{2}:\\d{2}\\s*(?:am|pm)?)?\\b"), "")
        cleaned = cleaned.replace(Regex("\\b\\d{2}:\\d{2}\\b"), "")
        cleaned = cleaned.replace(Regex("\\b\\d{2}[\\-/:.]\\d{2}[\\-/:.]\\d{4}\\b"), "")
        cleaned = cleaned.replace(Regex("\\b\\d{2}[\\-/:.]\\d{2}\\b"), "")
        
        // Remove standard single label words left over as noise
        cleaned = cleaned.replace(Regex("(?i)\\b(?:gd|tk|sd|vnd|usd|ref|id)\\b"), "")
        
        // Clean up punctuation, spaces, and separators
        cleaned = cleaned.replace(Regex("[.,:\\-\\|_\\*\\(\\)\\[\\]\\{\\}>/\\+]"), " ")
        cleaned = cleaned.replace(Regex("\\s+"), " ").trim()
        
        if (cleaned.length >= 3 && !isLineAmount(cleaned)) {
            return cleanExtractedNote(cleaned)
        }
        
        return null
    }
}
