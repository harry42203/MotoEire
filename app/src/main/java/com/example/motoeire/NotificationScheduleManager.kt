package com.example.motoeire

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationScheduleManager(private val context: Context) {

    fun scheduleNotifications(car: Car, settings: NotificationSettings) {
        Log.d("NotificationScheduleManager", "Scheduling notifications for car: ${car.nickname}")

        // Schedule NCT notifications
        if (settings.nctNotificationsEnabled) {
            scheduleReminderNotifications(
                car.id,
                car.nctRenewalDate,
                "NCT",
                car.nickname,
                settings.nctReminders
            )
        }

        // Schedule Tax notifications
        if (settings.taxNotificationsEnabled) {
            scheduleReminderNotifications(
                car.id,
                car.motorTaxRenewalDate,
                "TAX",
                car.nickname,
                settings.taxReminders
            )
        }

        // Schedule Insurance notifications
        if (settings.insuranceNotificationsEnabled) {
            scheduleReminderNotifications(
                car.id,
                car.insuranceRenewalDate,
                "INSURANCE",
                car.nickname,
                settings.insuranceReminders
            )
        }
    }

    private fun scheduleReminderNotifications(
        carId: Int,
        renewalDateMillis: Long,
        renewalType: String,
        carName: String,
        reminderDays: List<Int>
    ) {
        val renewalDate = Calendar.getInstance().apply {
            timeInMillis = renewalDateMillis
        }

        reminderDays.forEach { daysBeforeRenewal ->
            val reminderDate = Calendar.getInstance().apply {
                time = renewalDate.time
                add(Calendar.DAY_OF_YEAR, -daysBeforeRenewal)
            }

            val delayMillis = reminderDate.timeInMillis - System.currentTimeMillis()

            if (delayMillis > 0) {
                val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .setInputData(
                        workDataOf(
                            "carId" to carId,
                            "renewalType" to renewalType,
                            "carName" to carName,
                            "daysRemaining" to daysBeforeRenewal
                        )
                    )
                    .build()

                val uniqueWorkName = "${carId}_${renewalType}_${daysBeforeRenewal}"
                WorkManager.getInstance(context).enqueueUniqueWork(
                    uniqueWorkName,
                    ExistingWorkPolicy.REPLACE,
                    notificationWork
                )

                Log.d("NotificationScheduleManager", "Scheduled $renewalType notification for $carName in $daysBeforeRenewal days")
            }
        }
    }

    fun cancelNotifications(carId: Int) {
        Log.d("NotificationScheduleManager", "Canceling notifications for car ID: $carId")
        val renewalTypes = listOf("NCT", "TAX", "INSURANCE")
        val reminderDays = listOf(90, 30, 7, 1)

        renewalTypes.forEach { type ->
            reminderDays.forEach { days ->
                val uniqueWorkName = "${carId}_${type}_${days}"
                WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName)
            }
        }
    }
}