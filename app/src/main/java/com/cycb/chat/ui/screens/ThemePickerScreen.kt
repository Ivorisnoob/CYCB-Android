package com.cycb.chat.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Add
import kotlinx.coroutines.launch

data class ColorTheme(
    val name: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color
)

fun getDisplayThemes(): List<ColorTheme> {
    return com.cycb.chat.ui.theme.availableThemes.map { theme ->
        ColorTheme(
            name = theme.name,
            primary = theme.lightScheme.primary,
            secondary = theme.lightScheme.secondary,
            tertiary = theme.lightScheme.tertiary,
            background = theme.lightScheme.background
        )
    }
}

val colorThemes = getDisplayThemes()

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemePickerScreen(
    onBackClick: () -> Unit,
    onNavigateToCustomTheme: () -> Unit = {},
    viewModel: com.cycb.chat.viewmodel.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val customThemesPrefs = remember { com.cycb.chat.data.preferences.CustomThemesPreferences(context) }
    val customThemes by customThemesPrefs.customThemes.collectAsState(initial = emptyList())

    val savedThemeName by viewModel.selectedTheme.collectAsState()

    val allThemes = remember(customThemes) {
        colorThemes + customThemes.map { appTheme ->
            ColorTheme(
                name = appTheme.name,
                primary = appTheme.lightScheme.primary,
                secondary = appTheme.lightScheme.secondary,
                tertiary = appTheme.lightScheme.tertiary,
                background = appTheme.lightScheme.background
            )
        }
    }

    var selectedTheme by remember(savedThemeName, allThemes) {
        mutableStateOf(allThemes.find { it.name == savedThemeName } ?: colorThemes[0])
    }

    LaunchedEffect(savedThemeName, allThemes) {
        allThemes.find { it.name == savedThemeName }?.let {
            selectedTheme = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Choose Theme",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            ThemePreview(selectedTheme)

            Spacer(Modifier.height(16.dp))

            Text(
                "Select a color theme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allThemes) { theme ->
                    ThemeCard(
                        theme = theme,
                        isSelected = theme == selectedTheme,
                        onClick = {
                            selectedTheme = theme
                            viewModel.setSelectedTheme(theme.name)
                        }
                    )
                }

                item {
                    CreateCustomThemeCard(
                        onClick = onNavigateToCustomTheme
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemePreview(theme: ColorTheme) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(200.dp)
            .border(
                width = 4.dp,
                color = theme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            val infiniteTransition = rememberInfiniteTransition(label = "loading_indicators")

            val offset1X by infiniteTransition.animateFloat(
                initialValue = -20f,
                targetValue = 100f,
                animationSpec = infiniteRepeatable(
                    animation = tween(5200, easing = { fraction ->

                        val n = 7.5625f
                        val d = 2.75f
                        var f = fraction
                        if (f < 1f / d) {
                            n * f * f
                        } else if (f < 2f / d) {
                            f -= 1.5f / d
                            n * f * f + 0.75f
                        } else if (f < 2.5f / d) {
                            f -= 2.25f / d
                            n * f * f + 0.9375f
                        } else {
                            f -= 2.625f / d
                            n * f * f + 0.984375f
                        }
                    }),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offset1X"
            )
            val offset1Y by infiniteTransition.animateFloat(
                initialValue = 10f,
                targetValue = 80f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offset1Y"
            )

            val offset2X by infiniteTransition.animateFloat(
                initialValue = -90f,
                targetValue = -10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offset2X"
            )
            val offset2Y by infiniteTransition.animateFloat(
                initialValue = 5f,
                targetValue = 95f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offset2Y"
            )

            val offset3X by infiniteTransition.animateFloat(
                initialValue = 15f,
                targetValue = 140f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3600, easing = { fraction ->

                        val decay = 1f - (fraction * 0.7f)
                        val oscillation = kotlin.math.sin(fraction * 3.14159f * 2.5f)
                        (oscillation * decay * 0.3f) + fraction
                    }),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offset3X"
            )
            val offset3Y by infiniteTransition.animateFloat(
                initialValue = -70f,
                targetValue = -15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4100, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offset3Y"
            )

            val offset4X by infiniteTransition.animateFloat(
                initialValue = -65f,
                targetValue = -25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(6800, easing = { fraction ->

                        if (fraction == 0f || fraction == 1f) {
                            fraction
                        } else {
                            val decay = 1f / (1f + fraction * 10f)
                            val oscillation = kotlin.math.sin(fraction * 3.14159f * 4f)
                            fraction + (oscillation * decay * 0.2f)
                        }
                    }),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offset4X"
            )
            val offset4Y by infiniteTransition.animateFloat(
                initialValue = -60f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(5900, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offset4Y"
            )

            LoadingIndicator(
                modifier = Modifier
                    .offset(x = offset1X.dp, y = offset1Y.dp)
                    .size(160.dp),
                color = theme.primary.copy(alpha = 0.75f)
            )

            LoadingIndicator(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = offset2X.dp, y = offset2Y.dp)
                    .size(24.dp),
                color = theme.secondary.copy(alpha = 0.6f)
            )

            LoadingIndicator(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = offset3X.dp, y = offset3Y.dp)
                    .size(120.dp),
                color = theme.tertiary.copy(alpha = 0.7f)
            )

            LoadingIndicator(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = offset4X.dp, y = offset4Y.dp)
                    .size(80.dp),
                color = theme.primary.copy(alpha = 0.5f)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    theme.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = theme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeCard(
    theme: ColorTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "border_color"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "elevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .border(
                width = 3.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .offset(x = (-15).dp, y = (-15).dp)
                    .clip(CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                theme.primary.copy(alpha = 0.5f),
                                theme.primary.copy(alpha = 0.3f)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 15.dp, y = (-10).dp)
                    .clip(CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                theme.secondary.copy(alpha = 0.5f),
                                theme.secondary.copy(alpha = 0.3f)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = 20.dp, y = 10.dp)
                    .clip(CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                theme.tertiary.copy(alpha = 0.5f),
                                theme.tertiary.copy(alpha = 0.3f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    theme.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.primary
                )

                if (isSelected) {
                    Spacer(Modifier.height(4.dp))

                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateCustomThemeCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    "Create Custom",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    "Design your own",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}
