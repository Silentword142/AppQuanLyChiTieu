package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GeminiRequest
import com.example.data.api.GeminiRetrofitClient
import com.example.data.api.GenerationConfig
import com.example.data.api.InlineData
import com.example.data.api.Part
import com.example.data.api.ParsedTransaction
import com.example.data.local.BankAccountDao
import com.example.data.local.DebtDao
import com.example.data.local.TransactionDao
import com.example.data.model.BankAccount
import com.example.data.model.DebtEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val bankAccountDao: BankAccountDao,
    private val debtDao: DebtDao,
    private val context: Context? = null
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allAccounts: Flow<List<BankAccount>> = bankAccountDao.getAllAccounts()
    val allDebts: Flow<List<DebtEntity>> = debtDao.getAllDebts()

    suspend fun insertDebt(debt: DebtEntity): Long = withContext(Dispatchers.IO) {
        debtDao.insertDebt(debt)
    }

    suspend fun updateDebt(debt: DebtEntity) = withContext(Dispatchers.IO) {
        debtDao.updateDebt(debt)
    }

    suspend fun deleteDebt(debt: DebtEntity) = withContext(Dispatchers.IO) {
        debtDao.deleteDebt(debt)
    }

    suspend fun deleteDebtById(id: Int) = withContext(Dispatchers.IO) {
        debtDao.deleteDebtById(id)
    }

    suspend fun seedDefaultAccountsIfEmpty() = withContext(Dispatchers.IO) {
        val currentAccounts = bankAccountDao.getAllAccounts().first()
        if (currentAccounts.isEmpty()) {
            val defaults = listOf(
                BankAccount(name = "Vietcombank", accountNo = "1015688828", balance = 15350000.0, isLinked = true),
                BankAccount(name = "Techcombank", accountNo = "1903562214", balance = 42500000.0, isLinked = true),
                BankAccount(name = "MB Bank Credit", accountNo = "0944111222", balance = 30000000.0, isLinked = true, accountType = "TIN_DUNG", creditLimit = 50000000.0, creditSpent = 20000000.0),
                BankAccount(name = "Ví MoMo", accountNo = "0944111222", balance = 1200000.0, isLinked = true),
                BankAccount(name = "Tiền mặt", accountNo = "N/A", balance = 750000.0, isLinked = false)
            )
            for (acc in defaults) {
                bankAccountDao.insertAccount(acc)
            }
        }
    }

    suspend fun findAccountByBestMatch(bankName: String, accountNo: String, rawSms: String? = null): BankAccount? {
        val allCurrentAccounts = bankAccountDao.getAllAccounts().first()
        val txAccClean = accountNo.trim().replace(" ", "")

        // 1. Prioritize matching suffix/account number across all accounts
        if (txAccClean.isNotEmpty() && txAccClean != "N/A" && txAccClean != "AUTO") {
            val matchingSuffixAccounts = allCurrentAccounts.filter { acc ->
                val accClean = acc.accountNo.trim().replace(" ", "")
                accClean.isNotEmpty() && accClean != "n/a" && isSuffixCompatible(accClean, txAccClean)
            }
            if (matchingSuffixAccounts.isNotEmpty()) {
                // If there are multiple matching suffix accounts, prioritize the same brand family
                val txBrand = getBrandFamily(bankName)
                val sameBrandMatch = matchingSuffixAccounts.firstOrNull { 
                    getBrandFamily(it.name) == txBrand 
                }
                if (sameBrandMatch != null) return sameBrandMatch
                return matchingSuffixAccounts.first()
            }
        }

        // 2. Exact name match
        val exactNameMatch = allCurrentAccounts.firstOrNull { 
            it.name.trim().lowercase() == bankName.trim().lowercase() 
        }
        if (exactNameMatch != null) return exactNameMatch

        // 3. Fallback to smart match
        return findAccountBySmartMatch(bankName, accountNo, rawSms)
    }

    private suspend fun findAccountBySmartMatch(bankName: String, accountNo: String, rawSms: String? = null): BankAccount? {
        val allCurrentAccounts = bankAccountDao.getAllAccounts().first()
        
        val txBrand = getBrandFamily(bankName)
        val txAccClean = accountNo.trim().replace(" ", "")
        
        val rawSmsLower = rawSms?.lowercase() ?: ""
        val isCreditCardTx = rawSmsLower.contains("tín dụng") || 
                             rawSmsLower.contains("tin dung") || 
                             rawSmsLower.contains("visa") || 
                             rawSmsLower.contains("mastercard") || 
                             rawSmsLower.contains("jcb") || 
                             rawSmsLower.contains("credit") || 
                             rawSmsLower.contains("card") || 
                             rawSmsLower.contains("the visa") || 
                             rawSmsLower.contains("thẻ visa") ||
                             rawSmsLower.contains("han muc") ||
                             rawSmsLower.contains("hạn mức") ||
                             txAccClean.contains("*") ||
                             txAccClean.contains("x") ||
                             txAccClean.contains("X") ||
                             txAccClean.contains(".")
                             
        // Filter accounts in the same brand family
        val sameBrandAccounts = allCurrentAccounts.filter { getBrandFamily(it.name) == txBrand }
        if (sameBrandAccounts.isEmpty()) {
            // Fallback: loose name check on all accounts
            val matchedByName = allCurrentAccounts.firstOrNull { acc ->
                val accNorm = acc.name.lowercase().replace(" ", "").replace("ngânhàng", "").replace("ví", "")
                val txNorm = bankName.lowercase().replace(" ", "").replace("ngânhàng", "").replace("ví", "")
                accNorm.contains(txNorm) || txNorm.contains(accNorm)
            }
            return matchedByName
        }

        // 1. Try to find with both Brand + Suffix compatibility
        if (txAccClean.isNotBlank() && txAccClean != "N/A" && txAccClean != "AUTO") {
            // Priority 1A: Matching suffix in preferred credit/normal list based on isCreditCardTx
            val preferredAccounts = sameBrandAccounts.filter { 
                if (isCreditCardTx) it.accountType == "TIN_DUNG" else it.accountType != "TIN_DUNG" 
            }
            val matchedPrefSuffix = preferredAccounts.firstOrNull { acc ->
                val accClean = acc.accountNo.trim().replace(" ", "")
                accClean.isNotEmpty() && accClean != "n/a" && isSuffixCompatible(accClean, txAccClean)
            }
            if (matchedPrefSuffix != null) return matchedPrefSuffix

            // Priority 1B: Matching suffix in non-preferred list
            val nonPreferredAccounts = sameBrandAccounts.filter { 
                if (isCreditCardTx) it.accountType != "TIN_DUNG" else it.accountType == "TIN_DUNG" 
            }
            val matchedNonPrefSuffix = nonPreferredAccounts.firstOrNull { acc ->
                val accClean = acc.accountNo.trim().replace(" ", "")
                accClean.isNotEmpty() && accClean != "n/a" && isSuffixCompatible(accClean, txAccClean)
            }
            if (matchedNonPrefSuffix != null) return matchedNonPrefSuffix
        }

        // 2. No suffix match found or accountNo is blank: match by brand preference
        // Priority 2A: Return the account matching the credit card type preference
        val matchedPrefBrandOnly = sameBrandAccounts.firstOrNull { 
            if (isCreditCardTx) it.accountType == "TIN_DUNG" else it.accountType != "TIN_DUNG" 
        }
        if (matchedPrefBrandOnly != null) return matchedPrefBrandOnly

        // Priority 2B: Fallback to the first account in the brand family
        return sameBrandAccounts.first()
    }
    
    private fun getBrandFamily(name: String): String {
        val norm = name.lowercase().replace(" ", "").replace("ngânhàng", "").replace("ví", "").replace("credit", "").replace("thẻ", "")
        return when {
            norm.contains("vcb") || norm.contains("vietcombank") -> "vcb"
            norm.contains("techcombank") || norm.contains("tcb") -> "tcb"
            norm.contains("mbb") || norm.contains("mbbank") || norm.contains("mb") || norm.contains("mb bank") -> "mb"
            norm.contains("vp") || norm.contains("vpbank") -> "vp"
            norm.contains("tp") || norm.contains("tpbank") -> "tp"
            norm.contains("vib") -> "vib"
            norm.contains("sacom") || norm.contains("stb") || norm.contains("sacombank") -> "stb"
            norm.contains("acb") -> "acb"
            norm.contains("bidv") -> "bidv"
            norm.contains("agri") || norm.contains("agribank") -> "agribank"
            norm.contains("momo") -> "momo"
            norm.contains("vietin") || norm.contains("ctg") -> "ctg"
            norm.contains("shinhan") -> "shinhan"
            else -> norm
        }
    }
    
    private fun isSuffixCompatible(num1: String, num2: String): Boolean {
        val clean1 = num1.filter { it.isDigit() }
        val clean2 = num2.filter { it.isDigit() }
        if (clean1.isEmpty() || clean2.isEmpty()) return false
        
        if (clean1.length < 3 || clean2.length < 3) {
            return clean1 == clean2
        }
        
        if (clean1.length >= 4 && clean2.length >= 4) {
            val s1 = clean1.takeLast(4)
            val s2 = clean2.takeLast(4)
            if (s1 == s2) return true
        }
        
        return clean1.endsWith(clean2) || clean2.endsWith(clean1)
    }

    suspend fun insertTransaction(transaction: TransactionEntity): BankAccount? = insertTransaction(transaction, isReverseCascade = false)

    suspend fun insertTransaction(transaction: TransactionEntity, isReverseCascade: Boolean = false): BankAccount? = withContext(Dispatchers.IO) {
        // Find bank with best account number & name match
        val account = findAccountByBestMatch(transaction.bankName, transaction.accountNo ?: "", transaction.rawSms)
            
        if (account == null) {
            Log.d("FinanceRepository", "Skip transaction insertion: No matching user-added wallet or bank account for ${transaction.bankName} (${transaction.accountNo ?: ""})")
            return@withContext null
        }

        // Only insert transaction if a matching account exists
        transactionDao.insertTransaction(transaction)

        val finalUpdatedAccount = if (account.accountType == "TIN_DUNG") {
            val newSpent = if (transaction.type == "EXPENSE") {
                account.creditSpent + transaction.amount
            } else {
                account.creditSpent - transaction.amount
            }
            val newBalance = account.creditLimit - newSpent
            val updatedAccount = account.copy(
                balance = newBalance,
                creditSpent = newSpent,
                lastSynced = System.currentTimeMillis()
            )
            bankAccountDao.updateAccount(updatedAccount)
            updatedAccount
        } else {
            val newBalance = if (transaction.type == "EXPENSE") {
                account.balance - transaction.amount
            } else {
                account.balance + transaction.amount
            }
            val updatedAccount = account.copy(
                balance = newBalance,
                lastSynced = System.currentTimeMillis()
            )
            bankAccountDao.updateAccount(updatedAccount)
            updatedAccount
        }

        // AUTO-REVERSE TRANSACTION FOR INTERNAL TRANSFERS
        if (!isReverseCascade) {
            val allCurrentAccounts = bankAccountDao.getAllAccounts().first()
            val matchedSourceAccountId = finalUpdatedAccount?.id ?: -1
            
            // Find another of our own accounts mentioned in the note
            val targetOtherAccount = allCurrentAccounts.firstOrNull { otherAcc ->
                otherAcc.id != matchedSourceAccountId && noteContainsAccount(transaction.note, otherAcc.accountNo)
            }
            
            if (targetOtherAccount != null) {
                // Determine the reverse transaction type
                val reverseType = if (transaction.type == "EXPENSE") "INCOME" else "EXPENSE"
                
                // Construct the reverse transaction
                val reverseTx = TransactionEntity(
                    amount = transaction.amount,
                    type = reverseType,
                    category = transaction.category,
                    note = "${transaction.note} (Đối ứng)",
                    bankName = targetOtherAccount.name,
                    accountNo = targetOtherAccount.accountNo,
                    timestamp = transaction.timestamp,
                    rawSms = transaction.rawSms
                )
                
                // Call insertTransaction recursively with isReverseCascade = true to avoid infinite loops!
                insertTransaction(reverseTx, isReverseCascade = true)
                Log.d("FinanceRepository", "Auto-created reverse transaction for ${targetOtherAccount.name} (${targetOtherAccount.accountNo}): ${reverseType} ${transaction.amount}đ")
            }
        }

        if (!isReverseCascade) {
            context?.let { com.example.service.WearSyncService.syncToWearable(it) }
        }

        return@withContext finalUpdatedAccount
    }

    suspend fun updateTransaction(oldTx: TransactionEntity, newTx: TransactionEntity) = withContext(Dispatchers.IO) {
        // 1. Revert old transaction
        val oldAccount = findAccountByBestMatch(oldTx.bankName, oldTx.accountNo ?: "", oldTx.rawSms)
        if (oldAccount != null) {
            val updatedOldAccount = if (oldAccount.accountType == "TIN_DUNG") {
                val revertedSpent = if (oldTx.type == "EXPENSE") {
                    oldAccount.creditSpent - oldTx.amount
                } else {
                    oldAccount.creditSpent + oldTx.amount
                }
                val revertedBalance = oldAccount.creditLimit - revertedSpent
                oldAccount.copy(
                    balance = revertedBalance,
                    creditSpent = revertedSpent,
                    lastSynced = System.currentTimeMillis()
                )
            } else {
                val revertedBalance = if (oldTx.type == "EXPENSE") {
                    oldAccount.balance + oldTx.amount
                } else {
                    oldAccount.balance - oldTx.amount
                }
                oldAccount.copy(
                    balance = revertedBalance,
                    lastSynced = System.currentTimeMillis()
                )
            }
            bankAccountDao.updateAccount(updatedOldAccount)
        }

        // 2. Apply new transaction - get current account state freshly
        val newAccount = findAccountByBestMatch(newTx.bankName, newTx.accountNo ?: "", newTx.rawSms)
        if (newAccount != null) {
            val updatedNewAccount = if (newAccount.accountType == "TIN_DUNG") {
                val newSpent = if (newTx.type == "EXPENSE") {
                    newAccount.creditSpent + newTx.amount
                } else {
                    newAccount.creditSpent - newTx.amount
                }
                val newBalance = newAccount.creditLimit - newSpent
                newAccount.copy(
                    balance = newBalance,
                    creditSpent = newSpent,
                    lastSynced = System.currentTimeMillis()
                )
            } else {
                val newBalance = if (newTx.type == "EXPENSE") {
                    newAccount.balance - newTx.amount
                } else {
                    newAccount.balance + newTx.amount
                }
                newAccount.copy(
                    balance = newBalance,
                    lastSynced = System.currentTimeMillis()
                )
            }
            bankAccountDao.updateAccount(updatedNewAccount)
        }

        // 3. Save new transaction properties
        transactionDao.updateTransaction(newTx)
        context?.let { com.example.service.WearSyncService.syncToWearable(it) }
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction)

        // Reverse check on account balance using best account match
        val account = findAccountByBestMatch(transaction.bankName, transaction.accountNo ?: "", transaction.rawSms)
        if (account != null) {
            val updatedAccount = if (account.accountType == "TIN_DUNG") {
                val reversedSpent = if (transaction.type == "EXPENSE") {
                    account.creditSpent - transaction.amount
                } else {
                    account.creditSpent + transaction.amount
                }
                val reversedBalance = account.creditLimit - reversedSpent
                account.copy(
                    balance = reversedBalance,
                    creditSpent = reversedSpent,
                    lastSynced = System.currentTimeMillis()
                )
            } else {
                val reversedBalance = if (transaction.type == "EXPENSE") {
                    account.balance + transaction.amount // Add it back
                } else {
                    account.balance - transaction.amount // Extract it back
                }
                account.copy(
                    balance = reversedBalance,
                    lastSynced = System.currentTimeMillis()
                )
            }
            bankAccountDao.updateAccount(updatedAccount)
        }
        context?.let { com.example.service.WearSyncService.syncToWearable(it) }
    }

    suspend fun updateTransactionCategory(oldCategory: String, newCategory: String) = withContext(Dispatchers.IO) {
        transactionDao.updateTransactionCategory(oldCategory, newCategory)
        context?.let { com.example.service.WearSyncService.syncToWearable(it) }
    }

    suspend fun insertAccount(account: BankAccount) = withContext(Dispatchers.IO) {
        bankAccountDao.insertAccount(account)
        context?.let { com.example.service.WearSyncService.syncToWearable(it) }
    }

    suspend fun deleteAccountById(id: Int) = withContext(Dispatchers.IO) {
        bankAccountDao.deleteAccountById(id)
        context?.let { com.example.service.WearSyncService.syncToWearable(it) }
    }

    /**
     * Parse banking transactions text (SMS, App Notification) using local rule-based parsing.
     */
    suspend fun parseBankText(rawText: String, customApiKey: String? = null, preferredBank: String? = null): ParsedTransaction? = withContext(Dispatchers.IO) {
        try {
            return@withContext com.example.util.LocalOcrParser.parseOcrText(rawText, preferredBank)
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Failed to parse bank text locally", e)
            return@withContext null
        }
    }

    private fun cleanJsonString(raw: String?): String? {
        if (raw == null) return null
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        return clean.trim()
    }

    /**
     * Parse banking transactions from an image (Receipt, Invoice, Transfer Screenshot) using on-device ML Kit OCR.
     * Returns a Pair of ParsedTransaction? and clear error message if failed.
     */
    suspend fun parseBankImage(base64Image: String, mimeType: String, customApiKey: String? = null): Pair<ParsedTransaction?, String?> = withContext(Dispatchers.IO) {
        try {
            val decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            if (bitmap == null) {
                return@withContext Pair(null, "Không thể giải mã dữ liệu ảnh Base64!")
            }
            val text = com.example.util.LocalOcrParser.recognizeTextFromBitmap(bitmap)
            val parsedResult = com.example.util.LocalOcrParser.parseOcrText(text)
            return@withContext Pair(parsedResult, null)
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Failed to parse bank image locally", e)
            return@withContext Pair(null, "Lỗi nhận diện ảnh cục bộ: ${e.localizedMessage}")
        }
    }

    suspend fun restoreDatabase(accounts: List<BankAccount>, transactions: List<TransactionEntity>) = withContext(Dispatchers.IO) {
        bankAccountDao.deleteAllAccounts()
        transactionDao.deleteAllTransactions()
        for (acc in accounts) {
            bankAccountDao.insertAccount(acc)
        }
        for (tx in transactions) {
            transactionDao.insertTransaction(tx)
        }
        context?.let { com.example.service.WearSyncService.syncToWearable(it) }
    }

    private fun noteContainsAccount(note: String, accountNo: String): Boolean {
        val cleanAcc = accountNo.trim().replace(Regex("[^0-9a-zA-Z]"), "")
        if (cleanAcc.isEmpty() || cleanAcc.lowercase() == "n/a" || cleanAcc.length < 4) return false
        
        val cleanNote = note.lowercase().replace(Regex("[^a-z0-9]"), "")
        val cleanAccLower = cleanAcc.lowercase()
        
        if (cleanNote.contains(cleanAccLower)) return true
        
        if (cleanAcc.startsWith("0") && cleanAcc.length == 10) {
            val phoneSuffix = cleanAcc.substring(1)
            if (cleanNote.contains(phoneSuffix)) return true
        }
        
        return false
    }
}
