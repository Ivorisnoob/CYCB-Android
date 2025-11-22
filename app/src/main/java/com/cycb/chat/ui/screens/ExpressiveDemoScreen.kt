@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.cycb.chat.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CutCornerShape

@Composable
fun ExpressiveDemoScreen(
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Loading", "Buttons", "Cards", "Shapes", "Inputs", "Navigation", "Tooltips", "Toggle", "More")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Material 3 Expressive",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        ToggleButton(
                            checked = selectedTab == index,
                            onCheckedChange = { selectedTab = index },
                            shapes = when {
                                tabs.size == 1 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                index == tabs.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            }
                        ) {
                            Text(title)
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    when (selectedTab) {
                        0 -> LoadingComponentsDemo()
                        1 -> ButtonsDemo()
                        2 -> CardsDemo()
                        3 -> ShapesDemo()
                        4 -> InputsDemo()
                        5 -> NavigationDemo()
                        6 -> TooltipsDemo()
                        7 -> ToggleComponentsDemo()
                        8 -> MoreComponentsDemo()
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingComponentsDemo() {
    var progress by remember { mutableStateOf(0.5f) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DemoSection("LoadingIndicator (Shape-Morphing)") {
            Text(
                "Morphs between shapes for expressive loading",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LoadingIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Indeterminate", style = MaterialTheme.typography.labelSmall)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LoadingIndicator(
                        progress = { progress },
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Determinate", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        DemoSection("CircularWavyProgressIndicator") {
            Text(
                "Wavy circular progress with fluid animation",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularWavyProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Wavy", style = MaterialTheme.typography.labelSmall)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularWavyProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Progress", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        DemoSection("ContainedLoadingIndicator") {
            Text(
                "Morphing shapes inside a container",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ContainedLoadingIndicator(
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Morphing", style = MaterialTheme.typography.labelSmall)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ContainedLoadingIndicator(
                        progress = { progress },
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Progress", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        DemoSection("LinearWavyProgressIndicator") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Indeterminate wavy progress")
                LinearWavyProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Text("Determinate wavy progress: ${(progress * 100).toInt()}%")
                LinearWavyProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        DemoSection("Progress Control") {
            Column {
                Text(
                    "Adjust progress: ${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = progress,
                    onValueChange = { progress = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ButtonsDemo() {
    var toggleState by remember { mutableStateOf(false) }
    var selectedSegment by remember { mutableStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DemoSection("Button Variants") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Filled Button")
                }

                ElevatedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Favorite, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Elevated Button")
                }

                FilledTonalButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Settings, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Filled Tonal Button")
                }

                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Info, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Outlined Button")
                }

                TextButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Text Button")
                }
            }
        }

        DemoSection("ButtonGroup (Connected Buttons)") {
            Text(
                "Connected buttons with asymmetric shapes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
            ) {
                ToggleButton(
                    checked = selectedSegment == 0,
                    onCheckedChange = { selectedSegment = 0 },
                    modifier = Modifier.weight(1f),
                    shapes = ButtonGroupDefaults.connectedLeadingButtonShapes()
                ) {
                    Icon(Icons.Default.Home, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Home")
                }

                ToggleButton(
                    checked = selectedSegment == 1,
                    onCheckedChange = { selectedSegment = 1 },
                    modifier = Modifier.weight(1f),
                    shapes = ButtonGroupDefaults.connectedMiddleButtonShapes()
                ) {
                    Icon(Icons.Default.Search, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Search")
                }

                ToggleButton(
                    checked = selectedSegment == 2,
                    onCheckedChange = { selectedSegment = 2 },
                    modifier = Modifier.weight(1f),
                    shapes = ButtonGroupDefaults.connectedTrailingButtonShapes()
                ) {
                    Icon(Icons.Default.Person, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Profile")
                }
            }
        }

        DemoSection("Icon Buttons") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Add, "Add")
                }
                FilledIconButton(onClick = {}) {
                    Icon(Icons.Default.Edit, "Edit")
                }
                FilledTonalIconButton(onClick = {}) {
                    Icon(Icons.Default.Settings, "Settings")
                }
                OutlinedIconButton(onClick = {}) {
                    Icon(Icons.Default.Delete, "Delete")
                }
                IconToggleButton(
                    checked = toggleState,
                    onCheckedChange = { toggleState = it }
                ) {
                    Icon(
                        if (toggleState) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        "Favorite"
                    )
                }
            }
        }

        DemoSection("Floating Action Buttons") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Add, "Add")
                }

                SmallFloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Edit, "Edit")
                }

                LargeFloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Star, "Star")
                }
            }
        }

        DemoSection("Extended FABs") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExtendedFloatingActionButton(
                    onClick = {},
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Create New") }
                )

                ExtendedFloatingActionButton(
                    onClick = {},
                    expanded = false,
                    icon = { Icon(Icons.Default.Edit, null) },
                    text = { Text("Edit") }
                )
            }
        }

        DemoSection("Segmented Buttons") {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = selectedSegment == 0,
                    onClick = { selectedSegment = 0 },
                    shape = SegmentedButtonDefaults.itemShape(0, 3)
                ) {
                    Text("Day")
                }
                SegmentedButton(
                    selected = selectedSegment == 1,
                    onClick = { selectedSegment = 1 },
                    shape = SegmentedButtonDefaults.itemShape(1, 3)
                ) {
                    Text("Week")
                }
                SegmentedButton(
                    selected = selectedSegment == 2,
                    onClick = { selectedSegment = 2 },
                    shape = SegmentedButtonDefaults.itemShape(2, 3)
                ) {
                    Text("Month")
                }
            }
        }
    }
}

