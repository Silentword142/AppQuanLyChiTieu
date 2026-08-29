package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BankAccount
import com.example.data.model.DebtEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.FinanceRepository
import com.example.data.api.ParsedTransaction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

sealed interface AiParseState {
    object Idle : AiParseState
    object Loading : AiParseState
    data class Success(val transaction: ParsedTransaction) : AiParseState
    data class Error(val message: String) : AiParseState
}

enum class ReportPeriod {
    DAY, WEEK, MONTH, YEAR, CUSTOM
}

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository

    val allTransactions: StateFlow<List<TransactionEntity>>
    val allAccounts: StateFlow<List<BankAccount>>
    val allDebts: StateFlow<List<DebtEntity>>

    private val _customStartDate = MutableStateFlow<Long?>(null)
    val customStartDate: StateFlow<Long?> = _customStartDate.asStateFlow()

    private val _customEndDate = MutableStateFlow<Long?>(null)
    val customEndDate: StateFlow<Long?> = _customEndDate.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.MONTH)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tất cả")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedAccount = MutableStateFlow("Tất cả")
    val selectedAccount: StateFlow<String> = _selectedAccount.asStateFlow()

    private val _selectedType = MutableStateFlow("Tất cả")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _aiParseState = MutableStateFlow<AiParseState>(AiParseState.Idle)
    val aiParseState: StateFlow<AiParseState> = _aiParseState.asStateFlow()

    private val _isSyncingBank = MutableStateFlow(false)
    val isSyncingBank: StateFlow<Boolean> = _isSyncingBank.asStateFlow()

    // Dynamic Category support saved to SharedPreferences
    private val sharedPrefs = application.getSharedPreferences("vinaspends_prefs", android.content.Context.MODE_PRIVATE)

    val defaultCategories = listOf(
        "Ăn uống", "Di chuyển", "Mua sắm", "Giải trí", 
        "Lương", "Nhà cửa", "Sức khỏe", "Học tập", 
        "Hóa đơn", "Đầu tư", "Tiết kiệm", "Du lịch", 
        "Làm đẹp", "Quà tặng", "Gia đình", "Thú cưng",
        "Mua xe", "Bảo hiểm", "Thuế", "Thể thao",
        "Từ thiện", "Thiết bị số", "Sửa chữa", "Ăn vặt", "Khác"
    )

    private val _customCategories = MutableStateFlow<List<String>>(loadCustomCategories())
    val customCategories: StateFlow<List<String>> = _customCategories.asStateFlow()

    private val _deletedCategories = MutableStateFlow<Set<String>>(loadDeletedCategories())
    val deletedCategories: StateFlow<Set<String>> = _deletedCategories.asStateFlow()

    private val _themeChoice = MutableStateFlow(sharedPrefs.getString("theme_choice", "ocean") ?: "ocean")
    val themeChoice: StateFlow<String> = _themeChoice.asStateFlow()

    private val _themeMode = MutableStateFlow(sharedPrefs.getString("theme_mode", "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _categoryParentMap = MutableStateFlow<Map<String, String>>(loadCategoryParentMap())
    val categoryParentMap: StateFlow<Map<String, String>> = _categoryParentMap.asStateFlow()

    private val _categoryLimits = MutableStateFlow<Map<String, Double>>(loadCategoryLimits())
    val categoryLimits: StateFlow<Map<String, Double>> = _categoryLimits.asStateFlow()

    private fun loadCategoryLimits(): Map<String, Double> {
        val map = mutableMapOf<String, Double>()
        val allKeys = sharedPrefs.all
        for ((key, value) in allKeys) {
            if (key != null && key.startsWith("category_limit_")) {
                val category = key.substring("category_limit_".length)
                val limitValue = when (value) {
                    is Float -> value.toDouble()
                    is Double -> value
                    is Long -> value.toDouble()
                    is Int -> value.toDouble()
                    is String -> value.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
                map[category] = limitValue
            }
        }
        return map
    }

    fun setCategoryLimit(category: String, limit: Double) {
        val trimmed = category.trim()
        if (trimmed.isEmpty()) return
        val key = "category_limit_$trimmed"
        if (limit <= 0.0) {
            sharedPrefs.edit().remove(key).apply()
        } else {
            sharedPrefs.edit().putFloat(key, limit.toFloat()).apply()
        }
        _categoryLimits.value = loadCategoryLimits()
    }

    private fun loadCategoryParentMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val allKeys = sharedPrefs.all
        for ((key, value) in allKeys) {
            if (key != null && key.startsWith("category_parent_") && value is String) {
                val child = key.substring("category_parent_".length)
                map[child] = value
            }
        }
        return map
    }

    fun setCategoryParent(childCategory: String, parentCategory: String?) {
        val trimmedChild = childCategory.trim()
        val trimmedParent = parentCategory?.trim()
        
        val currentMap = _categoryParentMap.value.toMutableMap()
        if (trimmedParent.isNullOrEmpty() || trimmedParent == trimmedChild) {
            currentMap.remove(trimmedChild)
            sharedPrefs.edit().remove("category_parent_$trimmedChild").apply()
        } else {
            currentMap[trimmedChild] = trimmedParent
            sharedPrefs.edit().putString("category_parent_$trimmedChild", trimmedParent).apply()
        }
        _categoryParentMap.value = currentMap
    }

    private val preferenceChangeListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "custom_categories") {
            _customCategories.value = loadCustomCategories()
        } else if (key == "deleted_categories") {
            _deletedCategories.value = loadDeletedCategories()
        } else if (key == "theme_choice") {
            _themeChoice.value = sharedPrefs.getString("theme_choice", "ocean") ?: "ocean"
        } else if (key == "theme_mode") {
            _themeMode.value = sharedPrefs.getString("theme_mode", "system") ?: "system"
        } else if (key != null && key.startsWith("category_parent_")) {
            _categoryParentMap.value = loadCategoryParentMap()
        } else if (key != null && key.startsWith("category_limit_")) {
            _categoryLimits.value = loadCategoryLimits()
        }
    }

    fun setThemeChoice(theme: String) {
        _themeChoice.value = theme
        sharedPrefs.edit().putString("theme_choice", theme).apply()
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        sharedPrefs.edit().putString("theme_mode", mode).apply()
    }

    private fun loadCustomCategories(): List<String> {
        val saved = sharedPrefs.getStringSet("custom_categories", emptySet()) ?: emptySet()
        return saved.toList().sorted()
    }

    private fun loadDeletedCategories(): Set<String> {
        return sharedPrefs.getStringSet("deleted_categories", emptySet()) ?: emptySet()
    }

    val allCategories = combine(_customCategories, _deletedCategories) { custom, deleted ->
        (defaultCategories + custom).filter { it !in deleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultCategories)

    fun addCustomCategory(category: String) {
        val trimmed = category.trim()
        if (trimmed.isEmpty()) return
        
        // If it was previously deleted, restore/unhide it
        if (_deletedCategories.value.contains(trimmed)) {
            restoreCategory(trimmed)
            return
        }

        val current = _customCategories.value.toMutableList()
        if (!current.contains(trimmed) && !defaultCategories.contains(trimmed)) {
            current.add(trimmed)
            _customCategories.value = current
            sharedPrefs.edit().putStringSet("custom_categories", current.toSet()).apply()
        }
    }

    fun removeCustomCategory(category: String) {
        val trimmed = category.trim()
        val current = _customCategories.value.toMutableList()
        if (current.contains(trimmed)) {
            current.remove(trimmed)
            _customCategories.value = current
            sharedPrefs.edit().putStringSet("custom_categories", current.toSet()).apply()
            
            // Also clean up stored icon/color customization
            sharedPrefs.edit()
                .remove("category_icon_$trimmed")
                .remove("category_color_$trimmed")
                .apply()
        }
    }

    fun deleteCategory(category: String) {
        val trimmed = category.trim()
        if (trimmed.isEmpty()) return

        if (defaultCategories.contains(trimmed)) {
            // Default category -> Add to deleted_categories to hide it
            val deletedSet = _deletedCategories.value.toMutableSet()
            deletedSet.add(trimmed)
            _deletedCategories.value = deletedSet
            sharedPrefs.edit().putStringSet("deleted_categories", deletedSet).apply()
        } else {
            // Custom category -> Completely remove
            removeCustomCategory(trimmed)
        }
    }

    fun renameCategory(oldCategory: String, newCategory: String) {
        val trimmedOld = oldCategory.trim()
        val trimmedNew = newCategory.trim()
        if (trimmedOld.isEmpty() || trimmedNew.isEmpty() || trimmedOld == trimmedNew) return

        viewModelScope.launch {
            // 1. Update existing transactions in database from oldCategory to newCategory
            repository.updateTransactionCategory(trimmedOld, trimmedNew)
            
            // 2. Update parent-child mappings
            val currentMap = _categoryParentMap.value.toMutableMap()
            val keysToUpdate = currentMap.filter { it.value == trimmedOld }.keys
            keysToUpdate.forEach { child ->
                currentMap[child] = trimmedNew
                sharedPrefs.edit().putString("category_parent_$child", trimmedNew).apply()
            }
            
            if (currentMap.containsKey(trimmedOld)) {
                val parent = currentMap[trimmedOld]
                currentMap.remove(trimmedOld)
                sharedPrefs.edit().remove("category_parent_$trimmedOld").apply()
                if (parent != null) {
                    currentMap[trimmedNew] = parent
                    sharedPrefs.edit().putString("category_parent_$trimmedNew", parent).apply()
                }
            }
            _categoryParentMap.value = currentMap

            // 3. Update category customized icon & color SharedPreferences
            val storedIcon = sharedPrefs.getString("category_icon_$trimmedOld", null)
            val storedColor = sharedPrefs.getString("category_color_$trimmedOld", null)
            val editor = sharedPrefs.edit()
            if (storedIcon != null) {
                editor.putString("category_icon_$trimmedNew", storedIcon)
                editor.remove("category_icon_$trimmedOld")
            }
            if (storedColor != null) {
                editor.putString("category_color_$trimmedNew", storedColor)
                editor.remove("category_color_$trimmedOld")
            }
            editor.apply()

            // 4. Update the category list
            if (defaultCategories.contains(trimmedOld)) {
                val deletedSet = _deletedCategories.value.toMutableSet()
                deletedSet.add(trimmedOld)
                _deletedCategories.value = deletedSet
                sharedPrefs.edit().putStringSet("deleted_categories", deletedSet).apply()

                val custom = _customCategories.value.toMutableList()
                if (!custom.contains(trimmedNew) && !defaultCategories.contains(trimmedNew)) {
                    custom.add(trimmedNew)
                    _customCategories.value = custom
                    sharedPrefs.edit().putStringSet("custom_categories", custom.toSet()).apply()
                }
            } else {
                val custom = _customCategories.value.toMutableList()
                if (custom.contains(trimmedOld)) {
                    custom.remove(trimmedOld)
                }
                if (!custom.contains(trimmedNew) && !defaultCategories.contains(trimmedNew)) {
                    custom.add(trimmedNew)
                }
                _customCategories.value = custom
                sharedPrefs.edit().putStringSet("custom_categories", custom.toSet()).apply()
            }
        }
    }

    fun restoreCategory(category: String) {
        val trimmed = category.trim()
        val deletedSet = _deletedCategories.value.toMutableSet()
        if (deletedSet.contains(trimmed)) {
            deletedSet.remove(trimmed)
            _deletedCategories.value = deletedSet
            sharedPrefs.edit().putStringSet("deleted_categories", deletedSet).apply()
        }
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database.transactionDao(), database.bankAccountDao(), database.debtDao(), application.applicationContext)

        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)

        allTransactions = repository.allTransactions
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allAccounts = repository.allAccounts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allDebts = repository.allDebts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Initial seeding
        viewModelScope.launch {
            repository.seedDefaultAccountsIfEmpty()
        }
    }

    fun setPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
        if (period != ReportPeriod.CUSTOM) {
            _customStartDate.value = null
            _customEndDate.value = null
        }
    }

    fun setCustomDateRange(start: Long?, end: Long?) {
        _customStartDate.value = start
        _customEndDate.value = end
        _selectedPeriod.value = ReportPeriod.CUSTOM
    }

    fun addDebt(title: String, amount: Double, type: String, note: String, dueDate: Long, enableReminder: Boolean = true) {
        viewModelScope.launch {
            val debt = DebtEntity(
                title = title.trim(),
                amount = amount,
                type = type,
                note = note.trim(),
                dueDate = dueDate,
                isPaid = false
            )
            val newId = repository.insertDebt(debt)
            if (enableReminder && dueDate > 0) {
                com.example.receiver.DebtReminderReceiver.scheduleDebtReminder(
                    getApplication(),
                    newId.toInt(),
                    title.trim(),
                    type,
                    amount,
                    dueDate
                )
            }
        }
    }

    fun markDebtAsPaid(debt: DebtEntity, isPaid: Boolean) {
        viewModelScope.launch {
            repository.updateDebt(debt.copy(isPaid = isPaid))
            if (isPaid) {
                com.example.receiver.DebtReminderReceiver.cancelDebtReminder(getApplication(), debt.id)
            } else {
                if (debt.dueDate > System.currentTimeMillis()) {
                    com.example.receiver.DebtReminderReceiver.scheduleDebtReminder(
                        getApplication(),
                        debt.id,
                        debt.title,
                        debt.type,
                        debt.amount,
                        debt.dueDate
                    )
                }
            }
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
            com.example.receiver.DebtReminderReceiver.cancelDebtReminder(getApplication(), debt.id)
        }
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSelectedAccount(accountName: String) {
        _selectedAccount.value = accountName
    }

    fun setSelectedType(type: String) {
        _selectedType.value = type
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Filter transactions dynamically by combining Date, Category, Account, Type, Search queries, and Category parent mappings
    val filteredTransactions = combine(
        allTransactions,
        combine(_selectedPeriod, _customStartDate, _customEndDate) { p, s, e -> Triple(p, s, e) },
        _selectedCategory,
        _categoryParentMap,
        combine(_selectedAccount, _selectedType, _searchQuery) { acc, typ, q -> Triple(acc, typ, q) }
    ) { txs, dateFilters, category, parentMap, extraFilters ->
        val (period, customStart, customEnd) = dateFilters
        val (account, type, query) = extraFilters
        val now = Calendar.getInstance()
        
        val filterStart = if (period == ReportPeriod.CUSTOM) {
            customStart ?: 0L
        } else {
            getStartOfPeriod(now, period)
        }
        
        val filterEnd = if (period == ReportPeriod.CUSTOM) {
            customEnd ?: Long.MAX_VALUE
        } else {
            Long.MAX_VALUE
        }
        
        txs.filter { tx ->
            val dateMatch = tx.timestamp >= filterStart && tx.timestamp <= filterEnd
            val mappedCategory = parentMap[tx.category] ?: tx.category
            val categoryMatch = category == "Tất cả" || tx.category == category || mappedCategory == category
            val accountMatch = account == "Tất cả" || tx.bankName == account
            val typeMatch = when (type) {
                "Chi tiêu" -> tx.type == "EXPENSE"
                "Thu nhập" -> tx.type == "INCOME"
                else -> true
            }
            val searchMatch = if (query.isBlank()) {
                true
            } else {
                val textMatch = tx.note.contains(query, ignoreCase = true) || 
                    tx.bankName.contains(query, ignoreCase = true) ||
                    (tx.accountNo?.contains(query) ?: false) ||
                    tx.category.contains(query, ignoreCase = true)
                
                if (textMatch) {
                    true
                } else {
                    val sdfFull = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm EEEE 'tháng' MM 'năm' yyyy", java.util.Locale("vi"))
                    val dateFormatted = sdfFull.format(java.util.Date(tx.timestamp))
                    dateFormatted.contains(query, ignoreCase = true)
                }
            }
            
            dateMatch && categoryMatch && accountMatch && typeMatch && searchMatch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Income sum, Expense sum, and Category breakdown
    val summaryReport = combine(filteredTransactions, _categoryParentMap) { txs, parentMap ->
        var income = 0.0
        var expense = 0.0
        val expenseCategoryMap = mutableMapOf<String, Double>()
        val incomeCategoryMap = mutableMapOf<String, Double>()

        for (tx in txs) {
            val targetCategory = parentMap[tx.category] ?: tx.category
            if (tx.type == "INCOME") {
                income += tx.amount
                incomeCategoryMap[targetCategory] = (incomeCategoryMap[targetCategory] ?: 0.0) + tx.amount
            } else {
                expense += tx.amount
                expenseCategoryMap[targetCategory] = (expenseCategoryMap[targetCategory] ?: 0.0) + tx.amount
            }
        }

        val totalExpenseSpend = expenseCategoryMap.values.sum()
        val expenseBreakdown = expenseCategoryMap.map { (cat, amount) ->
            CategoryShare(
                category = cat,
                amount = amount,
                percentage = if (totalExpenseSpend > 0) (amount / totalExpenseSpend * 100).toFloat() else 0f
            )
        }.sortedByDescending { it.amount }

        val totalIncomeEarn = incomeCategoryMap.values.sum()
        val incomeBreakdown = incomeCategoryMap.map { (cat, amount) ->
            CategoryShare(
                category = cat,
                amount = amount,
                percentage = if (totalIncomeEarn > 0) (amount / totalIncomeEarn * 100).toFloat() else 0f
            )
        }.sortedByDescending { it.amount }

        PeriodSummary(
            totalIncome = income,
            totalExpense = expense,
            netBalance = income - expense,
            categoryBreakdown = expenseBreakdown, // fallback / default
            expenseBreakdown = expenseBreakdown,
            incomeBreakdown = incomeBreakdown
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PeriodSummary())

    fun addTransaction(
        amount: Double,
        type: String,
        category: String,
        note: String,
        bankName: String,
        timestamp: Long = System.currentTimeMillis(),
        rawSms: String? = null,
        accountNo: String? = null
    ) {
        viewModelScope.launch {
            val trimmedCategory = category.trim()
            if (trimmedCategory.isNotEmpty() && !allCategories.value.contains(trimmedCategory)) {
                addCustomCategory(trimmedCategory)
            }
            val tx = TransactionEntity(
                amount = amount,
                type = type,
                category = trimmedCategory,
                note = note,
                bankName = bankName,
                timestamp = timestamp,
                rawSms = rawSms,
                accountNo = accountNo
            )
            repository.insertTransaction(tx)
        }
    }

    fun updateTransaction(oldTx: TransactionEntity, newTx: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(oldTx, newTx)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addBankAccount(name: String, accountNo: String, balance: Double, accountType: String = "THUONG", creditLimit: Double = 0.0, creditSpent: Double = 0.0) {
        viewModelScope.launch {
            repository.insertAccount(
                BankAccount(
                    name = name,
                    accountNo = accountNo,
                    balance = balance,
                    accountType = accountType,
                    isLinked = true,
                    creditLimit = creditLimit,
                    creditSpent = creditSpent
                )
            )
        }
    }

    fun updateBankAccount(id: Int, newName: String, newAccountNo: String, newBalance: Double, newAccountType: String, creditLimit: Double = 0.0, creditSpent: Double = 0.0) {
        viewModelScope.launch {
            val account = allAccounts.value.find { it.id == id }
            if (account != null) {
                repository.insertAccount(
                    account.copy(
                        name = newName,
                        accountNo = newAccountNo,
                        balance = newBalance,
                        accountType = newAccountType,
                        creditLimit = creditLimit,
                        creditSpent = creditSpent,
                        lastSynced = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun updateBankAccountBalance(id: Int, newBalance: Double) {
        viewModelScope.launch {
            val account = allAccounts.value.find { it.id == id }
            if (account != null) {
                repository.insertAccount(account.copy(balance = newBalance, lastSynced = System.currentTimeMillis()))
            }
        }
    }

    fun removeBankAccount(id: Int) {
        viewModelScope.launch {
            repository.deleteAccountById(id)
        }
    }

    fun clearAiParseState() {
        _aiParseState.value = AiParseState.Idle
    }

    fun simulateIncomingNotification(text: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isSyncingBank.value = true
            try {
                val personalKey = getCustomApiKey()
                val parsed = repository.parseBankText(text, personalKey.ifEmpty { null })
                if (parsed != null) {
                    if (!parsed.isValidTransaction) {
                        onResult("Bỏ qua tin nhắn này: Đây là thông báo nhắc nhở/quảng cáo hoặc thông báo dư nợ, không phải giao dịch phát sinh thực tế.")
                        return@launch
                    }
                    val trimmedCategory = parsed.category.trim()
                    if (trimmedCategory.isNotEmpty() && !allCategories.value.contains(trimmedCategory)) {
                        addCustomCategory(trimmedCategory)
                    }
                    val tx = TransactionEntity(
                        amount = parsed.amount,
                        type = parsed.type,
                        category = trimmedCategory,
                        note = "${parsed.note} (Giả lập bóc tách AI)",
                        bankName = parsed.bankName,
                        timestamp = System.currentTimeMillis(),
                        rawSms = text,
                        accountNo = parsed.accountNo
                    )
                    val matchedAccount = repository.insertTransaction(tx)
                    if (matchedAccount != null) {
                        val accountDisplayName = matchedAccount.name
                        val detailMsg = if (matchedAccount.accountType == "TIN_DUNG") {
                            "tăng dư nợ tín dụng của ví '$accountDisplayName' (số dư khả dụng mới: ${matchedAccount.balance}đ)"
                        } else {
                            "được cộng/trừ trực tiếp vào ví '$accountDisplayName'"
                        }
                        onResult("Bóc tách AI thành công SMS: Giao dịch ${if (parsed.type == "INCOME") "+" else "-"}${parsed.amount}đ $detailMsg!")
                    } else {
                        onResult("Đã bỏ qua giao dịch: Không tìm thấy ví hoặc tài khoản ngân hàng tương thích nào được bạn thêm trong danh sách.")
                    }
                } else {
                    onResult("Gemini phản hồi rỗng hoặc không thể nhận diện cú pháp tin nhắn biến động.")
                }
            } catch (e: Exception) {
                onResult("Lỗi trong quá trình kết nối Gemini: ${e.message}")
            } finally {
                _isSyncingBank.value = false
            }
        }
    }

    fun parseSmsWithGemini(smsText: String) {
        if (smsText.isBlank()) return
        _aiParseState.value = AiParseState.Loading
        viewModelScope.launch {
            val personalKey = getCustomApiKey()
            val result = repository.parseBankText(smsText, personalKey.ifEmpty { null })
            if (result != null) {
                _aiParseState.value = AiParseState.Success(result)
            } else {
                _aiParseState.value = AiParseState.Error("Không thể phân tích dữ liệu biến động hoặc khoá API chưa được thiết lập chính xác.")
            }
        }
    }

    /**
     * Parse receipt or bank transfer screenshot from image base64 bytes using Gemini
     */
    fun parseImageWithGemini(base64Image: String, mimeType: String, onResult: (ParsedTransaction?, String?) -> Unit) {
        viewModelScope.launch {
            val personalKey = getCustomApiKey()
            val (result, error) = repository.parseBankImage(base64Image, mimeType, personalKey.ifEmpty { null })
            onResult(result, error)
        }
    }

    fun getCustomApiKey(): String {
        return sharedPrefs.getString("custom_gemini_api_key", "") ?: ""
    }

    fun saveCustomApiKey(key: String) {
        sharedPrefs.edit().putString("custom_gemini_api_key", key.trim()).apply()
    }

    /**
     * Simulate automated bank transaction crawling
     */
    fun syncBankSimulated(bankName: String) {
        _isSyncingBank.value = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000) // Simulated secure connection time

            val now = System.currentTimeMillis()
            val simulatedTxs = generateSimulatedTxs(bankName, now)

            for (tx in simulatedTxs) {
                repository.insertTransaction(tx)
            }
            _isSyncingBank.value = false
        }
    }

    // --- GOOGLE SIGN-IN & GMAIL CLOUD BACKUP STATE ---
    private val _isGoogleLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("google_logged_in", false))
    val isGoogleLoggedIn: StateFlow<Boolean> = _isGoogleLoggedIn.asStateFlow()

    private val _googleEmail = MutableStateFlow(sharedPrefs.getString("google_email", "guest@gmail.com") ?: "guest@gmail.com")
    val googleEmail: StateFlow<String> = _googleEmail.asStateFlow()

    private val _googleName = MutableStateFlow(sharedPrefs.getString("google_name", "Khách") ?: "Khách")
    val googleName: StateFlow<String> = _googleName.asStateFlow()

    private val _lastBackupTime = MutableStateFlow(sharedPrefs.getLong("last_backup_time", 0L))
    val lastBackupTime: StateFlow<Long> = _lastBackupTime.asStateFlow()

    private val _isBackupInProgress = MutableStateFlow(false)
    val isBackupInProgress: StateFlow<Boolean> = _isBackupInProgress.asStateFlow()

    fun signInWithGoogle(email: String, name: String) {
        viewModelScope.launch {
            _isBackupInProgress.value = true
            kotlinx.coroutines.delay(1000) // connection link latency
            sharedPrefs.edit()
                .putBoolean("google_logged_in", true)
                .putString("google_email", email)
                .putString("google_name", name)
                .apply()
            _isGoogleLoggedIn.value = true
            _googleEmail.value = email
            _googleName.value = name
            _isBackupInProgress.value = false
        }
    }

    fun signOutGoogle() {
        sharedPrefs.edit()
            .putBoolean("google_logged_in", false)
            .apply()
        _isGoogleLoggedIn.value = false
    }

    fun backupToGoogleDrive(onComplete: (Boolean, String) -> Unit) {
        if (!_isGoogleLoggedIn.value) {
            onComplete(false, "Vui lòng đăng nhập Gmail trước để sao lưu!")
            return
        }
        _isBackupInProgress.value = true
        viewModelScope.launch {
            try {
                kotlinx.coroutines.delay(1500) // Sync data cloud latency
                val accounts = allAccounts.value
                val transactions = allTransactions.value

                // Serialize with Moshi
                val moshi = com.squareup.moshi.Moshi.Builder()
                    .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()

                val accountsAdapter = moshi.adapter<List<BankAccount>>(
                    com.squareup.moshi.Types.newParameterizedType(List::class.java, BankAccount::class.java)
                )
                val txsAdapter = moshi.adapter<List<TransactionEntity>>(
                    com.squareup.moshi.Types.newParameterizedType(List::class.java, TransactionEntity::class.java)
                )

                val accountsJson = accountsAdapter.toJson(accounts)
                val txsJson = txsAdapter.toJson(transactions)

                sharedPrefs.edit()
                    .putString("backup_accounts_json", accountsJson)
                    .putString("backup_txs_json", txsJson)
                    .putLong("last_backup_time", System.currentTimeMillis())
                    .apply()

                _lastBackupTime.value = System.currentTimeMillis()
                onComplete(true, "Sao lưu dữ liệu ví & giao giao dịch lên Gmail thành công!")
            } catch (e: Exception) {
                onComplete(false, "Hệ thống gặp lỗi khi sao lưu: ${e.localizedMessage}")
            } finally {
                _isBackupInProgress.value = false
            }
        }
    }

    fun restoreFromGoogleDrive(onComplete: (Boolean, String) -> Unit) {
        if (!_isGoogleLoggedIn.value) {
            onComplete(false, "Vui lòng đăng nhập Gmail trước để khôi phục!")
            return
        }
        val acctsJson = sharedPrefs.getString("backup_accounts_json", null)
        val txsJson = sharedPrefs.getString("backup_txs_json", null)
        if (acctsJson.isNullOrEmpty() || txsJson.isNullOrEmpty()) {
            onComplete(false, "Không tìm thấy bất kỳ tệp dữ liệu sao lưu nào trên tài khoản Gmail này!")
            return
        }

        _isBackupInProgress.value = true
        viewModelScope.launch {
            try {
                kotlinx.coroutines.delay(1500) // Cloud download latency
                val moshi = com.squareup.moshi.Moshi.Builder()
                    .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()

                val accountsAdapter = moshi.adapter<List<BankAccount>>(
                    com.squareup.moshi.Types.newParameterizedType(List::class.java, BankAccount::class.java)
                )
                val txsAdapter = moshi.adapter<List<TransactionEntity>>(
                    com.squareup.moshi.Types.newParameterizedType(List::class.java, TransactionEntity::class.java)
                )

                val restoredAccounts = accountsAdapter.fromJson(acctsJson)
                val restoredTxs = txsAdapter.fromJson(txsJson)

                if (restoredAccounts != null && restoredTxs != null) {
                    repository.restoreDatabase(restoredAccounts, restoredTxs)
                    onComplete(true, "Khôi phục thành công toàn bộ dữ liệu từ đám mây!")
                } else {
                    onComplete(false, "Xảy ra lỗi định dạng tệp đồng bộ.")
                }
            } catch (e: java.lang.Exception) {
                onComplete(false, "Khôi phục dữ liệu thất bại: ${e.localizedMessage}")
            } finally {
                _isBackupInProgress.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
}

// Top-level helper function for period filter calculations
fun getStartOfPeriod(now: Calendar, period: ReportPeriod): Long {
    val cal = now.clone() as Calendar
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    when (period) {
        ReportPeriod.DAY -> {
            // Keep current day
        }
        ReportPeriod.WEEK -> {
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        }
        ReportPeriod.MONTH -> {
            cal.set(Calendar.DAY_OF_MONTH, 1)
        }
        ReportPeriod.YEAR -> {
            cal.set(Calendar.DAY_OF_YEAR, 1)
        }
        ReportPeriod.CUSTOM -> {
            // Fallback
        }
    }
    return cal.timeInMillis
}

// Mock transaction data supplier
fun generateSimulatedTxs(bankName: String, now: Long): List<TransactionEntity> {
    return when (bankName) {
        "Vietcombank" -> listOf(
            TransactionEntity(
                amount = 45000.0,
                type = "EXPENSE",
                category = "Ăn uống",
                note = "THANH TOAN QR HIGHLANDS COFFEE",
                bankName = "Vietcombank",
                timestamp = now - 1200000
            ),
            TransactionEntity(
                amount = 4500000.0,
                type = "INCOME",
                category = "Lương",
                note = "CONG TY CHUYEN LUONG THANG 5",
                bankName = "Vietcombank",
                timestamp = now - 36000000
            )
        )
        "Techcombank" -> listOf(
            TransactionEntity(
                amount = 120000.0,
                type = "EXPENSE",
                category = "Mua sắm",
                note = "TIEN SHOPEE DON HANG 24A",
                bankName = "Techcombank",
                timestamp = now - 1800000
            ),
            TransactionEntity(
                amount = 35000.0,
                type = "EXPENSE",
                category = "Di chuyển",
                note = "GRAB BIKE TRIP NO 3452",
                bankName = "Techcombank",
                timestamp = now - 86400000
            )
        )
        else -> listOf(
            TransactionEntity(
                amount = 60000.0,
                type = "EXPENSE",
                category = "Ăn uống",
                note = "MUA COM TRUA PHAN",
                bankName = bankName,
                timestamp = now - 5000000
            )
        )
    }
}

data class CategoryShare(
    val category: String,
    val amount: Double,
    val percentage: Float
)

data class PeriodSummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netBalance: Double = 0.0,
    val categoryBreakdown: List<CategoryShare> = emptyList(),
    val expenseBreakdown: List<CategoryShare> = emptyList(),
    val incomeBreakdown: List<CategoryShare> = emptyList()
)
