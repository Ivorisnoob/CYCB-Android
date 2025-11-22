package com.cycb.chat.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycb.chat.data.model.User
import com.cycb.chat.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class UpdateProfileState {
    object Idle : UpdateProfileState()
    object Loading : UpdateProfileState()
    object Success : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}

data class ProfileRelationship(
    val isFriend: Boolean = false,
    val hasSentRequest: Boolean = false,
    val hasReceivedRequest: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateState: StateFlow<UpdateProfileState> = _updateState.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    private val _relationship = MutableStateFlow(ProfileRelationship())
    val relationship: StateFlow<ProfileRelationship> = _relationship.asStateFlow()

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            try {
                val response = RetrofitClient.apiService.getUserProfile(userId)

                val profileData = response.user
                val user = User(
                    id = profileData.id ?: userId,
                    username = profileData.username,
                    displayName = profileData.displayName,
                    email = profileData.email,
                    profilePicture = profileData.profilePicture,
                    bio = profileData.bio,
                    isOnline = profileData.isOnline
                )
                android.util.Log.d("ProfileViewModel", "User loaded successfully: ${user.id}")
                _uiState.value = ProfileUiState.Success(user)

                loadRelationship(userId)
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Failed to load user: ${e.message}", e)
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadRelationship(userId: String) {
        viewModelScope.launch {
            try {
                val currentUser = RetrofitClient.apiService.getCurrentUser()
                _relationship.value = ProfileRelationship(
                    isFriend = currentUser.friends.contains(userId),
                    hasSentRequest = currentUser.friendRequestsSent.contains(userId),
                    hasReceivedRequest = currentUser.friendRequestsReceived.contains(userId)
                )
                android.util.Log.d("ProfileViewModel", "Relationship loaded: ${_relationship.value}")
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Failed to load relationship", e)
            }
        }
    }

    fun updateProfile(
        displayName: String,
        username: String,
        bio: String,
        email: String,
        profilePictureUrl: String? = null
    ) {
        viewModelScope.launch {
            _updateState.value = UpdateProfileState.Loading

            try {
                val updateData = mutableMapOf(
                    "displayName" to displayName,
                    "username" to username,
                    "bio" to bio,
                    "email" to email
                )

                profilePictureUrl?.let {
                    updateData["profilePicture"] = it
                }

                android.util.Log.d("ProfileViewModel", "Updating profile: $updateData")
                val response = RetrofitClient.apiService.updateProfile(updateData)
                android.util.Log.d("ProfileViewModel", "Profile updated successfully: ${response.user.id}")

                _uiState.value = ProfileUiState.Success(response.user)
                _updateState.value = UpdateProfileState.Success
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Profile update failed: ${e.message}", e)
                _updateState.value = UpdateProfileState.Error(
                    e.message ?: "Unknown error"
                )
            }
        }
    }

    private val _uploadedProfilePictureUrl = MutableStateFlow<String?>(null)
    val uploadedProfilePictureUrl: StateFlow<String?> = _uploadedProfilePictureUrl.asStateFlow()

    fun uploadProfilePicture(imageFile: File) {
        viewModelScope.launch {
            _uploadProgress.value = 0f

            try {
                android.util.Log.d("ProfileViewModel", "Uploading profile picture: ${imageFile.name}")
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                val uploadResponse = RetrofitClient.apiService.uploadProfilePicture(body)
                android.util.Log.d("ProfileViewModel", "Profile picture uploaded: ${uploadResponse.url}")

                _uploadedProfilePictureUrl.value = uploadResponse.url
                _uploadProgress.value = 1f
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Profile picture upload failed: ${e.message}", e)
                _updateState.value = UpdateProfileState.Error(
                    e.message ?: "Failed to upload image"
                )
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateProfileState.Idle
    }

    fun blockUser(userId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.blockUser(userId)
                onSuccess()
            } catch (e: Exception) {
                _updateState.value = UpdateProfileState.Error(
                    e.message ?: "Failed to block user"
                )
            }
        }
    }

    fun reportUser(userId: String, reason: String, description: String? = null, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val request = com.cycb.chat.data.model.ReportUserRequest(reason, description)
                RetrofitClient.apiService.reportUser(userId, request)
                onSuccess()
            } catch (e: Exception) {
                _updateState.value = UpdateProfileState.Error(
                    e.message ?: "Failed to report user"
                )
            }
        }
    }
}
