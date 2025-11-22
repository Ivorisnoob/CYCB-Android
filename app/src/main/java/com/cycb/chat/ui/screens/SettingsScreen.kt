package com.cycb.chat.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToThemePicker: () -> Unit = {},
    onNavigateToUpdate: () -> Unit = {},
    viewModel: com.cycb.chat.viewmodel.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var updateInfo by remember { mutableStateOf<com.cycb.chat.data.model.AppUpdateInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var isCheckingUpdate by remember { mutableStateOf(false) }

    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val messagesNotif by viewModel.messagesNotif.collectAsState()
    val friendRequestsNotif by viewModel.friendRequestsNotif.collectAsState()
    val chatInvitesNotif by viewModel.chatInvitesNotif.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()

    val darkMode by viewModel.darkMode.collectAsState()
    val dynamicColors by viewModel.dynamicColors.collectAsState()
    val compactMode by viewModel.compactMode.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()

    val readReceipts by viewModel.readReceipts.collectAsState()
    val typingIndicator by viewModel.typingIndicator.collectAsState()
    val lastSeenVisible by viewModel.lastSeenVisible.collectAsState()
    val profilePhotoVisible by viewModel.profilePhotoVisible.collectAsState()

    val autoDownloadMedia by viewModel.autoDownloadMedia.collectAsState()
    val autoPlayGifs by viewModel.autoPlayGifs.collectAsState()
    val enterToSend by viewModel.enterToSend.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showUpdateDialog && updateInfo != null) {
        com.cycb.chat.ui.components.UpdateDialog(
            updateInfo = updateInfo!!,
            onDownload = {
                com.cycb.chat.utils.UpdateManager.downloadUpdate(context, updateInfo!!)
                android.widget.Toast.makeText(
                    context,
                    "Downloading update...",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                showUpdateDialog = false
            },
            onDismiss = { showUpdateDialog = false }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, null) },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                SettingsSectionHeader(
                    title = "Notifications",
                    icon = Icons.Default.Notifications
                )
            }

            item {
                SettingsSwitchCard(
                    title = "Push Notifications",
                    subtitle = "Receive push notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                    icon = Icons.Default.Notifications
                )
            }

            if (notificationsEnabled) {
                item {
                    SettingsSwitchCard(
                        title = "Messages",
                        subtitle = "New message notifications",
                        checked = messagesNotif,
                        onCheckedChange = { viewModel.setMessagesNotif(it) },
                        icon = Icons.Default.Message
                    )
                }

                item {
                    SettingsSwitchCard(
                        title = "Friend Requests",
                        subtitle = "New friend request notifications",
                        checked = friendRequestsNotif,
                        onCheckedChange = { viewModel.setFriendRequestsNotif(it) },
                        icon = Icons.Default.PersonAdd
                    )
                }

                item {
                    SettingsSwitchCard(
                        title = "Group Invites",
                        subtitle = "Group chat invite notifications",
                        checked = chatInvitesNotif,
                        onCheckedChange = { viewModel.setChatInvitesNotif(it) },
                        icon = Icons.Default.GroupAdd
                    )
                }

                item {
                    SettingsSwitchCard(
                        title = "Sound",
                        subtitle = "Play notification sounds",
                        checked = soundEnabled,
                        onCheckedChange = { viewModel.setSoundEnabled(it) },
                        icon = Icons.Default.VolumeUp
                    )
                }

                item {
                    SettingsSwitchCard(
                        title = "Vibration",
                        subtitle = "Vibrate on notifications",
                        checked = vibrationEnabled,
                        onCheckedChange = { viewModel.setVibrationEnabled(it) },
                        icon = Icons.Default.Vibration
                    )
                }
            }

            item {
                SettingsSectionHeader(
                    title = "Appearance",
                    icon = Icons.Default.Palette
                )
            }

            item {
                SettingsSwitchCard(
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    checked = darkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) },
                    icon = Icons.Default.DarkMode
                )
            }

            item {
                SettingsClickableCard(
                    title = "Color Theme",
                    subtitle = selectedTheme,
                    icon = Icons.Default.Palette,
                    onClick = onNavigateToThemePicker
                )
            }

            item {
                SettingsSwitchCard(
                    title = "Dynamic Colors",
                    subtitle = "Use system color scheme (Android 12+)",
                    checked = dynamicColors,
                    onCheckedChange = { viewModel.setDynamicColors(it) },
                    icon = Icons.Default.Colorize
                )
            }

            item {
                SettingsSwitchCard(
                    title = "Compact Mode",
                    subtitle = "Reduce spacing and padding",
                    checked = compactMode,
                    onCheckedChange = { viewModel.setCompactMode(it) },
                    icon = Icons.Default.ViewCompact
                )
            }

            item {
                SettingsSectionHeader(
                    title = "Privacy & Security",
                    icon = Icons.Default.Security
                )
            }

            item {
                SettingsSwitchCard(
                    title = "Read Receipts",
                    subtitle = "Let others know when you've read messages",
                    checked = readReceipts,
                    onCheckedChange = { viewModel.setReadReceipts(it) },
                    icon = Icons.Default.DoneAll
                )
            }

            item {
                SettingsSwitchCard(
                    title = "Typing Indicator",
                    subtitle = "Show when you're typing",
                    checked = typingIndicator,
                    onCheckedChange = { viewModel.setTypingIndicator(it) },
                    icon = Icons.Default.Keyboard
                )
            }

            item {
                SettingsSwitchCard(
                    title = "Last Seen",
                    subtitle = "Show your online status",
                    checked = lastSeenVisible,
                    onCheckedChange = { viewModel.setLastSeenVisible(it) },
                    icon = Icons.Default.Visibility
                )
            }

            item {
                SettingsSwitchCard(
                    title = "Profile Photo Visibility",
                    subtitle = "Who can see your profile photo",
                    checked = profilePhotoVisible,
                    onCheckedChange = { viewModel.setProfilePhotoVisible(it) },
                    icon = Icons.Default.PhotoCamera
                )
            }

            item {
                var showPasswordDialog by remember { mutableStateOf(false) }

                if (showPasswordDialog) {
                    ChangePasswordDialog(
                        onDismiss = { showPasswordDialog = false },
                        onConfirm = { new ->
                            viewModel.updatePassword(
                                newPassword = new,
                                onSuccess = {
                                    showPasswordDialog = false
                                    android.widget.Toast.makeText(context, "Password updated successfully", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onError = { error ->
                                    android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )
                }

                SettingsClickableCard(
                    title = "Change Password",
                    subtitle = "Update your account password",
                    icon = Icons.Default.Lock,
                    onClick = { showPasswordDialog = true }
                )
            }

            item {
                SettingsSectionHeader(
                    title = "Chat Settings",
                    icon = Icons.Default.Chat
                )
            }

            item {
                SettingsSwitchCard(
                    title = "Auto-Download Media",
                    subtitle = "Automatically download images and videos",
                    checked = autoDownloadMedia,
                    onCheckedChange = { viewModel.setAutoDownloadMedia(it) },
                    icon = Icons.Default.Download
                )
            }

            item {
                SettingsSwitchCard(
                    title = "Auto-Play GIFs",
                    subtitle = "Play GIFs automatically",
                    checked = autoPlayGifs,
                    onCheckedChange = { viewModel.setAutoPlayGifs(it) },
                    icon = Icons.Default.Gif
                )
            }

            item {
                SettingsSwitchCard(
                    title = "Enter to Send",
                    subtitle = "Press Enter to send messages",
                    checked = enterToSend,
                    onCheckedChange = { viewModel.setEnterToSend(it) },
                    icon = Icons.Default.Send
                )
            }

            item {
                SettingsSectionHeader(
                    title = "About",
                    icon = Icons.Default.Info
                )
            }

            item {
                SettingsClickableCard(
                    title = if (isCheckingUpdate) "Checking..." else "Check for Updates",
                    subtitle = "Version ${com.cycb.chat.BuildConfig.VERSION_NAME} (Build ${com.cycb.chat.BuildConfig.VERSION_CODE})",
                    icon = Icons.Default.SystemUpdate,
                    onClick = {
                        if (!isCheckingUpdate) {
                            isCheckingUpdate = true
                            viewModel.checkForUpdates(context) { update ->
                                isCheckingUpdate = false
                                if (update != null && update.isUpdateAvailable) {
                                    onNavigateToUpdate()
                                } else {
                                    android.widget.Toast.makeText(
                                        context,
                                        "You're on the latest version!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                )
            }

            item {
                Spacer(Modifier.height(16.dp))

                ExpressiveLogoutButton(
                    onClick = { showLogoutDialog = true }
                )

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsSwitchCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCheckedChange(!checked)
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(8.dp))

            ExpressiveSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    val slideOffset by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "slide_offset"
    )

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (checked)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            .clickable {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onCheckedChange(!checked)
            }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .offset(x = (20.dp * slideOffset))
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(
                imageVector = if (checked) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.Center),
                tint = if (checked)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsClickableCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "settings_card_scale"
    )

    ElevatedCard(
        onClick = {
            isPressed = true
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .scale(scale),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveLogoutButton(
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "logout_scale"
    )

    ElevatedCard(
        onClick = {
            isPressed = true
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .scale(scale),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Logout",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = if (newPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (newPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = if (confirmPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword,
                    supportingText = {
                        if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                            Text("Passwords do not match")
                        }
                    }
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                        error = "All fields are required"
                    } else if (newPassword.length < 8) {
                        error = "Password must be at least 8 characters"
                    } else if (newPassword != confirmPassword) {
                        error = "Passwords do not match"
                    } else {
                        onConfirm(newPassword)
                    }
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
