package com.cycb.chat.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class AppColorTheme(
    val name: String,
    val lightScheme: androidx.compose.material3.ColorScheme,
    val darkScheme: androidx.compose.material3.ColorScheme
)

private val ElectricSunsetLight = lightColorScheme(
    primary = Color(0xFFFF6B35),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFFF7931E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDB3),
    onSecondaryContainer = Color(0xFF2B1700),
    tertiary = Color(0xFFFDC830),
    onTertiary = Color(0xFF3C2F00),
    tertiaryContainer = Color(0xFFFFF0C2),
    onTertiaryContainer = Color(0xFF1C1600),
    background = Color(0xFFFFF8F0),
    onBackground = Color(0xFF201A17),
    surface = Color(0xFFFFF8F0),
    onSurface = Color(0xFF201A17)
)

private val ElectricSunsetDark = darkColorScheme(
    primary = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF690005),
    primaryContainer = Color(0xFF93000A),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFFFB951),
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF6A3C00),
    onSecondaryContainer = Color(0xFFFFDDB3),
    tertiary = Color(0xFFFFE082),
    onTertiary = Color(0xFF3C2F00),
    tertiaryContainer = Color(0xFF564500),
    onTertiaryContainer = Color(0xFFFFF0C2),
    background = Color(0xFF201A17),
    onBackground = Color(0xFFEDE0DB),
    surface = Color(0xFF201A17),
    onSurface = Color(0xFFEDE0DB)
)

private val OceanBreezeLight = lightColorScheme(
    primary = Color(0xFF0077B6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCAE6FF),
    onPrimaryContainer = Color(0xFF001E2F),
    secondary = Color(0xFF00B4D8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBEEAFF),
    onSecondaryContainer = Color(0xFF001F29),
    tertiary = Color(0xFF90E0EF),
    onTertiary = Color(0xFF003544),
    tertiaryContainer = Color(0xFFB8EEFF),
    onTertiaryContainer = Color(0xFF001F29),
    background = Color(0xFFF0F9FF),
    onBackground = Color(0xFF001F29),
    surface = Color(0xFFF0F9FF),
    onSurface = Color(0xFF001F29)
)

private val OceanBreezeDark = darkColorScheme(
    primary = Color(0xFF8CCDFF),
    onPrimary = Color(0xFF00344E),
    primaryContainer = Color(0xFF004C6F),
    onPrimaryContainer = Color(0xFFCAE6FF),
    secondary = Color(0xFF5DD5FC),
    onSecondary = Color(0xFF003544),
    secondaryContainer = Color(0xFF004D61),
    onSecondaryContainer = Color(0xFFBEEAFF),
    tertiary = Color(0xFF5DD5FC),
    onTertiary = Color(0xFF003544),
    tertiaryContainer = Color(0xFF004D61),
    onTertiaryContainer = Color(0xFFB8EEFF),
    background = Color(0xFF001F29),
    onBackground = Color(0xFFBFE9FF),
    surface = Color(0xFF001F29),
    onSurface = Color(0xFFBFE9FF)
)

private val ForestGreenLight = lightColorScheme(
    primary = Color(0xFF2D6A4F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7E4C7),
    onPrimaryContainer = Color(0xFF002114),
    secondary = Color(0xFF52B788),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8F3DC),
    onSecondaryContainer = Color(0xFF002114),
    tertiary = Color(0xFF95D5B2),
    onTertiary = Color(0xFF003826),
    tertiaryContainer = Color(0xFFD8F3DC),
    onTertiaryContainer = Color(0xFF002114),
    background = Color(0xFFF1FAF5),
    onBackground = Color(0xFF002114),
    surface = Color(0xFFF1FAF5),
    onSurface = Color(0xFF002114)
)

