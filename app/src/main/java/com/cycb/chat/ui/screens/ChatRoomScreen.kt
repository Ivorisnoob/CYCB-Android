@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)

package com.cycb.chat.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycb.chat.data.model.ActiveCallInfo
import com.cycb.chat.data.model.Chat
import com.cycb.chat.data.model.Message
import com.cycb.chat.data.model.User
import com.cycb.chat.ui.components.AnimatedAvatarCluster
import com.cycb.chat.ui.components.MessageInputBar
import com.cycb.chat.ui.components.ProfilePicture
import com.cycb.chat.ui.components.TypingIndicator
import androidx.compose.foundation.layout.Column
import com.cycb.chat.viewmodel.ChatRoomUiState
import com.cycb.chat.viewmodel.ChatRoomViewModel
import com.cycb.chat.viewmodel.SendMessageState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale
import java.util.TimeZone

@Composable
fun ChatRoomScreen(
    chatId: String,
    chat: Chat?,
    viewModel: ChatRoomViewModel,
    voiceCallViewModel: com.cycb.chat.viewmodel.VoiceCallViewModel,
    currentUserId: String,
    currentUsername: String,
    onBackClick: () -> Unit,
    onProfileClick: (String) -> Unit = {},
    onGroupInfoClick: () -> Unit = {},
    onChatBackgroundClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val sendMessageState by viewModel.sendMessageState.collectAsState()
    val messageText by viewModel.messageText.collectAsState()
    val typingUsers by viewModel.typingUsers.collectAsState()
    val replyToMessage by viewModel.replyToMessage.collectAsState()
    val chatMembers by viewModel.chatMembers.collectAsState()
    val hasActiveCall by viewModel.hasActiveCall.collectAsState()
    val chatBackground by viewModel.chatBackground.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }

    var showGifPicker by remember { mutableStateOf(false) }

    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    var showFullScreenImage by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsViewModel: com.cycb.chat.viewmodel.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as android.app.Application)
    )
    val autoPlayGifs by settingsViewModel.autoPlayGifs.collectAsState()

    val currentCall by voiceCallViewModel.currentCall.collectAsState()
    val incomingCall by voiceCallViewModel.incomingCall.collectAsState()
    val isMuted by voiceCallViewModel.isMuted.collectAsState()
    val isSpeakerOn by voiceCallViewModel.isSpeakerOn.collectAsState()
    val callMode by voiceCallViewModel.callMode.collectAsState()
    val connectionState by voiceCallViewModel.connectionState.collectAsState()
    val callDuration by voiceCallViewModel.callDuration.collectAsState()
    val isInCall by voiceCallViewModel.isInCall.collectAsState()

    LaunchedEffect(hasActiveCall?.channelName, currentCall?.channelName) {
        val activeCallInfo = hasActiveCall
        if (activeCallInfo != null && currentCall == null && chat != null) {
            android.util.Log.d("ChatRoomScreen", "Auto-joining active call: ${activeCallInfo.channelName}")

            val callerId = activeCallInfo.startedBy.id ?: activeCallInfo.startedBy._id ?: ""
            val callerName = activeCallInfo.startedBy.displayName ?: activeCallInfo.startedBy.username

            voiceCallViewModel.joinExistingCall(
                chatId = chatId,
                channelName = activeCallInfo.channelName,
                callerId = callerId,
                callerName = callerName
            )
        }
    }

    var pendingCallAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val micPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {

            pendingCallAction?.invoke()
            pendingCallAction = null
        } else {

            android.widget.Toast.makeText(
                context,
                "Microphone permission is required for voice calls",
                android.widget.Toast.LENGTH_LONG
            ).show()
            pendingCallAction = null
        }
    }

    LaunchedEffect(chatId) {
        viewModel.loadMessages(chatId)
        voiceCallViewModel.setCurrentUser(currentUserId, currentUsername)
        voiceCallViewModel.setCurrentOpenChat(chatId)
    }

    DisposableEffect(chatId) {
        onDispose {
            viewModel.leaveChat()
            voiceCallViewModel.setCurrentOpenChat(null)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is ChatRoomUiState.Success) {
            val messages = (uiState as ChatRoomUiState.Success).messages
            if (messages.isNotEmpty()) {
                coroutineScope.launch {
                    listState.scrollToItem(0)
                }
            }
        }
    }

    LaunchedEffect(currentCall, isInCall) {
        android.util.Log.d("ChatRoomScreen", "currentCall: $currentCall, isInCall: $isInCall")
    }

    val error by voiceCallViewModel.error.collectAsState()
    LaunchedEffect(error) {
        error?.let {
            android.util.Log.e("ChatRoomScreen", "Voice call error: $it")

            voiceCallViewModel.clearError()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        ChatRoomTopBar(
            chat = chat,
            chatMembers = chatMembers,
            hasActiveCall = hasActiveCall != null,
            onBackClick = onBackClick,
            onMenuClick = onChatBackgroundClick,
            onVoiceCallClick = {

                if (currentCall != null) {
                    android.util.Log.d("ChatRoomScreen", "Already in a call, ignoring button press")
                    return@ChatRoomTopBar
                }

                val isGroupChat = chat?.type == "group"
                val activeCallInfo = hasActiveCall

                val callAction = {
                    if (activeCallInfo != null) {

                        android.util.Log.d("ChatRoomScreen", "Joining existing call: ${activeCallInfo.channelName}")
                        val callerId = activeCallInfo.startedBy.id ?: activeCallInfo.startedBy._id ?: ""
                        val callerName = activeCallInfo.startedBy.displayName ?: activeCallInfo.startedBy.username

                        voiceCallViewModel.joinExistingCall(
                            chatId = chatId,
                            channelName = activeCallInfo.channelName,
                            callerId = callerId,
                            callerName = callerName
                        )
                    } else {

                        voiceCallViewModel.initiateCall(
                            chatId = chatId,
                            chatName = chat?.name ?: "Chat",
                            callerId = currentUserId,
                            callerName = currentUsername,
                            isGroupChat = isGroupChat
                        )
                    }
                }

                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.RECORD_AUDIO
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {

                    callAction()
                } else {

                    pendingCallAction = callAction
                    micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                }
            },
            onTitleClick = {
                if (chat?.type == "group") {
                    onGroupInfoClick()
                } else {
                    chat?.otherUser?.getUserId()?.let { onProfileClick(it) }
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {

            val background = chatBackground

            when (background?.type) {
                "image" -> {

                    coil.compose.AsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(background.value)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Chat background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                else -> {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = when (background?.type) {
                                    "gradient" -> parseGradientBackground(background.value)
                                    else -> null
                                } ?: androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.background)
                            )
                            .then(
                                if (background?.type == "color") {
                                    Modifier.background(
                                        androidx.compose.ui.graphics.Color(
                                            android.graphics.Color.parseColor(background.value)
                                        )
                                    )
                                } else {
                                    Modifier
                                }
                            )
                    )
                }
            }

            when (val state = uiState) {
                is ChatRoomUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                is ChatRoomUiState.Success -> {
                    MessagesList(
                        messages = state.messages,
                        currentUserId = currentUserId,
                        isGroupChat = chat?.type == "group",
                        listState = listState,
                        viewModel = viewModel,
                        typingUsers = typingUsers.toList(),
                        autoPlayGifs = autoPlayGifs,
                        modifier = Modifier.fillMaxSize(),
                        onProfileClick = onProfileClick,
                        onImageClick = { imageUrl ->
                            fullScreenImageUrl = imageUrl
                            showFullScreenImage = true
                        }
                    )
                }
                is ChatRoomUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadMessages(chatId) }
                    )
                }
            }

            currentCall?.let { call ->
                if (call.chatId == chatId) {
                    com.cycb.chat.ui.components.VoiceCallOverlay(
                        chatName = chat?.name ?: "Voice Call",
                        participants = call.participants,
                        isMuted = isMuted,
                        isSpeakerOn = isSpeakerOn,
                        callMode = callMode,
                        connectionState = connectionState,
                        callDuration = callDuration,
                        onMuteToggle = { voiceCallViewModel.toggleMute() },
                        onSpeakerToggle = { voiceCallViewModel.toggleSpeaker() },
                        onModeToggle = {
                            if (callMode == com.cycb.chat.utils.VoiceCallManager.CallMode.SPEAKER) {
                                voiceCallViewModel.switchToListener(currentUserId, currentUsername)
                            } else {
                                voiceCallViewModel.switchToSpeaker(currentUserId, currentUsername)
                            }
                        },
                        onEndCall = { voiceCallViewModel.endCall() },
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }

            MessageInputBar(
                message = messageText,
                onMessageChange = { viewModel.updateMessageText(it) },
                onSend = {
                    if (selectedImageUri != null) {

                        isUploadingImage = true
                        coroutineScope.launch {
                            android.util.Log.d("ChatRoomScreen", "Starting image upload...")
                            com.cycb.chat.utils.ImageUploadHelper.uploadToBackend(
                                context = context,
                                imageUri = selectedImageUri!!
                            ).onSuccess { url ->
                                android.util.Log.d("ChatRoomScreen", "Image uploaded successfully: $url")
                                viewModel.sendImageMessage(url)
                                selectedImageUri = null
                                isUploadingImage = false
                            }.onFailure { error ->
                                android.util.Log.e("ChatRoomScreen", "Image upload failed", error)
                                android.widget.Toast.makeText(
                                    context,
                                    "Failed to upload image: ${error.message}",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                isUploadingImage = false
                            }
                        }
                    } else {

                        viewModel.sendMessage()
                    }
                },
                isSending = sendMessageState is SendMessageState.Sending || isUploadingImage,
                replyToMessage = replyToMessage,
                onClearReply = { viewModel.clearReply() },
                onVoiceMessageSend = { url, duration ->
                    viewModel.sendVoiceMessage(url, duration)
                },
                onImageSelected = { uri ->
                    selectedImageUri = uri
                },
                selectedImageUri = selectedImageUri,
                onRemoveImage = { selectedImageUri = null },
                onGifClick = { showGifPicker = true },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            if (showGifPicker) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showGifPicker = false },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    com.cycb.chat.ui.components.GifPicker(
                        onGifSelected = { gifUrl ->
                            viewModel.sendGifMessage(gifUrl)
                            showGifPicker = false
                        },
                        onDismiss = { showGifPicker = false },
                        searchGifs = { query ->
                            try {
                                val tenorApi = com.cycb.chat.network.TenorApiService.create()
                                val response = tenorApi.searchGifs(
                                    query = query,
                                    apiKey = com.cycb.chat.network.TenorApiService.API_KEY
                                )
                                response.results
                            } catch (e: Exception) {
                                android.util.Log.e("ChatRoomScreen", "Failed to search GIFs", e)
                                emptyList()
                            }
                        },
                        getTrendingGifs = {
                            try {
                                val tenorApi = com.cycb.chat.network.TenorApiService.create()
                                val response = tenorApi.getTrendingGifs(
                                    apiKey = com.cycb.chat.network.TenorApiService.API_KEY
                                )
                                response.results
                            } catch (e: Exception) {
                                android.util.Log.e("ChatRoomScreen", "Failed to get trending GIFs", e)
                                emptyList()
                            }
                        }
                    )
                }
            }

            if (showFullScreenImage && fullScreenImageUrl != null) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = {
                        showFullScreenImage = false
                        fullScreenImageUrl = null
                    },
                    properties = androidx.compose.ui.window.DialogProperties(
                        usePlatformDefaultWidth = false,
                        decorFitsSystemWindows = false
                    )
                ) {
                    FullScreenImageViewer(
                        imageUrl = fullScreenImageUrl!!,
                        onBackClick = {
                            showFullScreenImage = false
                            fullScreenImageUrl = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatRoomTopBar(
    chat: Chat?,
    chatMembers: List<User>,
    hasActiveCall: Boolean,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onTitleClick: () -> Unit = {}
) {
    val isGroup = chat?.type == "group"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 3.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 8.dp, bottom = 12.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isGroup && chatMembers.isNotEmpty()) {

                    AnimatedAvatarCluster(
                        participants = chatMembers,
                        groupName = chat?.name ?: "Group Chat",
                        onClusterClick = onTitleClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                } else if (isGroup && chatMembers.isEmpty()) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTitleClick() }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LoadingIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTitleClick() }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProfilePicture(
                            imageUrl = chat?.otherUser?.profilePicture,
                            displayName = chat?.name ?: chat?.otherUser?.displayName ?: "Chat",
                            size = 56.dp
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = chat?.name ?: chat?.otherUser?.displayName ?: "Chat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = if (chat?.otherUser?.isOnline == true) "Online" else "Offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (chat?.otherUser?.isOnline == true)
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row {

                    var isPressed by remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.85f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        ),
                        label = "call_button_scale"
                    )

                    IconButton(
                        onClick = {
                            isPressed = true
                            onVoiceCallClick()
                        },
                        modifier = Modifier.scale(scale)
                    ) {

                        val iconTint = if (hasActiveCall) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        val iconDesc = if (hasActiveCall) "Join Call" else "Voice call"

                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseScale by if (hasActiveCall) {
                            infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse_scale"
                            )
                        } else {
                            remember { mutableStateOf(1f) }
                        }

                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = iconDesc,
                            tint = iconTint,
                            modifier = if (hasActiveCall) Modifier.scale(pulseScale) else Modifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessagesList(
    messages: List<Message>,
    currentUserId: String,
    isGroupChat: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    viewModel: ChatRoomViewModel,
    typingUsers: List<com.cycb.chat.data.socket.TypingUser> = emptyList(),
    autoPlayGifs: Boolean = true,
    modifier: Modifier = Modifier,
    onProfileClick: ((String) -> Unit)? = null,
    onImageClick: ((String) -> Unit)? = null
) {

    val initialMessageIds = remember { mutableSetOf<String>() }
    val isInitialLoad = remember { mutableStateOf(true) }

    LaunchedEffect(messages) {
        if (isInitialLoad.value && messages.isNotEmpty()) {
            initialMessageIds.addAll(messages.map { it._id })
            isInitialLoad.value = false
        }
    }

    if (messages.isEmpty()) {
        EmptyMessagesState()
    } else {
        val reversedMessages = messages.reversed()

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(top = 8.dp, bottom = 150.dp, start = 0.dp, end = 0.dp),
            modifier = modifier.fillMaxSize(),
            reverseLayout = true
        ) {

            if (typingUsers.isNotEmpty()) {
                item(key = "typing_indicator") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, bottom = 8.dp, top = 8.dp)
                            .animateItem(
                                fadeInSpec = tween(durationMillis = 200),
                                fadeOutSpec = tween(durationMillis = 200),
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                    ) {
                        TypingIndicatorWithAvatars(
                            typingUsers = typingUsers
                        )
                    }
                }
            }

            items(
                items = reversedMessages,
                key = { it._id }
            ) { message ->

                if (message.messageType == "system_event") {
                    com.cycb.chat.ui.components.SystemMessageItem(message = message)
                } else {
                    val isSent = message.senderId._id == currentUserId

                    val shouldAnimate = !initialMessageIds.contains(message._id)

                    val messageIndex = reversedMessages.indexOf(message)
                    val prevMessage = reversedMessages.getOrNull(messageIndex - 1)
                    val nextMessage = reversedMessages.getOrNull(messageIndex + 1)

                    val isFirstInCluster = nextMessage?.senderId?._id != message.senderId._id

                    val isLastInCluster = prevMessage?.senderId?._id != message.senderId._id

                    Column(
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(durationMillis = 300),
                            fadeOutSpec = tween(durationMillis = 200),
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    ) {

                        if (isFirstInCluster && messageIndex > 0) {
                            Spacer(Modifier.height(12.dp))
                        } else if (!isFirstInCluster) {
                            Spacer(Modifier.height(2.dp))
                        }

                        if (shouldAnimate) {

                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(message._id) {
                                kotlinx.coroutines.delay(30)
                                visible = true
                            }

                            AnimatedVisibility(
                                visible = visible,
                                enter = slideInVertically(
                                    initialOffsetY = { it / 3 },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) + fadeIn(
                                    animationSpec = tween(durationMillis = 250)
                                ) + scaleIn(
                                    initialScale = 0.92f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ),
                                exit = shrinkVertically(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) + fadeOut(
                                    animationSpec = tween(durationMillis = 200)
                                ) + scaleOut(
                                    targetScale = 0.9f,
                                    animationSpec = tween(durationMillis = 200)
                                )
                            ) {

                                val wasSending = remember { mutableStateOf(message.isSending) }
                                LaunchedEffect(message.isSending) {
                                    wasSending.value = message.isSending
                                }

                                MessageBubble(
                                    message = message,
                                    isSent = isSent,
                                    showAvatar = isGroupChat && !isSent && isLastInCluster,
                                    showSenderName = isGroupChat && !isSent && isFirstInCluster,
                                    isFirstInCluster = isFirstInCluster,
                                    isLastInCluster = isLastInCluster,
                                    isGroupChat = isGroupChat,
                                    autoPlayGifs = autoPlayGifs,
                                    onReactionClick = { emoji ->
                                        viewModel.reactToMessage(message._id, emoji)
                                    },
                                    onReply = {
                                        viewModel.setReplyToMessage(message)
                                    },
                                    onDelete = {
                                        viewModel.deleteMessage(message._id)
                                    },
                                    onProfileClick = onProfileClick,
                                    onImageClick = onImageClick
                                )
                            }
                        } else {

                            MessageBubble(
                                message = message,
                                isSent = isSent,
                                showAvatar = isGroupChat && !isSent && isLastInCluster,
                                showSenderName = isGroupChat && !isSent && isFirstInCluster,
                                isFirstInCluster = isFirstInCluster,
                                isLastInCluster = isLastInCluster,
                                isGroupChat = isGroupChat,
                                autoPlayGifs = autoPlayGifs,
                                onReactionClick = { emoji ->
                                    viewModel.reactToMessage(message._id, emoji)
                                },
                                onReply = {
                                    viewModel.setReplyToMessage(message)
                                },
                                onDelete = {
                                    viewModel.deleteMessage(message._id)
                                },
                                onProfileClick = onProfileClick,
                                onImageClick = onImageClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isSent: Boolean,
    showAvatar: Boolean = false,
    showSenderName: Boolean = false,
    isFirstInCluster: Boolean = true,
    isLastInCluster: Boolean = true,
    isGroupChat: Boolean = false,
    autoPlayGifs: Boolean = true,
    onReactionClick: (String) -> Unit = {},
    onLongPress: () -> Unit = {},
    onReply: () -> Unit = {},
    onDelete: () -> Unit = {},
    onProfileClick: ((String) -> Unit)? = null,
    onImageClick: ((String) -> Unit)? = null
) {
    var showReactionPicker by remember { mutableStateOf(false) }
    var showReactionDetails by remember { mutableStateOf(false) }
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    val swipeThreshold = 80f

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bubble_scale"
    )

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bubble_offset"
    )

    val replyIconAlpha by animateFloatAsState(
        targetValue = if (offsetX > 20f) (offsetX / swipeThreshold).coerceIn(0f, 1f) else 0f,
        label = "reply_icon_alpha"
    )

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {

        if (replyIconAlpha > 0f) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = "Reply",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = replyIconAlpha),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp)
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = replyIconAlpha
                        scaleY = replyIconAlpha
                    }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
                .graphicsLayer {
                    translationX = animatedOffsetX
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX >= swipeThreshold) {
                                onReply()
                            }
                            offsetX = 0f
                        },
                        onDragCancel = {
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = offsetX + dragAmount

                            offsetX = newOffset.coerceIn(0f, swipeThreshold * 1.2f)
                        }
                    )
                }
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
            horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {

        if (showAvatar && !isSent) {
            ProfilePicture(
                imageUrl = message.senderId.profilePicture,
                displayName = message.senderId.displayName,
                size = 28.dp,
                modifier = Modifier.padding(end = 6.dp),
                onClick = if (onProfileClick != null) {
                    { onProfileClick(message.senderId._id) }
                } else null
            )
        } else if (!isSent && !showAvatar && isGroupChat) {

            Spacer(Modifier.width(34.dp))
        }

        Column(
            horizontalAlignment = if (isSent) Alignment.End else Alignment.Start
        ) {

            val bubbleShape = when {

                isSent && isFirstInCluster && isLastInCluster -> RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                !isSent && isFirstInCluster && isLastInCluster -> RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)

                isSent && isFirstInCluster -> RoundedCornerShape(20.dp, 20.dp, 20.dp, 20.dp)

                isSent && !isFirstInCluster && !isLastInCluster -> RoundedCornerShape(20.dp, 4.dp, 4.dp, 20.dp)

                isSent && isLastInCluster -> RoundedCornerShape(20.dp, 4.dp, 4.dp, 20.dp)

                !isSent && isFirstInCluster -> RoundedCornerShape(20.dp, 20.dp, 20.dp, 20.dp)

                !isSent && !isFirstInCluster && !isLastInCluster -> RoundedCornerShape(4.dp, 20.dp, 20.dp, 4.dp)

                !isSent && isLastInCluster -> RoundedCornerShape(4.dp, 20.dp, 20.dp, 4.dp)

                else -> RoundedCornerShape(20.dp)
            }

            Surface(
                shape = bubbleShape,
                color = if (isSent)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .scale(animatedScale)
                    .combinedClickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            scale = 0.96f
                            kotlinx.coroutines.GlobalScope.launch {
                                kotlinx.coroutines.delay(80)
                                scale = 1f
                            }
                        },
                        onLongClick = {

                            scale = 1.05f
                            kotlinx.coroutines.GlobalScope.launch {
                                kotlinx.coroutines.delay(100)
                                scale = 1f
                                kotlinx.coroutines.delay(50)
                                showReactionPicker = !showReactionPicker
                            }
                            onLongPress()
                        }
                    )
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {

                    if (showSenderName) {
                        Text(
                            text = message.senderId.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    message.replyTo?.let { reply ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSent)
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(40.dp)
                                        .background(
                                            if (isSent)
                                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                            else
                                                MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(2.dp)
                                        )
                                )

                                Spacer(Modifier.width(8.dp))

                                Column {
                                    Text(
                                        text = "Replying to ${reply.senderId?.displayName ?: reply.senderName ?: "User"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSent)
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                        else
                                            MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = when (reply.messageType ?: "text") {
                                            "voice" -> "🎤 Voice message"
                                            "image" -> "📷 Image"
                                            "file" -> "📎 File"
                                            "gif" -> "🎬 GIF"
                                            else -> reply.content
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSent)
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    when (message.messageType) {
                        "voice" -> {

                            com.cycb.chat.ui.components.VoiceMessagePlayer(
                                audioUrl = message.content,
                                duration = message.metadata?.duration ?: 0,
                                isOwnMessage = isSent
                            )
                        }
                        "image" -> {

                            com.cycb.chat.ui.components.ImageMessage(
                                imageUrl = message.content,
                                isOwnMessage = isSent,
                                onImageClick = onImageClick
                            )
                        }
                        "gif" -> {

                            com.cycb.chat.ui.components.GifMessage(
                                gifUrl = message.content,
                                isOwnMessage = isSent,
                                autoPlayGifs = autoPlayGifs
                            )
                        }
                        else -> {

                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSent)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessHigh
                            )
                        )
                    ) {

                        androidx.compose.animation.AnimatedVisibility(
                            visible = message.isSending,
                            enter = fadeIn(animationSpec = tween(200)) + scaleIn(
                                initialScale = 0.8f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessHigh
                                )
                            ),
                            exit = fadeOut(animationSpec = tween(200)) + scaleOut(
                                targetScale = 0.8f,
                                animationSpec = tween(200)
                            )
                        ) {
                            LoadingIndicator(
                                modifier = Modifier.size(12.dp),
                                color = if (isSent)
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        if (message.isEdited) {
                            Text(
                                text = "edited",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSent)
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }

                        androidx.compose.animation.Crossfade(
                            targetState = message.isSending,
                            animationSpec = tween(durationMillis = 300),
                            label = "timestamp_crossfade"
                        ) { isSending ->
                            Text(
                                text = if (isSending) "Sending..." else formatMessageTime(message.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSent)
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            if (message.reactions.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                ReactionChips(
                    reactions = message.reactions,
                    onReactionClick = onReactionClick,
                    onReactionLongClick = {
                        android.util.Log.d("MessageBubble", "Long press on reactions, showing details")
                        showReactionDetails = true
                    }
                )
            }
        }
    }

    if (showReactionDetails && message.reactions.isNotEmpty()) {
        ReactionDetailsSheet(
            reactions = message.reactions,
            onDismiss = {
                android.util.Log.d("MessageBubble", "Dismissing reaction details")
                showReactionDetails = false
            },
            onProfileClick = onProfileClick
        )
    }

            AnimatedVisibility(
                visible = showReactionPicker,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 200)
                ) + scaleIn(
                    initialScale = 0.85f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                ) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 150)
                ) + scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(durationMillis = 150)
                )
            ) {
                MessageActionsMenu(
                    isSent = isSent,
                    onEmojiSelected = { emoji ->
                        onReactionClick(emoji)
                        showReactionPicker = false
                    },
                    onDelete = {
                        onDelete()
                        showReactionPicker = false
                    }
                )
            }
        }
    }

