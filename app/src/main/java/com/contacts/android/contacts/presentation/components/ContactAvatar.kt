package com.contacts.android.contacts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.contacts.android.contacts.R
import com.contacts.android.contacts.presentation.theme.AvatarColors

enum class AvatarSize(val size: Dp, val textSize: TextUnit) {
    Small(40.dp, 16.sp),
    Medium(56.dp, 20.sp),
    Large(80.dp, 28.sp),
    ExtraLarge(120.dp, 40.sp)
}

@Composable
fun ContactAvatar(
    name: String,
    photoUri: String?,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.Medium
) {
    val initials = getInitials(name)
    val backgroundColor = getAvatarColor(name)

    Box(
        modifier = modifier
            .size(size.size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUri.isNullOrBlank()) {
            AsyncImage(
                model = photoUri,
                contentDescription = stringResource(R.string.profile_picture_of, name),
                modifier = Modifier
                    .size(size.size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size.size)
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = size.textSize,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White
                )
            }
        }
    }
}

private fun getInitials(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> {
            val first = parts.first().take(1)
            val last = parts.last().take(1)
            "$first$last".uppercase()
        }
    }
}

private fun getAvatarColor(name: String): Color {
    val hash = name.hashCode()
    val index = kotlin.math.abs(hash % AvatarColors.size)
    return AvatarColors[index]
}