private val ForestGreenDark = darkColorScheme(
    primary = Color(0xFF9BCFAD),
    onPrimary = Color(0xFF003826),
    primaryContainer = Color(0xFF005138),
    onPrimaryContainer = Color(0xFFB7E4C7),
    secondary = Color(0xFF9BCFAD),
    onSecondary = Color(0xFF003826),
    secondaryContainer = Color(0xFF005138),
    onSecondaryContainer = Color(0xFFD8F3DC),
    tertiary = Color(0xFF9BCFAD),
    onTertiary = Color(0xFF003826),
    tertiaryContainer = Color(0xFF005138),
    onTertiaryContainer = Color(0xFFD8F3DC),
    background = Color(0xFF002114),
    onBackground = Color(0xFFB7E4C7),
    surface = Color(0xFF002114),
    onSurface = Color(0xFFB7E4C7)
)

private val RoyalPurpleLight = lightColorScheme(
    primary = Color(0xFF6A4C93),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF9D4EDD),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF2DAFF),
    onSecondaryContainer = Color(0xFF31005D),
    tertiary = Color(0xFFC77DFF),
    onTertiary = Color(0xFF3E0066),
    tertiaryContainer = Color(0xFFF6EDFF),
    onTertiaryContainer = Color(0xFF1F0033),
    background = Color(0xFFF8F4FF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFF8F4FF),
    onSurface = Color(0xFF1C1B1F)
)

private val RoyalPurpleDark = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFE0B6FF),
    onSecondary = Color(0xFF4A0080),
    secondaryContainer = Color(0xFF6A1B9A),
    onSecondaryContainer = Color(0xFFF2DAFF),
    tertiary = Color(0xFFE0B6FF),
    onTertiary = Color(0xFF5A00A3),
    tertiaryContainer = Color(0xFF7B00D6),
    onTertiaryContainer = Color(0xFFF6EDFF),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5)
)

private val CherryBlossomLight = lightColorScheme(
    primary = Color(0xFFE63946),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFFF1FAEE),
    onSecondary = Color(0xFF001F29),
    secondaryContainer = Color(0xFFE8F5F2),
    onSecondaryContainer = Color(0xFF001F29),
    tertiary = Color(0xFFA8DADC),
    onTertiary = Color(0xFF003544),
    tertiaryContainer = Color(0xFFD0F0F2),
    onTertiaryContainer = Color(0xFF001F29),
    background = Color(0xFFFFF5F7),
    onBackground = Color(0xFF201A1B),
    surface = Color(0xFFFFF5F7),
    onSurface = Color(0xFF201A1B)
)

private val CherryBlossomDark = darkColorScheme(
    primary = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF690005),
    primaryContainer = Color(0xFF93000A),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFCCE5E2),
    onSecondary = Color(0xFF003544),
    secondaryContainer = Color(0xFF1E4D5C),
    onSecondaryContainer = Color(0xFFE8F5F2),
    tertiary = Color(0xFF8CCDFF),
    onTertiary = Color(0xFF00344E),
    tertiaryContainer = Color(0xFF004C6F),
    onTertiaryContainer = Color(0xFFD0F0F2),
    background = Color(0xFF201A1B),
    onBackground = Color(0xFFECE0E1),
    surface = Color(0xFF201A1B),
    onSurface = Color(0xFFECE0E1)
)

private val MidnightBlueLight = lightColorScheme(
    primary = Color(0xFF1A1A2E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E2FF),
    onPrimaryContainer = Color(0xFF001A41),
    secondary = Color(0xFF16213E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8E2FF),
    onSecondaryContainer = Color(0xFF001A41),
    tertiary = Color(0xFF0F3460),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD8E2FF),
    onTertiaryContainer = Color(0xFF001A41),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFF5F5F5),
    onSurface = Color(0xFF1A1C1E)
)

private val MidnightBlueDark = darkColorScheme(
    primary = Color(0xFFADC6FF),
    onPrimary = Color(0xFF002E69),
    primaryContainer = Color(0xFF004494),
    onPrimaryContainer = Color(0xFFD8E2FF),
    secondary = Color(0xFFADC6FF),
    onSecondary = Color(0xFF002E69),
    secondaryContainer = Color(0xFF004494),
    onSecondaryContainer = Color(0xFFD8E2FF),
    tertiary = Color(0xFFADC6FF),
    onTertiary = Color(0xFF002E69),
    tertiaryContainer = Color(0xFF004494),
    onTertiaryContainer = Color(0xFFD8E2FF),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6)
)

