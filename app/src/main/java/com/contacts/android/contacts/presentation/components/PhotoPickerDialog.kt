package com.contacts.android.contacts.presentation.components
import androidx.compose.ui.res.stringResource
import com.contacts.android.contacts.R

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Photo picker dialog with options for camera capture, gallery selection, and photo removal
 *
 * Provides a bottom sheet style dialog with three options:
 * 1. Take Photo - Opens camera to capture new photo
 * 2. Choose from Gallery - Opens photo picker to select existing photo
 * 3. Remove Photo - Removes current photo (only shown if photo exists)
 */
@Composable
fun PhotoPickerDialog(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseFromGallery: () -> Unit,
    onRemovePhoto: (() -> Unit)? = null,
    hasPhoto: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Photo,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = if (hasPhoto) stringResource(R.string.change_photo_title) else stringResource(R.string.add_photo),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Take Photo option
                PhotoOption(
                    icon = Icons.Default.CameraAlt,
                    label = stringResource(R.string.take_photo),
                    description = stringResource(R.string.capture_using_camera),
                    onClick = {
                        onDismiss()
                        onTakePhoto()
                    }
                )

                HorizontalDivider()

                // Choose from Gallery option
                PhotoOption(
                    icon = Icons.Default.PhotoLibrary,
                    label = stringResource(R.string.choose_from_gallery),
                    description = stringResource(R.string.select_from_photos),
                    onClick = {
                        onDismiss()
                        onChooseFromGallery()
                    }
                )

                // Remove Photo option (only if photo exists)
                if (hasPhoto && onRemovePhoto != null) {
                    HorizontalDivider()
                    PhotoOption(
                        icon = Icons.Default.Delete,
                        label = stringResource(R.string.remove_photo),
                        description = stringResource(R.string.delete_current_photo),
                        onClick = {
                            onDismiss()
                            onRemovePhoto()
                        },
                        isDestructive = true
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoOption(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
