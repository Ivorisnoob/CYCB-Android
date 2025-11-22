package com.cycb.chat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cycb.chat.data.preferences.SettingsPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsPreferences = SettingsPreferences(application)

    val notificationsEnabled = settingsPreferences.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val messagesNotif = settingsPreferences.messagesNotif
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val friendRequestsNotif = settingsPreferences.friendRequestsNotif
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val chatInvitesNotif = settingsPreferences.chatInvitesNotif
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val soundEnabled = settingsPreferences.soundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val vibrationEnabled = settingsPreferences.vibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val darkMode = settingsPreferences.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val dynamicColors = settingsPreferences.dynamicColors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val compactMode = settingsPreferences.compactMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val readReceipts = settingsPreferences.readReceipts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val typingIndicator = settingsPreferences.typingIndicator
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val lastSeenVisible = settingsPreferences.lastSeenVisible
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val profilePhotoVisible = settingsPreferences.profilePhotoVisible
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoDownloadMedia = settingsPreferences.autoDownloadMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoPlayGifs = settingsPreferences.autoPlayGifs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val enterToSend = settingsPreferences.enterToSend
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val selectedTheme = settingsPreferences.selectedTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Electric Sunset")

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setNotificationsEnabled(enabled)
        }
    }

    fun setMessagesNotif(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setMessagesNotif(enabled)
        }
    }

    fun setFriendRequestsNotif(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setFriendRequestsNotif(enabled)
        }
    }

    fun setChatInvitesNotif(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setChatInvitesNotif(enabled)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setSoundEnabled(enabled)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setVibrationEnabled(enabled)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setDarkMode(enabled)
        }
    }

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setDynamicColors(enabled)
        }
    }

    fun setCompactMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setCompactMode(enabled)
        }
    }

    fun setReadReceipts(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setReadReceipts(enabled)
        }
    }

    fun setTypingIndicator(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setTypingIndicator(enabled)
        }
    }

    fun setLastSeenVisible(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setLastSeenVisible(enabled)
        }
    }

    fun setProfilePhotoVisible(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setProfilePhotoVisible(enabled)
        }
    }

    fun setAutoDownloadMedia(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setAutoDownloadMedia(enabled)
        }
    }

    fun setAutoPlayGifs(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setAutoPlayGifs(enabled)
        }
    }

    fun setEnterToSend(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setEnterToSend(enabled)
        }
    }

    fun setSelectedTheme(themeName: String) {
        viewModelScope.launch {
            settingsPreferences.setSelectedTheme(themeName)
        }
    }

    fun updatePassword(newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = mapOf(
                    "newPassword" to newPassword
                )
                val response = com.cycb.chat.data.api.RetrofitClient.apiService.updatePassword(request)
                if (response.success) {
                    onSuccess()
                } else {
                    onError(response.message ?: "Failed to update password")
                }
            } catch (e: Exception) {
                onError(e.message ?: "An error occurred")
            }
        }
    }

    fun checkForUpdates(context: android.content.Context, onResult: (com.cycb.chat.data.model.AppUpdateInfo?) -> Unit) {
        viewModelScope.launch {
            val updateInfo = com.cycb.chat.utils.UpdateManager.checkForUpdates(context)
            onResult(updateInfo)
        }
    }
}
