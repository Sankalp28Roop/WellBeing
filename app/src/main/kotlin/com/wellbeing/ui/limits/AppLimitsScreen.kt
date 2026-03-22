package com.wellbeing.ui.limits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wellbeing.core.data.local.entities.AppLimit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLimitsScreen(
    viewModel: AppLimitsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val limits by viewModel.limits.collectAsState()
    val appLimitPin by viewModel.appLimitPin.collectAsState()
    
    var isAuthorized by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(showPinDialogInitialValue(appLimitPin, isAuthorized)) }
    
    LaunchedEffect(appLimitPin) {
        if (appLimitPin != null && !isAuthorized) {
            showPinDialog = true
        } else if (appLimitPin == null) {
            isAuthorized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Limits") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        // Icon placeholder (Back)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (isAuthorized) {
                items(limits) { limit ->
                    AppLimitItem(limit, onDelete = { viewModel.removeLimit(limit.packageName) })
                }
            } else {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Please enter PIN to access settings")
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        PinEntryDialog(
            onPinEntered = { pin ->
                if (pin == appLimitPin) {
                    isAuthorized = true
                    showPinDialog = false
                }
            },
            onDismiss = onNavigateBack
        )
    }
}

private fun showPinDialogInitialValue(pin: String?, authorized: Boolean): Boolean {
    return pin != null && !authorized
}

@Composable
fun PinEntryDialog(onPinEntered: (String) -> Unit, onDismiss: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter PIN") },
        text = {
            TextField(
                value = pin,
                onValueChange = { if (it.length <= 4) pin = it },
                label = { Text("4-digit PIN") }
            )
        },
        confirmButton = {
            Button(onClick = { if (pin.length == 4) onPinEntered(pin) }) {
                Text("Unlock")
            }
        }
    )
}

@Composable
fun AppLimitItem(limit: AppLimit, onDelete: () -> Unit) {
    val minutes = limit.dailyLimitMs / (1000 * 60)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = limit.packageName, style = MaterialTheme.typography.bodyLarge)
            Text(text = "$minutes min limit", style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onDelete) {
            // Icon placeholder (Delete)
        }
    }
}
