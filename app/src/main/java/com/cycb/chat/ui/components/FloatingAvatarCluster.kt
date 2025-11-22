package com.cycb.chat.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cycb.chat.data.model.CallParticipant
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun FloatingAvatarCluster(
    participants: List<CallParticipant>,
    modifier: Modifier = Modifier
) {

    val uniqueParticipants = participants.distinctBy { it.userId }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val containerWidth = with(LocalDensity.current) { maxWidth.toPx() }
        val containerHeight = with(LocalDensity.current) { maxHeight.toPx() }

        uniqueParticipants.forEachIndexed { index, participant ->
            FloatingAvatar(
                participant = participant,
                index = index,
                totalCount = uniqueParticipants.size,
                containerWidth = containerWidth,
                containerHeight = containerHeight
            )
        }
    }
}

@Composable
private fun FloatingAvatar(
    participant: CallParticipant,
    index: Int,
    totalCount: Int,
    containerWidth: Float,
    containerHeight: Float
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val angle = (index.toFloat() / totalCount) * 2 * Math.PI
    val radius = 80f
    val initialX = (containerWidth / 2) + (cos(angle) * radius).toFloat()
    val initialY = (containerHeight / 2) + (sin(angle) * radius).toFloat()

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "float_$index")

    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000 + (index * 200),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )

    val floatOffsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2500 + (index * 150),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_x"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000 + (index * 100),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(participant.userId) {
        kotlinx.coroutines.delay(index * 100L)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = when {
            !visible -> 0f
            isDragging -> 1.2f
            participant.isSpeaking -> 1.1f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "avatar_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isDragging) 12.dp else if (participant.isSpeaking) 4.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "avatar_elevation"
    )

    val isListener = participant.isMuted && !participant.isSpeaking
    val avatarSize = if (isListener) 48.dp else 64.dp

    Box(
        modifier = Modifier
            .offset(
                x = with(density) { (initialX + offsetX + if (!isDragging) floatOffsetX else 0f).toDp() },
                y = with(density) { (initialY + offsetY + if (!isDragging) floatOffsetY else 0f).toDp() }
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = if (!isDragging) rotation else 0f
            }
            .pointerInput(participant.userId) {
                detectDragGestures(
                    onDragStart = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isDragging = true
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    },
                    onDragEnd = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isDragging = false
                    }
                )
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {

                if (participant.isSpeaking) {
                    SpeakingPulseRing()
                }

                Surface(
                    modifier = Modifier.size(avatarSize),
                    shape = CircleShape,
                    color = if (isListener)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = elevation,
                    tonalElevation = if (isDragging) 4.dp else 0.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (!participant.profilePicture.isNullOrEmpty()) {

                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(participant.profilePicture)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "${participant.displayName ?: participant.username}'s profile picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {

                            Text(
                                text = (participant.displayName ?: participant.username).take(2).uppercase(),
                                style = if (isListener)
                                    MaterialTheme.typography.titleSmall
                                else
                                    MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isListener)
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else
                                    MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                if (participant.isSpeaking) {
                    SpeakingMicBadge()
                }

                if (participant.isMuted && !isListener) {
                    MutedBadge()
                }

                if (isListener) {
                    ListenerBadge()
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = participant.displayName ?: participant.username,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun SpeakingPulseRing() {
    val infiniteTransition = rememberInfiniteTransition(label = "speaking_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
    )
}

@Composable
private fun BoxScope.SpeakingMicBadge() {
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
        label = "mic_badge_scale"
    )

    val iconScale by rememberInfiniteTransition(label = "mic_pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_icon_scale"
    )

    Surface(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .size(24.dp)
            .scale(scale),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Speaking",
                modifier = Modifier
                    .padding(4.dp)
                    .scale(iconScale),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
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
        targetValue = if (visible) 0f else -180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "badge_rotation"
    )

    Surface(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(22.dp)
            .scale(scale)
            .graphicsLayer { rotationZ = rotation },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.error,
        shadowElevation = 4.dp
    ) {
        Icon(
            imageVector = Icons.Default.MicOff,
            contentDescription = "Muted",
            modifier = Modifier.padding(4.dp),
            tint = MaterialTheme.colorScheme.onError
        )
    }
}

@Composable
private fun BoxScope.ListenerBadge() {
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
        label = "listener_badge_scale"
    )

    Surface(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(20.dp)
            .scale(scale),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shadowElevation = 2.dp
    ) {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Listening",
            modifier = Modifier.padding(4.dp),
            tint = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}
