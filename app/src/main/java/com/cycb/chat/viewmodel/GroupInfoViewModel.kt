package com.cycb.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycb.chat.data.api.RetrofitClient
import com.cycb.chat.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GroupInfoUiState {
    object Loading : GroupInfoUiState()
    data class Success(
        val chatInfo: ChatInfo,
        val members: List<User>
    ) : GroupInfoUiState()
    data class Error(val message: String) : GroupInfoUiState()
}

class GroupInfoViewModel : ViewModel() {
    private val apiService = RetrofitClient.apiService

    private val _uiState = MutableStateFlow<GroupInfoUiState>(GroupInfoUiState.Loading)
    val uiState: StateFlow<GroupInfoUiState> = _uiState.asStateFlow()

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult: StateFlow<String?> = _actionResult.asStateFlow()

    fun loadGroupInfo(chatId: String) {
        viewModelScope.launch {
            _uiState.value = GroupInfoUiState.Loading
            try {
                val response = apiService.getChatMembers(chatId)
                _uiState.value = GroupInfoUiState.Success(
                    chatInfo = response.chat,
                    members = response.members
                )
            } catch (e: Exception) {
                _uiState.value = GroupInfoUiState.Error(
                    e.message ?: "Failed to load group info"
                )
            }
        }
    }

    fun updateGroupInfo(chatId: String, name: String?, description: String?, avatar: String?) {
        viewModelScope.launch {
            try {
                val request = UpdateGroupRequest(name, description, avatar)
                apiService.updateGroupInfo(chatId, request)
                _actionResult.value = "Group info updated successfully"
                loadGroupInfo(chatId)
            } catch (e: Exception) {
                _actionResult.value = "Failed to update group info: ${e.message}"
            }
        }
    }

    fun addMember(chatId: String, userId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.addGroupMember(chatId, AddMemberRequest(userId))
                _actionResult.value = response.message
                loadGroupInfo(chatId)
            } catch (e: Exception) {
                _actionResult.value = "Failed to add member: ${e.message}"
            }
        }
    }

    fun removeMember(chatId: String, memberId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.removeGroupMember(chatId, memberId)
                _actionResult.value = response.message ?: "Member removed successfully"
                loadGroupInfo(chatId)
            } catch (e: Exception) {
                _actionResult.value = "Failed to remove member: ${e.message}"
            }
        }
    }

    fun updateMemberRole(chatId: String, memberId: String, role: String) {
        viewModelScope.launch {
            try {
                val response = apiService.updateMemberRole(chatId, memberId, UpdateRoleRequest(role))
                _actionResult.value = response.message
                loadGroupInfo(chatId)
            } catch (e: Exception) {
                _actionResult.value = "Failed to update role: ${e.message}"
            }
        }
    }

    fun updatePermissions(chatId: String, permissions: GroupPermissions) {
        viewModelScope.launch {
            try {
                val response = apiService.updateGroupPermissions(
                    chatId,
                    UpdatePermissionsRequest(permissions)
                )
                _actionResult.value = response.message
                loadGroupInfo(chatId)
            } catch (e: Exception) {
                _actionResult.value = "Failed to update permissions: ${e.message}"
            }
        }
    }

    fun leaveGroup(chatId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                apiService.leaveChat(chatId)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = GroupInfoUiState.Error(
                    e.message ?: "Failed to leave group"
                )
            }
        }
    }

    fun updateBackground(chatId: String, type: String, value: String) {
        viewModelScope.launch {
            try {
                val request = com.cycb.chat.data.api.UpdateChatBackgroundRequest(
                    type = type,
                    value = value
                )
                val response = apiService.updateChatBackground(chatId, request)
                _actionResult.value = "Background updated successfully"
                loadGroupInfo(chatId)
            } catch (e: Exception) {
                _actionResult.value = "Failed to update background: ${e.message}"
            }
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }
}
