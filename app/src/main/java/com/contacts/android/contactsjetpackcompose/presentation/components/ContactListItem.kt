package com.contacts.android.contactsjetpackcompose.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.contacts.android.contactsjetpackcompose.domain.model.Contact

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
    onLongClick: () -> Unit = {}
) {
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = if (isSelectionMode) onSelectionToggle else onClick,
                onLongClick = if (!isSelectionMode) onLongClick else null
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection mode checkbox
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectionToggle() }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        ContactAvatar(
            name = contact.displayName,
            photoUri = contact.photoUri,
            size = AvatarSize.Medium
        )

        Spacer(modifier = Modifier.width(16.dp))

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
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Show phone number based on showPhoneNumber setting (like Fossify)
            if (showPhoneNumber && displayNumber != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = displayNumber!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Don't show favorite/remove buttons in selection mode
        if (!isSelectionMode) {
            // Favorite button
            if (showFavoriteButton) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = if (contact.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (contact.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Remove button
            if (showRemoveButton) {
                IconButton(onClick = onRemoveClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
