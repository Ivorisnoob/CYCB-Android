package com.cycb.chat.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.cycb.chat.data.model.CallParticipant
import com.cycb.chat.utils.VoiceCallManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCallOverlay(
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
    var isExpanded by remember { mutableStateOf(true) }

    val uniqueParticipants = participants.distinctBy { it.userId }

    val speakers = uniqueParticipants.filter { !it.isListener }
    val listeners = uniqueParticipants.filter { it.isListener }
    val speakerCount = speakers.size
    val listenerCount = listeners.size

    val targetHeight = if (isExpanded) 420.dp else 160.dp
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "overlay_height"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(animatedHeight)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 24.dp,
                    bottomEnd = 24.dp
                )
            ),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        shape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = 24.dp,
            bottomEnd = 24.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            OverlayHeader(
                chatName = chatName,
                callDuration = callDuration,
                isExpanded = isExpanded,
                onToggleExpand = { isExpanded = !isExpanded }
            )

            if (connectionState != VoiceCallManager.ConnectionState.CONNECTED) {
                Spacer(modifier = Modifier.height(8.dp))
                ConnectionStatusIndicator(connectionState)
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (speakerCount > 0) {
                            Text(
                                text = "$speakerCount speaking",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (speakerCount > 0 && listenerCount > 0) {
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (listenerCount > 0) {
                            Text(
                                text = "$listenerCount listening",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (speakers.isNotEmpty()) {
                        Text(
                            text = "SPEAKING",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FloatingAvatarCluster(
                            participants = speakers,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }

                    if (listeners.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "LISTENING",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ListenersRow(listeners = listeners)
                    }
                }
            }

            AnimatedVisibility(
                visible = !isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-8).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        uniqueParticipants.take(3).forEach { participant ->
                            MiniAvatar(participant)
                        }
                        if (uniqueParticipants.size > 3) {
                            Text(
                                text = "+${uniqueParticipants.size - 3}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.height(12.dp))

            VoiceControlsBar(
                isMuted = isMuted,
                isSpeakerOn = isSpeakerOn,
                callMode = callMode,
                onMuteToggle = onMuteToggle,
                onSpeakerToggle = onSpeakerToggle,
                onModeToggle = onModeToggle,
                onEndCall = onEndCall
            )
        }
    }
}

@Composable
private fun OverlayHeader(
    chatName: String,
    callDuration: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            Column {
                Text(
                    text = "Voice Chat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = callDuration,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = onToggleExpand) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConnectionStatusIndicator(state: VoiceCallManager.ConnectionState) {
    val (text, color) = when (state) {
        VoiceCallManager.ConnectionState.CONNECTING -> "Connecting..." to MaterialTheme.colorScheme.tertiary
        VoiceCallManager.ConnectionState.RECONNECTING -> "Reconnecting..." to MaterialTheme.colorScheme.error
        VoiceCallManager.ConnectionState.FAILED -> "Connection Failed" to MaterialTheme.colorScheme.error
        else -> return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (state == VoiceCallManager.ConnectionState.CONNECTING ||
                state == VoiceCallManager.ConnectionState.RECONNECTING) {
                ContainedLoadingIndicator(
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SpeakersSection(speakers: List<CallParticipant>) {
    Column {
        Text(
            text = "SPEAKING (${speakers.size})",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        DraggableAvatarCluster(
            speakers = speakers.take(5),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ListenersSection(listeners: List<CallParticipant>) {
    Column {
        Text(
            text = "LISTENING (${listeners.size})",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listeners.forEach { listener ->
                ListenerChip(listener)
            }
        }
    }
}

@Composable
private fun ListenerChip(participant: CallParticipant) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = participant.username,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VoiceControlsBar(
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    callMode: VoiceCallManager.CallMode,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onModeToggle: () -> Unit,
    onEndCall: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        FilledTonalButton(
            onClick = onModeToggle,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(40.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = if (callMode == VoiceCallManager.CallMode.SPEAKER)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(
                imageVector = if (callMode == VoiceCallManager.CallMode.SPEAKER)
                    Icons.Default.Hearing
                else
                    Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (callMode == VoiceCallManager.CallMode.SPEAKER)
                    "Switch to Listening"
                else
                    "Switch to Speaking",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            ControlButton(
                icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                label = if (isMuted) "Unmute" else "Mute",
                onClick = onMuteToggle,
                enabled = callMode == VoiceCallManager.CallMode.SPEAKER,
                containerColor = if (isMuted)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )

            ControlButton(
                icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                label = if (isSpeakerOn) "Speaker" else "Earpiece",
                onClick = onSpeakerToggle,
                enabled = true,
                containerColor = if (isSpeakerOn)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )

            ControlButton(
                icon = Icons.Default.CallEnd,
                label = "Leave",
                onClick = onEndCall,
                enabled = true,
                containerColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    containerColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(70.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.38f }
    ) {
        FilledIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = containerColor,
                disabledContainerColor = containerColor.copy(alpha = 0.38f)
            ),
            shape = CircleShape
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ListenersRow(listeners: List<CallParticipant>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listeners.take(5).forEach { listener ->
            ListenerChip(listener)
        }
        if (listeners.size > 5) {
            Text(
                text = "+${listeners.size - 5}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MiniAvatar(participant: CallParticipant) {
    Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (!participant.profilePicture.isNullOrEmpty()) {
                androidx.compose.foundation.Image(
                    painter = rememberAsyncImagePainter(
                        model = androidx.compose.ui.platform.LocalContext.current.let { context ->
                            coil.request.ImageRequest.Builder(context)
                                .data(participant.profilePicture)
                                .crossfade(true)
                                .build()
                        }
                    ),
                    contentDescription = "${participant.displayName ?: participant.username}'s profile picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Text(
                    text = (participant.displayName ?: participant.username).take(1).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun SmallIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isActive: Boolean,
    tint: Color = if (isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}
