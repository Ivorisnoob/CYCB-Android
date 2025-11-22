package com.cycb.chat.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsPreferences(private val context: Context) {

    companion object {

        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val MESSAGES_NOTIF = booleanPreferencesKey("messages_notif")
        val FRIEND_REQUESTS_NOTIF = booleanPreferencesKey("friend_requests_notif")
        val CHAT_INVITES_NOTIF = booleanPreferencesKey("chat_invites_notif")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")

        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val COMPACT_MODE = booleanPreferencesKey("compact_mode")
        val SELECTED_THEME = androidx.datastore.preferences.core.stringPreferencesKey("selected_theme")

        val READ_RECEIPTS = booleanPreferencesKey("read_receipts")
        val TYPING_INDICATOR = booleanPreferencesKey("typing_indicator")
        val LAST_SEEN_VISIBLE = booleanPreferencesKey("last_seen_visible")
        val PROFILE_PHOTO_VISIBLE = booleanPreferencesKey("profile_photo_visible")

        val AUTO_DOWNLOAD_MEDIA = booleanPreferencesKey("auto_download_media")
        val AUTO_PLAY_GIFS = booleanPreferencesKey("auto_play_gifs")
        val ENTER_TO_SEND = booleanPreferencesKey("enter_to_send")
    }

    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[NOTIFICATIONS_ENABLED] ?: true }

    val messagesNotif: Flow<Boolean> = context.settingsDataStore.data
        .map { it[MESSAGES_NOTIF] ?: true }

    val friendRequestsNotif: Flow<Boolean> = context.settingsDataStore.data
        .map { it[FRIEND_REQUESTS_NOTIF] ?: true }

    val chatInvitesNotif: Flow<Boolean> = context.settingsDataStore.data
        .map { it[CHAT_INVITES_NOTIF] ?: true }

    val soundEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[SOUND_ENABLED] ?: true }

    val vibrationEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[VIBRATION_ENABLED] ?: true }

    val darkMode: Flow<Boolean> = context.settingsDataStore.data
        .map { it[DARK_MODE] ?: false }

    val dynamicColors: Flow<Boolean> = context.settingsDataStore.data
        .map { it[DYNAMIC_COLORS] ?: false }

    val compactMode: Flow<Boolean> = context.settingsDataStore.data
        .map { it[COMPACT_MODE] ?: false }

    val readReceipts: Flow<Boolean> = context.settingsDataStore.data
        .map { it[READ_RECEIPTS] ?: true }

    val typingIndicator: Flow<Boolean> = context.settingsDataStore.data
        .map { it[TYPING_INDICATOR] ?: true }

    val lastSeenVisible: Flow<Boolean> = context.settingsDataStore.data
        .map { it[LAST_SEEN_VISIBLE] ?: true }

    val profilePhotoVisible: Flow<Boolean> = context.settingsDataStore.data
        .map { it[PROFILE_PHOTO_VISIBLE] ?: true }

    val autoDownloadMedia: Flow<Boolean> = context.settingsDataStore.data
        .map { it[AUTO_DOWNLOAD_MEDIA] ?: true }

    val autoPlayGifs: Flow<Boolean> = context.settingsDataStore.data
        .map { it[AUTO_PLAY_GIFS] ?: true }

    val enterToSend: Flow<Boolean> = context.settingsDataStore.data
        .map { it[ENTER_TO_SEND] ?: false }

    val selectedTheme: Flow<String> = context.settingsDataStore.data
        .map { it[SELECTED_THEME] ?: "Electric Sunset" }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setMessagesNotif(enabled: Boolean) {
        context.settingsDataStore.edit { it[MESSAGES_NOTIF] = enabled }
    }

    suspend fun setFriendRequestsNotif(enabled: Boolean) {
        context.settingsDataStore.edit { it[FRIEND_REQUESTS_NOTIF] = enabled }
    }

    suspend fun setChatInvitesNotif(enabled: Boolean) {
        context.settingsDataStore.edit { it[CHAT_INVITES_NOTIF] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[VIBRATION_ENABLED] = enabled }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.settingsDataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        context.settingsDataStore.edit { it[DYNAMIC_COLORS] = enabled }
    }

    suspend fun setCompactMode(enabled: Boolean) {
        context.settingsDataStore.edit { it[COMPACT_MODE] = enabled }
    }

    suspend fun setReadReceipts(enabled: Boolean) {
        context.settingsDataStore.edit { it[READ_RECEIPTS] = enabled }
    }

    suspend fun setTypingIndicator(enabled: Boolean) {
        context.settingsDataStore.edit { it[TYPING_INDICATOR] = enabled }
    }

    suspend fun setLastSeenVisible(enabled: Boolean) {
        context.settingsDataStore.edit { it[LAST_SEEN_VISIBLE] = enabled }
    }

    suspend fun setProfilePhotoVisible(enabled: Boolean) {
        context.settingsDataStore.edit { it[PROFILE_PHOTO_VISIBLE] = enabled }
    }

    suspend fun setAutoDownloadMedia(enabled: Boolean) {
        context.settingsDataStore.edit { it[AUTO_DOWNLOAD_MEDIA] = enabled }
    }

    suspend fun setAutoPlayGifs(enabled: Boolean) {
        context.settingsDataStore.edit { it[AUTO_PLAY_GIFS] = enabled }
    }

    suspend fun setEnterToSend(enabled: Boolean) {
        context.settingsDataStore.edit { it[ENTER_TO_SEND] = enabled }
    }

    suspend fun setSelectedTheme(themeName: String) {
        context.settingsDataStore.edit { it[SELECTED_THEME] = themeName }
    }
}
