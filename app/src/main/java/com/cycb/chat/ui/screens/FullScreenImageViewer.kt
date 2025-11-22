@file:OptIn(ExperimentalMaterial3Api::class)

package com.cycb.chat.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

@Composable
fun FullScreenImageViewer(
    imageUrl: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    var dismissProgress by remember { mutableStateOf(0f) }
    var isDismissing by remember { mutableStateOf(false) }

    var isDownloading by remember { mutableStateOf(false) }
    var showDownloadSuccess by remember { mutableStateOf(false) }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(50)
        isVisible = true
    }

    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isDismissing) 0f else (1f - dismissProgress.coerceIn(0f, 1f)),
        animationSpec = tween(durationMillis = 200),
        label = "background_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = backgroundAlpha))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + scaleIn(
                initialScale = 0.85f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
            exit = fadeOut(
                animationSpec = tween(durationMillis = 200)
            ) + scaleOut(
                targetScale = 0.9f,
                animationSpec = tween(durationMillis = 200)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 4f)

                            if (newScale > 1f) {

                                scale = newScale
                                offsetX += pan.x
                                offsetY += pan.y

                                val maxOffsetX = (size.width * (scale - 1)) / 2
                                val maxOffsetY = (size.height * (scale - 1)) / 2
                                offsetX = offsetX.coerceIn(-maxOffsetX, maxOffsetX)
                                offsetY = offsetY.coerceIn(-maxOffsetY, maxOffsetY)
                            } else {

                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    }
                    .pointerInput(Unit) {

                        detectDragGestures(
                            onDragEnd = {
                                if (scale == 1f) {
                                    if (dismissProgress > 0.3f) {
                                        isDismissing = true
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(200)
                                            onBackClick()
                                        }
                                    } else {

                                        coroutineScope.launch {
                                            animate(
                                                initialValue = dismissProgress,
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            ) { value, _ ->
                                                dismissProgress = value
                                            }
                                        }
                                    }
                                }
                            }
                        ) { change, dragAmount ->
                            if (scale == 1f) {
                                change.consume()
                                val dragY = dragAmount.y
                                dismissProgress += dragY / size.height
                                dismissProgress = dismissProgress.coerceIn(0f, 1f)
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = scale * (1f - dismissProgress * 0.2f)
                        scaleY = scale * (1f - dismissProgress * 0.2f)
                        translationX = offsetX
                        translationY = offsetY + (dismissProgress * 300f)
                    },
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Full screen image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator(
                                modifier = Modifier.size(48.dp),
                                color = Color.White
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Failed to load image",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White
                                )
                                Text(
                                    "Swipe down to close",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = isVisible && !isDismissing,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                var isBackPressed by remember { mutableStateOf(false) }
                val backScale by animateFloatAsState(
                    targetValue = if (isBackPressed) 0.85f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    ),
                    label = "back_scale"
                )

                IconButton(
                    onClick = {
                        isBackPressed = true
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(100)
                            onBackClick()
                        }
                    },
                    modifier = Modifier.graphicsLayer {
                        scaleX = backScale
                        scaleY = backScale
                    }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                var isDownloadPressed by remember { mutableStateOf(false) }
                val downloadScale by animateFloatAsState(
                    targetValue = if (isDownloadPressed) 0.85f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    ),
                    label = "download_scale"
                )

                IconButton(
                    onClick = {
                        isDownloadPressed = true
                        isDownloading = true
                        coroutineScope.launch {
                            try {
                                val result = com.cycb.chat.utils.ImageDownloadHelper.downloadImage(
                                    context = context,
                                    imageUrl = imageUrl
                                )

                                if (result.isSuccess) {
                                    showDownloadSuccess = true
                                    kotlinx.coroutines.delay(2000)
                                    showDownloadSuccess = false
                                } else {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Failed to download: ${result.exceptionOrNull()?.message}",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Error: ${e.message}",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            } finally {
                                isDownloading = false
                                isDownloadPressed = false
                            }
                        }
                    },
                    enabled = !isDownloading,
                    modifier = Modifier.graphicsLayer {
                        scaleX = downloadScale
                        scaleY = downloadScale
                    }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (isDownloading) {
                                CircularWavyProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = "Download",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showDownloadSuccess,
            enter = fadeIn() + scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
            exit = fadeOut() + scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(durationMillis = 200)
            ),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.8f),
                modifier = Modifier.padding(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF4CAF50)
                    )
                    Column {
                        Text(
                            text = "Downloaded!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Saved to Downloads",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isVisible && !isDismissing && scale == 1f,
            enter = fadeIn(animationSpec = tween(delayMillis = 500)),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "Pinch to zoom • Swipe down to close",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