@Composable
fun ReactionChips(
    reactions: List<com.cycb.chat.data.model.Reaction>,
    onReactionClick: (String) -> Unit,
    onReactionLongClick: () -> Unit = {}
) {
    val groupedReactions = reactions.groupBy { it.emoji }
    val hapticFeedback = LocalHapticFeedback.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        groupedReactions.forEach { (emoji, reactionList) ->
            var isPressed by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.85f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                ),
                label = "reaction_scale"
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                ),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .scale(scale)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                    .combinedClickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            isPressed = true
                            kotlinx.coroutines.GlobalScope.launch {
                                kotlinx.coroutines.delay(100)
                                isPressed = false
                                kotlinx.coroutines.delay(50)
                                onReactionClick(emoji)
                            }
                        },
                        onLongClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onReactionLongClick()
                        }
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 16.sp
                    )
                    if (reactionList.size > 1) {
                        Text(
                            text = reactionList.size.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MessageActionsMenu(
    isSent: Boolean,
    onEmojiSelected: (String) -> Unit,
    onDelete: () -> Unit
) {
    val emojis = listOf("❤️", "😭", "🥀", "😮", "😢", "🙏")

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(50)
        visible = true
    }

    val menuScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "menu_scale"
    )

    val menuAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "menu_alpha"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .graphicsLayer {
                scaleX = menuScale
                scaleY = menuScale
                alpha = menuAlpha
            }
    ) {

        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp,
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                emojis.forEachIndexed { index, emoji ->
                    var scale by remember { mutableStateOf(1f) }
                    val animatedScale by animateFloatAsState(
                        targetValue = scale,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        ),
                        label = "emoji_scale_$index"
                    )

                    var emojiVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index * 30L)
                        emojiVisible = true
                    }

                    val emojiScale by animateFloatAsState(
                        targetValue = if (emojiVisible) 1f else 0.3f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "emoji_entrance_$index"
                    )

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .scale(animatedScale * emojiScale)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                scale = 1.4f
                                kotlinx.coroutines.GlobalScope.launch {
                                    kotlinx.coroutines.delay(120)
                                    scale = 1f
                                    kotlinx.coroutines.delay(30)
                                    onEmojiSelected(emoji)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = 28.sp
                        )
                    }
                }
            }
        }

        if (isSent) {
            var isPressed by remember { mutableStateOf(false) }
            val deleteScale by animateFloatAsState(
                targetValue = if (isPressed) 0.92f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                ),
                label = "delete_scale"
            )

            var deleteVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(150)
                deleteVisible = true
            }

            val deleteAlpha by animateFloatAsState(
                targetValue = if (deleteVisible) 1f else 0f,
                animationSpec = tween(durationMillis = 200),
                label = "delete_alpha"
            )

            Surface(
                onClick = {
                    isPressed = true
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(100)
                        isPressed = false
                        kotlinx.coroutines.delay(30)
                        onDelete()
                    }
                },
                modifier = Modifier
                    .scale(deleteScale)
                    .graphicsLayer { alpha = deleteAlpha },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete message",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Delete message",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ReactionPicker(
    onEmojiSelected: (String) -> Unit
) {
    MessageActionsMenu(
        isSent = false,
        onEmojiSelected = onEmojiSelected,
        onDelete = {}
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LoadingIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Loading messages...",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EmptyMessagesState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "👋",
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Start the conversation!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Send your first message below",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.displayLarge
            )

            Text(
                text = "Oops!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onRetry,
                shape = MaterialTheme.shapes.large
            ) {
                Text("Try Again")
            }
        }
    }
}

