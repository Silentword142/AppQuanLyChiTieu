package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String, // "Ăn uống", "Di chuyển", "Mua sắm", "Giải trí", "Lương", "Nhà cửa", "Khác"
    val note: String,
    val bankName: String, // "Vietcombank", "Techcombank", "MB Bank", "TPBank", "VPBank", "Tiền mặt", "Ví MoMo"
    val timestamp: Long,
    val rawSms: String? = null,
    val accountNo: String? = null
)

@Entity(tableName = "bank_accounts")
data class BankAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val accountNo: String,
    val balance: Double,
    val isLinked: Boolean = false,
    val lastSynced: Long = System.currentTimeMillis(),
    val accountType: String = "THUONG", // "THUONG" for normal/debit wallets, "TIN_DUNG" for credit wallets
    val creditLimit: Double = 0.0,
    val creditSpent: Double = 0.0
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,            // Tên khoản nợ / Tên người nợ
    val amount: Double,           // Số tiền
    val type: String,             // "VAY" (Nợ tôi phải trả) hoặc "CHO_VAY" (Nợ người ta phải trả tôi)
    val note: String,             // Ghi chú chi tiết
    val dueDate: Long,            // Ngày đến hạn trả nợ
    val isPaid: Boolean = false   // Trạng thái đã trả hay chưa
)
