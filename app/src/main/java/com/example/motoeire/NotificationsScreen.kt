package com.example.motoeire

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.notificationSettings.collectAsState()

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Quick Actions
            SettingsSectionHeader(
                icon = Icons.Outlined.NotificationsActive,
                title = "Quick Actions",
                description = "Manage all notifications at once"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.enableAllNotifications() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Enable All")
                }

                Button(
                    onClick = { viewModel.disableAllNotifications() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Disable All")
                }
            }

            Divider()

            // NCT Notifications
            NotificationTypeCard(
                title = "NCT Reminders",
                enabled = settings.nctNotificationsEnabled,
                reminders = settings.nctReminders,
                defaultReminders = listOf(90, 30, 7, 1),
                onEnabledChange = { viewModel.updateNctEnabled(it) },
                onRemindersChange = { viewModel.updateNctReminders(it) }
            )

            // Tax Notifications
            NotificationTypeCard(
                title = "Motor Tax Reminders",
                enabled = settings.taxNotificationsEnabled,
                reminders = settings.taxReminders,
                defaultReminders = listOf(30, 7, 1),
                onEnabledChange = { viewModel.updateTaxEnabled(it) },
                onRemindersChange = { viewModel.updateTaxReminders(it) }
            )

            // Insurance Notifications
            NotificationTypeCard(
                title = "Insurance Reminders",
                enabled = settings.insuranceNotificationsEnabled,
                reminders = settings.insuranceReminders,
                defaultReminders = listOf(30, 7, 1),
                onEnabledChange = { viewModel.updateInsuranceEnabled(it) },
                onRemindersChange = { viewModel.updateInsuranceReminders(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun NotificationTypeCard(
    title: String,
    enabled: Boolean,
    reminders: List<Int>,
    defaultReminders: List<Int>,
    onEnabledChange: (Boolean) -> Unit,
    onRemindersChange: (List<Int>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (enabled) "🟢 Enabled" else "🔴 Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) Color.Green else MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = enabled,
                        onCheckedChange = onEnabledChange
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            if (expanded && enabled) {
                Divider()

                Text(
                    text = "Reminders (days before renewal):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    reminders.forEach { days ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = when (days) {
                                    90 -> "3 months before"
                                    30 -> "1 month before"
                                    7 -> "1 week before"
                                    1 -> "1 day before"
                                    else -> "$days days before"
                                },
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color.Green,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                TextButton(
                    onClick = { onRemindersChange(defaultReminders) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Reset to Default")
                }
            }
        }
    }
}