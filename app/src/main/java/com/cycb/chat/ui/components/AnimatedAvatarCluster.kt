package com.cycb.chat.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cycb.chat.data.model.User
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedAvatarCluster(
    participants: List<User>,
    groupName: String,
    modifier: Modifier = Modifier,
    maxVisibleAvatars: Int = 8,
    onClusterClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.clickable { onClusterClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val visibleParticipants = participants.take(maxVisibleAvatars)

            visibleParticipants.forEachIndexed { index, user ->
                FloatingAvatar(
                    user = user,
                    index = index,
                    total = visibleParticipants.size
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = groupName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Group info",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun FloatingAvatar(
    user: User,
    index: Int,
    total: Int
) {

    val position = calculateAvatarPosition(index, total)
    val size = calculateAvatarSize(index, total)

    val infiniteTransition = rememberInfiniteTransition(label = "avatar_float_$index")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(index * 200)
        ),
        label = "float_y_$index"
    )

    val offsetX by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2500,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(index * 300)
        ),
        label = "float_x_$index"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(index * 250)
        ),
        label = "scale_$index"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(index * 350)
        ),
        label = "rotation_$index"
    )

    Box(
        modifier = Modifier
            .offset(x = position.first, y = position.second)
            .graphicsLayer {
                translationX = offsetX
                translationY = offsetY
                scaleX = scale
                scaleY = scale
                rotationZ = rotation

                shadowElevation = 4f
                shape = CircleShape
                clip = true
            }
    ) {
        ProfilePicture(
            imageUrl = user.profilePicture,
            displayName = user.displayName,
            size = size,
            modifier = Modifier.clip(CircleShape)
        )
    }
}

private fun calculateAvatarPosition(index: Int, total: Int): Pair<Dp, Dp> {

    val angle = (index.toFloat() / total) * 2 * PI
    val radiusBase = when {
        total <= 3 -> 25
        total <= 5 -> 35
        else -> 45
    }

    val angleVariation = (index % 3 - 1) * 0.3
    val radiusVariation = (index % 2) * 5

    val radius = radiusBase + radiusVariation
    val x = (radius * cos(angle + angleVariation) * 0.8).toFloat()
    val y = (radius * sin(angle + angleVariation) * 0.5).toFloat()

    return Pair(x.dp, y.dp)
}

private fun calculateAvatarSize(index: Int, total: Int): Dp {
    return when {
        index == 0 -> 48.dp
        index < 3 -> 40.dp
        index < 6 -> 36.dp
        else -> 32.dp
    }
}
