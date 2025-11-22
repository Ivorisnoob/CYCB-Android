package com.cycb.chat.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cycb.chat.data.model.Message
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MessageInputBar(
    message: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
    replyToMessage: Message? = null,
    onClearReply: () -> Unit = {},
    onVoiceMessageSend: ((String, Int) -> Unit)? = null,
    onImageSelected: ((android.net.Uri) -> Unit)? = null,
    selectedImageUri: android.net.Uri? = null,
    onRemoveImage: () -> Unit = {},
    onGifClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { onImageSelected?.invoke(it) }
    }

    val sendButtonScale by animateFloatAsState(
        targetValue = if (message.isNotEmpty() && !isSending) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessHigh
        ),
        label = "send_button_scale"
    )

    val sendButtonRotation by animateFloatAsState(
        targetValue = if (message.isNotEmpty()) 0f else -45f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "send_button_rotation"
    )

    Surface(
        color = Color.Transparent,
        modifier = modifier
    ) {
        Column {

            AnimatedVisibility(
                visible = selectedImageUri != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                selectedImageUri?.let { uri ->
                    ImagePreview(
                        imageUri = uri,
                        onRemove = onRemoveImage
                    )
                }
            }

            AnimatedVisibility(
                visible = replyToMessage != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                replyToMessage?.let { reply ->
                    ReplyPreview(
                        reply = reply,
                        onClearReply = onClearReply
                    )
                }
            }

            val inputBarScale by animateFloatAsState(
                targetValue = if (message.isNotEmpty()) 1.02f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "inputBarScale"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .graphicsLayer {
                        scaleX = inputBarScale
                        scaleY = inputBarScale
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    TextField(
                        value = message,
                        onValueChange = onMessageChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp, max = 120.dp)
                            .focusRequester(focusRequester),
                        readOnly = false,
                        placeholder = {
                            Text(
                                "Message...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        leadingIcon = {
                            Row {

                                var isImagePressed by remember { mutableStateOf(false) }
                                val imageScale by animateFloatAsState(
                                    targetValue = if (isImagePressed) 0.85f else 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessHigh
                                    ),
                                    label = "image_scale"
                                )

                                IconButton(
                                    onClick = {
                                        isImagePressed = true
                                        imagePickerLauncher.launch("image
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReplyPreview(
    reply: Message,
    onClearReply: () -> Unit
) {

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "reply_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
        shadowElevation = 2.dp,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            val iconRotation by animateFloatAsState(
                targetValue = if (visible) 0f else -90f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "icon_rotation"
            )

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer { rotationZ = iconRotation }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Replying to ${reply.senderId.displayName}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    val (emoji, text) = when (reply.messageType ?: "text") {
                        "voice" -> "🎤" to "Voice message"
                        "image" -> "📷" to "Image"
                        "file" -> "📎" to "File"
                        "gif" -> "🎬" to "GIF"
                        else -> null to reply.content
                    }

                    if (emoji != null) {
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            var isPressed by remember { mutableStateOf(false) }
            val closeScale by animateFloatAsState(
                targetValue = if (isPressed) 0.85f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                ),
                label = "close_scale"
            )

            val coroutineScope = rememberCoroutineScope()

            FilledIconButton(
                onClick = {
                    isPressed = true
                    coroutineScope.launch {
                        delay(100)
                        isPressed = false
                        onClearReply()
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .graphicsLayer {
                        scaleX = closeScale
                        scaleY = closeScale
                    },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear reply",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
