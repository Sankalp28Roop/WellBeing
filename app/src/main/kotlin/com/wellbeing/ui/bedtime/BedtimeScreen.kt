package com.wellbeing.ui.bedtime

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wellbeing.core.data.local.entities.BedtimeSchedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BedtimeScreen(
    viewModel: BedtimeViewModel = hiltViewModel()
) {
    val schedule by viewModel.schedule.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Bedtime Mode") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Schedule", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            
            schedule?.let { s ->
                Text(text = "From ${s.startHour}:${s.startMinute} to ${s.endHour}:${s.endMinute}")
                Spacer(Modifier.height(24.dp))
                
                Row {
                    Text(text = "Grayscale", modifier = Modifier.weight(1f))
                    Switch(checked = s.isGrayscaleEnabled, onCheckedChange = { 
                        viewModel.updateSchedule(s.copy(isGrayscaleEnabled = it))
                    })
                }
                
                Row {
                    Text(text = "Dark Theme", modifier = Modifier.weight(1f))
                    Switch(checked = s.isDarkThemeEnabled, onCheckedChange = { 
                        viewModel.updateSchedule(s.copy(isDarkThemeEnabled = it))
                    })
                }
            } ?: run {
                Button(onClick = { 
                    viewModel.updateSchedule(BedtimeSchedule(startHour = 22, startMinute = 0, endHour = 7, endMinute = 0, daysOfWeek = "1,2,3,4,5"))
                }) {
                    Text("Setup Initial Schedule")
                }
            }
        }
    }
}
