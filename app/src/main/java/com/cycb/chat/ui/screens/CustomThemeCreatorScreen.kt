package com.cycb.chat.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomThemeCreatorScreen(
    onBackClick: () -> Unit,
    onThemeCreated: (String) -> Unit = {},
    viewModel: com.cycb.chat.viewmodel.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val customThemesPrefs = remember { com.cycb.chat.data.preferences.CustomThemesPreferences(context) }

    var themeName by remember { mutableStateOf("") }

    var lightPrimary by remember { mutableStateOf("#FF6B35") }
    var lightSecondary by remember { mutableStateOf("#F7931E") }
    var lightTertiary by remember { mutableStateOf("#FDC830") }
    var lightBackground by remember { mutableStateOf("#FFF8F0") }
    var lightSurface by remember { mutableStateOf("#FFFFFF") }

    var darkPrimary by remember { mutableStateOf("#FFB4AB") }
    var darkSecondary by remember { mutableStateOf("#FFB951") }
    var darkTertiary by remember { mutableStateOf("#FFE082") }
    var darkBackground by remember { mutableStateOf("#201A17") }
    var darkSurface by remember { mutableStateOf("#2C2622") }

    var showPreview by remember { mutableStateOf(false) }
    var previewDarkMode by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Custom Theme",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showPreview = !showPreview }
                    ) {
                        Icon(
                            if (showPreview) Icons.Default.Edit else Icons.Default.Visibility,
                            contentDescription = if (showPreview) "Edit" else "Preview"
                        )
                    }
                },
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (themeName.isNotBlank()) {
                        coroutineScope.launch {

                            customThemesPrefs.saveCustomTheme(
                                name = themeName,
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

                            viewModel.setSelectedTheme(themeName)

                            onThemeCreated(themeName)
                        }
                    }
                },
                icon = { Icon(Icons.Default.Check, "Save") },
                text = { Text("Save Theme") },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showPreview) {

                CustomThemePreview(
                    themeName = themeName.ifBlank { "Custom Theme" },
                    lightPrimary = parseHexColor(lightPrimary),
                    lightSecondary = parseHexColor(lightSecondary),
                    lightTertiary = parseHexColor(lightTertiary),
                    lightBackground = parseHexColor(lightBackground),
                    darkPrimary = parseHexColor(darkPrimary),
                    darkSecondary = parseHexColor(darkSecondary),
                    darkTertiary = parseHexColor(darkTertiary),
                    darkBackground = parseHexColor(darkBackground),
                    isDarkMode = previewDarkMode
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Light", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = previewDarkMode,
                        onCheckedChange = { previewDarkMode = it },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text("Dark", style = MaterialTheme.typography.bodyMedium)
                }
            } else {

                OutlinedTextField(
                    value = themeName,
                    onValueChange = { themeName = it },
                    label = { Text("Theme Name") },
                    placeholder = { Text("My Awesome Theme") },
                    leadingIcon = { Icon(Icons.Default.Palette, null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    singleLine = true
                )

                ThemeColorSection(
                    title = "Light Theme Colors",
                    icon = Icons.Default.LightMode,
                    colors = listOf(
                        ColorInput("Primary", lightPrimary) { lightPrimary = it },
                        ColorInput("Secondary", lightSecondary) { lightSecondary = it },
                        ColorInput("Tertiary", lightTertiary) { lightTertiary = it },
                        ColorInput("Background", lightBackground) { lightBackground = it },
                        ColorInput("Surface", lightSurface) { lightSurface = it }
                    )
                )

                ThemeColorSection(
                    title = "Dark Theme Colors",
                    icon = Icons.Default.DarkMode,
                    colors = listOf(
                        ColorInput("Primary", darkPrimary) { darkPrimary = it },
                        ColorInput("Secondary", darkSecondary) { darkSecondary = it },
                        ColorInput("Tertiary", darkTertiary) { darkTertiary = it },
                        ColorInput("Background", darkBackground) { darkBackground = it },
                        ColorInput("Surface", darkSurface) { darkSurface = it }
                    )
                )

                QuickColorPresets(
                    onPresetSelected = { preset ->
                        when (preset) {
                            "Vibrant" -> {

                                lightPrimary = "#FF0080"
                                lightSecondary = "#00D9FF"
                                lightTertiary = "#FFD700"
                                lightBackground = "#FFF5F9"
                                lightSurface = "#FFFFFF"

                                darkPrimary = "#FFB3D9"
                                darkSecondary = "#80ECFF"
                                darkTertiary = "#FFEB80"
                                darkBackground = "#1A1A1A"
                                darkSurface = "#2A2A2A"
                            }
                            "Nature" -> {

                                lightPrimary = "#2E7D32"
                                lightSecondary = "#66BB6A"
                                lightTertiary = "#A5D6A7"
                                lightBackground = "#F1F8F4"
                                lightSurface = "#FFFFFF"

                                darkPrimary = "#81C784"
                                darkSecondary = "#A5D6A7"
                                darkTertiary = "#C8E6C9"
                                darkBackground = "#1B2E1F"
                                darkSurface = "#2A3E2F"
                            }
                            "Ocean" -> {

                                lightPrimary = "#0277BD"
                                lightSecondary = "#0288D1"
                                lightTertiary = "#4FC3F7"
                                lightBackground = "#E1F5FE"
                                lightSurface = "#FFFFFF"

                                darkPrimary = "#4FC3F7"
                                darkSecondary = "#81D4FA"
                                darkTertiary = "#B3E5FC"
                                darkBackground = "#0D1F2D"
                                darkSurface = "#1A2F3D"
                            }
                            "Sunset" -> {

                                lightPrimary = "#FF6F00"
                                lightSecondary = "#FF8F00"
                                lightTertiary = "#FFAB00"
                                lightBackground = "#FFF8E1"
                                lightSurface = "#FFFFFF"

                                darkPrimary = "#FFB74D"
                                darkSecondary = "#FFCC80"
                                darkTertiary = "#FFE0B2"
                                darkBackground = "#2D1F0D"
                                darkSurface = "#3D2F1D"
                            }
                        }
                    }
                )

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

data class ColorInput(
    val label: String,
    val value: String,
    val onValueChange: (String) -> Unit
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeColorSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: List<ColorInput>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            colors.forEach { colorInput ->
                ColorInputField(
                    label = colorInput.label,
                    value = colorInput.value,
                    onValueChange = colorInput.onValueChange
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColorInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val color = parseHexColor(value)
    var showColorPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .clickable { showColorPicker = true }
        )

        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->

                val filtered = newValue.filter { it.isDigit() || it.uppercaseChar() in 'A'..'F' || it == '#' }
                if (filtered.length <= 7) {
                    onValueChange(filtered.uppercase())
                }
            },
            label = { Text(label) },
            placeholder = { Text("#RRGGBB") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii
            ),
            singleLine = true,
            prefix = { if (!value.startsWith("#")) Text("#") }
        )
    }

    if (showColorPicker) {
        com.cycb.chat.ui.components.ColorPickerDialog(
            initialColor = color,
            onDismiss = { showColorPicker = false },
            onColorSelected = { selectedColor ->
                val hex = "#${selectedColor.toArgb().toUInt().toString(16).uppercase().takeLast(6)}"
                onValueChange(hex)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuickColorPresets(
    onPresetSelected: (String) -> Unit
) {
    var selectedPreset by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Quick Presets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    ButtonGroupDefaults.ConnectedSpaceBetween
                )
            ) {
                val presets = listOf("Vibrant", "Nature", "Ocean", "Sunset")

                presets.forEachIndexed { index, preset ->
                    ToggleButton(
                        checked = selectedPreset == preset,
                        onCheckedChange = {
                            selectedPreset = preset
                            onPresetSelected(preset)
                        },
                        modifier = Modifier.weight(1f),
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            presets.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        }
                    ) {
                        Text(
                            preset,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomThemePreview(
    themeName: String,
    lightPrimary: Color,
    lightSecondary: Color,
    lightTertiary: Color,
    lightBackground: Color,
    darkPrimary: Color,
    darkSecondary: Color,
    darkTertiary: Color,
    darkBackground: Color,
    isDarkMode: Boolean
) {
    val primary = if (isDarkMode) darkPrimary else lightPrimary
    val secondary = if (isDarkMode) darkSecondary else lightSecondary
    val tertiary = if (isDarkMode) darkTertiary else lightTertiary
    val background = if (isDarkMode) darkBackground else lightBackground

    val infiniteTransition = rememberInfiniteTransition(label = "preview_infinite")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val offsetY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = (-25).dp, y = (-25).dp + offsetY.dp)
                    .graphicsLayer { rotationZ = rotation * 0.3f }
                    .clip(CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                primary.copy(alpha = 0.4f),
                                primary.copy(alpha = 0.2f)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 25.dp, y = (-15).dp - offsetY.dp)
                    .graphicsLayer { rotationZ = -rotation * 0.2f }
                    .clip(CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                secondary.copy(alpha = 0.4f),
                                secondary.copy(alpha = 0.2f)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = 30.dp, y = 15.dp + offsetY.dp * 0.5f)
                    .graphicsLayer { rotationZ = rotation * 0.4f }
                    .clip(CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                tertiary.copy(alpha = 0.4f),
                                tertiary.copy(alpha = 0.2f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    themeName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = primary
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    if (isDarkMode) "Dark Mode" else "Light Mode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = primary.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(primary, secondary, tertiary).forEachIndexed { index, color ->
                        val dotScale by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 1500,
                                    delayMillis = index * 200,
                                    easing = FastOutSlowInEasing
                                ),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "dot_scale_$index"
                        )

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer {
                                    scaleX = dotScale
                                    scaleY = dotScale
                                }
                                .clip(CircleShape)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                        colors = listOf(color, color.copy(alpha = 0.8f))
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

fun parseHexColor(hex: String): Color {
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