@Composable
private fun CardsDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DemoSection("Card Variants") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Filled Card",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Standard card with filled background",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Elevated Card",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Card with elevation and shadow",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Outlined Card",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Card with border outline",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        DemoSection("Clickable Cards") {
            ElevatedCard(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Interactive Card",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Tap to interact",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShapesDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DemoSection("Material 3 Shape Scale") {
            Text(
                "Standard corner radius scale from extraSmall to extraLarge",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ShapeItem("extraSmall (4dp)", MaterialTheme.shapes.extraSmall)
                ShapeItem("small (8dp)", MaterialTheme.shapes.small)
                ShapeItem("medium (12dp)", MaterialTheme.shapes.medium)
                ShapeItem("large (16dp)", MaterialTheme.shapes.large)
                ShapeItem("extraLarge (28dp)", MaterialTheme.shapes.extraLarge)
            }
        }

        DemoSection("Custom Rounded Corners") {
            Text(
                "Different corner radius combinations",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ShapeItem("All Corners (24dp)", RoundedCornerShape(24.dp))
                ShapeItem(
                    "Top Corners Only",
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                ShapeItem(
                    "Bottom Corners Only",
                    RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                ShapeItem(
                    "Left Corners Only",
                    RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                )
                ShapeItem(
                    "Right Corners Only",
                    RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                )
                ShapeItem(
                    "Diagonal (TL & BR)",
                    RoundedCornerShape(topStart = 24.dp, bottomEnd = 24.dp)
                )
                ShapeItem(
                    "Diagonal (TR & BL)",
                    RoundedCornerShape(topEnd = 24.dp, bottomStart = 24.dp)
                )
            }
        }

        DemoSection("Asymmetric Shapes") {
            Text(
                "Each corner with different radius",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ShapeItem(
                    "Progressive (4, 8, 16, 24)",
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 8.dp,
                        bottomEnd = 16.dp,
                        bottomStart = 24.dp
                    )
                )
                ShapeItem(
                    "Speech Bubble (Left)",
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomEnd = 20.dp,
                        bottomStart = 4.dp
                    )
                )
                ShapeItem(
                    "Speech Bubble (Right)",
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomEnd = 4.dp,
                        bottomStart = 20.dp
                    )
                )
            }
        }

        DemoSection("Special Shapes") {
            Text(
                "Circle and extreme rounded shapes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Circle",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(80.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Pill (50%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                ShapeItem("Fully Rounded (100dp)", RoundedCornerShape(100.dp))
            }
        }

        DemoSection("Shape Morphing Preview") {
            Text(
                "Shapes used in expressive components",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "S",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "M",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.tertiary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "L",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "XL",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        DemoSection("Shape Usage Examples") {
            Text(
                "How shapes are applied to components",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text("Button with extraLarge shape")
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        "Card with large shape",
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Text(
                        "Surface with medium shape",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InputsDemo() {
    var textValue by remember { mutableStateOf("") }
    var sliderValue by remember { mutableStateOf(50f) }
    var switchValue by remember { mutableStateOf(true) }
    var checkboxValue by remember { mutableStateOf(false) }
    var radioValue by remember { mutableStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DemoSection("Text Fields") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Outlined TextField") },
                    placeholder = { Text("Enter text...") },
                    leadingIcon = { Icon(Icons.Default.Edit, null) }
                )

                TextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Filled TextField") },
                    placeholder = { Text("Enter text...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
            }
        }

        DemoSection("Slider") {
            Column {
                Text("Value: ${sliderValue.toInt()}")
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        DemoSection("Switch") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable notifications")
                Switch(
                    checked = switchValue,
                    onCheckedChange = { switchValue = it }
                )
            }
        }

        DemoSection("Checkbox") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checkboxValue,
                    onCheckedChange = { checkboxValue = it }
                )
                Spacer(Modifier.width(8.dp))
                Text("Accept terms and conditions")
            }
        }

        DemoSection("Radio Buttons") {
            Column {
                listOf("Option 1", "Option 2", "Option 3").forEachIndexed { index, label ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = radioValue == index,
                            onClick = { radioValue = index }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        }

        DemoSection("Chips") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text("Assist") },
                    leadingIcon = { Icon(Icons.Default.Star, null, modifier = Modifier.size(18.dp)) }
                )

                FilterChip(
                    selected = false,
                    onClick = {},
                    label = { Text("Filter") }
                )

                SuggestionChip(
                    onClick = {},
                    label = { Text("Suggest") }
                )
            }
        }
    }
}

@Composable
private fun NavigationDemo() {
    var selectedNav by remember { mutableStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DemoSection("Navigation Bar") {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedNav == 0,
                    onClick = { selectedNav = 0 },
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedNav == 1,
                    onClick = { selectedNav = 1 },
                    icon = { Icon(Icons.Default.Search, "Search") },
                    label = { Text("Search") }
                )
                NavigationBarItem(
                    selected = selectedNav == 2,
                    onClick = { selectedNav = 2 },
                    icon = { Icon(Icons.Default.Person, "Profile") },
                    label = { Text("Profile") }
                )
            }
        }

        DemoSection("Tabs") {
            var selectedTab by remember { mutableStateOf(0) }

            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Tab 1") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Tab 2") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Tab 3") }
                )
            }
        }

        DemoSection("Badges") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BadgedBox(badge = { Badge { Text("3") } }) {
                    Icon(Icons.Default.Notifications, "Notifications")
                }

                BadgedBox(badge = { Badge() }) {
                    Icon(Icons.Default.Email, "Email")
                }

                BadgedBox(badge = { Badge { Text("99+") } }) {
                    Icon(Icons.Default.ShoppingCart, "Cart")
                }
            }
        }
    }
}

