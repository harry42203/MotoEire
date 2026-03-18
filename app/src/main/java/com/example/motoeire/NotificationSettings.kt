package com.example.motoeire

data class NotificationSettings(
    val nctNotificationsEnabled: Boolean = true,
    val taxNotificationsEnabled: Boolean = true,
    val insuranceNotificationsEnabled: Boolean = true,

    // NCT reminders: 3 months, 1 month, 1 week, 1 day
    val nctReminders: List<Int> = listOf(90, 30, 7, 1),
    // Tax reminders: 1 month, 1 week, 1 day
    val taxReminders: List<Int> = listOf(30, 7, 1),
    // Insurance reminders: 1 month, 1 week, 1 day
    val insuranceReminders: List<Int> = listOf(30, 7, 1)
)