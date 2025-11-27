package com.contacts.android.contacts.presentation.components

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.contacts.android.contacts.R

@Composable
fun ContactInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (actionIcon != null && onActionClick != null) {
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhoneNumberItem(
    phoneNumber: String,
    type: String,
    onCallClick: () -> Unit,
    onMessageClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Phone icon
        Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onClick = { onCallClick() },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        clipboardManager.setText(AnnotatedString(phoneNumber))
                        Toast
                            .makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT)
                            .show()
                    }
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = phoneNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isPrimary) {
                    androidx.compose.material3.Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text(
                            text = "Primary",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = type,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Message button
        IconButton(
            onClick = onMessageClick
        ) {
            Icon(
                imageVector = Icons.Default.Message,
                contentDescription = stringResource(R.string.send_message),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Call button
        IconButton(
            onClick = onCallClick
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = stringResource(R.string.action_call),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmailItem(
    email: String,
    type: String,
    onEmailClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onEmailClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    clipboardManager.setText(AnnotatedString(email))
                    Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
                }
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Email icon
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isPrimary) {
                    androidx.compose.material3.Badge(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text(
                            text = "Primary",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = type,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddressItem(
    address: String,
    type: String,
    onMapClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Location icon
        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = type,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3f
            )
        }

        // Map button
        if (onMapClick != null) {
            IconButton(
                onClick = onMapClick,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = stringResource(R.string.open_in_maps),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
