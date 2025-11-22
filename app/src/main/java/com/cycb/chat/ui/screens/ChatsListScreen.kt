@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.cycb.chat.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.ripple
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycb.chat.data.model.Chat
import com.cycb.chat.ui.components.ProfilePicture
import com.cycb.chat.ui.components.UnreadBadge
import com.cycb.chat.viewmodel.ChatsUiState
import com.cycb.chat.viewmodel.ChatsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChatsListScreen(
    viewModel: ChatsViewModel,
    onChatClick: (String) -> Unit,
    onCreateChatClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onDiscoverClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onDemoClick: () -> Unit = {},
    userProfilePicture: String? = null,
    userDisplayName: String = ""
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val pinnedChatIds by viewModel.pinnedChatIds.collectAsState()
    val listState = rememberLazyListState()
    var isRefreshing by remember { mutableStateOf(false) }

    var showOptionsSheet by remember { mutableStateOf(false) }
    var selectedChatId by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val showFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }

    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.loadChats()
            kotlinx.coroutines.delay(1000)
            isRefreshing = false
        }
    }

    if (showOptionsSheet && selectedChatId != null) {
        ChatOptionsSheet(
            chatId = selectedChatId!!,
            isPinned = pinnedChatIds.contains(selectedChatId),
            onDismiss = { showOptionsSheet = false },
            onPinClick = {
                viewModel.togglePinChat(selectedChatId!!)
                showOptionsSheet = false
            },
            onDeleteClick = {
                viewModel.hideChat(selectedChatId!!)
                showOptionsSheet = false
            }
        )
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ChatsTopAppBar(
                onDiscoverClick = onDiscoverClick,
                onSearchClick = onSearchClick,
                onProfileClick = onProfileClick,
                userProfilePicture = userProfilePicture,
                userDisplayName = userDisplayName,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {

            var fabExpanded by remember { mutableStateOf(false) }

            FloatingActionButtonMenu(
                expanded = fabExpanded,
                modifier = Modifier.animateFloatingActionButton(
                    visible = showFab || fabExpanded,
                    alignment = Alignment.BottomEnd
                ),
                button = {

                    val motionScheme = MaterialTheme.motionScheme
                    val rotation by animateFloatAsState(
                        targetValue = if (fabExpanded) 45f else 0f,
                        animationSpec = motionScheme.fastSpatialSpec(),
                        label = "fab_rotation"
                    )

                    AnimatedContent(
                        targetState = fabExpanded,
                        transitionSpec = {
                            fadeIn(
                                animationSpec = motionScheme.fastEffectsSpec()
                            ) togetherWith fadeOut(
                                animationSpec = motionScheme.fastEffectsSpec()
                            ) using SizeTransform { initialSize, targetSize ->
                                motionScheme.defaultSpatialSpec()
                            }
                        },
                        label = "fab_morph"
                    ) { expanded ->
                        if (expanded) {

                            FloatingActionButton(
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    fabExpanded = false
                                },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                elevation = FloatingActionButtonDefaults.elevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    focusedElevation = 0.dp,
                                    hoveredElevation = 0.dp
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Close",
                                    modifier = Modifier.graphicsLayer { rotationZ = rotation }
                                )
                            }
                        } else {

                            ExtendedFloatingActionButton(
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    fabExpanded = true
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "New"
                                    )
                                },
                                text = { Text("New") },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                elevation = FloatingActionButtonDefaults.elevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    focusedElevation = 0.dp,
                                    hoveredElevation = 0.dp
                                )
                            )
                        }
                    }
                }
            ) {

                FloatingActionButtonMenuItem(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        fabExpanded = false
                        onSearchClick()
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    text = { Text("New Chat") }
                )

                FloatingActionButtonMenuItem(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        fabExpanded = false
                        onCreateChatClick()
                    },
                    icon = { Icon(Icons.Default.Group, contentDescription = null) },
                    text = { Text("New Group") }
                )

                FloatingActionButtonMenuItem(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        fabExpanded = false
                        onDiscoverClick()
                    },
                    icon = { Icon(Icons.Default.Explore, contentDescription = null) },
                    text = { Text("Discover") }
                )

                FloatingActionButtonMenuItem(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        fabExpanded = false
                        onDemoClick()
                    },
                    icon = { Icon(Icons.Default.Palette, contentDescription = null) },
                    text = { Text("Expressive Demo") }
                )
            }
        }
    ) { paddingValues ->

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                isRefreshing = true
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ChatsUiState.Loading -> {
                    LoadingState()
                }
                is ChatsUiState.Success -> {

                    val chats = state.chats

                    if (chats.isEmpty()) {
                        EmptyState(
                            onCreateChatClick = onCreateChatClick,
                            isSearching = searchQuery.isNotBlank()
                        )
                    } else {
                        ChatsList(
                            chats = chats,
                            pinnedChatIds = pinnedChatIds,
                            onChatClick = onChatClick,
                            onChatLongClick = { chatId ->
                                selectedChatId = chatId
                                showOptionsSheet = true
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            listState = listState
                        )
                    }
                }
                is ChatsUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadChats() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatsTopAppBar(
    onDiscoverClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    userProfilePicture: String? = null,
    userDisplayName: String = "",
    scrollBehavior: TopAppBarScrollBehavior
) {
    LargeTopAppBar(
        title = {

            val titleStyle = if (scrollBehavior.state.collapsedFraction > 0.5f) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.headlineLarge
            }

            Text(
                "Chats",
                style = titleStyle,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer {

                    val scale = 1f - (scrollBehavior.state.collapsedFraction * 0.05f)
                    scaleX = scale
                    scaleY = scale
                }
            )
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, "Search Users")
            }

            IconButton(onClick = onDiscoverClick) {
                Icon(Icons.Default.Explore, "Discover")
            }

            Spacer(Modifier.width(4.dp))

            IconButton(
                onClick = {
                    android.util.Log.d("ChatsListScreen", "Profile icon clicked!")
                    onProfileClick()
                },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                ProfilePicture(
                    imageUrl = userProfilePicture,
                    displayName = userDisplayName,
                    size = 40.dp
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    )
}

@Composable
private fun ChatsList(
    chats: List<Chat>,
    pinnedChatIds: Set<String>,
    onChatClick: (String) -> Unit,
    onChatLongClick: (String) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = chats,
            key = { it.getChatId() }
        ) { chat ->
            ChatListItem(
                chat = chat,
                isPinned = pinnedChatIds.contains(chat.getChatId()),
                onClick = { onChatClick(chat.getChatId()) },
                onLongClick = { onChatLongClick(chat.getChatId()) }
            )
        }

        item {
            Spacer(Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListItem(
    chat: Chat,
    isPinned: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "chat_item_scale"
    )

    val badgePulse by rememberInfiniteTransition(label = "badge_pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_pulse_scale"
    )

    val coroutineScope = rememberCoroutineScope()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .scale(scale),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 12.dp,
            hoveredElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .combinedClickable(
                    onClick = {
                        isPressed = true
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        coroutineScope.launch {
                            delay(100)
                            isPressed = false
                            delay(50)
                            onClick()
                        }
                    },
                    onLongClick = onLongClick,
                    indication = ripple(),
                    interactionSource = remember { MutableInteractionSource() }
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val avatarScale by animateFloatAsState(
                targetValue = if (chat.unreadCount > 0) 1.05f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "avatar_scale"
            )

            Box(modifier = Modifier.scale(avatarScale)) {
                ProfilePicture(
                    imageUrl = if (chat.type == "group") chat.avatar else chat.otherUser?.profilePicture,
                    displayName = chat.name ?: chat.otherUser?.displayName ?: "Unknown",
                    size = 60.dp
                )

                if (chat.hasActiveCall) {
                    LiveCallIndicator(
                        participantsCount = chat.activeCallParticipantsCount,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                } else if (chat.otherUser?.isOnline == true) {

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(18.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                CircleShape
                            )
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    MaterialTheme.colorScheme.tertiary,
                                    CircleShape
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isPinned) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pinned",
                                modifier = Modifier.size(14.dp).rotate(45f),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                        }

                        Text(
                            text = chat.name ?: chat.otherUser?.displayName ?: "Unknown",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (chat.unreadCount > 0)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = formatTimestamp(chat.lastMessage?.timestamp ?: chat.updatedAt),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (chat.unreadCount > 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.lastMessage?.content ?: "No messages yet",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                        color = if (chat.unreadCount > 0)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (chat.unreadCount > 0) {
                        Spacer(Modifier.width(12.dp))
                        Box(modifier = Modifier.scale(badgePulse)) {
                            UnreadBadge(count = chat.unreadCount)
                        }
                    }
                }
            }
        }
    }
}

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
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Loading chats",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

        }
    }
}

