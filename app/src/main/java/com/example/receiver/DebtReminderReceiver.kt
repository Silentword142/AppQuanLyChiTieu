package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class DebtReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val debtId = intent.getIntExtra("debt_id", 0)
        val title = intent.getStringExtra("debt_title") ?: "Nhắc nhở trả nợ"
        val desc = intent.getStringExtra("debt_desc") ?: "Hôm nay đến ngày thanh toán khoản nợ của bạn."
        val amount = intent.getDoubleExtra("debt_amount", 0.0)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "debt_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nhắc nhở nợ nần",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh thông báo nhắc nhở các khoản vay/nợ đến hạn"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            debtId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedAmount = String.format("%,.0fđ", amount)
        val contentText = "$desc: $formattedAmount"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(debtId, notification)
    }

    companion object {
        fun scheduleDebtReminder(
            context: Context,
            debtId: Int,
            title: String,
            type: String,
            amount: Double,
            dueDateMs: Long
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, DebtReminderReceiver::class.java).apply {
                putExtra("debt_id", debtId)
                putExtra("debt_title", if (type == "VAY") "Đến hạn trả nợ: $title" else "Đến hạn đòi nợ: $title")
                putExtra("debt_desc", if (type == "VAY") "Bạn có khoản nợ cần thanh toán" else "Bạn có khoản cho vay cần thu hồi")
                putExtra("debt_amount", amount)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                debtId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // If the due date is in the future, schedule it!
            if (dueDateMs > System.currentTimeMillis()) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            android.app.AlarmManager.RTC_WAKEUP,
                            dueDateMs,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            android.app.AlarmManager.RTC_WAKEUP,
                            dueDateMs,
                            pendingIntent
                        )
                    }
                } catch (e: Exception) {
                    // Fallback to normal set
                    alarmManager.set(
                        android.app.AlarmManager.RTC_WAKEUP,
                        dueDateMs,
                        pendingIntent
                    )
                }
            }
        }

        fun cancelDebtReminder(context: Context, debtId: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, DebtReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                debtId,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}
