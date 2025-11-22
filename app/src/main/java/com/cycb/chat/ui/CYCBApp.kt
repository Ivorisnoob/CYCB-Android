package com.cycb.chat.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import com.cycb.chat.MainActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cycb.chat.ui.auth.LoginScreen
import com.cycb.chat.ui.auth.SignUpScreen
import com.cycb.chat.ui.screens.ChatRoomScreen
import com.cycb.chat.ui.screens.ChatsListScreen
import com.cycb.chat.ui.screens.FriendsScreen
import com.cycb.chat.ui.screens.CreateGroupChatScreen
import com.cycb.chat.ui.screens.ProfileScreen
import com.cycb.chat.ui.screens.EditProfileScreen
import com.cycb.chat.ui.screens.SettingsScreen
import com.cycb.chat.ui.screens.SearchUsersScreen
import com.cycb.chat.ui.screens.GroupInfoScreen
import com.cycb.chat.ui.screens.PublicChatsScreen
import com.cycb.chat.ui.screens.EditGroupScreen
import com.cycb.chat.ui.screens.AddMembersScreen
import com.cycb.chat.ui.screens.ChatBackgroundPickerScreen
import com.cycb.chat.viewmodel.AuthViewModel
import com.cycb.chat.viewmodel.ChatRoomViewModel
import com.cycb.chat.viewmodel.ChatsViewModel
import com.cycb.chat.viewmodel.FriendsViewModel
import com.cycb.chat.viewmodel.CreateGroupViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CYCBApp(
    initialRoute: String? = null,
    chatId: String? = null,
    userId: String? = null
) {
    val context = LocalContext.current
    val authViewModel = remember { AuthViewModel(context) }

    val user by authViewModel.user.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val navController = rememberNavController()

    LaunchedEffect(initialRoute, chatId, userId, user) {
        if (user != null && initialRoute != null) {
            when (initialRoute) {
                "chat" -> chatId?.let { navController.navigate("chat/$it") }
                "friends" -> navController.navigate("friends")
                "chats" -> navController.navigate("chats")
                "profile" -> userId?.let { navController.navigate("profile/$it") }
            }
        }
    }

    LaunchedEffect(Unit) {
        com.cycb.chat.data.api.ApiConfig.initialize()
    }

    val chatsViewModel = remember(user) { ChatsViewModel(context.applicationContext as android.app.Application) }
    val chatRoomViewModel = remember(user) { ChatRoomViewModel() }
    val friendsViewModel = remember(user) { FriendsViewModel() }
    val publicChatsViewModel = remember(user) { com.cycb.chat.viewmodel.PublicChatsViewModel() }
    val voiceCallViewModel = remember(user) { com.cycb.chat.viewmodel.VoiceCallViewModel(context.applicationContext as android.app.Application) }

    val currentCall by voiceCallViewModel.currentCall.collectAsState()
    val callDuration by voiceCallViewModel.callDuration.collectAsState()

    LaunchedEffect(currentCall, callDuration) {
        MainActivity.hasActiveCall = currentCall != null
        MainActivity.currentCallDuration = callDuration
        MainActivity.currentCallChatName = currentCall?.callerName ?: "Voice Call"
        android.util.Log.d("CYCBApp", "Updated MainActivity call state: hasActiveCall=${currentCall != null}, duration=$callDuration")
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, currentCall) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {

                    if (currentCall != null && com.cycb.chat.utils.OverlayPermissionHelper.hasOverlayPermission(context)) {
                        android.util.Log.d("CYCBApp", "App paused with active call - starting overlay")
                        com.cycb.chat.service.CallOverlayService.startOverlay(
                            context,
                            "Voice Call",
                            callDuration
                        )
                    }
                }
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {

                    android.util.Log.d("CYCBApp", "App resumed - stopping overlay")
                    com.cycb.chat.service.CallOverlayService.stopOverlay(context)
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(currentCall, callDuration) {
        if (currentCall != null) {
            com.cycb.chat.service.CallOverlayService.updateOverlay(context, callDuration)
        }
    }

    DisposableEffect(Unit) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                if (intent?.action == com.cycb.chat.service.CallOverlayService.ACTION_END_CALL) {
                    android.util.Log.d("CYCBApp", "End call broadcast received")
                    voiceCallViewModel.endCall()
                }
            }
        }
        val filter = android.content.IntentFilter(com.cycb.chat.service.CallOverlayService.ACTION_END_CALL)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LoadingIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Loading",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        return
    }

    val startDestination = if (user == null) "login" else "home"

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentChatId = currentBackStackEntry?.arguments?.getString("chatId")

    val isMuted by voiceCallViewModel.isMuted.collectAsState()
    val callMode by voiceCallViewModel.callMode.collectAsState()

    val bottomNavItems = listOf(
        com.cycb.chat.ui.components.BottomNavItem(
            route = "dashboard",
            label = "Home",
            icon = androidx.compose.material.icons.Icons.Outlined.Home,
            selectedIcon = androidx.compose.material.icons.Icons.Filled.Home
        ),
        com.cycb.chat.ui.components.BottomNavItem(
            route = "chats",
            label = "Chats",
            icon = androidx.compose.material.icons.Icons.Outlined.Chat,
            selectedIcon = androidx.compose.material.icons.Icons.Filled.Chat
        ),
        com.cycb.chat.ui.components.BottomNavItem(
            route = "friends",
            label = "People",
            icon = androidx.compose.material.icons.Icons.Outlined.People,
            selectedIcon = androidx.compose.material.icons.Icons.Filled.People
        )
    )

    val showBottomBar = currentRoute == "home"

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = 0,
        pageCount = { 3 }
    )

    LaunchedEffect(initialRoute, user) {
        if (user != null && initialRoute != null) {
            when (initialRoute) {
                "chat" -> chatId?.let { navController.navigate("chat/$it") }
                "friends" -> {
                    navController.navigate("home")
                    pagerState.scrollToPage(2)
                }
                "chats" -> {
                    navController.navigate("home")
                    pagerState.scrollToPage(1)
                }
                "profile" -> userId?.let { navController.navigate("profile/$it") }
            }
        }
    }

    val currentHomeTab = when (pagerState.currentPage) {
        0 -> "dashboard"
        1 -> "chats"
        2 -> "friends"
        else -> "dashboard"
    }
    val effectiveRoute = if (currentRoute == "home") currentHomeTab else currentRoute
    val scope = rememberCoroutineScope()

    androidx.compose.material3.Scaffold(
        bottomBar = {
            if (showBottomBar) {
                com.cycb.chat.ui.components.BottomNavigationBar(
                    items = bottomNavItems,
                    currentRoute = effectiveRoute,
                    onItemClick = { route ->
                        val targetPage = when (route) {
                            "dashboard" -> 0
                            "chats" -> 1
                            "friends" -> 2
                            else -> 0
                        }
                        scope.launch {
                            pagerState.animateScrollToPage(targetPage)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(navController = navController, startDestination = startDestination) {

                composable("login") {
                    LoginScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = {
                            navController.navigate("home") { popUpTo("login") { inclusive = true } }
                        },
                        onNavigateToSignUp = { navController.navigate("signup") }
                    )
                }

                composable("signup") {
                    SignUpScreen(
                        viewModel = authViewModel,
                        onSignUpSuccess = {
                            navController.navigate("home") { popUpTo("signup") { inclusive = true } }
                        },
                        onNavigateToLogin = { navController.popBackStack() }
                    )
                }

                composable("home") {
                    MainScreensWithSwipe(
                        pagerState = pagerState,
                        user = user,
                        chatsViewModel = chatsViewModel,
                        friendsViewModel = friendsViewModel,
                        navController = navController
                    )
                }

                composable("create_group") {
                    val createGroupViewModel = remember { CreateGroupViewModel() }

                    CreateGroupChatScreen(
                        viewModel = createGroupViewModel,
                        onBackClick = { navController.popBackStack() },
                        onGroupCreated = { groupId ->
                            navController.navigate("chat/$groupId") {
                                popUpTo("chats") { inclusive = false }
                            }
                        }
                    )
                }

                composable("public_chats") {
                    PublicChatsScreen(
                        viewModel = publicChatsViewModel,
                        onChatClick = { chatId ->
                            navController.navigate("chat/$chatId")
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable("expressive_demo") {
                    com.cycb.chat.ui.screens.ExpressiveDemoScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "chat/{chatId}",
                    arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable

                    LaunchedEffect(chatId) {
                        chatsViewModel.markChatAsOpened(chatId)
                    }

                    DisposableEffect(chatId) {
                        onDispose {
                            chatsViewModel.markChatAsClosed()
                        }
                    }

                    val chatsState = chatsViewModel.uiState.collectAsState()
                    val chat =
                        if (chatsState.value is com.cycb.chat.viewmodel.ChatsUiState.Success) {
                            (chatsState.value as com.cycb.chat.viewmodel.ChatsUiState.Success).chats
                                .find { it.id == chatId }
                        } else null

                    ChatRoomScreen(
                        chatId = chatId,
                        chat = chat,
                        viewModel = chatRoomViewModel,
                        voiceCallViewModel = voiceCallViewModel,
                        currentUserId = user?.getUserId() ?: "",
                        currentUsername = user?.displayName ?: user?.username ?: "",
                        onBackClick = { navController.popBackStack() },
                        onProfileClick = { userId ->
                            navController.navigate("profile/$userId")
                        },
                        onGroupInfoClick = {
                            navController.navigate("group_info/$chatId")
                        },
                        onChatBackgroundClick = {
                            navController.navigate("chat_background/$chatId")
                        }
                    )
                }

                composable(
                    route = "profile/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                    val isOwnProfile = userId == user?.id

                    val profileUser = if (isOwnProfile) {
                        user
                    } else {
                        val friendsState = friendsViewModel.uiState.collectAsState()
                        if (friendsState.value is com.cycb.chat.viewmodel.FriendsUiState.Success) {
                            (friendsState.value as com.cycb.chat.viewmodel.FriendsUiState.Success).friends
                                .find { it.id == userId }
                        } else null
                    }

                    ProfileScreen(
                        userId = userId,
                        user = profileUser,
                        isOwnProfile = isOwnProfile,
                        onBackClick = { navController.popBackStack() },
                        onNavigateToChat = { chatId ->
                            navController.navigate("chat/$chatId")
                        },
                        onEditClick = {
                            navController.navigate("edit_profile")
                        },
                        onSettingsClick = {
                            navController.navigate("settings")
                        },
                        onAddFriendClick = {

                        }
                    )
                }

                composable("edit_profile") {
                    EditProfileScreen(
                        user = user ?: return@composable,
                        onSaveClick = { updatedUser ->
                            authViewModel.refreshUser()
                            navController.popBackStack()
                        },
                        onCancelClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        onBackClick = { navController.popBackStack() },
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        onNavigateToThemePicker = {
                            navController.navigate("theme_picker")
                        },
                        onNavigateToUpdate = {
                            navController.navigate("update_screen")
                        }
                    )
                }

                composable("theme_picker") {
                    com.cycb.chat.ui.screens.ThemePickerScreen(
                        onBackClick = { navController.popBackStack() },
                        onNavigateToCustomTheme = {
                            navController.navigate("custom_theme_creator")
                        }
                    )
                }

                composable("custom_theme_creator") {
                    com.cycb.chat.ui.screens.CustomThemeCreatorScreen(
                        onBackClick = { navController.popBackStack() },
                        onThemeCreated = { themeName ->
                            navController.popBackStack()
                        }
                    )
                }

                composable("update_screen") {
                    val updateInfo = com.cycb.chat.utils.UpdateManager.cachedUpdateInfo
                    if (updateInfo != null) {
                        com.cycb.chat.ui.screens.UpdateScreen(
                            updateInfo = updateInfo,
                            onBackClick = { navController.popBackStack() }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                    }
                }

                composable("search_users") {
                    SearchUsersScreen(
                        onBackClick = { navController.popBackStack() },
                        onUserClick = { userId ->
                            navController.navigate("profile/$userId")
                        }
                    )
                }

                composable(
                    route = "group_info/{chatId}",
                    arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
                    val groupInfoViewModel = remember { com.cycb.chat.viewmodel.GroupInfoViewModel() }
                    val groupInfoState by groupInfoViewModel.uiState.collectAsState()

                    LaunchedEffect(chatId) {
                        groupInfoViewModel.loadGroupInfo(chatId)
                    }

                    when (val state = groupInfoState) {
                        is com.cycb.chat.viewmodel.GroupInfoUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                LoadingIndicator()
                            }
                        }
                        is com.cycb.chat.viewmodel.GroupInfoUiState.Success -> {
                            val actionResult by groupInfoViewModel.actionResult.collectAsState()
                            val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

                            LaunchedEffect(actionResult) {
                                actionResult?.let {
                                    snackbarHostState.showSnackbar(it)
                                    groupInfoViewModel.clearActionResult()
                                }
                            }

                            val chat = com.cycb.chat.data.model.Chat(
                                id = state.chatInfo.id,
                                type = state.chatInfo.type,
                                name = state.chatInfo.name,
                                otherUser = null,
                                lastMessage = null,
                                unreadCount = 0,
                                updatedAt = ""
                            )

                            androidx.compose.material3.Scaffold(
                                snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) }
                            ) { paddingValues ->
                                Box(modifier = Modifier.padding(paddingValues)) {
                                    GroupInfoScreen(
                                        chat = chat,
                                        members = state.members,
                                        currentUserId = user?.getUserId() ?: "",
                                        onBackClick = { navController.popBackStack() },
                                        onMemberClick = { userId ->
                                            navController.navigate("profile/$userId")
                                        },
                                        onLeaveGroup = {
                                            groupInfoViewModel.leaveGroup(chatId) {
                                                navController.navigate("chats") {
                                                    popUpTo("chats") { inclusive = false }
                                                }
                                            }
                                        },
                                        onEditGroup = {
                                            navController.navigate("edit_group/$chatId")
                                        },
                                        onAddMembers = {
                                            navController.navigate("add_members/$chatId")
                                        },
                                        onPromoteToAdmin = { memberId ->
                                            groupInfoViewModel.updateMemberRole(chatId, memberId, "admin")
                                        },
                                        onDemoteFromAdmin = { memberId ->
                                            groupInfoViewModel.updateMemberRole(chatId, memberId, "member")
                                        },
                                        onRemoveMember = { memberId ->
                                            groupInfoViewModel.removeMember(chatId, memberId)
                                        },
                                        onChatBackgroundClick = {
                                            navController.navigate("chat_background/$chatId")
                                        }
                                    )
                                }
                            }
                        }
                        is com.cycb.chat.viewmodel.GroupInfoUiState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(state.message)
                                    Spacer(Modifier.height(16.dp))
                                    Button(onClick = { groupInfoViewModel.loadGroupInfo(chatId) }) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }
                }

                composable(
                    route = "edit_group/{chatId}",
                    arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
                    val groupInfoViewModel = remember { com.cycb.chat.viewmodel.GroupInfoViewModel() }
                    val groupInfoState by groupInfoViewModel.uiState.collectAsState()

                    LaunchedEffect(chatId) {
                        groupInfoViewModel.loadGroupInfo(chatId)
                    }

                    when (val state = groupInfoState) {
                        is com.cycb.chat.viewmodel.GroupInfoUiState.Success -> {
                            EditGroupScreen(
                                chatId = chatId,
                                initialName = state.chatInfo.name ?: "",
                                initialDescription = state.chatInfo.description,
                                onNavigateBack = { navController.popBackStack() },
                                viewModel = groupInfoViewModel
                            )
                        }
                        else -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                ContainedLoadingIndicator()
                            }
                        }
                    }
                }

                composable(
                    route = "add_members/{chatId}",
                    arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
                    val groupInfoViewModel = remember { com.cycb.chat.viewmodel.GroupInfoViewModel() }
                    val groupInfoState by groupInfoViewModel.uiState.collectAsState()

                    LaunchedEffect(chatId) {
                        groupInfoViewModel.loadGroupInfo(chatId)
                    }

                    when (val state = groupInfoState) {
                        is com.cycb.chat.viewmodel.GroupInfoUiState.Success -> {
                            AddMembersScreen(
                                chatId = chatId,
                                currentMembers = state.members,
                                onNavigateBack = { navController.popBackStack() },
                                friendsViewModel = friendsViewModel,
                                groupViewModel = groupInfoViewModel
                            )
                        }
                        else -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularWavyProgressIndicator()
                            }
                        }
                    }
                }

                composable(
                    route = "chat_background/{chatId}",
                    arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
                    val currentBackground = chatRoomViewModel.chatBackground.collectAsState().value

                    ChatBackgroundPickerScreen(
                        chatId = chatId,
                        currentBackground = currentBackground,
                        onBackClick = { navController.popBackStack() },
                        onBackgroundSelected = { type: String, value: String ->
                            chatRoomViewModel.updateChatBackground(chatId, type, value)
                        }
                    )
                }
            }
        }
    }

    currentCall?.let { call ->
        val showPip = call.chatId != currentChatId
        if (showPip) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                ) {
                    com.cycb.chat.ui.components.PictureInPictureCallView(
                        call = call,
                        callDuration = callDuration,
                        isMuted = isMuted,
                        callMode = callMode,
                        onMuteToggle = { voiceCallViewModel.toggleMute() },
                        onEndCall = { voiceCallViewModel.endCall() },
                        onSwitchToSpeaker = {
                            user?.let { u ->
                                voiceCallViewModel.switchToSpeaker(u.getUserId(), u.displayName ?: u.username)
                            }
                        },
                        onSwitchToListener = {
                            user?.let { u ->
                                voiceCallViewModel.switchToListener(u.getUserId(), u.displayName ?: u.username)
                            }
                        },
                        onExpand = {
                            navController.navigate("chat/${call.chatId}")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreensWithSwipe(
    pagerState: androidx.compose.foundation.pager.PagerState,
    user: com.cycb.chat.data.model.User?,
    chatsViewModel: ChatsViewModel,
    friendsViewModel: FriendsViewModel,
    navController: androidx.navigation.NavHostController
) {
    androidx.compose.foundation.pager.HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> {

                val dashboardViewModel: com.cycb.chat.viewmodel.DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                val currentUserSummary = user?.let {
                    com.cycb.chat.data.model.UserSummary(it.getUserId(), it.username, it.displayName, it.profilePicture)
                }

                com.cycb.chat.ui.screens.DashboardScreen(
                    viewModel = dashboardViewModel,
                    chatsViewModel = chatsViewModel,
                    currentUser = currentUserSummary,
                    onChatClick = { chatId -> navController.navigate("chat/$chatId") },
                    onNoteClick = { userId ->
                        chatsViewModel.getOrCreatePrivateChat(
                            userId = userId,
                            onSuccess = { chatId ->
                                navController.navigate("chat/$chatId")
                            },
                            onError = { error ->
                                android.util.Log.e("CYCBApp", "Failed to open chat from note: $error")
                            }
                        )
                    },
                    onUserClick = { userId -> navController.navigate("profile/$userId") },
                    onSearchClick = { navController.navigate("search_users") },
                    onSettingsClick = { navController.navigate("settings") },
                    onNewGroupClick = { navController.navigate("create_group") },
                    onPublicChatsClick = { navController.navigate("public_chats") },
                    onMoreClick = { navController.navigate("settings") }
                )
            }
            1 -> {

                ChatsListScreen(
                    viewModel = chatsViewModel,
                    onChatClick = { chatId -> navController.navigate("chat/$chatId") },
                    onCreateChatClick = {
                        navController.navigate("create_group")
                    },
                    onDiscoverClick = {
                        navController.navigate("public_chats")
                    },
                    onSearchClick = {
                        navController.navigate("search_users")
                    },
                    onDemoClick = {
                        navController.navigate("expressive_demo")
                    },
                    onProfileClick = {
                        user?.id?.let { userId ->
                            navController.navigate("profile/$userId")
                        }
                    },
                    userProfilePicture = user?.profilePicture,
                    userDisplayName = user?.displayName ?: ""
                )
            }
            2 -> {

                LaunchedEffect(Unit) {
                    friendsViewModel.loadFriends()
                }

                FriendsScreen(
                    viewModel = friendsViewModel,
                    onFriendClick = { friendId ->
                        navController.navigate("profile/$friendId")
                    },
                    onMessageClick = { friendId ->
                        chatsViewModel.getOrCreatePrivateChat(
                            userId = friendId,
                            onSuccess = { chatId ->
                                navController.navigate("chat/$chatId")
                            },
                            onError = { error ->
                                android.util.Log.e("CYCBApp", "Failed to create chat: $error")
                            }
                        )
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
