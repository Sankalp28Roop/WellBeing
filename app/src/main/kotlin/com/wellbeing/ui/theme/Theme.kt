package com.wellbeing.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// FamilyGuard Style Colors
object WellbeingColors {
    // Primary Blue Gradient
    val Blue500 = Color(0xFF3B82F6)
    val Blue600 = Color(0xFF6366F1)
    val Blue100 = Color(0xFFDBEAFE)
    val Blue800 = Color(0xFF1E3A5F)
    
    // Status Colors
    val Green500 = Color(0xFF22C55E)
    val Green100 = Color(0xFFDCFCE7)
    val Green800 = Color(0xFF14532D)
    
    val Red500 = Color(0xFFDC2626)
    val Red100 = Color(0xFFFEE2E2)
    val Red800 = Color(0xFF7F1D1D)
    
    val Orange500 = Color(0xFFF59E0B)
    val Orange100 = Color(0xFFFEF3C7)
    
    val Purple100 = Color(0xFFF3E8FF)
    val Purple500 = Color(0xFFA855F7)
    
    // Neutral Colors
    val Slate50 = Color(0xFFF8FAFC)
    val Slate100 = Color(0xFFF1F5F9)
    val Slate200 = Color(0xFFE2E8F0)
    val Slate400 = Color(0xFF94A3B8)
    val Slate500 = Color(0xFF64748B)
    val Slate800 = Color(0xFF1E293B)
    val Slate900 = Color(0xFF0F172A)
}

private val LightColorScheme = lightColorScheme(
    primary = WellbeingColors.Blue500,
    onPrimary = Color.White,
    primaryContainer = WellbeingColors.Blue100,
    onPrimaryContainer = WellbeingColors.Blue800,
    
    secondary = WellbeingColors.Green500,
    onSecondary = Color.White,
    secondaryContainer = WellbeingColors.Green100,
    onSecondaryContainer = WellbeingColors.Green800,
    
    tertiary = WellbeingColors.Purple500,
    tertiaryContainer = WellbeingColors.Purple100,
    
    error = WellbeingColors.Red500,
    errorContainer = WellbeingColors.Red100,
    
    background = WellbeingColors.Slate50,
    onBackground = WellbeingColors.Slate800,
    
    surface = Color.White,
    onSurface = WellbeingColors.Slate800,
    surfaceVariant = WellbeingColors.Slate100,
    onSurfaceVariant = WellbeingColors.Slate500,
    
    outline = WellbeingColors.Slate200,
    outlineVariant = WellbeingColors.Slate400
)

private val DarkColorScheme = darkColorScheme(
    primary = WellbeingColors.Blue500,
    onPrimary = Color.White,
    primaryContainer = WellbeingColors.Blue800,
    onPrimaryContainer = WellbeingColors.Blue100,
    
    secondary = WellbeingColors.Green500,
    onSecondary = Color.White,
    secondaryContainer = WellbeingColors.Green800,
    onSecondaryContainer = WellbeingColors.Green100,
    
    tertiary = WellbeingColors.Purple500,
    tertiaryContainer = WellbeingColors.Purple100,
    
    error = WellbeingColors.Red500,
    errorContainer = WellbeingColors.Red800,
    
    background = WellbeingColors.Slate900,
    onBackground = WellbeingColors.Slate50,
    
    surface = WellbeingColors.Slate800,
    onSurface = WellbeingColors.Slate50,
    surfaceVariant = WellbeingColors.Slate800,
    onSurfaceVariant = WellbeingColors.Slate400,
    
    outline = WellbeingColors.Slate400,
    outlineVariant = WellbeingColors.Slate500
)

@Composable
fun WellBeingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WellbeingTypography,
        content = content
    )
}
