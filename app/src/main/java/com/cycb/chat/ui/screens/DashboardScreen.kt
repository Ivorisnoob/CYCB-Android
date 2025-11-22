package com.cycb.chat.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.cycb.chat.data.model.Note
import com.cycb.chat.data.model.UserSummary
import com.cycb.chat.ui.components.NotesRow
import com.cycb.chat.viewmodel.DashboardUiState
import com.cycb.chat.viewmodel.DashboardViewModel
import com.cycb.chat.viewmodel.ChatsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    chatsViewModel: ChatsViewModel,
    currentUser: UserSummary?,
    onChatClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNewGroupClick: () -> Unit,
    onPublicChatsClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showNoteDialog by remember { mutableStateOf(false) }
    var noteContent by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }

                    IconButton(onClick = onSettingsClick) {

                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadDashboardData() },
            modifier = Modifier.padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is DashboardUiState.Loading -> {
                        if (!isRefreshing) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularWavyProgressIndicator()
                            }
                        }
                    }
                    is DashboardUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    "Error: ${state.message}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Button(onClick = { viewModel.loadDashboardData() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    is DashboardUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {

                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PaddingTitle("Notes")
                                    NotesRow(
                                        currentUser = currentUser,
                                        notes = state.notes,
                                        onAddNoteClick = { showNoteDialog = true },
                                        onNoteClick = { note ->
                                            onNoteClick(note.userId._id)
                                        }
                                    )
                                }
                            }

                            if (state.onlineFriends.isNotEmpty()) {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        PaddingTitle("Online Now")
                                        LazyRow(
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(state.onlineFriends) { friend ->
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.width(64.dp)
                                                ) {
                                                    Box {
                                                        AsyncImage(
                                                            model = friend.profilePicture ?: "https://ui-avatars.com/api/?name=${friend.username}",
                                                            contentDescription = friend.username,
                                                            modifier = Modifier
                                                                .size(56.dp)
                                                                .clip(CircleShape)
                                                                .clickable(
                                                                    indication = ripple(),
                                                                    interactionSource = remember { MutableInteractionSource() }
                                                                ) { onUserClick(friend.getUserId()) },
                                                            contentScale = ContentScale.Crop
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .size(14.dp)
                                                                .background(Color(0xFF4CAF50), CircleShape)
                                                                .align(Alignment.BottomEnd)
                                                                .padding(2.dp)
                                                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = friend.displayName.split(" ").first(),
                                                        style = MaterialTheme.typography.labelMedium,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PaddingTitle("Quick Actions")
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        QuickActionButton(
                                            text = "New Group",
                                            icon = Icons.Default.Group,
                                            modifier = Modifier.weight(1f),
                                            onClick = onNewGroupClick
                                        )
                                        QuickActionButton(
                                            text = "Public Chats",
                                            icon = Icons.Default.Public,
                                            modifier = Modifier.weight(1f),
                                            onClick = onPublicChatsClick
                                        )
                                        QuickActionButton(
                                            text = "More",
                                            icon = Icons.Default.MoreHoriz,
                                            modifier = Modifier.weight(1f),
                                            onClick = onMoreClick
                                        )
                                    }
                                }
                            }

                            item {
                                PaddingTitle("Recent Chats")
                            }

                            items(state.recentChats.take(3)) { chat ->
                                ChatListItem(
                                    chat = chat,
                                    isPinned = false,
                                    onClick = { onChatClick(chat.getChatId()) },
                                    onLongClick = { }
                                )
                            }

                        }
                    }
                }
            }
        }
    }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Share a thought") },
            text = {
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { if (it.length <= 60) noteContent = it },
                    label = { Text("What's on your mind?") },
                    supportingText = { Text("${noteContent.length}/60") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createNote(noteContent)
                        showNoteDialog = false
                        noteContent = ""
                    },
                    enabled = noteContent.isNotBlank()
                ) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PaddingTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
fun QuickActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}
