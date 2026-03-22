package com.wellbeing.ui.focus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeScreen(
    viewModel: FocusModeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Focus Mode") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Profiles", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.profiles) { profile ->
                    ProfileItem(
                        profile = profile,
                        isActive = uiState.activeProfile?.id == profile.id,
                        onToggle = { viewModel.toggleFocusMode(profile.id, !it) }
                    )
                }
            }
            
            Button(
                onClick = { /* Show Create Profile Dialog */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create New Profile")
            }
        }
    }
}

@Composable
fun ProfileItem(
    profile: com.wellbeing.core.data.local.entities.FocusModeProfile,
    isActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = profile.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(checked = isActive, onCheckedChange = onToggle)
        }
    }
}
