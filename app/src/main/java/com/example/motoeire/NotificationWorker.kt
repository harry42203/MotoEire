package com.example.motoeire

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val carId = inputData.getInt("carId", -1)
            val renewalType = inputData.getString("renewalType") ?: return@withContext Result.failure()
            val carName = inputData.getString("carName") ?: "Vehicle"
            val daysRemaining = inputData.getInt("daysRemaining", 0)

            sendNotification(carId, renewalType, carName, daysRemaining)
            Result.success()
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Error sending notification", e)
            Result.retry()
        }
    }

    private fun sendNotification(carId: Int, renewalType: String, carName: String, daysRemaining: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // Create channel if needed (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Vehicle Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val (title, message) = when (renewalType) {
            "NCT" -> "NCT Renewal" to "$carName - NCT expires in $daysRemaining days"
            "TAX" -> "Motor Tax Renewal" to "$carName - Motor tax expires in $daysRemaining days"
            "INSURANCE" -> "Insurance Renewal" to "$carName - Insurance expires in $daysRemaining days"
            else -> "Reminder" to "Vehicle renewal reminder"
        }

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(carId + renewalType.hashCode(), notification)
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "vehicle_reminders"
    }
}