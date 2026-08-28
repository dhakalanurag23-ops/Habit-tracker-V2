package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DesignAesthetic
import com.example.ui.theme.LocalThemeConfig
import com.example.ui.theme.aestheticContainer
import com.example.ui.theme.googleAiGlow
import com.example.util.LocalAppStrings
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Gemini-Style Bottom Prompt Bar Layout
 *
 * Left: Microphone button. When pressed, dynamically transitions into a Stop square button
 * alongside an Up Arrow (Send) button.
 * Center: Text input field with placeholder "Ask anything".
 * Right: Plus (+) icon button with attachment modal sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiPromptBottomBar(
    onSendPrompt: (String) -> Unit,
    onOpenCreateTask: () -> Unit,
    onOpenPhotoToTask: () -> Unit,
    onOpenMealSnap: () -> Unit,
    onOpenSideQuests: () -> Unit,
    onOpenFutureSelf: () -> Unit,
    onOpenAiCoach: () -> Unit,
    isProcessing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val themeConfig = LocalThemeConfig.current
    val glowAccent = themeConfig.glowAccent

    var textInput by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var showPlusSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Speech Recognizer setup
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    DisposableEffect(speechRecognizer) {
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isRecording = false
            }
            override fun onError(error: Int) {
                isRecording = false
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val transcribed = matches[0]
                    textInput = if (textInput.isBlank()) transcribed else "$textInput $transcribed"
                }
                isRecording = false
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    textInput = matches[0]
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && speechRecognizer != null) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer.setRecognitionListener(recognitionListener)
            speechRecognizer.startListening(intent)
            isRecording = true
        }
    }

    fun startListening() {
        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isRecording = false
    }

    fun submit() {
        if (textInput.isNotBlank()) {
            val prompt = textInput.trim()
            textInput = ""
            if (isRecording) {
                stopListening()
            }
            onSendPrompt(prompt)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .aestheticContainer(
                    aesthetic = themeConfig.aesthetic,
                    glowAccent = glowAccent,
                    hasGlow = themeConfig.isGlowEnabled,
                    cornerRadius = 28.dp
                )
                .border(
                    width = 1.dp,
                    color = if (isRecording) Color(0xFFEF4444) else glowAccent.primary.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .testTag("gemini_prompt_bar")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Side: Voice Microphone / Stop Square button
                if (!isRecording) {
                    IconButton(
                        onClick = { startListening() },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("btn_mic")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice input",
                            tint = glowAccent.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    // Recording Active -> Stop Square button + Up Arrow Send button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Stop button
                        IconButton(
                            onClick = { stopListening() },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                .testTag("btn_stop_mic")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop recording",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Instant Send button during recording
                        IconButton(
                            onClick = { submit() },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(glowAccent.primary)
                                .testTag("btn_send_voice")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Center: Text Input Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (textInput.isEmpty()) {
                        Text(
                            text = if (isRecording) "Listening..." else "Ask anything...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isRecording) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 15.sp
                            )
                        )
                    }
                    BasicTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(glowAccent.primary),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { submit() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_gemini_prompt")
                    )
                }

                // Right Side: Send button if text is typed, or Plus (+) button for attachments
                if (textInput.isNotBlank() && !isRecording) {
                    IconButton(
                        onClick = { submit() },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(glowAccent.primary)
                            .testTag("btn_send_prompt")
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Submit",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    IconButton(
                        onClick = { showPlusSheet = true },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("btn_plus_menu")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Actions and Attachments",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for '+' Actions & Attachments
    if (showPlusSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPlusSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "AI Actions & Attachments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showPlusSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action grid items
                PromptActionItem(
                    icon = Icons.Default.Add,
                    title = "Create Custom Task",
                    subtitle = "Manually set title, time, alarm intensity & streak",
                    iconTint = glowAccent.primary,
                    onClick = {
                        showPlusSheet = false
                        onOpenCreateTask()
                    }
                )

                PromptActionItem(
                    icon = Icons.Default.Restaurant,
                    title = "AI Calorie Snap (Photo of Meal)",
                    subtitle = "Gemini estimates calories & macros from photo",
                    iconTint = Color(0xFF10B981),
                    onClick = {
                        showPlusSheet = false
                        onOpenMealSnap()
                    }
                )

                PromptActionItem(
                    icon = Icons.Default.DocumentScanner,
                    title = "Photo / Note to Task",
                    subtitle = "Extract tasks from whiteboards, paper & notes",
                    iconTint = Color(0xFF3B82F6),
                    onClick = {
                        showPlusSheet = false
                        onOpenPhotoToTask()
                    }
                )

                PromptActionItem(
                    icon = Icons.Default.Casino,
                    title = "Chaos Mode (Side Quest Generator)",
                    subtitle = "Convert daily habits into RPG quests with XP",
                    iconTint = Color(0xFF8B5CF6),
                    onClick = {
                        showPlusSheet = false
                        onOpenSideQuests()
                    }
                )

                PromptActionItem(
                    icon = Icons.Default.Psychology,
                    title = "5-Year Future Self Simulator",
                    subtitle = "Simulate your health & life archetype in 2031",
                    iconTint = Color(0xFFF59E0B),
                    onClick = {
                        showPlusSheet = false
                        onOpenFutureSelf()
                    }
                )

                PromptActionItem(
                    icon = Icons.Default.Chat,
                    title = "Personal AI Coach",
                    subtitle = "Chat with Gemini coach (Supportive or Roast Mode)",
                    iconTint = Color(0xFFEC4899),
                    onClick = {
                        showPlusSheet = false
                        onOpenAiCoach()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PromptActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
