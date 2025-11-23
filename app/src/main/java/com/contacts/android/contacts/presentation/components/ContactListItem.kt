package com.contacts.android.contacts.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.contacts.android.contacts.R
import com.contacts.android.contacts.domain.model.Contact

/**
 * Contact List Item with context menu for actions
 *
 * INTERACTION MODEL:
 * - Tap: Navigate to contact details
 * - Long press: Show context menu with actions (favorite, delete)
 * - More button: Show context menu (alternative to long press)
 *
 * This replaces swipe gestures with a clearer, safer interaction model
 * that prevents accidental deletions.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactListItem(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showFavoriteButton: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    showRemoveButton: Boolean = false,
    onRemoveClick: () -> Unit = {},
    showPhoneNumber: Boolean = true,
    startNameWithSurname: Boolean = false,
    formatPhoneNumbers: Boolean = true,
    // Selection mode
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectionToggle: () -> Unit = {},
    onLongClick: () -> Unit = {},
    // Context menu actions
    showContextMenu: Boolean = true,
    onDelete: () -> Unit = {},
    onFavoriteToggle: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }
    // Performance optimization: Memoize computed values to avoid recomposition
    val displayName by remember(contact.firstName, contact.lastName, startNameWithSurname) {
        derivedStateOf {
            if (startNameWithSurname && contact.lastName.isNotEmpty()) {
                "${contact.lastName}, ${contact.firstName}".trim()
            } else {
                contact.displayName
            }
        }
    }

    val displayNumber by remember(contact.primaryPhone, formatPhoneNumbers) {
        derivedStateOf {
            contact.primaryPhone?.let { phone ->
                if (formatPhoneNumbers) {
                    phone.getFormattedNumber()
                } else {
                    phone.number
                }
            }
        }
    }

    Box {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = if (isSelectionMode) onSelectionToggle else onClick,
                    onLongClick = if (!isSelectionMode) {
                        {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (showContextMenu) {
                                showMenu = true
                            } else {
                                onLongClick()
                            }
                        }
                    } else null
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection mode checkbox
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectionToggle() }
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            ContactAvatar(
                name = contact.displayName,
                photoUri = contact.photoUri,
                size = AvatarSize.Medium
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (contact.isFavorite && !showFavoriteButton) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(R.string.favorites),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Show phone number based on showPhoneNumber setting (like Fossify)
                // Only show if contact has a name, otherwise the number is already shown as the main title
                val hasName = contact.firstName.isNotEmpty() || contact.lastName.isNotEmpty() || 
                              !contact.organization.isNullOrEmpty() || !contact.nickname.isNullOrEmpty()
                              
                if (showPhoneNumber && displayNumber != null && hasName) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = displayNumber!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Fossify style: No visible buttons in normal mode, only show in specific modes
            if (!isSelectionMode) {
                // Favorite button (only shown in favorites screen)
                if (showFavoriteButton) {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = if (contact.isFavorite) stringResource(R.string.remove_from_favorites) else stringResource(R.string.favorites_add),
                            tint = if (contact.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Remove button (only shown in group details)
                if (showRemoveButton) {
                    IconButton(onClick = onRemoveClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_remove),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Fossify style: NO visible More button in contact list
                // Actions accessible only via long press
            }
        }

        // Context menu dropdown
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            // Favorite/Unfavorite option
            DropdownMenuItem(
                text = {
                    Text(
                        if (contact.isFavorite)
                            stringResource(R.string.remove_from_favorites)
                        else
                            stringResource(R.string.favorites_add)
                    )
                },
                onClick = {
                    showMenu = false
                    onFavoriteToggle()
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = null,
                        tint = if (contact.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            // Delete option
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(R.string.contact_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    showMenu = false
                    onDelete()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}
