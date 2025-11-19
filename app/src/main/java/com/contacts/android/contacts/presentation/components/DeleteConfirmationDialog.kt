package com.contacts.android.contacts.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight

/**
 * Confirmation dialog for destructive actions (delete contact, delete group, etc.)
 *
 * Follows Material Design guidelines for destructive action confirmations:
 * - Clear warning icon
 * - Explicit title and description
 * - Destructive action in error color
 * - Safe cancel option
 */
@Composable
fun DeleteConfirmationDialog(
    title: String = "Delete Contact",
    message: String = "Are you sure you want to delete this contact? This action cannot be undone.",
    icon: ImageVector = Icons.Default.Warning,
    confirmText: String = "Delete",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(
                    text = confirmText,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

/**
 * Batch delete confirmation dialog for multiple contacts
 */
@Composable
fun BatchDeleteConfirmationDialog(
    count: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DeleteConfirmationDialog(
        title = "Delete $count contact${if (count != 1) "s" else ""}?",
        message = "Are you sure you want to delete $count contact${if (count != 1) "s" else ""}? This action cannot be undone.",
        icon = Icons.Default.Delete,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
