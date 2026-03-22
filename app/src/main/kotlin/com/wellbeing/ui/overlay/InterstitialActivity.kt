package com.wellbeing.ui.overlay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wellbeing.ui.theme.WellBeingTheme
import com.wellbeing.core.data.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InterstitialActivity : ComponentActivity() {
    
    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val packageName = intent.getStringExtra("EXTRA_PACKAGE_NAME") ?: "App"
        val reason = intent.getStringExtra("EXTRA_REASON") ?: "Restricted"

        setContent {
            WellBeingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BlockScreen(
                        packageName = packageName,
                        reason = reason,
                        onClose = { finish() },
                        onTakeABreak = { minutes ->
                            takeABreak(minutes)
                        }
                    )
                }
            }
        }
    }

    private fun takeABreak(minutes: Int) {
        val until = System.currentTimeMillis() + (minutes * 60 * 1000L)
        MainScope().launch {
            settingsRepository.setPausedUntilTimestamp(until)
            finish()
        }
    }
}

@Composable
fun BlockScreen(
    packageName: String,
    reason: String,
    onClose: () -> Unit,
    onTakeABreak: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Time's up!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "You've reached your limit for $packageName",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = reason,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        if (reason.contains("Focus")) {
            Text("Take a break?", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OutlinedButton(onClick = { onTakeABreak(5) }) { Text("5 min") }
                OutlinedButton(onClick = { onTakeABreak(10) }) { Text("10 min") }
                OutlinedButton(onClick = { onTakeABreak(15) }) { Text("15 min") }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("OK")
        }
    }
}
