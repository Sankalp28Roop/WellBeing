package com.wellbeing.ui.goals

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wellbeing.ui.theme.WellbeingColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Goals",
                        style = MaterialTheme.typography.titleLarge,
                        color = WellbeingColors.Slate800
                    )
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Goal Card with Progress Ring
            GoalProgressCard(uiState, viewModel)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Quick Goal Buttons
            QuickGoalSection(viewModel)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Statistics Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoalStatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🎯",
                    title = "Goals Met",
                    value = "5/7",
                    color = WellbeingColors.Green500,
                    bgColor = WellbeingColors.Green100
                )
                GoalStatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "📊",
                    title = "Avg Daily",
                    value = "3.2h",
                    color = WellbeingColors.Blue500,
                    bgColor = WellbeingColors.Blue100
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Achievements Section
            AchievementsCard()
        }
    }

    if (showGoalDialog) {
        GoalPickerDialog(
            currentGoalMs = uiState.dailyScreenTimeGoalMs,
            onDismiss = { showGoalDialog = false },
            onConfirm = { goalMs ->
                viewModel.setScreenTimeGoal(goalMs)
                showGoalDialog = false
            }
        )
    }
}

@Composable
fun GoalProgressCard(
    uiState: GoalsUiState,
    viewModel: GoalsViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎯 Daily Screen Time Goal",
                style = MaterialTheme.typography.titleMedium,
                color = WellbeingColors.Slate800
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Circular Progress
            GoalProgressCircle(uiState)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Goal Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Switch(
                    checked = uiState.isGoalEnabled,
                    onCheckedChange = { viewModel.toggleGoal(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = WellbeingColors.Green500
                    )
                )
                Text(
                    text = if (uiState.isGoalEnabled) "Goal Active" else "Goal Disabled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WellbeingColors.Slate500
                )
            }
        }
    }
}

@Composable
fun GoalProgressCircle(uiState: GoalsUiState) {
    val animatedProgress by animateFloatAsState(
        targetValue = uiState.goalProgress.coerceIn(0f, 1f),
        label = "progress"
    )
    
    val progressColor = when {
        uiState.goalProgress >= 1f -> WellbeingColors.Red500
        uiState.goalProgress >= 0.8f -> WellbeingColors.Orange500
        else -> WellbeingColors.Blue500
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(180.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            
            // Background circle
            drawCircle(
                color = WellbeingColors.Slate100,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )
            
            // Progress arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val currentMinutes = uiState.currentScreenTimeMs / (1000 * 60)
            val currentHours = currentMinutes / 60
            val remainingMinutes = currentMinutes % 60
            
            Text(
                text = "${currentHours}h ${remainingMinutes}m",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = WellbeingColors.Slate800
            )
            
            val goalHours = uiState.dailyScreenTimeGoalMs / (1000 * 60 * 60)
            Text(
                text = "of ${goalHours}h goal",
                style = MaterialTheme.typography.bodySmall,
                color = WellbeingColors.Slate500
            )
            
            Spacer(Modifier.height(8.dp))
            
            val remaining = uiState.dailyScreenTimeGoalMs - uiState.currentScreenTimeMs
            val remainingMinutesTotal = (remaining / (1000 * 60)).coerceAtLeast(0)
            val remainingHours = remainingMinutesTotal / 60
            
            if (remaining > 0) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = WellbeingColors.Green100
                ) {
                    Text(
                        text = "${remainingHours}h ${remainingMinutesTotal % 60}m remaining",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = WellbeingColors.Green500
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = WellbeingColors.Red100
                ) {
                    Text(
                        text = "Goal exceeded!",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = WellbeingColors.Red500
                    )
                }
            }
        }
    }
}

@Composable
fun QuickGoalSection(viewModel: GoalsViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "⚡ Quick Set Goal",
                style = MaterialTheme.typography.titleSmall,
                color = WellbeingColors.Slate800
            )
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(1, 2, 3, 4, 5, 6).forEach { hours ->
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.setScreenTimeGoal(hours * 60 * 60 * 1000L) },
                        label = { 
                            Text(
                                "${hours}h",
                                style = MaterialTheme.typography.labelMedium
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WellbeingColors.Blue500,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun GoalStatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    value: String,
    color: Color,
    bgColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = WellbeingColors.Slate500
            )
        }
    }
}

@Composable
fun AchievementsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = "🏆 Rewards",
                    style = MaterialTheme.typography.titleSmall,
                    color = WellbeingColors.Slate800
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = WellbeingColors.Blue100
                ) {
                    Text(
                        "280 pts",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = WellbeingColors.Blue500
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Achievement items
            AchievementItem(
                emoji = "🌟",
                title = "Screen Saver",
                subtitle = "Under goal 3 days",
                points = "+50",
                bgColor = WellbeingColors.Green100,
                textColor = WellbeingColors.Green500
            )
            
            Spacer(Modifier.height(8.dp))
            
            AchievementItem(
                emoji = "📚",
                title = "Study Champion",
                subtitle = "2h education apps",
                points = "+80",
                bgColor = WellbeingColors.Blue100,
                textColor = WellbeingColors.Blue500
            )
            
            Spacer(Modifier.height(8.dp))
            
            AchievementItem(
                emoji = "🌙",
                title = "Sleep Star",
                subtitle = "Bedtime 7 nights",
                points = "+150",
                bgColor = WellbeingColors.Orange100,
                textColor = WellbeingColors.Orange500
            )
        }
    }
}

@Composable
fun AchievementItem(
    emoji: String,
    title: String,
    subtitle: String,
    points: String,
    bgColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = textColor
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = WellbeingColors.Slate500
            )
        }
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = textColor.copy(alpha = 0.1f)
        ) {
            Text(
                points,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}

@Composable
fun GoalPickerDialog(
    currentGoalMs: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var hours by remember { mutableStateOf((currentGoalMs / (1000 * 60 * 60)).toInt()) }
    var minutes by remember { mutableStateOf(((currentGoalMs % (1000 * 60 * 60)) / (1000 * 60)).toInt()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Set Daily Goal",
                style = MaterialTheme.typography.titleMedium
            ) 
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Set your maximum daily screen time",
                    style = MaterialTheme.typography.bodySmall,
                    color = WellbeingColors.Slate500
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hours", style = MaterialTheme.typography.labelSmall, color = WellbeingColors.Slate500)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (hours > 0) hours-- }) {
                                Icon(Icons.Default.Remove, "Decrease", tint = WellbeingColors.Blue500)
                            }
                            Text("$hours", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                            IconButton(onClick = { if (hours < 12) hours++ }) {
                                Icon(Icons.Default.Add, "Increase", tint = WellbeingColors.Blue500)
                            }
                        }
                    }
                    Text(" : ", style = MaterialTheme.typography.headlineMedium)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Min", style = MaterialTheme.typography.labelSmall, color = WellbeingColors.Slate500)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (minutes >= 5) minutes -= 5 else minutes = 0 }) {
                                Icon(Icons.Default.Remove, "Decrease", tint = WellbeingColors.Blue500)
                            }
                            Text("$minutes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                            IconButton(onClick = { if (minutes < 55) minutes += 5 else minutes = 55 }) {
                                Icon(Icons.Default.Add, "Increase", tint = WellbeingColors.Blue500)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm((hours * 60 + minutes) * 60 * 1000L) }) {
                Text("Save", color = WellbeingColors.Blue500)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = WellbeingColors.Slate500)
            }
        }
    )
}
