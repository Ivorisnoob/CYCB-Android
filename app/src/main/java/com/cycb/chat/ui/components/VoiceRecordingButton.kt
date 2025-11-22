@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.cycb.chat.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cycb.chat.utils.VoiceRecorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun VoiceRecordingButton(
    onRecordingComplete: (File, Long) -> Unit,
    onRecordingCanceled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val voiceRecorder = remember { VoiceRecorder(context) }

    var isRecording by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0L) }
    var amplitudes by remember { mutableStateOf(listOf<Int>()) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {

            scope.launch {
                startRecording(
                    voiceRecorder = voiceRecorder,
                    onStart = {
                        isRecording = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onAmplitude = { amp ->
                        amplitudes = (amplitudes + amp).takeLast(30)
                    },
                    onDuration = { duration ->
                        recordingDuration = duration
                    }
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isRecording) {
                scope.launch {
                    voiceRecorder.cancelRecording()
                }
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isProcessing) {

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Processing...",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LinearWavyProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else if (isRecording) {

            RecordingUI(
                duration = recordingDuration,
                amplitudes = amplitudes,
                onSend = {
                    isProcessing = true
                    scope.launch {
                        voiceRecorder.stopRecording().onSuccess { (file, duration) ->
                            isRecording = false
                            isProcessing = false
                            recordingDuration = 0L
                            amplitudes = emptyList()
                            onRecordingComplete(file, duration)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }.onFailure {
                            isRecording = false
                            isProcessing = false
                            recordingDuration = 0L
                            amplitudes = emptyList()
                            onRecordingCanceled()
                        }
                    }
                },
                onCancel = {
                    scope.launch {
                        voiceRecorder.cancelRecording()
                        isRecording = false
                        recordingDuration = 0L
                        amplitudes = emptyList()
                        onRecordingCanceled()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            )
        } else {

            MicButton(
                onStartRecording = {
                    if (hasPermission) {
                        scope.launch {
                            startRecording(
                                voiceRecorder = voiceRecorder,
                                onStart = {
                                    isRecording = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onAmplitude = { amp ->
                                    amplitudes = (amplitudes + amp).takeLast(30)
                                },
                                onDuration = { duration ->
                                    recordingDuration = duration
                                }
                            )
                        }
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
        }
    }
}

@Composable
private fun MicButton(
    onStartRecording: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "mic_scale"
    )

    Surface(
        modifier = Modifier
            .size(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        onClick = onStartRecording
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Record voice message",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun RecordingUI(
    duration: Long,
    amplitudes: List<Int>,
    onSend: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer,
                onClick = onCancel
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    CircularWavyProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.error,
                        trackColor = MaterialTheme.colorScheme.errorContainer
                    )

                    Text(
                        text = formatDuration(duration),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                LiveWaveform(
                    amplitudes = amplitudes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                )
            }

            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                onClick = onSend
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveWaveform(
    amplitudes: List<Int>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val displayAmplitudes = amplitudes.takeLast(30)

        repeat(30) { index ->
            val amplitude = displayAmplitudes.getOrNull(index) ?: 0
            val height = (amplitude / 100f).coerceIn(0.1f, 1f)

            val animatedHeight by animateFloatAsState(
                targetValue = height,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                ),
                label = "waveform_bar_$index"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                        )
                    )
                    .graphicsLayer {
                        scaleY = animatedHeight
                    }
            )
        }
    }
}

private suspend fun startRecording(
    voiceRecorder: VoiceRecorder,
    onStart: () -> Unit,
    onAmplitude: (Int) -> Unit,
    onDuration: (Long) -> Unit
) {
    voiceRecorder.startRecording().onSuccess {
        onStart()

        kotlinx.coroutines.coroutineScope {
            launch {
                voiceRecorder.getAmplitudeFlow().collect { amplitude ->
                    onAmplitude(amplitude)
                }
            }

            launch {
                while (voiceRecorder.isRecording()) {
                    onDuration(voiceRecorder.getDuration())
                    delay(100)
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}
