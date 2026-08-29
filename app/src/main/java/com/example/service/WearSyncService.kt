package com.example.service

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.BankAccount
import com.example.data.model.TransactionEntity
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WearSyncService : WearableListenerService() {
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d("WearSyncService", "Message received from wear: ${messageEvent.path}")
        if (messageEvent.path == "/request_sync") {
            val senderId = messageEvent.sourceNodeId
            serviceScope.launch {
                try {
                    val db = AppDatabase.getDatabase(applicationContext)
                    val accounts = db.bankAccountDao().getAllAccounts().first()
                    val transactions = db.transactionDao().getAllTransactions().first()

                    val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()

                    val syncData = SyncData(accounts = accounts, transactions = transactions)
                    val adapter = moshi.adapter(SyncData::class.java)
                    val jsonString = adapter.toJson(syncData)

                    Wearable.getMessageClient(applicationContext)
                        .sendMessage(senderId, "/sync_response", jsonString.toByteArray(Charsets.UTF_8))
                    Log.d("WearSyncService", "Sync response sent successfully to $senderId!")
                } catch (e: Exception) {
                    Log.e("WearSyncService", "Error during sync: ", e)
                    try {
                        Wearable.getMessageClient(applicationContext)
                            .sendMessage(senderId, "/sync_error", (e.message ?: "Unknown error").toByteArray(Charsets.UTF_8))
                    } catch (ex: Exception) {
                        Log.e("WearSyncService", "Error sending sync error message: ", ex)
                    }
                }
            }
        }
    }

    companion object {
        fun syncToWearable(context: Context) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val db = AppDatabase.getDatabase(context.applicationContext)
                    val accounts = db.bankAccountDao().getAllAccounts().first()
                    val transactions = db.transactionDao().getAllTransactions().first()

                    val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()

                    val syncData = SyncData(accounts = accounts, transactions = transactions)
                    val adapter = moshi.adapter(SyncData::class.java)
                    val jsonString = adapter.toJson(syncData)

                    val nodeClient = Wearable.getNodeClient(context.applicationContext)
                    val messageClient = Wearable.getMessageClient(context.applicationContext)

                    nodeClient.connectedNodes.addOnSuccessListener { nodes ->
                        for (node in nodes) {
                            messageClient.sendMessage(node.id, "/sync_response", jsonString.toByteArray(Charsets.UTF_8))
                            Log.d("WearSyncService", "Live push sync sent successfully to node ${node.displayName} (${node.id})")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WearSyncService", "Error during live push sync: ", e)
                }
            }
        }
    }
}

data class SyncData(
    val accounts: List<BankAccount>,
    val transactions: List<TransactionEntity>
)
