package com.cycb.chat.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cycb.chat.utils.VoicePlayer
import kotlinx.coroutines.launch

@Composable
fun VoiceMessagePlayer(
    audioUrl: String,
    duration: Int = 0,
    isOwnMessage: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val voicePlayer = remember { VoicePlayer(context) }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var totalDuration by remember { mutableStateOf(duration) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(audioUrl) {
        isLoading = true
        voicePlayer.prepare(audioUrl).onSuccess { dur ->
            totalDuration = dur
            isLoading = false
        }.onFailure {
            isLoading = false
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            scope.launch {
                voicePlayer.getProgressFlow().collect { progress ->
                    currentPosition = (progress * totalDuration).toInt()
                    if (progress >= 0.99f) {
                        isPlaying = false
                        currentPosition = 0
                        voicePlayer.seekTo(0)
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voicePlayer.release()
        }
    }

    val containerColor = if (isOwnMessage) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isOwnMessage) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val playButtonScale by animateFloatAsState(
        targetValue = if (isPlaying) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "play_button_scale"
    )

    Surface(
        modifier = modifier.width(280.dp),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = playButtonScale
                        scaleY = playButtonScale
                    },
                shape = CircleShape,
                color = if (isOwnMessage) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                onClick = {
                    if (isLoading) return@Surface

                    if (isPlaying) {
                        voicePlayer.pause()
                        isPlaying = false
                    } else {
                        voicePlayer.play()
                        isPlaying = true
                    }
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isLoading) {
                        CircularWavyProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                VoiceWaveform(
                    progress = if (totalDuration > 0) currentPosition.toFloat() / totalDuration else 0f,
                    isPlaying = isPlaying,
                    color = contentColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = voicePlayer.formatTime(currentPosition),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = voicePlayer.formatTime(totalDuration),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceWaveform(
    progress: Float,
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {

    val barCount = 40
    val bars = remember {
        List(barCount) { (20..80).random() }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveform_animation"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        bars.forEachIndexed { index, height ->
            val barProgress = index.toFloat() / barCount
            val isPassed = barProgress <= progress

            val animatedHeight by animateFloatAsState(
                targetValue = if (isPlaying && isPassed) {
                    height * (0.7f + 0.3f * kotlin.math.sin(animatedProgress * 2 * Math.PI + index).toFloat())
                } else {
                    height.toFloat()
                },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "bar_height_$index"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isPassed) {
                            color
                        } else {
                            color.copy(alpha = 0.3f)
                        }
                    )
                    .graphicsLayer {
                        scaleY = animatedHeight / 100f
                    }
            )
        }
    }
}