@Composable
private fun MoreComponentsDemo() {
    var showDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DemoSection("Dialogs") {
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show Alert Dialog")
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    icon = { Icon(Icons.Default.Info, null) },
                    title = { Text("Dialog Title") },
                    text = { Text("This is an example of Material 3 AlertDialog with expressive design.") },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

        DemoSection("Snackbar") {
            Snackbar(
                action = {
                    TextButton(onClick = {}) {
                        Text("Action")
                    }
                }
            ) {
                Text("This is a snackbar message")
            }
        }

        DemoSection("List Items") {
            Column {
                ListItem(
                    headlineContent = { Text("One line list item") },
                    leadingContent = {
                        Icon(Icons.Default.Person, null)
                    }
                )

                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("Two line list item") },
                    supportingContent = { Text("Supporting text") },
                    leadingContent = {
                        Icon(Icons.Default.Email, null)
                    },
                    trailingContent = {
                        Icon(Icons.Default.ChevronRight, null)
                    }
                )

                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("Three line list item") },
                    supportingContent = { Text("Supporting text that can span multiple lines for more detailed information") },
                    leadingContent = {
                        Icon(Icons.Default.Star, null)
                    }
                )
            }
        }

        DemoSection("Surfaces") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Surface 1")
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 3.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Surface 2")
                    }
                }
            }
        }
    }
}

