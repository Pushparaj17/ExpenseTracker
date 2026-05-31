package com.push.dev.expensetracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.push.dev.expensetracker.R

object NotificationHelper {

    private const val CHANNEL_ID = "expense_tracker_channel"
    private const val CHANNEL_NAME = "Expense Tracker"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Expense Tracker notifications"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun showBudgetAlert(context: Context, spent: Double, budget: Double) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Budget Alert!")
            .setContentText(
                "You've spent ₹${String.format("%.2f", spent)} of your ₹${String.format("%.2f", budget)} budget."
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1001, notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }
}