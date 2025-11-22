package com.cycb.chat.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cycb.chat.data.model.CallParticipant
import com.cycb.chat.utils.VoiceCallManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCallScreen(
    chatName: String,
    participants: List<CallParticipant>,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    callMode: VoiceCallManager.CallMode,
    connectionState: VoiceCallManager.ConnectionState,
    callDuration: String,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onModeToggle: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {

    val expressiveSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f + gradientOffset * 0.2f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            ConnectionStatusBar(
                connectionState = connectionState,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = chatName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedCallDuration(
                duration = callDuration,
                connectionState = connectionState
            )

            Spacer(modifier = Modifier.height(48.dp))

            ParticipantsList(
                participants = participants,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            CallModeIndicator(
                callMode = callMode,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            CallControls(
                isMuted = isMuted,
                isSpeakerOn = isSpeakerOn,
                callMode = callMode,
                onMuteToggle = onMuteToggle,
                onSpeakerToggle = onSpeakerToggle,
                onEndCall = onEndCall
            )

            Spacer(modifier = Modifier.height(16.dp))

            ModeToggleFAB(
                callMode = callMode,
                onClick = onModeToggle
            )
        }
    }
}

@Composable
private fun ConnectionStatusBar(
    connectionState: VoiceCallManager.ConnectionState,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor) = when (connectionState) {
        VoiceCallManager.ConnectionState.CONNECTING -> "Connecting..." to MaterialTheme.colorScheme.tertiary
        VoiceCallManager.ConnectionState.CONNECTED -> "Connected" to MaterialTheme.colorScheme.primary
        VoiceCallManager.ConnectionState.RECONNECTING -> "Reconnecting..." to MaterialTheme.colorScheme.error
        VoiceCallManager.ConnectionState.FAILED -> "Connection Failed" to MaterialTheme.colorScheme.error
        else -> "Disconnected" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    AnimatedVisibility(
        visible = connectionState != VoiceCallManager.ConnectionState.CONNECTED,
        enter = slideInVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            color = statusColor.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (connectionState == VoiceCallManager.ConnectionState.CONNECTING ||
                    connectionState == VoiceCallManager.ConnectionState.RECONNECTING) {
                    LoadingIndicator(
                        modifier = Modifier.size(16.dp),
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AnimatedCallDuration(
    duration: String,
    connectionState: VoiceCallManager.ConnectionState
) {

    val scale by animateFloatAsState(
        targetValue = if (connectionState == VoiceCallManager.ConnectionState.CONNECTED) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "duration_scale"
    )

    Text(
        text = duration,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.scale(scale)
    )
}

@Composable
private fun ParticipantsList(
    participants: List<CallParticipant>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        participants.forEach { participant ->
            ParticipantItem(participant = participant)
        }
    }
}

@Composable
private fun ParticipantItem(participant: CallParticipant) {

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "participant_scale"
    )

    val speakingScale by animateFloatAsState(
        targetValue = if (participant.isSpeaking) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "speaking_scale"
    )

    Column(
        modifier = Modifier.scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {

            if (participant.isSpeaking) {
                SpeakingIndicator()
            }

            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .scale(speakingScale),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = participant.username.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (participant.isMuted) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error,
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = "Muted",
                        modifier = Modifier.padding(6.dp),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = participant.username,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SpeakingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "speaking")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_ring"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_alpha"
    )

    Box(
        modifier = Modifier
            .size(96.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
    )
}

@Composable
private fun CallModeIndicator(
    callMode: VoiceCallManager.CallMode,
    modifier: Modifier = Modifier
) {

    val cornerRadius by animateDpAsState(
        targetValue = if (callMode == VoiceCallManager.CallMode.SPEAKER) 20.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "mode_shape"
    )
    val shape = RoundedCornerShape(cornerRadius)

    val containerColor = if (callMode == VoiceCallManager.CallMode.SPEAKER)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.secondaryContainer

    val contentColor = if (callMode == VoiceCallManager.CallMode.SPEAKER)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSecondaryContainer

    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (callMode == VoiceCallManager.CallMode.SPEAKER)
                    Icons.Default.Mic
                else
                    Icons.Default.Hearing,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
            Text(
                text = if (callMode == VoiceCallManager.CallMode.SPEAKER) "Speaking Mode" else "Listening Mode",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun ModeToggleFAB(
    callMode: VoiceCallManager.CallMode,
    onClick: () -> Unit
) {

    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "fab_scale"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (callMode == VoiceCallManager.CallMode.SPEAKER) 28.dp else 24.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fab_shape"
    )
    val shape = RoundedCornerShape(cornerRadius)

    val containerColor = if (callMode == VoiceCallManager.CallMode.SPEAKER)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.secondary

    val contentColor = if (callMode == VoiceCallManager.CallMode.SPEAKER)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSecondary

    ExtendedFloatingActionButton(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier.scale(scale),
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp
        )
    ) {
        Icon(
            imageVector = if (callMode == VoiceCallManager.CallMode.SPEAKER)
                Icons.Default.Hearing
            else
                Icons.Default.Mic,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (callMode == VoiceCallManager.CallMode.SPEAKER)
                "Switch to Listening"
            else
                "Switch to Speaking",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

@Composable
private fun CallControls(
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    callMode: VoiceCallManager.CallMode,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onEndCall: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        CallControlButton(
            icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
            label = if (isMuted) "Unmute" else "Mute",
            isActive = isMuted,
            onClick = onMuteToggle,
            enabled = callMode == VoiceCallManager.CallMode.SPEAKER,
            containerColor = if (isMuted)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.surfaceVariant
        )

        EndCallButton(onClick = onEndCall)

        CallControlButton(
            icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
            label = if (isSpeakerOn) "Speaker On" else "Speaker Off",
            isActive = isSpeakerOn,
            onClick = onSpeakerToggle,
            enabled = true,
            containerColor = if (isSpeakerOn)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun CallControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    containerColor: Color
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.38f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer { this.alpha = alpha }
    ) {
        FilledIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(64.dp)
                .scale(scale),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = containerColor,
                disabledContainerColor = containerColor.copy(alpha = 0.38f)
            ),
            shape = CircleShape
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled)
                MaterialTheme.colorScheme.onSurfaceVariant
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        )
    }
}

@Composable
private fun EndCallButton(onClick: () -> Unit) {

    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "end_call_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledIconButton(
            onClick = {
                isPressed = true
                onClick()
            },
            modifier = Modifier
                .size(72.dp)
                .scale(scale),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "End Call",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onError
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "End Call",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
    }
}
