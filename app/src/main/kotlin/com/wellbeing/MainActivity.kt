package com.wellbeing

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wellbeing.ui.dashboard.DashboardScreen
import com.wellbeing.ui.limits.AppLimitsScreen
import com.wellbeing.ui.focus.FocusModeScreen
import com.wellbeing.ui.bedtime.BedtimeScreen
import com.wellbeing.ui.onboarding.OnboardingScreen
import com.wellbeing.ui.settings.SettingsScreen
import com.wellbeing.ui.goals.GoalsScreen
import com.wellbeing.ui.theme.WellBeingTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    object Goals : Screen("goals", "Goals", Icons.Filled.Flag, Icons.Outlined.Flag)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    companion object {
        const val ACCESSIBILITY_SERVICE_NAME = "com.wellbeing/.service.WellbeingAccessibilityService"
        
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            val serviceName = ComponentName(context, com.wellbeing.service.WellbeingAccessibilityService::class.java).flattenToString()
            
            return enabledServices.any { it.id == serviceName }
        }
        
        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
    
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WellBeingTheme {
                val navController = rememberNavController()
                val startDestination = if (hasUsageStatsPermission()) "dashboard" else "onboarding"
                
                val bottomNavItems = listOf(Screen.Dashboard, Screen.Goals, Screen.Settings)
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }
                
                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                bottomNavItems.forEach { screen ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                                contentDescription = screen.title
                                            )
                                        },
                                        label = { Text(screen.title) },
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(navController = navController, startDestination = startDestination) {
                            composable("onboarding") { 
                                OnboardingScreen(onFinish = { 
                                    navController.navigate("dashboard") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }) 
                            }
                            composable("dashboard") { 
                                DashboardScreen() 
                            }
                            composable("goals") { 
                                GoalsScreen() 
                            }
                            composable("limits") { 
                                AppLimitsScreen(onNavigateBack = { navController.popBackStack() }) 
                            }
                            composable("focus") { 
                                FocusModeScreen() 
                            }
                            composable("bedtime") { 
                               BedtimeScreen()
                            }
                            composable("settings") {
                                SettingsScreen(onNavigateBack = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }
}
