package com.example.data.local

import androidx.room.*
import com.example.data.model.BankAccount
import com.example.data.model.DebtEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET category = :newCategory WHERE category = :oldCategory")
    suspend fun updateTransactionCategory(oldCategory: String, newCategory: String)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("SELECT * FROM transactions WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp DESC")
    fun getTransactionsInRange(start: Long, end: Long): Flow<List<TransactionEntity>>
}

@Dao
interface BankAccountDao {
    @Query("SELECT * FROM bank_accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<BankAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: BankAccount): Long

    @Update
    suspend fun updateAccount(account: BankAccount)

    @Query("UPDATE bank_accounts SET balance = :newBalance, lastSynced = :timestamp WHERE name = :name")
    suspend fun updateBalanceByBankName(name: String, newBalance: Double, timestamp: Long)

    @Query("SELECT * FROM bank_accounts WHERE name = :name LIMIT 1")
    suspend fun getAccountByName(name: String): BankAccount?

    @Query("DELETE FROM bank_accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Int)

    @Query("DELETE FROM bank_accounts")
    suspend fun deleteAllAccounts()
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY dueDate ASC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebtById(id: Int)
}
