package com.example.motoeire.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("NotificationBootReceiver", "Device booted, rescheduling notifications")
            // Optional: Reschedule notifications here if needed
        }
    }
}