@Composable
private fun TooltipsDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DemoSection("PlainTooltip") {
            Text(
                "Hover or long-press icons to see tooltips",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text("Add") } },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Add, "Add")
                    }
                }

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text("Edit") } },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                }

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text("Delete") } },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text("Share") } },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Share, "Share")
                    }
                }
            }
        }

        DemoSection("RichTooltip") {
            Text(
                "Tooltips with additional content",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                    tooltip = {
                        RichTooltip(
                            title = { Text("Rich Tooltip") },
                            action = {
                                TextButton(onClick = {}) {
                                    Text("Action")
                                }
                            }
                        ) {
                            Text("This is a rich tooltip with title and action button")
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Info, "Info")
                    }
                }

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                    tooltip = {
                        RichTooltip(
                            title = { Text("Help") }
                        ) {
                            Text("Get help with this feature")
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Help, "Help")
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleComponentsDemo() {
    var toggleState1 by remember { mutableStateOf(false) }
    var toggleState2 by remember { mutableStateOf(false) }
    var toggleState3 by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DemoSection("ToggleButton (Expressive)") {
            Text(
                "Toggle buttons with shape morphing",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToggleButton(
                    checked = toggleState1,
                    onCheckedChange = { toggleState1 = it },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        if (toggleState1) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Favorite")
                }

                ToggleButton(
                    checked = toggleState2,
                    onCheckedChange = { toggleState2 = it },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        if (toggleState2) Icons.Default.Star else Icons.Default.StarBorder,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Star")
                }
            }
        }

        DemoSection("ElevatedToggleButton (Expressive)") {
            Text(
                "Elevated toggle with shadow",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            ElevatedToggleButton(
                checked = toggleState3,
                onCheckedChange = { toggleState3 = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    if (toggleState3) Icons.Default.Lock else Icons.Default.LockOpen,
                    null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (toggleState3) "Locked" else "Unlocked")
            }
        }

        DemoSection("OutlinedToggleButton") {
            var checked by remember { mutableStateOf(false) }
            OutlinedToggleButton(
                checked = checked,
                onCheckedChange = { checked = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    if (checked) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                    null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (checked) "Notifications On" else "Notifications Off")
            }
        }

        DemoSection("IconToggleButton Variants") {
            var checked1 by remember { mutableStateOf(false) }
            var checked2 by remember { mutableStateOf(false) }
            var checked3 by remember { mutableStateOf(false) }
            var checked4 by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconToggleButton(
                    checked = checked1,
                    onCheckedChange = { checked1 = it }
                ) {
                    Icon(
                        if (checked1) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        "Favorite"
                    )
                }

                FilledIconToggleButton(
                    checked = checked2,
                    onCheckedChange = { checked2 = it }
                ) {
                    Icon(
                        if (checked2) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        "Bookmark"
                    )
                }

                FilledTonalIconToggleButton(
                    checked = checked3,
                    onCheckedChange = { checked3 = it }
                ) {
                    Icon(
                        if (checked3) Icons.Default.Star else Icons.Default.StarBorder,
                        "Star"
                    )
                }

                OutlinedIconToggleButton(
                    checked = checked4,
                    onCheckedChange = { checked4 = it }
                ) {
                    Icon(
                        if (checked4) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                        "Like"
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun ShapeItem(label: String, shape: androidx.compose.ui.graphics.Shape) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .size(width = 80.dp, height = 48.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
    }
}