private val SunsetOrangeLight = lightColorScheme(
    primary = Color(0xFFFF6F00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDCC5),
    onPrimaryContainer = Color(0xFF2A1800),
    secondary = Color(0xFFFF8F00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = Color(0xFF2A1800),
    tertiary = Color(0xFFFFAB00),
    onTertiary = Color(0xFF3C2F00),
    tertiaryContainer = Color(0xFFFFE8B2),
    onTertiaryContainer = Color(0xFF1C1600),
    background = Color(0xFFFFF8E1),
    onBackground = Color(0xFF201A17),
    surface = Color(0xFFFFF8E1),
    onSurface = Color(0xFF201A17)
)

private val SunsetOrangeDark = darkColorScheme(
    primary = Color(0xFFFFB77C),
    onPrimary = Color(0xFF4A2800),
    primaryContainer = Color(0xFF6A3C00),
    onPrimaryContainer = Color(0xFFFFDCC5),
    secondary = Color(0xFFFFCC80),
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF6A3C00),
    onSecondaryContainer = Color(0xFFFFE0B2),
    tertiary = Color(0xFFFFE082),
    onTertiary = Color(0xFF3C2F00),
    tertiaryContainer = Color(0xFF564500),
    onTertiaryContainer = Color(0xFFFFE8B2),
    background = Color(0xFF201A17),
    onBackground = Color(0xFFEDE0DB),
    surface = Color(0xFF201A17),
    onSurface = Color(0xFFEDE0DB)
)

private val MintFreshLight = lightColorScheme(
    primary = Color(0xFF06FFA5),
    onPrimary = Color(0xFF003826),
    primaryContainer = Color(0xFFB7FFE5),
    onPrimaryContainer = Color(0xFF002114),
    secondary = Color(0xFF00D9FF),
    onSecondary = Color(0xFF003544),
    secondaryContainer = Color(0xFFB8EEFF),
    onSecondaryContainer = Color(0xFF001F29),
    tertiary = Color(0xFF7FFFD4),
    onTertiary = Color(0xFF003826),
    tertiaryContainer = Color(0xFFD0FFF0),
    onTertiaryContainer = Color(0xFF002114),
    background = Color(0xFFF0FFF4),
    onBackground = Color(0xFF002114),
    surface = Color(0xFFF0FFF4),
    onSurface = Color(0xFF002114)
)

private val MintFreshDark = darkColorScheme(
    primary = Color(0xFF5DFFCA),
    onPrimary = Color(0xFF003826),
    primaryContainer = Color(0xFF005138),
    onPrimaryContainer = Color(0xFFB7FFE5),
    secondary = Color(0xFF5DD5FC),
    onSecondary = Color(0xFF003544),
    secondaryContainer = Color(0xFF004D61),
    onSecondaryContainer = Color(0xFFB8EEFF),
    tertiary = Color(0xFF5DFFCA),
    onTertiary = Color(0xFF003826),
    tertiaryContainer = Color(0xFF005138),
    onTertiaryContainer = Color(0xFFD0FFF0),
    background = Color(0xFF002114),
    onBackground = Color(0xFFB7FFE5),
    surface = Color(0xFF002114),
    onSurface = Color(0xFFB7FFE5)
)

private val RoseGoldLight = lightColorScheme(
    primary = Color(0xFFB76E79),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E2),
    onPrimaryContainer = Color(0xFF3E001D),
    secondary = Color(0xFFE8B4B8),
    onSecondary = Color(0xFF4A0023),
    secondaryContainer = Color(0xFFFFE0E5),
    onSecondaryContainer = Color(0xFF31001D),
    tertiary = Color(0xFFF4DFE0),
    onTertiary = Color(0xFF3E001D),
    tertiaryContainer = Color(0xFFFFF0F2),
    onTertiaryContainer = Color(0xFF1F0010),
    background = Color(0xFFFFF5F7),
    onBackground = Color(0xFF201A1B),
    surface = Color(0xFFFFF5F7),
    onSurface = Color(0xFF201A1B)
)

