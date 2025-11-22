package com.cycb.chat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cycb.chat.data.model.Chat
import com.cycb.chat.data.model.User
import com.cycb.chat.ui.components.ProfilePicture
import com.cycb.chat.ui.theme.GroupAvatarShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(
    chat: Chat?,
    members: List<User>,
    currentUserId: String,
    onBackClick: () -> Unit,
    onMemberClick: (String) -> Unit,
    onLeaveGroup: () -> Unit,
    onEditGroup: () -> Unit,
    onAddMembers: () -> Unit = {},
    onPromoteToAdmin: (String) -> Unit = {},
    onDemoteFromAdmin: (String) -> Unit = {},
    onRemoveMember: (String) -> Unit = {},
    onChatBackgroundClick: () -> Unit = {}
) {

    val currentUser = members.find { it.getUserId() == currentUserId }
    val isAdmin = currentUser?.isAdmin() == true
    var showLeaveDialog by remember { mutableStateOf(false) }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Group?") },
            text = { Text("Are you sure you want to leave this group? You won't be able to see messages anymore.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        onLeaveGroup()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Info", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {

                    if (isAdmin) {
                        IconButton(onClick = onAddMembers) {
                            Icon(Icons.Default.PersonAdd, "Add Members")
                        }
                    }

                    if (isAdmin) {
                        IconButton(onClick = onEditGroup) {
                            Icon(Icons.Default.Edit, "Edit Group")
                        }
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    ProfilePicture(
                        imageUrl = chat?.avatar,
                        displayName = chat?.name ?: "Group",
                        size = 100.dp
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        chat?.name ?: "Group",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        "${members.size} members",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column {
                        GroupActionItem(
                            icon = Icons.Default.Notifications,
                            title = "Mute Notifications",
                            onClick = {  }
                        )
                        HorizontalDivider()

                        if (isAdmin) {
                            GroupActionItem(
                                icon = Icons.Default.Image,
                                title = "Chat Background",
                                onClick = onChatBackgroundClick
                            )
                            HorizontalDivider()
                        }
                        GroupActionItem(
                            icon = Icons.Default.Image,
                            title = "Media & Files",
                            onClick = {  }
                        )
                    }
                }
            }

            item {
                Text(
                    "Members (${members.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(members) { member ->
                GroupMemberItem(
                    user = member,
                    currentUserId = currentUserId,
                    onClick = { onMemberClick(member.getUserId()) },
                    isCurrentUserAdmin = isAdmin,
                    showAdminBadge = member.isAdmin(),
                    onPromoteToAdmin = { onPromoteToAdmin(member.getUserId()) },
                    onDemoteFromAdmin = { onDemoteFromAdmin(member.getUserId()) },
                    onRemoveMember = { onRemoveMember(member.getUserId()) }
                )
            }

            item {
                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showLeaveDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
                    )
                ) {
                    Icon(Icons.Default.ExitToApp, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Leave Group")
                }
            }
        }
    }
}

@Composable
fun GroupActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(16.dp))

        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GroupMemberItem(
    user: User,
    currentUserId: String,
    onClick: () -> Unit,
    isCurrentUserAdmin: Boolean = false,
    showAdminBadge: Boolean = false,
    onPromoteToAdmin: () -> Unit = {},
    onDemoteFromAdmin: () -> Unit = {},
    onRemoveMember: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val isSelf = user.getUserId() == currentUserId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfilePicture(
                imageUrl = user.profilePicture,
                displayName = user.displayName,
                size = 48.dp
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        user.displayName,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (showAdminBadge) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                "Admin",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (isSelf) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                "You",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                Text(
                    "@${user.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (user.isOnline) {
                    Surface(
                        modifier = Modifier.size(12.dp),
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    ) {}
                }

                if (isCurrentUserAdmin && !isSelf) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {

                            if (showAdminBadge) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.PersonRemove,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text("Demote from Admin")
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        onDemoteFromAdmin()
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.PersonAdd,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text("Promote to Admin")
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        onPromoteToAdmin()
                                    }
                                )
                            }

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            "Remove from Group",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onRemoveMember()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
