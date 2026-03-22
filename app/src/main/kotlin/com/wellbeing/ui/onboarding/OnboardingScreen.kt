package com.wellbeing.ui.onboarding

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (currentStep) {
            1 -> PermissionStep(
                title = "Usage Access",
                description = "We need this to track your app usage and show you your dashboard.",
                buttonText = "Grant Usage Access",
                onButtonClick = { 
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                },
                onNext = { currentStep++ }
            )
            2 -> PermissionStep(
                title = "Accessibility Service",
                description = "Required to enforce app limits and Focus mode.",
                buttonText = "Open Accessibility Settings",
                onButtonClick = { 
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                onNext = { currentStep++ }
            )
            3 -> PermissionStep(
                title = "All Set!",
                description = "You're ready to take control of your digital wellbeing.",
                buttonText = "Go to Dashboard",
                onButtonClick = onFinish,
                onNext = {}
            )
        }
    }
}

@Composable
fun PermissionStep(
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    onNext: () -> Unit
) {
    Text(text = title, style = MaterialTheme.typography.displaySmall)
    Spacer(Modifier.height(16.dp))
    Text(text = description, style = MaterialTheme.typography.bodyLarge)
    Spacer(Modifier.height(32.dp))
    Button(onClick = onButtonClick) {
        Text(buttonText)
    }
    Spacer(Modifier.height(16.dp))
    TextButton(onClick = onNext) {
        Text("Done / Next")
    }
}
