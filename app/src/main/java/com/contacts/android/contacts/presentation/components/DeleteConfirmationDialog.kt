package com.contacts.android.contacts.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.contacts.android.contacts.R

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
    title: String? = null,
    message: String? = null,
    icon: ImageVector = Icons.Default.Warning,
    confirmText: String? = null,
    dismissText: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val actualTitle = title ?: stringResource(R.string.contact_delete)
    val actualMessage = message ?: stringResource(R.string.delete_contact_default_message)
    val actualConfirmText = confirmText ?: stringResource(R.string.action_delete)
    val actualDismissText = dismissText ?: stringResource(R.string.action_cancel)

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
                text = actualTitle,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = actualMessage)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(
                    text = actualConfirmText,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = actualDismissText)
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
    val pluralSuffix = if (count != 1) "s" else ""
    DeleteConfirmationDialog(
        title = stringResource(R.string.delete_multiple_contacts, count, pluralSuffix),
        message = stringResource(R.string.delete_multiple_confirmation, count, pluralSuffix),
        icon = Icons.Default.Delete,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
