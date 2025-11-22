package com.cycb.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBackgroundPickerScreen(
    chatId: String,
    currentBackground: com.cycb.chat.data.model.ChatBackground?,
    onBackClick: () -> Unit,
    onBackgroundSelected: (String, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(currentBackground?.type ?: "color") }
    var selectedValue by remember { mutableStateOf(currentBackground?.value ?: "#FFFFFF") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat Background") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onBackgroundSelected(selectedType, selectedValue)
                            onBackClick()
                        }
                    ) {
                        Text("Save")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                "Background Type",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
            ) {
                ToggleButton(
                    checked = selectedType == "color",
                    onCheckedChange = { selectedType = "color" },
                    modifier = Modifier.weight(1f),
                    shapes = ButtonGroupDefaults.connectedLeadingButtonShapes()
                ) {
                    Text("Color")
                }

                ToggleButton(
                    checked = selectedType == "gradient",
                    onCheckedChange = { selectedType = "gradient" },
                    modifier = Modifier.weight(1f),
                    shapes = ButtonGroupDefaults.connectedMiddleButtonShapes()
                ) {
                    Text("Gradient")
                }

                ToggleButton(
                    checked = selectedType == "image",
                    onCheckedChange = { selectedType = "image" },
                    modifier = Modifier.weight(1f),
                    shapes = ButtonGroupDefaults.connectedTrailingButtonShapes()
                ) {
                    Text("Image")
                }
            }

            Spacer(Modifier.height(8.dp))

            when (selectedType) {
                "color" -> {
                    ColorPicker(
                        selectedColor = selectedValue,
                        onColorSelected = { selectedValue = it }
                    )
                }
                "gradient" -> {
                    GradientPicker(
                        selectedGradient = selectedValue,
                        onGradientSelected = { selectedValue = it }
                    )
                }
                "image" -> {
                    ImagePicker(
                        currentImageUrl = selectedValue,
                        onImageSelected = { selectedValue = it }
                    )
                }
            }
        }
    }
}

@Composable
fun ColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "#FFFFFF" to "White",
        "#F5F5F5" to "Light Gray",
        "#E8EAF6" to "Light Blue",
        "#F3E5F5" to "Light Purple",
        "#E0F2F1" to "Light Teal",
        "#FFF3E0" to "Light Orange",
        "#FCE4EC" to "Light Pink",
        "#E8F5E9" to "Light Green",
        "#212121" to "Dark",
        "#263238" to "Blue Gray",
        "#311B92" to "Deep Purple",
        "#1B5E20" to "Dark Green"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Choose a color",
            style = MaterialTheme.typography.titleMedium
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(colors) { (color, name) ->
                ColorOption(
                    color = color,
                    name = name,
                    isSelected = selectedColor == color,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
fun ColorOption(
    color: String,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = Color(android.graphics.Color.parseColor(color)),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = if (color == "#FFFFFF" || color.startsWith("#F") || color.startsWith("#E"))
                        Color.Black else Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Text(
            name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GradientPicker(
    selectedGradient: String,
    onGradientSelected: (String) -> Unit
) {
    val gradients = listOf(
        "linear-gradient(135deg, #667eea, #764ba2)" to "Purple Bliss",
        "linear-gradient(135deg, #f093fb, #f5576c)" to "Pink Dream",
        "linear-gradient(135deg, #4facfe, #00f2fe)" to "Ocean Blue",
        "linear-gradient(135deg, #43e97b, #38f9d7)" to "Mint Fresh",
        "linear-gradient(135deg, #fa709a, #fee140)" to "Sunset",
        "linear-gradient(135deg, #30cfd0, #330867)" to "Deep Ocean",
        "linear-gradient(135deg, #a8edea, #fed6e3)" to "Pastel",
        "linear-gradient(135deg, #ff9a9e, #fecfef)" to "Cotton Candy"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Choose a gradient",
            style = MaterialTheme.typography.titleMedium
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(gradients) { (gradient, name) ->
                GradientOption(
                    gradient = gradient,
                    name = name,
                    isSelected = selectedGradient == gradient,
                    onClick = { onGradientSelected(gradient) }
                )
            }
        }
    }
}

@Composable
fun GradientOption(
    gradient: String,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    brush = parseGradient(gradient),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Text(
            name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun parseGradient(gradientString: String): Brush {

    val colors = gradientString
        .substringAfter("(")
        .substringBefore(")")
        .split(",")
        .drop(1)
        .map { it.trim() }
        .map { Color(android.graphics.Color.parseColor(it)) }

    return if (colors.size >= 2) {
        Brush.linearGradient(colors)
    } else {
        Brush.linearGradient(listOf(Color(0xFF667eea), Color(0xFF764ba2)))
    }
}

@Composable
fun ImagePicker(
    currentImageUrl: String,
    onImageSelected: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            imageUri = it

            scope.launch {
                isUploading = true
                val result = com.cycb.chat.utils.ImageUploadHelper.uploadToBackend(context, it)
                isUploading = false
                result.onSuccess { url ->
                    onImageSelected(url)
                    imageUri = null
                }.onFailure { error ->
                    android.widget.Toast.makeText(
                        context,
                        "Upload failed: ${error.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    imageUri = null
                }
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Upload Custom Image",
            style = MaterialTheme.typography.titleMedium
        )

        if (currentImageUrl.startsWith("http")) {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                coil.compose.AsyncImage(
                    model = coil.request.ImageRequest.Builder(context)
                        .data(currentImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Current background",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

                Text(
                    "Current Background",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Button(
            onClick = {
                launcher.launch("image/*")
            },
            enabled = !isUploading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isUploading) {
                CircularWavyProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text("Uploading...")
            } else {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Choose Image")
            }
        }

        Text(
            "Tip: Choose a high-quality image for best results",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
