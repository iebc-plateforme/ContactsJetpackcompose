package com.contacts.android.contacts.presentation.util
import androidx.compose.ui.res.stringResource
import com.contacts.android.contacts.R

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestContactsPermission(
    onPermissionsGranted: @Composable () -> Unit
) {
    val contactsPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
        )
    )

    LaunchedEffect(contactsPermissions.allPermissionsGranted) {
        if (!contactsPermissions.allPermissionsGranted) {
            contactsPermissions.launchMultiplePermissionRequest()
        }
    }

    when {
        contactsPermissions.allPermissionsGranted -> {
            onPermissionsGranted()
        }
        contactsPermissions.shouldShowRationale -> {
            PermissionRationaleScreen(
                icon = Icons.Default.Contacts,
                title = "Contacts Access Required",
                description = "This app needs access to your contacts to display, create, and manage them. Without this permission, the app cannot function.",
                onRequestPermission = {
                    contactsPermissions.launchMultiplePermissionRequest()
                }
            )
        }
        else -> {
            PermissionDeniedScreen(
                icon = Icons.Default.Contacts,
                title = "Contacts Access Denied",
                description = "You have permanently denied contacts permission. Please enable it in app settings to use this app."
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestCameraPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit = {}
) {
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    LaunchedEffect(cameraPermissionState.status) {
        when {
            cameraPermissionState.status.isGranted -> {
                onPermissionGranted()
            }
            cameraPermissionState.status.shouldShowRationale -> {
                // Permission was denied, but can be requested again
            }
            else -> {
                // Permission was permanently denied
                onPermissionDenied()
            }
        }
    }

    when {
        cameraPermissionState.status.isGranted -> {
            // Permission granted, callback handled in LaunchedEffect
        }
        cameraPermissionState.status.shouldShowRationale -> {
            AlertDialog(
                onDismissRequest = onPermissionDenied,
                title = { Text(stringResource(R.string.camera_permission_required)) },
                text = { Text(stringResource(R.string.camera_permission_description)) },
                confirmButton = {
                    TextButton(onClick = {
                        cameraPermissionState.launchPermissionRequest()
                    }) {
                        Text(stringResource(R.string.grant_permission))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onPermissionDenied) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }
        else -> {
            // First time or permanently denied - request permission
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
        }
    }
}

@Composable
private fun PermissionRationaleScreen(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.grant_permission))
        }
    }
}

@Composable
private fun PermissionDeniedScreen(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.open_settings))
        }
    }
}
