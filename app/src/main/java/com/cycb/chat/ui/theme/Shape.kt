package com.cycb.chat.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

val MessageBubbleSent = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomStart = 20.dp,
    bottomEnd = 4.dp
)

val MessageBubbleReceived = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomStart = 4.dp,
    bottomEnd = 20.dp
)

val ProfilePictureShape = CircleShape
val GroupAvatarShape = RoundedCornerShape(16.dp)
val CardShape = RoundedCornerShape(16.dp)
val ButtonShape = RoundedCornerShape(24.dp)
val FabShape = CircleShape
