package com.cycb.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycb.chat.data.api.RetrofitClient
import com.cycb.chat.data.model.PublicChat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PublicChatsUiState {
    object Loading : PublicChatsUiState()
    data class Success(val chats: List<PublicChat>) : PublicChatsUiState()
    data class Error(val message: String) : PublicChatsUiState()
}

class PublicChatsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<PublicChatsUiState>(PublicChatsUiState.Loading)
    val uiState: StateFlow<PublicChatsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadPublicChats()
    }

    fun loadPublicChats(search: String? = null) {
        viewModelScope.launch {
            _uiState.value = PublicChatsUiState.Loading

            try {
                val response = RetrofitClient.apiService.getPublicChats(
                    limit = 20,
                    search = search
                )
                android.util.Log.d("PublicChatsViewModel", "Loaded ${response.chats.size} public chats")
                _uiState.value = PublicChatsUiState.Success(response.chats)
            } catch (e: Exception) {
                android.util.Log.e("PublicChatsViewModel", "Failed to load public chats", e)
                _uiState.value = PublicChatsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refreshChats() {
        viewModelScope.launch {
            _isRefreshing.value = true

            try {
                val response = RetrofitClient.apiService.getPublicChats(
                    limit = 20,
                    search = _searchQuery.value.ifEmpty { null }
                )
                _uiState.value = PublicChatsUiState.Success(response.chats)
            } catch (e: Exception) {
                android.util.Log.e("PublicChatsViewModel", "Failed to refresh public chats", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        loadPublicChats(query.ifEmpty { null })
    }

    fun joinChat(chatId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PublicChatsViewModel", "Joining chat: $chatId")
                RetrofitClient.apiService.joinPublicChat(chatId)
                android.util.Log.d("PublicChatsViewModel", "Successfully joined chat: $chatId")

                refreshChats()
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("PublicChatsViewModel", "Failed to join chat", e)
            }
        }
    }
}
