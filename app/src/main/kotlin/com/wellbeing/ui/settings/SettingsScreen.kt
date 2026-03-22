package com.wellbeing.ui.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wellbeing.ui.theme.WellbeingColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = WellbeingColors.Slate800
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = WellbeingColors.Slate500)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WellbeingColors.Slate50)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Export Section
            SettingsSection(title = "📤 Export Data") {
                SettingsItem(
                    icon = Icons.Default.Code,
                    title = "Export as JSON",
                    subtitle = "Detailed usage data format",
                    onClick = {
                        scope.launch {
                            viewModel.exportJson()?.let { file ->
                                val shareIntent = viewModel.shareFile(file)
                                context.startActivity(Intent.createChooser(shareIntent, "Share Report"))
                            }
                        }
                    }
                )
                SettingsItem(
                    icon = Icons.Default.TableChart,
                    title = "Export as CSV",
                    subtitle = "Spreadsheet compatible",
                    onClick = {
                        scope.launch {
                            viewModel.exportCsv()?.let { file ->
                                val shareIntent = viewModel.shareFile(file)
                                context.startActivity(Intent.createChooser(shareIntent, "Share Report"))
                            }
                        }
                    }
                )
            }

            // Notifications Section
            SettingsSection(title = "🔔 Notifications") {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Usage Reminders",
                    subtitle = "Periodic screen time reminders",
                    checked = uiState.remindersEnabled,
                    onCheckedChange = { viewModel.toggleReminders(it) }
                )
                SettingsToggleItem(
                    icon = Icons.Default.Warning,
                    title = "Goal Alerts",
                    subtitle = "Notify when goal exceeded",
                    checked = uiState.goalAlertsEnabled,
                    onCheckedChange = { viewModel.toggleGoalAlerts(it) }
                )
            }

            // Privacy Section
            SettingsSection(title = "🔒 Privacy") {
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Privacy Policy",
                    subtitle = "View our privacy practices",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Clear All Data",
                    subtitle = "Delete all usage history",
                    onClick = { showClearDialog = true },
                    isDestructive = true
                )
            }

            // About Section
            SettingsSection(title = "ℹ️ About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "1.0.0",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Default.Build,
                    title = "Build",
                    subtitle = "Release APK",
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = WellbeingColors.Red500) },
            title = { Text("Clear All Data?") },
            text = { 
                Text(
                    "This will permanently delete all your usage history, app limits, and settings. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear", color = WellbeingColors.Red500)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = WellbeingColors.Slate500)
                }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = WellbeingColors.Blue500,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isDestructive) WellbeingColors.Red500 else WellbeingColors.Slate500,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isDestructive) WellbeingColors.Red500 else WellbeingColors.Slate800
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = WellbeingColors.Slate500
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = WellbeingColors.Slate400
        )
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = WellbeingColors.Slate500,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = WellbeingColors.Slate800
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = WellbeingColors.Slate500
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = WellbeingColors.Blue500
            )
        )
    }
}
