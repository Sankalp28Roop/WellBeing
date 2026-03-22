package com.wellbeing.ui.dashboard

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.wellbeing.core.data.local.entities.AppUsageStat
import com.wellbeing.ui.familyguard.FamilyGuardActivity
import com.wellbeing.ui.theme.WellbeingColors
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AnimatedContent(
        targetState = uiState.selectedApp,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
            } else {
                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
            }
        },
        label = "DashboardTransition"
    ) { selectedApp ->
        if (selectedApp != null) {
            AppDetailScreen(
                stat = selectedApp,
                onBack = { viewModel.selectApp(null) },
                onSetTimer = { limitMs -> viewModel.setAppLimit(selectedApp.packageName, limitMs) }
            )
        } else {
            MainDashboard(uiState, viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    uiState: DashboardUiState,
    viewModel: DashboardViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            color = WellbeingColors.Slate800
                        )
                        Text(
                            getGreeting(),
                            style = MaterialTheme.typography.bodySmall,
                            color = WellbeingColors.Slate500
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = WellbeingColors.Slate500)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WellbeingColors.Blue500)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(WellbeingColors.Slate50)
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stats Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.AccessTime,
                            iconBg = WellbeingColors.Blue100,
                            iconColor = WellbeingColors.Blue500,
                            title = formatScreenTime(uiState.totalScreenTimeMs),
                            subtitle = "Screen Time Today",
                            badge = "+12%",
                            badgeColor = WellbeingColors.Red500
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Lock,
                            iconBg = WellbeingColors.Green100,
                            iconColor = WellbeingColors.Green500,
                            title = "${uiState.stats.count { it.totalForegroundMs > 0 }}",
                            subtitle = "Apps Used",
                            badge = "Active",
                            badgeColor = WellbeingColors.Green500
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.TouchApp,
                            iconBg = WellbeingColors.Orange100,
                            iconColor = WellbeingColors.Orange500,
                            title = "${uiState.unlockCount}",
                            subtitle = "Unlocks Today",
                            badge = null,
                            badgeColor = null
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Notifications,
                            iconBg = WellbeingColors.Purple100,
                            iconColor = WellbeingColors.Purple500,
                            title = "${uiState.totalNotifications}",
                            subtitle = "Notifications",
                            badge = null,
                            badgeColor = null
                        )
                    }
                }

                // Usage Breakdown Card
                item {
                    UsageBreakdownCard(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }

                // Quick Actions
                item {
                    QuickActionsCard(
                        isLockActive = uiState.isLockModeActive,
                        isFocusActive = uiState.isFocusModeActive,
                        isPaused = uiState.isPaused,
                        onPause = { viewModel.togglePause() },
                        onUnpause = { viewModel.unpause() },
                        onLock = { viewModel.toggleLockMode() },
                        onFocus = { viewModel.toggleFocusMode() }
                    )
                }

                // App Usage List
                item {
                    Text(
                        "App Usage",
                        style = MaterialTheme.typography.titleMedium,
                        color = WellbeingColors.Slate800,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(uiState.stats.take(8)) { stat ->
                    AppUsageItem(
                        stat = stat,
                        pivot = uiState.selectedPivot,
                        onClick = { viewModel.selectApp(stat) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconColor: Color,
    title: String,
    subtitle: String,
    badge: String?,
    badgeColor: Color?
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (badge != null && badgeColor != null) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = badgeColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            badge,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = WellbeingColors.Slate800
            )
            
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = WellbeingColors.Slate500
            )
        }
    }
}

@Composable
fun UsageBreakdownCard(
    uiState: DashboardUiState,
    viewModel: DashboardViewModel
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "App Usage Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    color = WellbeingColors.Slate800
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.selectedPivot == DashboardPivot.SCREEN_TIME,
                        onClick = { viewModel.onPivotSelected(DashboardPivot.SCREEN_TIME) },
                        label = { Text("Today", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WellbeingColors.Blue100,
                            selectedLabelColor = WellbeingColors.Blue800
                        )
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Simple bar chart
            WeeklyBarChart(uiState)
            
            Spacer(Modifier.height(16.dp))
            
            // Top apps
            uiState.stats.take(5).forEachIndexed { index, stat ->
                val maxTime = uiState.stats.maxOfOrNull { it.totalForegroundMs } ?: 1
                val progress = stat.totalForegroundMs.toFloat() / maxTime
                val colors = listOf(WellbeingColors.Blue500, WellbeingColors.Orange500, WellbeingColors.Green500, WellbeingColors.Purple500, WellbeingColors.Red500)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        getAppEmoji(stat.packageName),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        getAppName(stat.packageName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WellbeingColors.Slate500,
                        modifier = Modifier.width(80.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(WellbeingColors.Slate100)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(99.dp))
                                .background(colors[index % colors.size])
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = WellbeingColors.Slate500,
                        modifier = Modifier.width(35.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsCard(
    isLockActive: Boolean = false,
    isFocusActive: Boolean = false,
    isPaused: Boolean = false,
    onPause: () -> Unit = {},
    onUnpause: () -> Unit = {},
    onLock: () -> Unit = {},
    onFocus: () -> Unit = {}
) {
    val context = LocalContext.current
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                color = WellbeingColors.Slate800
            )
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    emoji = if (isPaused) "▶️" else "⏸",
                    label = if (isPaused) "Resume" else "Pause",
                    bgColor = if (isPaused) WellbeingColors.Green100 else WellbeingColors.Red100,
                    textColor = if (isPaused) WellbeingColors.Green500 else WellbeingColors.Red500,
                    onClick = { if (isPaused) onUnpause() else onPause() }
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    emoji = if (isLockActive) "🔓" else "🔒",
                    label = if (isLockActive) "Unlock" else "Lock",
                    bgColor = if (isLockActive) WellbeingColors.Green100 else WellbeingColors.Orange100,
                    textColor = if (isLockActive) WellbeingColors.Green500 else WellbeingColors.Orange500,
                    onClick = onLock
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    emoji = "🎯",
                    label = if (isFocusActive) "Active" else "Focus",
                    bgColor = if (isFocusActive) WellbeingColors.Green100 else WellbeingColors.Blue100,
                    textColor = if (isFocusActive) WellbeingColors.Green500 else WellbeingColors.Blue500,
                    onClick = onFocus
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    emoji = "🛡️",
                    label = "Family",
                    bgColor = WellbeingColors.Green100,
                    textColor = WellbeingColors.Green500,
                    onClick = { 
                        context.startActivity(Intent(context, FamilyGuardActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}

@Composable
fun AppUsageItem(
    stat: AppUsageStat,
    pivot: DashboardPivot,
    onClick: () -> Unit
) {
    val valueText = when (pivot) {
        DashboardPivot.SCREEN_TIME -> formatScreenTime(stat.totalForegroundMs)
        DashboardPivot.UNLOCKS -> "${stat.timesOpened} opens"
        DashboardPivot.NOTIFICATIONS -> "${stat.notificationCount} notifications"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(WellbeingColors.Slate100),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    getAppEmoji(stat.packageName),
                    fontSize = 20.sp
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    text = getAppName(stat.packageName),
                    style = MaterialTheme.typography.titleSmall,
                    color = WellbeingColors.Slate800
                )
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodySmall,
                    color = WellbeingColors.Slate500
                )
            }
            
            IconButton(onClick = onClick) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "View details",
                    tint = WellbeingColors.Slate400
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    stat: AppUsageStat,
    onBack: () -> Unit,
    onSetTimer: (Long) -> Unit
) {
    var showTimerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        getAppName(stat.packageName),
                        style = MaterialTheme.typography.titleLarge,
                        color = WellbeingColors.Slate800
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = WellbeingColors.Slate500)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WellbeingColors.Slate50)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // App Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(WellbeingColors.Blue100),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            getAppEmoji(stat.packageName),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            formatScreenTime(stat.totalForegroundMs),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = WellbeingColors.Blue500
                        )
                        Text(
                            "Screen Time Today",
                            style = MaterialTheme.typography.bodySmall,
                            color = WellbeingColors.Slate500
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.TouchApp,
                    iconBg = WellbeingColors.Green100,
                    iconColor = WellbeingColors.Green500,
                    title = "${stat.timesOpened}",
                    subtitle = "Times Opened",
                    badge = null,
                    badgeColor = null
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Notifications,
                    iconBg = WellbeingColors.Purple100,
                    iconColor = WellbeingColors.Purple500,
                    title = "${stat.notificationCount}",
                    subtitle = "Notifications",
                    badge = null,
                    badgeColor = null
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Set Timer Button
            Button(
                onClick = { showTimerDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WellbeingColors.Blue500)
            ) {
                Icon(Icons.Default.Timer, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Set App Limit")
            }
        }
    }

    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = { Text("Set App Limit") },
            text = {
                Text("Set a daily time limit for ${getAppName(stat.packageName)}")
            },
            confirmButton = {
                TextButton(onClick = {
                    onSetTimer(60 * 60 * 1000L) // 1 hour default
                    showTimerDialog = false
                }) {
                    Text("Set 1 Hour", color = WellbeingColors.Blue500)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimerDialog = false }) {
                    Text("Cancel", color = WellbeingColors.Slate500)
                }
            }
        )
    }
}

