package com.contacts.android.contactsjetpackcompose.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.contacts.android.contactsjetpackcompose.domain.model.Contact
import kotlinx.coroutines.delay

/**
 * Swipeable contact list item with swipe-to-delete and swipe-to-favorite gestures
 *
 * - Swipe right: Toggle favorite (yellow/primary background with star icon)
 * - Swipe left: Delete contact (red background with delete icon)
 *
 * Features:
 * - Haptic feedback on swipe thresholds
 * - Visual feedback with colored backgrounds
 * - Smooth animations
 * - Undo functionality for delete (handled by caller via snackbar)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableContactListItem(
    contact: Contact,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
    showFavoriteButton: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    showRemoveButton: Boolean = false,
    onRemoveClick: () -> Unit = {},
    showPhoneNumber: Boolean = true,
    startNameWithSurname: Boolean = false,
    formatPhoneNumbers: Boolean = true,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectionToggle: () -> Unit = {},
    onLongClick: () -> Unit = {},
    enableSwipeActions: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    var dismissDirection by remember { mutableStateOf<SwipeToDismissBoxValue?>(null) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swipe right: Toggle favorite
                    if (enableSwipeActions && !isSelectionMode) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        dismissDirection = SwipeToDismissBoxValue.StartToEnd
                        onFavoriteToggle()
                        true
                    } else {
                        false
                    }
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe left: Delete
                    if (enableSwipeActions && !isSelectionMode) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        dismissDirection = SwipeToDismissBoxValue.EndToStart
                        onDelete()
                        true
                    } else {
                        false
                    }
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
        positionalThreshold = { distance -> distance * 0.4f }
    )

    // Reset state after dismissal animation
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            delay(300)
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = enableSwipeActions && !isSelectionMode,
        enableDismissFromEndToStart = enableSwipeActions && !isSelectionMode,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    else -> Color.Transparent
                },
                animationSpec = tween(durationMillis = 200),
                label = "background_color"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    else -> Alignment.Center
                }
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = direction != null && direction != SwipeToDismissBoxValue.Settled,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> {
                            // Favorite icon
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (contact.isFavorite)
                                        Icons.Outlined.StarOutline
                                    else
                                        Icons.Default.Star,
                                    contentDescription = if (contact.isFavorite)
                                        "Remove from favorites"
                                    else
                                        "Add to favorites",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = if (contact.isFavorite) "Remove from favorites" else "Add to favorites",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        SwipeToDismissBoxValue.EndToStart -> {
                            // Delete icon
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Delete",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            ContactListItem(
                contact = contact,
                onClick = onClick,
                showFavoriteButton = showFavoriteButton,
                onFavoriteClick = onFavoriteClick,
                showRemoveButton = showRemoveButton,
                onRemoveClick = onRemoveClick,
                showPhoneNumber = showPhoneNumber,
                startNameWithSurname = startNameWithSurname,
                formatPhoneNumbers = formatPhoneNumbers,
                isSelectionMode = isSelectionMode,
                isSelected = isSelected,
                onSelectionToggle = onSelectionToggle,
                onLongClick = onLongClick
            )
        }
    }
}
