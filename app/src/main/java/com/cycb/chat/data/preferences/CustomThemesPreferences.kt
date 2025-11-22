package com.cycb.chat.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cycb.chat.ui.theme.AppColorTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

private val Context.customThemesDataStore: DataStore<Preferences> by preferencesDataStore(name = "custom_themes")

@Serializable
data class SerializableCustomTheme(
    val name: String,
    val lightPrimary: String,
    val lightSecondary: String,
    val lightTertiary: String,
    val lightBackground: String,
    val lightSurface: String,
    val darkPrimary: String,
    val darkSecondary: String,
    val darkTertiary: String,
    val darkBackground: String,
    val darkSurface: String
)

class CustomThemesPreferences(private val context: Context) {

    companion object {
        val CUSTOM_THEMES_JSON = stringPreferencesKey("custom_themes_json")
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val customThemes: Flow<List<AppColorTheme>> = context.customThemesDataStore.data
        .map { preferences ->
            val themesJson = preferences[CUSTOM_THEMES_JSON] ?: "[]"
            try {
                val serializableThemes = json.decodeFromString<List<SerializableCustomTheme>>(themesJson)
                serializableThemes.map { it.toAppColorTheme() }
            } catch (e: Exception) {
                emptyList()
            }
        }

    suspend fun saveCustomTheme(
        name: String,
        lightPrimary: String,
        lightSecondary: String,
        lightTertiary: String,
        lightBackground: String,
        lightSurface: String,
        darkPrimary: String,
        darkSecondary: String,
        darkTertiary: String,
        darkBackground: String,
        darkSurface: String
    ) {
        context.customThemesDataStore.edit { preferences ->
            val currentJson = preferences[CUSTOM_THEMES_JSON] ?: "[]"
            val currentThemes = try {
                json.decodeFromString<MutableList<SerializableCustomTheme>>(currentJson)
            } catch (e: Exception) {
                mutableListOf()
            }

            currentThemes.removeAll { it.name == name }

            currentThemes.add(
                SerializableCustomTheme(
                    name = name,
                    lightPrimary = lightPrimary,
                    lightSecondary = lightSecondary,
                    lightTertiary = lightTertiary,
                    lightBackground = lightBackground,
                    lightSurface = lightSurface,
                    darkPrimary = darkPrimary,
                    darkSecondary = darkSecondary,
                    darkTertiary = darkTertiary,
                    darkBackground = darkBackground,
                    darkSurface = darkSurface
                )
            )

            preferences[CUSTOM_THEMES_JSON] = json.encodeToString(currentThemes)
        }
    }

    suspend fun deleteCustomTheme(name: String) {
        context.customThemesDataStore.edit { preferences ->
            val currentJson = preferences[CUSTOM_THEMES_JSON] ?: "[]"
            val currentThemes = try {
                json.decodeFromString<MutableList<SerializableCustomTheme>>(currentJson)
            } catch (e: Exception) {
                mutableListOf()
            }

            currentThemes.removeAll { it.name == name }
            preferences[CUSTOM_THEMES_JSON] = json.encodeToString(currentThemes)
        }
    }
}

fun SerializableCustomTheme.toAppColorTheme(): AppColorTheme {
    return AppColorTheme(
        name = name,
        lightScheme = lightColorScheme(
            primary = parseHexColor(lightPrimary),
            secondary = parseHexColor(lightSecondary),
            tertiary = parseHexColor(lightTertiary),
            background = parseHexColor(lightBackground),
            surface = parseHexColor(lightSurface)
        ),
        darkScheme = darkColorScheme(
            primary = parseHexColor(darkPrimary),
            secondary = parseHexColor(darkSecondary),
            tertiary = parseHexColor(darkTertiary),
            background = parseHexColor(darkBackground),
            surface = parseHexColor(darkSurface)
        )
    )
}

private fun parseHexColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        when (cleanHex.length) {
            6 -> Color(android.graphics.Color.parseColor("#$cleanHex"))
            8 -> Color(android.graphics.Color.parseColor("#$cleanHex"))
            else -> Color.Gray
        }
    } catch (e: Exception) {
        Color.Gray
    }
}