private val RoseGoldDark = darkColorScheme(
    primary = Color(0xFFFFB1C1),
    onPrimary = Color(0xFF5E1133),
    primaryContainer = Color(0xFF7B2949),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = Color(0xFFFFB1C1),
    onSecondary = Color(0xFF5E1133),
    secondaryContainer = Color(0xFF7B2949),
    onSecondaryContainer = Color(0xFFFFE0E5),
    tertiary = Color(0xFFFFD9E2),
    onTertiary = Color(0xFF5E1133),
    tertiaryContainer = Color(0xFF7B2949),
    onTertiaryContainer = Color(0xFFFFF0F2),
    background = Color(0xFF201A1B),
    onBackground = Color(0xFFECE0E1),
    surface = Color(0xFF201A1B),
    onSurface = Color(0xFFECE0E1)
)

private val CyberNeonLight = lightColorScheme(
    primary = Color(0xFFFF006E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E2),
    onPrimaryContainer = Color(0xFF3E001D),
    secondary = Color(0xFF8338EC),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF2DAFF),
    onSecondaryContainer = Color(0xFF31005D),
    tertiary = Color(0xFF3A86FF),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD8E2FF),
    onTertiaryContainer = Color(0xFF001A41),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFF5F5F5),
    onSurface = Color(0xFF1A1C1E)
)

private val CyberNeonDark = darkColorScheme(
    primary = Color(0xFFFF6FA3),
    onPrimary = Color(0xFF5E1133),
    primaryContainer = Color(0xFF7B2949),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = Color(0xFFE0B6FF),
    onSecondary = Color(0xFF4A0080),
    secondaryContainer = Color(0xFF6A1B9A),
    onSecondaryContainer = Color(0xFFF2DAFF),
    tertiary = Color(0xFFADC6FF),
    onTertiary = Color(0xFF002E69),
    tertiaryContainer = Color(0xFF004494),
    onTertiaryContainer = Color(0xFFD8E2FF),
    background = Color(0xFF0A0A0A),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF0A0A0A),
    onSurface = Color(0xFFE2E2E6)
)

private val AutumnLeavesLight = lightColorScheme(
    primary = Color(0xFFD62828),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFFF77F00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDB3),
    onSecondaryContainer = Color(0xFF2B1700),
    tertiary = Color(0xFFFCAB10),
    onTertiary = Color(0xFF3C2F00),
    tertiaryContainer = Color(0xFFFFE8B2),
    onTertiaryContainer = Color(0xFF1C1600),
    background = Color(0xFFFFF8F0),
    onBackground = Color(0xFF201A17),
    surface = Color(0xFFFFF8F0),
    onSurface = Color(0xFF201A17)
)

private val AutumnLeavesDark = darkColorScheme(
    primary = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF690005),
    primaryContainer = Color(0xFF93000A),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFFFB951),
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF6A3C00),
    onSecondaryContainer = Color(0xFFFFDDB3),
    tertiary = Color(0xFFFFE082),
    onTertiary = Color(0xFF3C2F00),
    tertiaryContainer = Color(0xFF564500),
    onTertiaryContainer = Color(0xFFFFE8B2),
    background = Color(0xFF201A17),
    onBackground = Color(0xFFEDE0DB),
    surface = Color(0xFF201A17),
    onSurface = Color(0xFFEDE0DB)
)

private val ArcticIceLight = lightColorScheme(
    primary = Color(0xFF4CC9F0),
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFFB8EEFF),
    onPrimaryContainer = Color(0xFF001F29),
    secondary = Color(0xFF4895EF),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8E2FF),
    onSecondaryContainer = Color(0xFF001A41),
    tertiary = Color(0xFF4361EE),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD8E2FF),
    onTertiaryContainer = Color(0xFF001A41),
    background = Color(0xFFF0F8FF),
    onBackground = Color(0xFF001F29),
    surface = Color(0xFFF0F8FF),
    onSurface = Color(0xFF001F29)
)

