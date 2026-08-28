package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HomeScreen
import com.example.ui.theme.HabitPulseTheme
import com.example.util.ScanNotesTileService
import com.example.util.VisionScannerTileService
import com.example.viewmodel.HabitViewModel

class MainActivity : ComponentActivity() {

    private val habitViewModel: HabitViewModel by viewModels()
    private var openAiVisionDirectly by mutableStateOf(false)
    private var launchCameraDirectly by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)

        setContent {
            val uiState by habitViewModel.uiState.collectAsStateWithLifecycle()
            HabitPulseTheme(config = uiState.themeConfig) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        viewModel = habitViewModel,
                        initialOpenAiVision = openAiVisionDirectly,
                        autoLaunchCamera = launchCameraDirectly,
                        onAiVisionOpenedHandled = {
                            openAiVisionDirectly = false
                            launchCameraDirectly = false
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val isScanAction = intent.action == ScanNotesTileService.ACTION_SCAN_NOTES ||
                intent.action == VisionScannerTileService.ACTION_GEMINI_VISION_SCAN
        val hasScanExtra = intent.getBooleanExtra(ScanNotesTileService.EXTRA_OPEN_AI_VISION, false) ||
                intent.getBooleanExtra(VisionScannerTileService.EXTRA_OPEN_AI_VISION, false)
        val shouldLaunchCamera = intent.getBooleanExtra(VisionScannerTileService.EXTRA_LAUNCH_CAMERA, false)

        if (isScanAction || hasScanExtra) {
            openAiVisionDirectly = true
            launchCameraDirectly = shouldLaunchCamera
        }
    }
}
