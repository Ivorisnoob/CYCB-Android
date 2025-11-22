package com.cycb.chat.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cycb.chat.data.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileScreen(
    userId: String,
    user: User?,
    isOwnProfile: Boolean,
    onBackClick: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onEditClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onAddFriendClick: () -> Unit = {}
) {
    val viewModel: com.cycb.chat.viewmodel.ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val profileState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(userId, user) {
        if (user == null || isOwnProfile) {
            viewModel.loadUser(userId)
        }

        viewModel.loadRelationship(userId)
        delay(50)
        isVisible = true
    }

    val displayUser = when {
        user != null -> user
        profileState is com.cycb.chat.viewmodel.ProfileUiState.Success ->
            (profileState as com.cycb.chat.viewmodel.ProfileUiState.Success).user
        else -> null
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                ProfileTopBar(
                    userName = user?.displayName ?: "",
                    scrollBehavior = scrollBehavior,
                    onBackClick = onBackClick,
                    isOwnProfile = isOwnProfile,
                    onMenuClick = {
                        if (isOwnProfile) {
                            onSettingsClick()
                        } else {

                        }
                    }
                )
            }
        ) { padding ->
            when {
                profileState is com.cycb.chat.viewmodel.ProfileUiState.Loading && displayUser == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
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
                                text = "Loading profile",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                displayUser != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    item {
                        AnimatedProfileContent(delay = 0) {
                            ProfileHeader(
                                user = displayUser,
                                isOwnProfile = isOwnProfile,
                                viewModel = viewModel,
                                onMessageClick = {

                                    coroutineScope.launch {
                                        try {
                                            android.util.Log.d("ProfileScreen", "Creating private chat with: ${displayUser.getUserId()}")

                                            android.widget.Toast.makeText(
                                                context,
                                                "Opening chat...",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()

                                            val response = com.cycb.chat.data.api.RetrofitClient.apiService.getOrCreatePrivateChat(
                                                mapOf(
                                                    "type" to "private",
                                                    "participantId" to displayUser.getUserId()
                                                )
                                            )

                                            android.util.Log.d("ProfileScreen", "Chat ready: ${response.chat.getChatId()}")

                                            kotlinx.coroutines.delay(200)

                                            onNavigateToChat(response.chat.getChatId())

                                            android.widget.Toast.makeText(
                                                context,
                                                "💬 Chat opened!",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()

                                        } catch (e: retrofit2.HttpException) {
                                            android.util.Log.e("ProfileScreen", "HTTP Error creating chat: ${e.code()}")
                                            val errorMsg = when (e.code()) {
                                                404 -> "User not found 😕"
                                                403 -> "Cannot message this user"
                                                else -> "Failed to open chat"
                                            }
                                            android.widget.Toast.makeText(
                                                context,
                                                errorMsg,
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        } catch (e: Exception) {
                                            android.util.Log.e("ProfileScreen", "Error creating chat", e)
                                            android.widget.Toast.makeText(
                                                context,
                                                "Oops! Something went wrong 😅",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                onAddFriendClick = {
                                    coroutineScope.launch {
                                        try {
                                            android.util.Log.d("ProfileScreen", "Sending friend request to: ${displayUser.getUserId()}")
                                            val response = com.cycb.chat.data.api.RetrofitClient.apiService.sendFriendRequest(displayUser.getUserId())
                                            android.util.Log.d("ProfileScreen", "Friend request response: $response")

                                            if (response.success) {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Friend request sent!",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()

                                                viewModel.loadRelationship(userId)
                                            }
                                        } catch (e: retrofit2.HttpException) {
                                            android.util.Log.e("ProfileScreen", "HTTP Error: ${e.code()} - ${e.message()}")
                                            val errorMsg = when (e.code()) {
                                                400 -> "Already friends or request pending"
                                                404 -> "User not found"
                                                else -> "Failed to send request"
                                            }
                                            android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            android.util.Log.e("ProfileScreen", "Error sending friend request", e)
                                            android.widget.Toast.makeText(
                                                context,
                                                "Error: ${e.message}",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                onRemoveFriendClick = {
                                    coroutineScope.launch {
                                        try {
                                            com.cycb.chat.data.api.RetrofitClient.apiService.removeFriend(displayUser.getUserId())
                                            android.widget.Toast.makeText(
                                                context,
                                                "Friend removed",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                            viewModel.loadRelationship(userId)
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Failed to remove friend",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                onAcceptFriendClick = {
                                    coroutineScope.launch {
                                        try {
                                            com.cycb.chat.data.api.RetrofitClient.apiService.acceptFriendRequest(displayUser.getUserId())
                                            android.widget.Toast.makeText(
                                                context,
                                                "Friend request accepted!",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                            viewModel.loadRelationship(userId)
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Failed to accept request",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                onEditClick = onEditClick
                            )
                        }
                    }

                    if (!displayUser.bio.isNullOrEmpty()) {
                        item {
                            AnimatedProfileContent(delay = 100) {
                                AboutSection(bio = displayUser.bio ?: "")
                            }
                        }
                    }

                    item {
                        AnimatedProfileContent(delay = 200) {
                            InfoSection(user = displayUser)
                        }
                    }

                    if (!isOwnProfile) {
                        item {
                            AnimatedProfileContent(delay = 300) {
                                ProfileSettingsSection(
                                    isMuted = false,
                                    onMuteToggle = {  },
                                    onBlockClick = {  },
                                    onReportClick = {  }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
            else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Unable to load profile",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(
    userName: String,
    scrollBehavior: TopAppBarScrollBehavior,
    onBackClick: () -> Unit,
    isOwnProfile: Boolean = false,
    onMenuClick: () -> Unit
) {
    val colorTransitionFraction = scrollBehavior.state.collapsedFraction

    TopAppBar(
        title = {
            AnimatedVisibility(
                visible = colorTransitionFraction > 0.5f,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Text(
                    userName,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            if (isOwnProfile) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(
                alpha = colorTransitionFraction
            ),
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        scrollBehavior = scrollBehavior,
        windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    )
}

@Composable
fun ProfileHeader(
    user: User,
    isOwnProfile: Boolean,
    viewModel: com.cycb.chat.viewmodel.ProfileViewModel,
    onMessageClick: () -> Unit,
    onAddFriendClick: () -> Unit,
    onRemoveFriendClick: () -> Unit,
    onAcceptFriendClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val relationship by viewModel.relationship.collectAsState()
    var showMessageAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(showMessageAnimation) {
        if (showMessageAnimation) {
            delay(1000)
            showMessageAnimation = false
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val scale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "profile_scale"
            )

        var showFullScreenImage by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
        ) {
            AsyncImage(
                model = user.profilePicture,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .clickable {
                        if (!user.profilePicture.isNullOrEmpty()) {
                            showFullScreenImage = true
                        }
                    },
                contentScale = ContentScale.Crop
            )

            if (user.isOnline) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_scale"
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .scale(pulseScale)
                        .background(Color(0xFF4CAF50), CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = user.displayName,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "@${user.username}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        if (user.isOnline) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                        CircleShape
                    )
            )

            Spacer(Modifier.width(6.dp))

            Text(
                text = if (user.isOnline) "Online" else "Offline",
                style = MaterialTheme.typography.bodyMedium,
                color = if (user.isOnline)
                    Color(0xFF4CAF50)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isOwnProfile) {
                    AnimatedActionButton(
                        text = "Edit Profile",
                        icon = Icons.Default.Edit,
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    AnimatedActionButton(
                        text = "Message",
                        icon = Icons.AutoMirrored.Filled.Send,
                        onClick = {
                            showMessageAnimation = true
                            onMessageClick()
                        },
                        modifier = Modifier.weight(1f)
                    )

                when {
                    relationship.isFriend -> {
                        AnimatedActionButton(
                            text = "Remove Friend",
                            icon = Icons.Default.PersonRemove,
                            onClick = onRemoveFriendClick,
                            modifier = Modifier.weight(1f),
                            isOutlined = true
                        )
                    }
                    relationship.hasSentRequest -> {
                        AnimatedActionButton(
                            text = "Request Sent",
                            icon = Icons.Default.Schedule,
                            onClick = {  },
                            modifier = Modifier.weight(1f),
                            isOutlined = true,
                            enabled = false
                        )
                    }
                    relationship.hasReceivedRequest -> {
                        AnimatedActionButton(
                            text = "Accept Request",
                            icon = Icons.Default.Check,
                            onClick = onAcceptFriendClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    else -> {
                        AnimatedActionButton(
                            text = "Add Friend",
                            icon = Icons.Default.Add,
                            onClick = onAddFriendClick,
                            modifier = Modifier.weight(1f),
                            isOutlined = true
                        )
                    }
                }
            }
        }

        if (showFullScreenImage && !user.profilePicture.isNullOrEmpty()) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showFullScreenImage = false },
                properties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                com.cycb.chat.ui.screens.FullScreenImageViewer(
                    imageUrl = user.profilePicture!!,
                    onBackClick = { showFullScreenImage = false }
                )
            }
        }
    }

        AnimatedVisibility(
            visible = showMessageAnimation,
            enter = fadeIn() + scaleIn(
                initialScale = 0.3f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { -it * 2 },
                animationSpec = tween(600)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "💬",
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isOutlined: Boolean = false,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    var isClicked by remember { mutableStateOf(false) }
    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "button_scale"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isClicked) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_bounce"
    )

    val iconRotation by animateFloatAsState(
        targetValue = if (isClicked) 15f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_rotation"
    )

    LaunchedEffect(isClicked) {
        if (isClicked) {
            delay(400)
            isClicked = false
        }
    }

    val coroutineScope = rememberCoroutineScope()

    if (isOutlined) {
        OutlinedButton(
            onClick = {
                isPressed = true
                isClicked = true
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                coroutineScope.launch {
                    delay(100)
                    isPressed = false
                }
                onClick()
            },
            enabled = enabled,
            modifier = modifier
                .height(52.dp)
                .scale(scale),
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .scale(iconScale)
                    .graphicsLayer {
                        rotationZ = iconRotation
                    }
            )
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        Button(
            onClick = {
                isPressed = true
                isClicked = true
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                coroutineScope.launch {
                    delay(100)
                    isPressed = false
                }
                onClick()
            },
            enabled = enabled,
            modifier = modifier
                .height(52.dp)
                .scale(scale),
            shape = MaterialTheme.shapes.large,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .scale(iconScale)
                    .graphicsLayer {
                        rotationZ = iconRotation
                    }
            )
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun AboutSection(bio: String) {
    ProfileSection(title = "About") {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun InfoSection(user: User) {
    ProfileSection(title = "Info") {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = user.email ?: "Not provided"
                )

                Spacer(Modifier.height(12.dp))

                InfoRow(
                    icon = Icons.Default.DateRange,
                    label = "Joined",
                    value = "March 2024"
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ProfileSettingsSection(
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    onBlockClick: () -> Unit,
    onReportClick: () -> Unit
) {
    ProfileSection(title = "Settings") {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column {
                SettingsItem(
                    icon = if (isMuted) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                    title = if (isMuted) "Unmute Notifications" else "Mute Notifications",
                    onClick = onMuteToggle,
                    iconTint = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                SettingsItem(
                    icon = Icons.Default.Block,
                    title = "Block User",
                    onClick = onBlockClick,
                    iconTint = MaterialTheme.colorScheme.error
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                SettingsItem(
                    icon = Icons.Default.Report,
                    title = "Report User",
                    onClick = onReportClick,
                    iconTint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        content()
    }
}

@Composable
fun AnimatedProfileContent(
    delay: Long = 0,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 300)
        ) + expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    ) {
        content()
    }
}
