package com.example.wear.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wear_transactions")
data class WearTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String,
    val note: String,
    val bankName: String,
    val timestamp: Long
)

@Entity(tableName = "wear_bank_accounts")
data class WearBankAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val balance: Double,
    val lastSynced: Long = System.currentTimeMillis()
)

@Entity(tableName = "wear_category_limits")
data class WearCategoryLimit(
    @PrimaryKey val category: String,
    val limitAmount: Double
)
