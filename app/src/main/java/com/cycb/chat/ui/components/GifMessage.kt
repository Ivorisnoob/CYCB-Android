package com.cycb.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GifMessage(
    gifUrl: String,
    isOwnMessage: Boolean,
    autoPlayGifs: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var shouldPlay by remember { mutableStateOf(autoPlayGifs) }

    LaunchedEffect(autoPlayGifs) {
        shouldPlay = autoPlayGifs
    }

    Surface(
        modifier = modifier
            .widthIn(max = 280.dp)
            .heightIn(max = 280.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = if (isOwnMessage) 1.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(
                    if (isOwnMessage)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable {

                    if (!autoPlayGifs) {
                        shouldPlay = !shouldPlay
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(gifUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "GIF",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
                onState = { state ->
                    isLoading = state is AsyncImagePainter.State.Loading
                    hasError = state is AsyncImagePainter.State.Error
                }
            )

            if (!shouldPlay && !isLoading && !hasError) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play GIF",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (isLoading) {
                LoadingIndicator(
                    modifier = Modifier.size(32.dp)
                )
            }

            if (hasError) {
                Text(
                    text = "Failed to load GIF",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
