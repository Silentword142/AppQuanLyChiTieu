package com.example.wear

import android.content.Context
import android.util.Log
import com.example.wear.data.*
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WearSyncListenerService : WearableListenerService() {
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d("WearSyncListener", "Background received message from phone: ${messageEvent.path}")
        if (messageEvent.path == "/sync_response") {
            val jsonString = String(messageEvent.data, Charsets.UTF_8)
            serviceScope.launch {
                try {
                    val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                    val adapter = moshi.adapter(PhoneSyncData::class.java)
                    val phoneSyncData = adapter.fromJson(jsonString)
                    if (phoneSyncData != null) {
                        val db = WearDatabase.getDatabase(applicationContext, serviceScope)
                        val wearAccounts = phoneSyncData.accounts.map {
                            WearBankAccount(name = it.name, balance = it.balance)
                        }
                        val wearTransactions = phoneSyncData.transactions.map {
                            WearTransaction(
                                amount = it.amount,
                                type = it.type,
                                category = it.category,
                                note = it.note,
                                bankName = it.bankName,
                                timestamp = it.timestamp
                            )
                        }
                        db.wearDao().clearAndSync(wearAccounts, wearTransactions)
                        Log.d("WearSyncListener", "Background sync completed successfully!")

                        val sharedPrefs = getSharedPreferences("wear_finance_prefs", Context.MODE_PRIVATE)
                        sharedPrefs.edit().putLong("last_sync_time", System.currentTimeMillis()).apply()
                    }
                } catch (e: Exception) {
                    Log.e("WearSyncListener", "Error during background sync: ", e)
                }
            }
        }
    }
}
