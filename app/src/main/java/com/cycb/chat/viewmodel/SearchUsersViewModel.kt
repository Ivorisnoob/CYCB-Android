package com.cycb.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycb.chat.data.api.RetrofitClient
import com.cycb.chat.data.model.User
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

sealed class SearchUsersUiState {
    object Idle : SearchUsersUiState()
    object Loading : SearchUsersUiState()
    data class Success(val users: List<User>) : SearchUsersUiState()
    data class Error(val message: String) : SearchUsersUiState()
}

@OptIn(FlowPreview::class)
class SearchUsersViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<SearchUsersUiState>(SearchUsersUiState.Idle)
    val uiState: StateFlow<SearchUsersUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {

        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .collect { query ->
                    if (query.length >= 2) {
                        searchUsers(query)
                    } else if (query.isEmpty()) {
                        _uiState.value = SearchUsersUiState.Idle
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun searchUsers(query: String) {
        viewModelScope.launch {
            _uiState.value = SearchUsersUiState.Loading
            try {
                val response = RetrofitClient.apiService.searchUsers(query)
                _uiState.value = SearchUsersUiState.Success(response.users)
            } catch (e: Exception) {
                android.util.Log.e("SearchUsersViewModel", "Search error", e)
                _uiState.value = SearchUsersUiState.Error(
                    e.message ?: "Search failed"
                )
            }
        }
    }

    fun sendFriendRequest(userId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.sendFriendRequest(userId)
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("SearchUsersViewModel", "Send friend request error", e)
                onError(e.message ?: "Failed to send friend request")
            }
        }
    }
}
