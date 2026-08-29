package com.example.wear.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface WearDao {
    @Query("SELECT * FROM wear_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WearTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: WearTransaction): Long

    @Query("SELECT * FROM wear_bank_accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<WearBankAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: WearBankAccount)

    @Query("UPDATE wear_bank_accounts SET balance = balance + :amount WHERE name = :name")
    suspend fun updateBalance(name: String, amount: Double)

    @Query("SELECT * FROM wear_category_limits")
    fun getAllLimits(): Flow<List<WearCategoryLimit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimit(limit: WearCategoryLimit)

    @Query("DELETE FROM wear_bank_accounts")
    suspend fun clearAccounts()

    @Query("DELETE FROM wear_transactions")
    suspend fun clearTransactions()

    @Transaction
    suspend fun clearAndSync(accounts: List<WearBankAccount>, transactions: List<WearTransaction>) {
        clearAccounts()
        clearTransactions()
        accounts.forEach { insertAccount(it) }
        transactions.forEach { insertTransaction(it) }
    }
}

@Database(entities = [WearTransaction::class, WearBankAccount::class, WearCategoryLimit::class], version = 1, exportSchema = false)
abstract class WearDatabase : RoomDatabase() {
    abstract fun wearDao(): WearDao

    companion object {
        @Volatile
        private var INSTANCE: WearDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): WearDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WearDatabase::class.java,
                    "vinaspends_wear_db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch(Dispatchers.IO) {
                            val dao = getDatabase(context, scope).wearDao()
                            // Default Accounts
                            dao.insertAccount(WearBankAccount(name = "Tiền mặt", balance = 5000000.0))
                            dao.insertAccount(WearBankAccount(name = "Vietcombank", balance = 12500000.0))
                            dao.insertAccount(WearBankAccount(name = "Techcombank", balance = 8200000.0))
                            dao.insertAccount(WearBankAccount(name = "Ví MoMo", balance = 1500000.0))
                            
                            // Default Limits for major categories
                            dao.insertLimit(WearCategoryLimit("Ăn uống", 4000000.0))
                            dao.insertLimit(WearCategoryLimit("Di chuyển", 1000000.0))
                            dao.insertLimit(WearCategoryLimit("Mua sắm", 3000000.0))
                            dao.insertLimit(WearCategoryLimit("Giải trí", 1500000.0))
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