@Composable
private fun EmptyState(
    onCreateChatClick: () -> Unit,
    isSearching: Boolean
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
                text = if (isSearching) "🔍" else "💬",
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isSearching) "No chats found" else "No chats yet! 👋",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isSearching)
                    "Try a different search term"
                else
                    "Start a conversation to see it here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (!isSearching) {
                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onCreateChatClick,
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Start Chatting")
                }
            }
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

private fun formatTimestamp(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(timestamp) ?: return timestamp

        val now = Calendar.getInstance()
        val messageTime = Calendar.getInstance().apply { time = date }

        when {
            now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) -> {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            }
            now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
            now.get(Calendar.WEEK_OF_YEAR) == messageTime.get(Calendar.WEEK_OF_YEAR) -> {
                SimpleDateFormat("EEE", Locale.getDefault()).format(date)
            }
            else -> {
                SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
            }
        }
    } catch (e: Exception) {
        timestamp
    }
}

@Composable
private fun LiveCallIndicator(
    participantsCount: Int,
    modifier: Modifier = Modifier
) {

    val pulseScale by rememberInfiniteTransition(label = "live_pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_pulse_scale"
    )

    val pulseAlpha by rememberInfiniteTransition(label = "live_alpha").animateFloat(
        initialValue = 0.6f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_alpha"
    )

    Box(modifier = modifier) {

        Box(
            modifier = Modifier
                .size(24.dp)
                .scale(pulseScale)
                .background(
                    MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha),
                    CircleShape
                )
        )

        Surface(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.Center),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.error,
            shadowElevation = 4.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Live call",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        if (participantsCount > 1) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(16.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 2.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = if (participantsCount > 9) "9+" else participantsCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatOptionsSheet(
    chatId: String,
    isPinned: Boolean,
    onDismiss: () -> Unit,
    onPinClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Chat Options",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(8.dp))

            ListItem(
                headlineContent = { Text(if (isPinned) "Unpin Chat" else "Pin Chat") },
                leadingContent = {
                    Icon(
                        imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.clickable(onClick = onPinClick),
                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )

            ListItem(
                headlineContent = { Text("Delete Chat") },
                supportingContent = { Text("Hide from list until new message") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.clickable(onClick = onDeleteClick),
                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    }
}
