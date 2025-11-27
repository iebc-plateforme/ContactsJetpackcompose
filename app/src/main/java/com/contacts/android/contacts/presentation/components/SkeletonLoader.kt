package com.contacts.android.contacts.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ContactDetailSkeletonLoader() {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar skeleton
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(brush)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Organization text skeleton
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(brush)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action chips skeleton
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(brush)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cards skeleton
        repeat(3) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Title skeleton
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(brush)
                        )
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(brush)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Content lines
                    repeat(2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(brush)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.4f)
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(brush)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(brush)
                            )
                        }
                        if (it < 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}
