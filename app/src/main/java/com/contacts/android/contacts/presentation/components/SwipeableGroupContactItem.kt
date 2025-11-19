package com.contacts.android.contacts.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.contacts.android.contacts.R
import com.contacts.android.contacts.domain.model.Contact
import kotlinx.coroutines.delay

/**
 * Swipeable Contact List Item for Group Detail Screen
 *
 * - Swipe left: Remove from group (orange/warning background)
 * - Non-destructive action (contact remains in app, just removed from this group)
 * - 60% threshold for safety
 * - Progressive haptic feedback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableGroupContactItem(
    contact: Contact,
    onClick: () -> Unit,
    onRemoveFromGroup: () -> Unit,
    modifier: Modifier = Modifier,
    showPhoneNumber: Boolean = true,
    startNameWithSurname: Boolean = false,
    formatPhoneNumbers: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    var hasTriggeredMidHaptic by remember { mutableStateOf(false) }

    // 60% threshold for safety
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe left: Remove from group
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRemoveFromGroup()
                    true
                }
                else -> false
            }
        },
        positionalThreshold = { distance -> distance * 0.6f }
    )

    // Progressive haptic feedback
    LaunchedEffect(dismissState.targetValue) {
        if (dismissState.targetValue != SwipeToDismissBoxValue.Settled && !hasTriggeredMidHaptic) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            hasTriggeredMidHaptic = true
        }
    }

    // Reset state after action
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            delay(300)
            dismissState.reset()
            hasTriggeredMidHaptic = false
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false, // Only swipe left enabled
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection

            // Animated background color - orange/warning for "remove"
            val color by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> Color.Transparent
                },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "background_color"
            )

            // Icon scale animation
            val iconScale by animateFloatAsState(
                targetValue = if (direction == SwipeToDismissBoxValue.EndToStart) 1.2f else 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "icon_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = direction == SwipeToDismissBoxValue.EndToStart,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.remove),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Icon(
                            imageVector = Icons.Default.PersonRemove,
                            contentDescription = stringResource(R.string.remove),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier
                                .size(32.dp)
                                .graphicsLayer(
                                    scaleX = iconScale,
                                    scaleY = iconScale
                                )
                        )
                    }
                }
            }
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) 4.dp else 0.dp
        ) {
            ContactListItem(
                contact = contact,
                onClick = onClick,
                showRemoveButton = true,
                onRemoveClick = onRemoveFromGroup,
                showPhoneNumber = showPhoneNumber,
                startNameWithSurname = startNameWithSurname,
                formatPhoneNumbers = formatPhoneNumbers
            )
        }
    }
}

/**
 * UX NOTES:
 *
 * - Orange/tertiary color indicates "remove" (less severe than red delete)
 * - Only left swipe enabled (right swipe reserved for favorite in other screens)
 * - 60% threshold matches app-wide swipe safety standards
 * - No confirmation needed (action is reversible - can re-add to group)
 * - PersonRemove icon clearly indicates "remove from group" not "delete"
 */
