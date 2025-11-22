package com.cycb.chat.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cycb.chat.data.model.CallParticipant

@Composable
fun DraggableAvatarCluster(
    speakers: List<CallParticipant>,
    modifier: Modifier = Modifier,
    onAvatarReordered: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> }
) {

    var avatarPositions by remember { mutableStateOf(speakers.mapIndexed { index, _ -> index to Offset.Zero }.toMap()) }
    var draggedAvatarId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(speakers.size) {
        avatarPositions = speakers.mapIndexed { index, _ -> index to Offset.Zero }.toMap()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        speakers.forEachIndexed { index, speaker ->
            DraggableAvatar(
                participant = speaker,
                index = index,
                isDragged = draggedAvatarId == speaker.userId,
                offset = avatarPositions[index] ?: Offset.Zero,
                onDragStart = {
                    draggedAvatarId = speaker.userId
                },
                onDrag = { dragAmount ->
                    val currentOffset = avatarPositions[index] ?: Offset.Zero
                    avatarPositions = avatarPositions + (index to currentOffset + dragAmount)
                },
                onDragEnd = {
                    draggedAvatarId = null

                    avatarPositions = avatarPositions + (index to Offset.Zero)
                }
            )
        }
    }
}

@Composable
private fun DraggableAvatar(
    participant: CallParticipant,
    index: Int,
    isDragged: Boolean,
    offset: Offset,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(participant.userId) {
        kotlinx.coroutines.delay(index * 50L)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = when {
            !visible -> 0f
            isDragged -> 1.15f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "avatar_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isDragged) 8.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "avatar_elevation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset(x = (index * 68).dp)
            .graphicsLayer {
                translationX = offset.x
                translationY = offset.y
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(participant.userId) {
                detectDragGestures(
                    onDragStart = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDragStart()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDragEnd()
                    }
                )
            }
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {

            if (participant.isSpeaking) {
                SpeakingPulseRing()
            }

            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = elevation,
                tonalElevation = if (isDragged) 4.dp else 0.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = participant.username.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (participant.isMuted) {
                MutedBadge()
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = participant.username,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun SpeakingPulseRing() {
    val infiniteTransition = rememberInfiniteTransition(label = "speaking")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .size(68.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
    )
}

@Composable
private fun BoxScope.MutedBadge() {

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "badge_scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (visible) 0f else -45f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "badge_rotation"
    )

    Surface(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(20.dp)
            .scale(scale)
            .graphicsLayer { rotationZ = rotation },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.error,
        shadowElevation = 2.dp
    ) {
        Icon(
            imageVector = Icons.Default.MicOff,
            contentDescription = "Muted",
            modifier = Modifier.padding(4.dp),
            tint = MaterialTheme.colorScheme.onError
        )
    }
}