private fun formatMessageTime(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(timestamp) ?: return timestamp
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    } catch (e: Exception) {
        timestamp
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TypingIndicatorWithAvatars(
    typingUsers: List<com.cycb.chat.data.socket.TypingUser>
) {

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(50)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Box(
                    modifier = Modifier.height(36.dp)
                ) {
                    typingUsers.take(3).forEachIndexed { index, user ->
                        val offsetX = (index * 20).dp

                        key(user.userId) {
                            var avatarVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(user.userId) {
                                kotlinx.coroutines.delay(index * 100L)
                                avatarVisible = true
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = avatarVisible,
                                enter = scaleIn(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) + fadeIn(),
                                exit = scaleOut() + fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = offsetX)
                                        .size(36.dp)
                                        .shadow(2.dp, CircleShape)
                                ) {
                                    ProfilePicture(
                                        imageUrl = user.profilePicture,
                                        displayName = user.displayName,
                                        size = 36.dp
                                    )
                                }
                            }
                        }
                    }
                }

                if (typingUsers.size > 1) {
                    Spacer(Modifier.width(((typingUsers.size.coerceAtMost(3) - 1) * 20).dp))
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TypingIndicator()

                    Text(
                        text = formatTypingText(typingUsers),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

private fun formatTypingText(users: List<com.cycb.chat.data.socket.TypingUser>): String {
    return when (users.size) {
        0 -> ""
        1 -> "${users[0].displayName} is typing..."
        2 -> "${users[0].displayName} and ${users[1].displayName} are typing..."
        3 -> "${users[0].displayName}, ${users[1].displayName}, and ${users[2].displayName} are typing..."
        else -> "${users[0].displayName}, ${users[1].displayName}, and ${users.size - 2} others are typing..."
    }
}

private fun parseGradientBackground(gradientString: String): androidx.compose.ui.graphics.Brush {
    return try {

        val colors = gradientString
            .substringAfter("(")
            .substringBefore(")")
            .split(",")
            .drop(1)
            .map { it.trim() }
            .map { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(it)) }

        if (colors.size >= 2) {
            androidx.compose.ui.graphics.Brush.linearGradient(colors)
        } else {

            androidx.compose.ui.graphics.Brush.linearGradient(
                listOf(
                    androidx.compose.ui.graphics.Color(0xFF667eea),
                    androidx.compose.ui.graphics.Color(0xFF764ba2)
                )
            )
        }
    } catch (e: Exception) {

        androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(
                androidx.compose.ui.graphics.Color(0xFF667eea),
                androidx.compose.ui.graphics.Color(0xFF764ba2)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReactionDetailsSheet(
    reactions: List<com.cycb.chat.data.model.Reaction>,
    onDismiss: () -> Unit,
    onProfileClick: ((String) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    val groupedReactions = reactions.groupBy { it.emoji }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle()
                Text(
                    text = "Reactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            groupedReactions.forEach { (emoji, reactionList) ->
                item(key = emoji) {

                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(emoji) {
                        kotlinx.coroutines.delay(50)
                        visible = true
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter = slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeIn()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = emoji,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Text(
                                        text = "${reactionList.size}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            reactionList.forEachIndexed { index, reaction ->
                                key(reaction.userId + reaction.emoji) {
                                    var itemVisible by remember { mutableStateOf(false) }
                                    LaunchedEffect(reaction.userId) {
                                        kotlinx.coroutines.delay(index * 50L)
                                        itemVisible = true
                                    }

                                    AnimatedVisibility(
                                        visible = itemVisible,
                                        enter = slideInHorizontally(
                                            initialOffsetX = { it / 4 },
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessHigh
                                            )
                                        ) + fadeIn()
                                    ) {
                                        ReactionUserItem(
                                            reaction = reaction,
                                            onClick = {
                                                onProfileClick?.invoke(reaction.userId)
                                                scope.launch {
                                                    sheetState.hide()
                                                    onDismiss()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReactionUserItem(
    reaction: com.cycb.chat.data.model.Reaction,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var userInfo by remember { mutableStateOf<com.cycb.chat.data.model.ProfileResponse?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(reaction.userId) {
        try {
            val response = com.cycb.chat.data.api.RetrofitClient.apiService.getUserProfile(reaction.userId)
            userInfo = response.user
        } catch (e: Exception) {
            android.util.Log.e("ReactionUserItem", "Failed to fetch user: ${e.message}")
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "user_item_scale"
    )

    Surface(
        onClick = {
            isPressed = true
            kotlinx.coroutines.GlobalScope.launch {
                kotlinx.coroutines.delay(100)
                isPressed = false
                kotlinx.coroutines.delay(50)
                onClick()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            var avatarVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(50)
                avatarVisible = true
            }

            AnimatedVisibility(
                visible = avatarVisible,
                enter = scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            ) {
                if (userInfo != null) {
                    ProfilePicture(
                        imageUrl = userInfo?.profilePicture,
                        displayName = userInfo?.displayName ?: "User",
                        size = 44.dp
                    )
                } else {

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        ContainedLoadingIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = userInfo?.displayName ?: "Loading...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (userInfo != null) {
                    Text(
                        text = formatReactionTime(reaction.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "@${reaction.userId.take(8)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View profile",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatReactionTime(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(timestamp) ?: return "Just now"

        val now = System.currentTimeMillis()
        val diff = now - date.time

        when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            diff < 604800000 -> "${diff / 86400000}d ago"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        "Recently"
    }
}
