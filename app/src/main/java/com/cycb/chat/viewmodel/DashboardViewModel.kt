package com.cycb.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycb.chat.data.api.RetrofitClient
import com.cycb.chat.data.model.Note
import com.cycb.chat.data.model.User
import com.cycb.chat.data.model.Chat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val notes: List<Note>,
        val onlineFriends: List<User>,
        val recentChats: List<Chat>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {

                val notesDeferred = async { RetrofitClient.apiService.getNotes() }
                val friendsDeferred = async { RetrofitClient.apiService.getFriends() }
                val chatsDeferred = async { RetrofitClient.apiService.getChats() }

                val notesResponse = notesDeferred.await()
                val friendsResponse = friendsDeferred.await()
                val chatsResponse = chatsDeferred.await()

                val onlineFriends = friendsResponse.friends.filter { it.isOnline }

                _uiState.value = DashboardUiState.Success(
                    notes = notesResponse.notes,
                    onlineFriends = onlineFriends,
                    recentChats = chatsResponse.chats
                )
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Failed to load dashboard")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun createNote(content: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.createNote(mapOf("content" to content))
                loadDashboardData()
            } catch (e: Exception) {

                android.util.Log.e("DashboardVM", "Failed to create note", e)
            }
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.deleteNote()
                loadDashboardData()
            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Failed to delete note", e)
            }
        }
    }
}