private val ArcticIceDark = darkColorScheme(
    primary = Color(0xFF5DD5FC),
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFB8EEFF),
    secondary = Color(0xFFADC6FF),
    onSecondary = Color(0xFF002E69),
    secondaryContainer = Color(0xFF004494),
    onSecondaryContainer = Color(0xFFD8E2FF),
    tertiary = Color(0xFFADC6FF),
    onTertiary = Color(0xFF002E69),
    tertiaryContainer = Color(0xFF004494),
    onTertiaryContainer = Color(0xFFD8E2FF),
    background = Color(0xFF001F29),
    onBackground = Color(0xFFBFE9FF),
    surface = Color(0xFF001F29),
    onSurface = Color(0xFFBFE9FF)
)

private val ChristmasMagicLight = lightColorScheme(
    primary = Color(0xFFCC0000),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE6E6),
    onPrimaryContainer = Color(0xFF660000),
    secondary = Color(0xFF00AA00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFFCC),
    onSecondaryContainer = Color(0xFF004400),
    tertiary = Color(0xFFFFD700),
    onTertiary = Color(0xFF4A3800),
    tertiaryContainer = Color(0xFFFFF8DC),
    onTertiaryContainer = Color(0xFF2A1F00),
    background = Color(0xFFFFF5F0),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFAF5),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFFFEEE6),
    onSurfaceVariant = Color(0xFF4A4A4A)
)

private val ChristmasMagicDark = darkColorScheme(
    primary = Color(0xFFFF5555),
    onPrimary = Color(0xFF660000),
    primaryContainer = Color(0xFF990000),
    onPrimaryContainer = Color(0xFFFFCCCC),
    secondary = Color(0xFF66FF66),
    onSecondary = Color(0xFF003300),
    secondaryContainer = Color(0xFF006600),
    onSecondaryContainer = Color(0xFFCCFFCC),
    tertiary = Color(0xFFFFD700),
    onTertiary = Color(0xFF4A3800),
    tertiaryContainer = Color(0xFF7A5F00),
    onTertiaryContainer = Color(0xFFFFF8DC),
    background = Color(0xFF0D1A0D),
    onBackground = Color(0xFFFFE6CC),
    surface = Color(0xFF1A2E1A),
    onSurface = Color(0xFFFFE6CC),
    surfaceVariant = Color(0xFF2A3A2A),
    onSurfaceVariant = Color(0xFFFFCCCC)
)

private val WinterWonderlandLight = lightColorScheme(
    primary = Color(0xFF00BFFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F7FF),
    onPrimaryContainer = Color(0xFF003D5C),
    secondary = Color(0xFFADD8E6),
    onSecondary = Color(0xFF003D5C),
    secondaryContainer = Color(0xFFF0F8FF),
    onSecondaryContainer = Color(0xFF001F3D),
    tertiary = Color(0xFFB0E0E6),
    onTertiary = Color(0xFF003D5C),
    tertiaryContainer = Color(0xFFE6F7FF),
    onTertiaryContainer = Color(0xFF001F29),
    background = Color(0xFFFAFDFF),
    onBackground = Color(0xFF001F29),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF001F29),
    surfaceVariant = Color(0xFFF0F8FF),
    onSurfaceVariant = Color(0xFF003D5C)
)

private val WinterWonderlandDark = darkColorScheme(
    primary = Color(0xFF66D9FF),
    onPrimary = Color(0xFF003D5C),
    primaryContainer = Color(0xFF005580),
    onPrimaryContainer = Color(0xFFCCF0FF),
    secondary = Color(0xFF99E6FF),
    onSecondary = Color(0xFF003D5C),
    secondaryContainer = Color(0xFF004D73),
    onSecondaryContainer = Color(0xFFE0F7FF),
    tertiary = Color(0xFFCCF0FF),
    onTertiary = Color(0xFF003D5C),
    tertiaryContainer = Color(0xFF005580),
    onTertiaryContainer = Color(0xFFF0F8FF),
    background = Color(0xFF001F33),
    onBackground = Color(0xFFE0F7FF),
    surface = Color(0xFF002A47),
    onSurface = Color(0xFFE0F7FF),
    surfaceVariant = Color(0xFF003D5C),
    onSurfaceVariant = Color(0xFFCCF0FF)
)

