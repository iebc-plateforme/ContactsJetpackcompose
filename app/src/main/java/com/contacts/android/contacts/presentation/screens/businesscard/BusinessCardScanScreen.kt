package com.contacts.android.contacts.presentation.screens.businesscard

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.contacts.android.contacts.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun BusinessCardScanScreen(
    onBackClick: () -> Unit,
    onSaveContact: (BusinessCardData) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BusinessCardScanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onEvent(BusinessCardEvent.ImageSelected(it, context)) }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && state.tempImageUri != null) {
            viewModel.onEvent(BusinessCardEvent.ImageSelected(state.tempImageUri!!, context))
        }
    }

    // Show save confirmation dialog
    LaunchedEffect(state.showSaveConfirmation) {
        if (state.showSaveConfirmation && state.extractedData != null) {
            onSaveContact(state.extractedData!!)
            viewModel.onEvent(BusinessCardEvent.ResetState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.scan_business_card),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    if (state.extractedData != null) {
                        IconButton(onClick = {
                            viewModel.onEvent(BusinessCardEvent.SaveContact)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = stringResource(R.string.action_save)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.scanning_card),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                state.imageUri != null -> {
                    ScannedCardContent(
                        imageUri = state.imageUri!!,
                        extractedData = state.extractedData,
                        onRetake = {
                            viewModel.onEvent(BusinessCardEvent.ResetState)
                        },
                        onDataChanged = { field, value ->
                            viewModel.onEvent(BusinessCardEvent.UpdateField(field, value))
                        }
                    )
                }

                else -> {
                    EmptyStateWithActions(
                        onCameraClick = {
                            if (cameraPermissionState.status.isGranted) {
                                val uri = viewModel.createTempImageUri(context)
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        },
                        onGalleryClick = {
                            imagePickerLauncher.launch("image/*")
                        }
                    )
                }
            }

            // Error Snackbar
            state.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = {
                            viewModel.onEvent(BusinessCardEvent.ClearError)
                        }) {
                            Text(stringResource(R.string.dismiss))
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
private fun EmptyStateWithActions(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CreditCard,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.scan_card_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.scan_card_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Camera Button
        Button(
            onClick = onCameraClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.take_photo))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Gallery Button
        OutlinedButton(
            onClick = onGalleryClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Photo, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.choose_from_gallery))
        }
    }
}

@Composable
private fun ScannedCardContent(
    imageUri: Uri,
    extractedData: BusinessCardData?,
    onRetake: () -> Unit,
    onDataChanged: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = stringResource(R.string.scanned_card),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        // Retake button
        OutlinedButton(
            onClick = onRetake,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.retake))
        }

        if (extractedData != null) {
            HorizontalDivider()

            Text(
                text = stringResource(R.string.extracted_information),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Editable Fields
            OutlinedTextField(
                value = extractedData.name,
                onValueChange = { onDataChanged("name", it) },
                label = { Text(stringResource(R.string.full_name)) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = extractedData.phone,
                onValueChange = { onDataChanged("phone", it) },
                label = { Text(stringResource(R.string.contact_phone)) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = extractedData.email,
                onValueChange = { onDataChanged("email", it) },
                label = { Text(stringResource(R.string.contact_email)) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = extractedData.company,
                onValueChange = { onDataChanged("company", it) },
                label = { Text(stringResource(R.string.contact_organization)) },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = extractedData.title,
                onValueChange = { onDataChanged("title", it) },
                label = { Text(stringResource(R.string.contact_title)) },
                leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = extractedData.address,
                onValueChange = { onDataChanged("address", it) },
                label = { Text(stringResource(R.string.contact_address)) },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
    }
}
