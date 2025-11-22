package com.cycb.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycb.chat.data.api.RetrofitClient
import com.cycb.chat.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FriendsUiState {
    object Loading : FriendsUiState()
    data class Success(
        val friends: List<User>,
        val friendRequests: List<User>
    ) : FriendsUiState()
    data class Error(val message: String) : FriendsUiState()
}

class FriendsViewModel : ViewModel() {
    private val apiService = RetrofitClient.apiService

    private val _uiState = MutableStateFlow<FriendsUiState>(FriendsUiState.Loading)
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {

        viewModelScope.launch {
            kotlinx.coroutines.delay(100)
            loadFriends()
        }
    }

    fun loadFriends() {
        viewModelScope.launch {
            _uiState.value = FriendsUiState.Loading
            try {
                android.util.Log.d("FriendsViewModel", "Loading friends...")
                android.util.Log.d("FriendsViewModel", "Token set: ${com.cycb.chat.data.api.RetrofitClient.getCurrentUserId() != null}")

                val friendsResponse = apiService.getFriends()
                android.util.Log.d("FriendsViewModel", "Friends loaded: ${friendsResponse.friends.size}")

                val requestsResponse = apiService.getFriendRequests()
                android.util.Log.d("FriendsViewModel", "Requests loaded: ${requestsResponse.requests.size}")

                _uiState.value = FriendsUiState.Success(
                    friends = friendsResponse.friends,
                    friendRequests = requestsResponse.requests
                )
            } catch (e: Exception) {
                android.util.Log.e("FriendsViewModel", "Failed to load friends", e)
                val errorMessage = when (e) {
                    is retrofit2.HttpException -> {
                        when (e.code()) {
                            401 -> "Session expired. Please login again."
                            else -> "Failed to load friends: ${e.code()}"
                        }
                    }
                    else -> e.message ?: "Failed to load friends"
                }
                _uiState.value = FriendsUiState.Error(errorMessage)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun acceptFriendRequest(userId: String) {
        viewModelScope.launch {
            try {
                apiService.acceptFriendRequest(userId)
                loadFriends()
            } catch (e: Exception) {
                _uiState.value = FriendsUiState.Error(e.message ?: "Failed to accept request")
            }
        }
    }

    fun declineFriendRequest(userId: String) {
        viewModelScope.launch {
            try {
                apiService.rejectFriendRequest(userId)
                loadFriends()
            } catch (e: Exception) {
                _uiState.value = FriendsUiState.Error(e.message ?: "Failed to decline request")
            }
        }
    }

    fun removeFriend(userId: String) {
        viewModelScope.launch {
            try {
                apiService.removeFriend(userId)
                loadFriends()
            } catch (e: Exception) {
                _uiState.value = FriendsUiState.Error(e.message ?: "Failed to remove friend")
            }
        }
    }

    fun sendFriendRequest(username: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("FriendsViewModel", "Searching for user: $username")

                val searchResponse = apiService.searchUsers(username, limit = 1)

                if (searchResponse.users.isEmpty()) {
                    _uiState.value = FriendsUiState.Error("User '$username' not found")
                    return@launch
                }

                val user = searchResponse.users.first()
                android.util.Log.d("FriendsViewModel", "Found user: ${user.username}, sending friend request")

                apiService.sendFriendRequest(user.getUserId())
                android.util.Log.d("FriendsViewModel", "Friend request sent successfully")

                loadFriends()
            } catch (e: Exception) {
                android.util.Log.e("FriendsViewModel", "Failed to send friend request", e)
                val errorMessage = when (e) {
                    is retrofit2.HttpException -> {
                        when (e.code()) {
                            404 -> "User not found"
                            409 -> "Friend request already sent or you're already friends"
                            else -> "Failed to send friend request: ${e.code()}"
                        }
                    }
                    else -> e.message ?: "Failed to send friend request"
                }
                _uiState.value = FriendsUiState.Error(errorMessage)
            }
        }
    }
}