private val ValentinesLoveLight = lightColorScheme(
    primary = Color(0xFFFF1493),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0F0),
    onPrimaryContainer = Color(0xFF660033),
    secondary = Color(0xFFFF69B4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD6E8),
    onSecondaryContainer = Color(0xFF66003D),
    tertiary = Color(0xFFFFB6C1),
    onTertiary = Color(0xFF66003D),
    tertiaryContainer = Color(0xFFFFE6EE),
    onTertiaryContainer = Color(0xFF330020),
    background = Color(0xFFFFF0F5),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFF5F8),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFFFE0EC),
    onSurfaceVariant = Color(0xFF4A1A33)
)

private val ValentinesLoveDark = darkColorScheme(
    primary = Color(0xFFFF69B4),
    onPrimary = Color(0xFF660033),
    primaryContainer = Color(0xFF99004D),
    onPrimaryContainer = Color(0xFFFFCCE0),
    secondary = Color(0xFFFF99CC),
    onSecondary = Color(0xFF660033),
    secondaryContainer = Color(0xFF99004D),
    onSecondaryContainer = Color(0xFFFFE0EC),
    tertiary = Color(0xFFFFB3D9),
    onTertiary = Color(0xFF66003D),
    tertiaryContainer = Color(0xFF8C0040),
    onTertiaryContainer = Color(0xFFFFE6F0),
    background = Color(0xFF1A0F14),
    onBackground = Color(0xFFFFE0EC),
    surface = Color(0xFF2E1A26),
    onSurface = Color(0xFFFFE0EC),
    surfaceVariant = Color(0xFF3D1F33),
    onSurfaceVariant = Color(0xFFFFCCE0)
)

private val HalloweenSpookyLight = lightColorScheme(
    primary = Color(0xFFFF6600),
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFFFFCC99),
    onPrimaryContainer = Color(0xFF4D2000),
    secondary = Color(0xFF9933FF),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6CCFF),
    onSecondaryContainer = Color(0xFF330066),
    tertiary = Color(0xFF000000),
    onTertiary = Color(0xFFFF9933),
    tertiaryContainer = Color(0xFF333333),
    onTertiaryContainer = Color(0xFFFFCC99),
    background = Color(0xFF1A1A1A),
    onBackground = Color(0xFFFFAA33),
    surface = Color(0xFF262626),
    onSurface = Color(0xFFFFAA33),
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFE6CCFF)
)

private val HalloweenSpookyDark = darkColorScheme(
    primary = Color(0xFFFF9933),
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFF995C00),
    onPrimaryContainer = Color(0xFFFFCC99),
    secondary = Color(0xFFCC66FF),
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFF6600CC),
    onSecondaryContainer = Color(0xFFE6CCFF),
    tertiary = Color(0xFF66FF66),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF003300),
    onTertiaryContainer = Color(0xFFCCFFCC),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFF9933),
    surface = Color(0xFF0D0D0D),
    onSurface = Color(0xFFFF9933),
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFCC66FF)
)

private val SpringBloomLight = lightColorScheme(
    primary = Color(0xFFFF77AA),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0EC),
    onPrimaryContainer = Color(0xFF66002B),
    secondary = Color(0xFF77DD77),
    onSecondary = Color(0xFF1A3D1A),
    secondaryContainer = Color(0xFFD5FFD5),
    onSecondaryContainer = Color(0xFF002600),
    tertiary = Color(0xFFFFE066),
    onTertiary = Color(0xFF3D3000),
    tertiaryContainer = Color(0xFFFFF9D9),
    onTertiaryContainer = Color(0xFF1F1800),
    background = Color(0xFFFFFDF7),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFF9F5),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFFFEEE6),
    onSurfaceVariant = Color(0xFF4A3D3D)
)