@Composable
fun WeeklyBarChart(state: DashboardUiState) {
    val chartData = state.weeklyDeviceStats.map { stat ->
        when (state.selectedPivot) {
            DashboardPivot.SCREEN_TIME -> stat.totalScreenTimeMs / (1000f * 60f * 60f)
            DashboardPivot.UNLOCKS -> stat.unlockCount.toFloat()
            DashboardPivot.NOTIFICATIONS -> stat.totalNotifications.toFloat()
        }
    }.ifEmpty { listOf(1f, 2f, 1.5f, 3f, 2.5f, 1f, 2f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val model = entryModelOf(*chartData.toTypedArray())
        Chart(
            chart = columnChart(),
            model = model,
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(),
        )
    }
}

fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning! 👋"
        hour < 17 -> "Good afternoon! 👋"
        else -> "Good evening! 👋"
    }
}

fun formatScreenTime(ms: Long): String {
    val totalMinutes = ms / (1000 * 60)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}

fun getAppName(packageName: String): String {
    return when {
        packageName.contains("youtube") -> "YouTube"
        packageName.contains("instagram") -> "Instagram"
        packageName.contains("tiktok") -> "TikTok"
        packageName.contains("whatsapp") -> "WhatsApp"
        packageName.contains("chrome") -> "Chrome"
        packageName.contains("facebook") -> "Facebook"
        packageName.contains("twitter") -> "Twitter"
        packageName.contains("snapchat") -> "Snapchat"
        packageName.contains("netflix") -> "Netflix"
        packageName.contains("spotify") -> "Spotify"
        packageName.contains("gmail") -> "Gmail"
        packageName.contains("messages") -> "Messages"
        else -> packageName.split(".").last().replaceFirstChar { it.uppercase() }
    }
}

fun getAppEmoji(packageName: String): String {
    return when {
        packageName.contains("youtube") -> "▶️"
        packageName.contains("instagram") -> "📸"
        packageName.contains("tiktok") -> "🎵"
        packageName.contains("whatsapp") -> "💬"
        packageName.contains("chrome") -> "🌐"
        packageName.contains("facebook") -> "👥"
        packageName.contains("twitter") -> "🐦"
        packageName.contains("snapchat") -> "👻"
        packageName.contains("netflix") -> "🎬"
        packageName.contains("spotify") -> "🎧"
        packageName.contains("gmail") -> "📧"
        packageName.contains("messages") -> "💭"
        else -> "📱"
    }
}
