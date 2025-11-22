package com.cycb.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycb.chat.data.api.RetrofitClient
import com.cycb.chat.data.model.CreateChatRequest
import com.cycb.chat.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateGroupUiState(
    val groupName: String = "",
    val groupDescription: String = "",
    val groupAvatar: String? = null,
    val availableFriends: List<User> = emptyList(),
    val selectedMembers: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isPublic: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CreateGroupViewModel : ViewModel() {
    private val apiService = RetrofitClient.apiService

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    init {
        loadFriends()
    }

    private fun loadFriends() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = apiService.getFriends()
                _uiState.value = _uiState.value.copy(
                    availableFriends = response.friends,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load friends",
                    isLoading = false
                )
            }
        }
    }

    fun updateGroupName(name: String) {
        if (name.length <= 50) {
            _uiState.value = _uiState.value.copy(groupName = name)
        }
    }

    fun updateGroupDescription(description: String) {
        if (description.length <= 200) {
            _uiState.value = _uiState.value.copy(groupDescription = description)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun togglePublicPrivate() {
        _uiState.value = _uiState.value.copy(isPublic = !_uiState.value.isPublic)
    }

    fun toggleMemberSelection(userId: String) {
        val currentSelection = _uiState.value.selectedMembers
        val newSelection = if (currentSelection.contains(userId)) {
            currentSelection - userId
        } else {
            if (currentSelection.size < 99) {
                currentSelection + userId
            } else {
                currentSelection
            }
        }
        _uiState.value = _uiState.value.copy(selectedMembers = newSelection)
    }

    fun removeMember(userId: String) {
        _uiState.value = _uiState.value.copy(
            selectedMembers = _uiState.value.selectedMembers - userId
        )
    }

    fun updateGroupAvatar(uri: android.net.Uri?) {
        _uiState.value = _uiState.value.copy(groupAvatar = uri?.toString())
    }

    fun isFormValid(): Boolean {
        val state = _uiState.value

        return if (state.isPublic) {
            state.groupName.length >= 3
        } else {
            state.groupName.length >= 3 && state.selectedMembers.size >= 2
        }
    }

    fun createGroup(context: android.content.Context, onSuccess: (String) -> Unit) {
        if (!isFormValid()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {

                var avatarUrl: String? = null
                if (_uiState.value.groupAvatar != null) {
                    try {
                        val uri = android.net.Uri.parse(_uiState.value.groupAvatar)
                        val result = com.cycb.chat.utils.ImageUploadHelper.uploadToBackend(
                            context = context,
                            imageUri = uri
                        )
                        result.onSuccess { url ->
                            avatarUrl = url
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("CreateGroupViewModel", "Avatar upload failed", e)

                    }
                }

                val request = CreateChatRequest(
                    type = "group",
                    name = _uiState.value.groupName,
                    description = _uiState.value.groupDescription.ifBlank { null },
                    isPublic = _uiState.value.isPublic,
                    memberIds = _uiState.value.selectedMembers.toList(),
                    members = _uiState.value.selectedMembers.toList()
                )

                android.util.Log.d("CreateGroupViewModel", "Creating group with request: $request")
                val response = apiService.createChat(request)
                android.util.Log.d("CreateGroupViewModel", "Response: $response")

                if (avatarUrl != null && response.success) {
                    try {
                        apiService.updateGroupInfo(
                            chatId = response.chat.getChatId(),
                            request = com.cycb.chat.data.model.UpdateGroupRequest(
                                avatar = avatarUrl
                            )
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("CreateGroupViewModel", "Failed to update avatar", e)

                    }
                }

                _uiState.value = _uiState.value.copy(isLoading = false)

                val chatId = response.chat.getChatId()
                if (response.success && chatId.isNotEmpty()) {
                    android.util.Log.d("CreateGroupViewModel", "Group created successfully: $chatId")

                    _uiState.value = CreateGroupUiState(
                        availableFriends = _uiState.value.availableFriends
                    )

                    onSuccess(chatId)
                } else {
                    android.util.Log.e("CreateGroupViewModel", "Failed to create group: ${response}")
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to create group"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("CreateGroupViewModel", "Exception creating group", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to create group",
                    isLoading = false
                )
            }
        }
    }
}
