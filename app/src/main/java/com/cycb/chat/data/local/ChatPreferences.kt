package com.cycb.chat.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chat_prefs")

class ChatPreferences(private val context: Context) {

    companion object {
        private val PINNED_CHATS_KEY = stringSetPreferencesKey("pinned_chats")
        private val HIDDEN_CHATS_KEY = stringSetPreferencesKey("hidden_chats")
    }

    val pinnedChatIds: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[PINNED_CHATS_KEY] ?: emptySet()
        }

    val hiddenChatIds: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[HIDDEN_CHATS_KEY] ?: emptySet()
        }

    suspend fun togglePinChat(chatId: String) {
        context.dataStore.edit { preferences ->
            val currentPinned = preferences[PINNED_CHATS_KEY] ?: emptySet()
            if (currentPinned.contains(chatId)) {
                preferences[PINNED_CHATS_KEY] = currentPinned - chatId
            } else {
                preferences[PINNED_CHATS_KEY] = currentPinned + chatId
            }
        }
    }

    suspend fun hideChat(chatId: String) {
        context.dataStore.edit { preferences ->
            val currentHidden = preferences[HIDDEN_CHATS_KEY] ?: emptySet()
            preferences[HIDDEN_CHATS_KEY] = currentHidden + chatId
        }
    }

    suspend fun unhideChat(chatId: String) {
        context.dataStore.edit { preferences ->
            val currentHidden = preferences[HIDDEN_CHATS_KEY] ?: emptySet()
            if (currentHidden.contains(chatId)) {
                preferences[HIDDEN_CHATS_KEY] = currentHidden - chatId
            }
        }
    }
}