private val SpringBloomDark = darkColorScheme(
    primary = Color(0xFFFFAACC),
    onPrimary = Color(0xFF66002B),
    primaryContainer = Color(0xFF99004D),
    onPrimaryContainer = Color(0xFFFFE0EC),
    secondary = Color(0xFF99FFAA),
    onSecondary = Color(0xFF003D1A),
    secondaryContainer = Color(0xFF00662B),
    onSecondaryContainer = Color(0xFFD5FFD5),
    tertiary = Color(0xFFFFFF99),
    onTertiary = Color(0xFF3D3000),
    tertiaryContainer = Color(0xFF666000),
    onTertiaryContainer = Color(0xFFFFF9D9),
    background = Color(0xFF1A1F1A),
    onBackground = Color(0xFFFFEEE0),
    surface = Color(0xFF262E26),
    onSurface = Color(0xFFFFEEE0),
    surfaceVariant = Color(0xFF333D33),
    onSurfaceVariant = Color(0xFFFFCCDD)
)

private val SummerVibesLight = lightColorScheme(
    primary = Color(0xFFFFAA00),
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFFFFE6B3),
    onPrimaryContainer = Color(0xFF4D3300),
    secondary = Color(0xFF00CED1),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB3F5F5),
    onSecondaryContainer = Color(0xFF003D40),
    tertiary = Color(0xFFFF4500),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD1B3),
    onTertiaryContainer = Color(0xFF4D1500),
    background = Color(0xFFFFFAF0),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFF8E8),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFFFEED9),
    onSurfaceVariant = Color(0xFF4D3D26)
)

private val SummerVibesDark = darkColorScheme(
    primary = Color(0xFFFFCC33),
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFF997A00),
    onPrimaryContainer = Color(0xFFFFE6B3),
    secondary = Color(0xFF00FFFF),
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFF00999C),
    onSecondaryContainer = Color(0xFFB3F5F5),
    tertiary = Color(0xFFFF6347),
    onTertiary = Color(0xFF1A1A1A),
    tertiaryContainer = Color(0xFF993D29),
    onTertiaryContainer = Color(0xFFFFD1B3),
    background = Color(0xFF0D1F26),
    onBackground = Color(0xFFFFE6B3),
    surface = Color(0xFF1A2E33),
    onSurface = Color(0xFFFFE6B3),
    surfaceVariant = Color(0xFF26404D),
    onSurfaceVariant = Color(0xFF99FFFF)
)

val availableThemes = listOf(
    AppColorTheme("Electric Sunset", ElectricSunsetLight, ElectricSunsetDark),
    AppColorTheme("Ocean Breeze", OceanBreezeLight, OceanBreezeDark),
    AppColorTheme("Forest Green", ForestGreenLight, ForestGreenDark),
    AppColorTheme("Royal Purple", RoyalPurpleLight, RoyalPurpleDark),
    AppColorTheme("Cherry Blossom", CherryBlossomLight, CherryBlossomDark),
    AppColorTheme("Midnight Blue", MidnightBlueLight, MidnightBlueDark),
    AppColorTheme("Sunset Orange", SunsetOrangeLight, SunsetOrangeDark),
    AppColorTheme("Mint Fresh", MintFreshLight, MintFreshDark),
    AppColorTheme("Rose Gold", RoseGoldLight, RoseGoldDark),
    AppColorTheme("Cyber Neon", CyberNeonLight, CyberNeonDark),
    AppColorTheme("Autumn Leaves", AutumnLeavesLight, AutumnLeavesDark),
    AppColorTheme("Arctic Ice", ArcticIceLight, ArcticIceDark),

    AppColorTheme("Christmas Magic", ChristmasMagicLight, ChristmasMagicDark),
    AppColorTheme("Winter Wonderland", WinterWonderlandLight, WinterWonderlandDark),
    AppColorTheme("Valentine's Love", ValentinesLoveLight, ValentinesLoveDark),
    AppColorTheme("Halloween Spooky", HalloweenSpookyLight, HalloweenSpookyDark),
    AppColorTheme("Spring Bloom", SpringBloomLight, SpringBloomDark),
    AppColorTheme("Summer Vibes", SummerVibesLight, SummerVibesDark)
)

fun getThemeByName(name: String): AppColorTheme {
    return availableThemes.find { it.name == name } ?: availableThemes[0]
}
