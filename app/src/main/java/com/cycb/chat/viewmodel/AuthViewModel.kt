package com.cycb.chat.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycb.chat.data.api.RetrofitClient
import com.cycb.chat.data.model.LoginRequest
import com.cycb.chat.data.model.User
import com.cycb.chat.data.socket.SocketManager
import com.cycb.chat.data.storage.TokenManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(context: Context) : ViewModel() {
    private val tokenManager = TokenManager(context)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {

        checkSavedAuth()
    }

    private fun checkSavedAuth() {
        viewModelScope.launch {
            try {

                val token = tokenManager.token.first()
                android.util.Log.d("AuthViewModel", "Checking saved auth, token: ${token?.take(20)}...")
                if (!token.isNullOrEmpty()) {

                    RetrofitClient.setToken(token)
                    try {
                        android.util.Log.d("AuthViewModel", "Fetching current user...")
                        val user = RetrofitClient.apiService.getCurrentUser()
                        android.util.Log.d("AuthViewModel", "User fetched: ${user.id}, ${user.username}")
                        _user.value = user
                        RetrofitClient.setCurrentUserId(user.getUserId())

                        SocketManager.getInstance().connect(token)

                        registerFCMToken()
                    } catch (e: Exception) {

                        android.util.Log.e("AuthViewModel", "Failed to fetch user: ${e.message}", e)
                        tokenManager.clearToken()
                        RetrofitClient.setToken(null)
                    }
                }
                _isLoading.value = false
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "checkSavedAuth error: ${e.message}", e)
                _isLoading.value = false
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(username, password)
                )

                android.util.Log.d("AuthViewModel", "Login successful: ${response.user.getUserId()}, ${response.user.username}")
                tokenManager.saveToken(response.token, response.user.getUserId())
                RetrofitClient.setToken(response.token)
                RetrofitClient.setCurrentUserId(response.user.getUserId())
                _user.value = response.user
                android.util.Log.d("AuthViewModel", "User state set: ${_user.value?.id}")

                SocketManager.getInstance().connect(response.token)

                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Login failed: ${e.message}", e)
                _uiState.value = AuthUiState.Error(
                    e.message ?: "Login failed. Please try again."
                )
            }
        }
    }

    fun register(username: String, password: String, displayName: String, email: String? = null) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = RetrofitClient.apiService.register(
                    com.cycb.chat.data.model.RegisterRequest(
                        username = username,
                        password = password,
                        displayName = displayName,
                        email = email
                    )
                )

                tokenManager.saveToken(response.token, response.user.getUserId())
                RetrofitClient.setToken(response.token)
                RetrofitClient.setCurrentUserId(response.user.getUserId())
                _user.value = response.user

                SocketManager.getInstance().connect(response.token)

                registerFCMToken()

                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Registration failed", e)
                val errorMessage = when {
                    e is retrofit2.HttpException -> {
                        try {
                            val errorBody = e.response()?.errorBody()?.string()
                            android.util.Log.e("AuthViewModel", "Error body: $errorBody")

                            val jsonError = org.json.JSONObject(errorBody ?: "{}")
                            jsonError.optString("error", "Registration failed")
                        } catch (parseError: Exception) {
                            "Registration failed: ${e.code()}"
                        }
                    }
                    else -> e.message ?: "Registration failed. Please try again."
                }
                _uiState.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    private fun registerFCMToken() {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                android.util.Log.d("AuthViewModel", "FCM Token: $token")

                val response = RetrofitClient.apiService.registerFCMToken(
                    mapOf("fcmToken" to token)
                )

                if (response.isSuccessful) {
                    android.util.Log.d("AuthViewModel", "FCM token registered successfully")
                } else {
                    android.util.Log.e("AuthViewModel", "Failed to register FCM token: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error registering FCM token", e)
            }
        }
    }

    fun clearError() {
        _uiState.value = AuthUiState.Idle
    }

    fun refreshUser() {
        viewModelScope.launch {
            try {
                android.util.Log.d("AuthViewModel", "Refreshing user data...")
                val user = RetrofitClient.apiService.getCurrentUser()
                android.util.Log.d("AuthViewModel", "User refreshed: ${user.id}, ${user.username}")
                _user.value = user
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Failed to refresh user: ${e.message}", e)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
            RetrofitClient.setToken(null)
            RetrofitClient.setCurrentUserId(null)
            SocketManager.getInstance().disconnect()
            _user.value = null
        }
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
