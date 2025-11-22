package com.cycb.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycb.chat.data.api.RetrofitClient
import com.cycb.chat.data.model.Chat
import com.cycb.chat.data.model.LastMessage
import com.cycb.chat.data.socket.SocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed class ChatsUiState {
    object Loading : ChatsUiState()
    data class Success(val chats: List<Chat>) : ChatsUiState()
    data class Error(val message: String) : ChatsUiState()
}

class ChatsViewModel(application: android.app.Application) : androidx.lifecycle.AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<ChatsUiState>(ChatsUiState.Loading)
    val uiState: StateFlow<ChatsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _rawChats = MutableStateFlow<List<Chat>>(emptyList())

    private val socketManager = SocketManager.getInstance()
    private var currentOpenChatId: String? = null

    private val chatPreferences = com.cycb.chat.data.local.ChatPreferences(application)

    val pinnedChatIds: StateFlow<Set<String>> = chatPreferences.pinnedChatIds
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        loadChats()
        setupSocketListeners()
        observeChats()
    }

    private fun observeChats() {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                _rawChats,
                chatPreferences.pinnedChatIds,
                chatPreferences.hiddenChatIds,
                _searchQuery
            ) { chats, pinnedIds, hiddenIds, query ->

                val visibleChats = chats.filter { !hiddenIds.contains(it.getChatId()) }

                val filteredChats = if (query.isBlank()) {
                    visibleChats
                } else {
                    visibleChats.filter { chat ->
                        val name = chat.name ?: chat.otherUser?.displayName ?: ""
                        name.contains(query, ignoreCase = true)
                    }
                }

                filteredChats.sortedWith(
                    compareByDescending<Chat> { pinnedIds.contains(it.getChatId()) }
                        .thenByDescending { it.updatedAt }
                )
            }.collect { sortedChats ->

                if (_uiState.value !is ChatsUiState.Loading || sortedChats.isNotEmpty()) {
                    _uiState.value = ChatsUiState.Success(sortedChats)
                }
            }
        }
    }

    private fun setupSocketListeners() {

        viewModelScope.launch {
            socketManager.newMessage.collect { message ->
                message?.let {
                    updateChatWithNewMessage(it.chatId, it.content, it.createdAt)
                }
            }
        }
    }

    private fun updateChatWithNewMessage(chatId: String, content: String, timestamp: String) {
        val currentChats = _rawChats.value
        val chatExists = currentChats.any { it.getChatId() == chatId }

        if (chatExists) {
            val updatedChats = currentChats.map { chat ->
                if (chat.getChatId() == chatId) {

                    val newUnreadCount = if (currentOpenChatId == chatId) {
                        0
                    } else {
                        chat.unreadCount + 1
                    }

                    chat.copy(
                        lastMessage = LastMessage(content, timestamp),
                        unreadCount = newUnreadCount,
                        updatedAt = timestamp
                    )
                } else {
                    chat
                }
            }
            _rawChats.value = updatedChats

            viewModelScope.launch {
                chatPreferences.unhideChat(chatId)
            }
        } else {

            loadChats()
        }
    }

    fun loadChats() {
        viewModelScope.launch {
            if (_rawChats.value.isEmpty()) {
                _uiState.value = ChatsUiState.Loading
            }
            try {
                val response = RetrofitClient.apiService.getChats()
                _rawChats.value = response.chats

            } catch (e: Exception) {
                _uiState.value = ChatsUiState.Error(
                    e.message ?: "Failed to load chats"
                )
            }
        }
    }

    fun markChatAsOpened(chatId: String) {
        currentOpenChatId = chatId
        clearUnreadCount(chatId)
    }

    fun markChatAsClosed() {
        currentOpenChatId = null
    }

    private fun clearUnreadCount(chatId: String) {
        val currentChats = _rawChats.value
        val updatedChats = currentChats.map { chat ->
            if (chat.getChatId() == chatId) {
                chat.copy(unreadCount = 0)
            } else {
                chat
            }
        }
        _rawChats.value = updatedChats

        viewModelScope.launch {
            try {
                RetrofitClient.apiService.markChatAsRead(chatId)
            } catch (e: Exception) {
                android.util.Log.e("ChatsViewModel", "Failed to mark chat as read", e)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getOrCreatePrivateChat(userId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getOrCreatePrivateChat(
                    mapOf(
                        "type" to "private",
                        "participantId" to userId
                    )
                )
                if (response.success) {

                    val currentChats = _rawChats.value
                    val chatExists = currentChats.any { it.getChatId() == response.chat.getChatId() }
                    if (!chatExists) {
                        _rawChats.value = currentChats + response.chat
                    }
                    onSuccess(response.chat.getChatId())
                } else {
                    onError("Failed to create chat")
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatsViewModel", "Error creating private chat", e)
                onError(e.message ?: "Failed to create chat")
            }
        }
    }

    fun togglePinChat(chatId: String) {
        viewModelScope.launch {
            chatPreferences.togglePinChat(chatId)
        }
    }

    fun hideChat(chatId: String) {
        viewModelScope.launch {
            chatPreferences.hideChat(chatId)
        }
    }

    fun isChatPinned(chatId: String): kotlinx.coroutines.flow.Flow<Boolean> {
        return chatPreferences.pinnedChatIds.map { pinnedIds ->
            pinnedIds.contains(chatId)
        }
    }
}
