package com.example.wear.data

data class PhoneBankAccount(
    val id: Int = 0,
    val name: String,
    val balance: Double,
    val isLinked: Boolean = false,
    val lastSynced: Long = System.currentTimeMillis()
)

data class PhoneTransaction(
    val id: Int = 0,
    val amount: Double,
    val type: String,
    val category: String,
    val note: String,
    val bankName: String,
    val timestamp: Long
)

data class PhoneSyncData(
    val accounts: List<PhoneBankAccount>,
    val transactions: List<PhoneTransaction>
)
