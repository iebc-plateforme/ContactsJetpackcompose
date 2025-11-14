package com.contacts.android.contactsjetpackcompose.presentation.screens.editcontact

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contactsjetpackcompose.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditContactViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var showPhotoPickerDialog by remember { mutableStateOf(false) }
    var showBirthdayPickerDialog by remember { mutableStateOf(false) }

    // Camera photo URI (temporary file for camera capture)
    val cameraPhotoUri = remember {
        val photoFile = java.io.File(
            context.cacheDir,
            "contact_photo_${System.currentTimeMillis()}.jpg"
        )
        androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, will be handled by camera launcher
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.onEvent(EditContactEvent.PhotoUriChanged(cameraPhotoUri.toString()))
        }
    }

    // Photo picker launcher (gallery)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            // Take persistable URI permission for long-term access
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Handle if permission can't be persisted
            }
            viewModel.onEvent(EditContactEvent.PhotoUriChanged(it.toString()))
        }
    }

    // Animation for save button
    val saveButtonScale by animateFloatAsState(
        targetValue = if (state.isValid && !state.isSaving) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "save_button_scale"
    )

    // Handle navigation events
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is EditContactViewModel.NavigationEvent.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Column {
                        AnimatedContent(
                            targetState = if (state.isEditMode) "Edit Contact" else "New Contact",
                            transitionSpec = {
                                (slideInVertically { it } + fadeIn()).togetherWith(
                                    slideOutVertically { -it } + fadeOut()
                                )
                            },
                            label = "title_animation"
                        ) { title ->
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = if (state.isValid) "Ready to save" else "Fill required fields",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.isValid)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateBack()
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onEvent(EditContactEvent.SaveContact)
                        },
                        enabled = state.isValid && !state.isSaving,
                        modifier = Modifier.scale(saveButtonScale)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving...")
                        } else {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save")
                        }
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Avatar and photo with gradient background
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Gradient background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar with animation
                        var avatarScale by remember { mutableStateOf(0.8f) }
                        LaunchedEffect(Unit) {
                            avatarScale = 1f
                        }

                        Box(
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            ContactAvatar(
                                name = state.firstName + " " + state.lastName,
                                photoUri = state.photoUri,
                                size = AvatarSize.ExtraLarge,
                                modifier = Modifier.scale(
                                    animateFloatAsState(
                                        targetValue = avatarScale,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "avatar_scale"
                                    ).value
                                )
                            )

                            // Camera button badge with photo picker
                            FloatingActionButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showPhotoPickerDialog = true
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .offset(x = 8.dp, y = 8.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                AnimatedContent(
                                    targetState = state.photoUri != null,
                                    transitionSpec = {
                                        (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
                                    },
                                    label = "camera_icon"
                                ) { hasPhoto ->
                                    Icon(
                                        imageVector = if (hasPhoto) Icons.Default.Edit else Icons.Default.CameraAlt,
                                        contentDescription = if (hasPhoto) "Change photo" else "Add photo",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Name preview with animation
                        AnimatedContent(
                            targetState = (state.firstName + " " + state.lastName).trim()
                                .takeIf { it.isNotBlank() } ?: "New Contact",
                            transitionSpec = {
                                (fadeIn() + slideInVertically { it / 2 }).togetherWith(
                                    fadeOut() + slideOutVertically { -it / 2 }
                                )
                            },
                            label = "name_preview"
                        ) { name ->
                            Text(
                                text = name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Name fields with card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Personal Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        OutlinedTextField(
                            value = state.firstName,
                            onValueChange = {
                                viewModel.onEvent(EditContactEvent.FirstNameChanged(it))
                            },
                            label = { Text("First name") },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = MaterialTheme.shapes.large,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.lastName,
                            onValueChange = {
                                viewModel.onEvent(EditContactEvent.LastNameChanged(it))
                            },
                            label = { Text("Last name") },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = MaterialTheme.shapes.large,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // Phone numbers section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(text = "Phone")
            }

            itemsIndexed(state.phoneNumbers) { index, phone ->
                PhoneNumberField(
                    number = phone.number,
                    type = phone.type,
                    onNumberChange = {
                        viewModel.onEvent(EditContactEvent.PhoneNumberChanged(index, it))
                    },
                    onTypeChange = {
                        viewModel.onEvent(EditContactEvent.PhoneTypeChanged(index, it))
                    },
                    onRemove = {
                        viewModel.onEvent(EditContactEvent.RemovePhoneNumber(index))
                    },
                    canRemove = state.phoneNumbers.size > 1
                )
            }

            item {
                AddFieldButton(
                    text = "Add phone number",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onEvent(EditContactEvent.AddPhoneNumber)
                    }
                )
            }

            // Email section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(text = "Email")
            }

            itemsIndexed(state.emails) { index, email ->
                EmailField(
                    email = email.email,
                    type = email.type,
                    onEmailChange = {
                        viewModel.onEvent(EditContactEvent.EmailChanged(index, it))
                    },
                    onTypeChange = {
                        viewModel.onEvent(EditContactEvent.EmailTypeChanged(index, it))
                    },
                    onRemove = {
                        viewModel.onEvent(EditContactEvent.RemoveEmail(index))
                    },
                    canRemove = state.emails.size > 1
                )
            }

            item {
                AddFieldButton(
                    text = "Add email",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onEvent(EditContactEvent.AddEmail)
                    }
                )
            }

            // Address section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(text = "Address")
            }

            itemsIndexed(state.addresses) { index, address ->
                AddressField(
                    street = address.street,
                    city = address.city,
                    state = address.state,
                    postalCode = address.postalCode,
                    country = address.country,
                    type = address.type,
                    onStreetChange = {
                        viewModel.onEvent(EditContactEvent.AddressStreetChanged(index, it))
                    },
                    onCityChange = {
                        viewModel.onEvent(EditContactEvent.AddressCityChanged(index, it))
                    },
                    onStateChange = {
                        viewModel.onEvent(EditContactEvent.AddressStateChanged(index, it))
                    },
                    onPostalCodeChange = {
                        viewModel.onEvent(EditContactEvent.AddressPostalCodeChanged(index, it))
                    },
                    onCountryChange = {
                        viewModel.onEvent(EditContactEvent.AddressCountryChanged(index, it))
                    },
                    onTypeChange = {
                        viewModel.onEvent(EditContactEvent.AddressTypeChanged(index, it))
                    },
                    onRemove = {
                        viewModel.onEvent(EditContactEvent.RemoveAddress(index))
                    },
                    canRemove = state.addresses.size > 1
                )
            }

            item {
                AddFieldButton(
                    text = "Add address",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onEvent(EditContactEvent.AddAddress)
                    }
                )
            }

            // Organization section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(text = "Organization")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = state.organization,
                        onValueChange = {
                            viewModel.onEvent(EditContactEvent.OrganizationChanged(it))
                        },
                        label = { Text("Company") },
                        leadingIcon = {
                            Icon(Icons.Default.Business, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.title,
                        onValueChange = {
                            viewModel.onEvent(EditContactEvent.TitleChanged(it))
                        },
                        label = { Text("Job title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.birthday,
                        onValueChange = {
                            viewModel.onEvent(EditContactEvent.BirthdayChanged(it))
                        },
                        label = { Text("Birthday") },
                        placeholder = { Text("YYYY-MM-DD") },
                        leadingIcon = {
                            Icon(Icons.Default.Cake, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showBirthdayPickerDialog = true
                                }
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Pick date",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = false
                    )
                }
            }

            // Notes section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(text = "Notes")
            }

            item {
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = {
                        viewModel.onEvent(EditContactEvent.NotesChanged(it))
                    },
                    label = { Text("Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .heightIn(min = 120.dp),
                    maxLines = 5
                )
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Error message
        state.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { /* Dismiss error */ }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(error)
            }
        }
    }

    // Photo Picker Dialog
    if (showPhotoPickerDialog) {
        PhotoPickerDialog(
            onDismiss = { showPhotoPickerDialog = false },
            onTakePhoto = {
                // Check camera permission and launch camera
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    when (androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.CAMERA
                    )) {
                        android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                            cameraLauncher.launch(cameraPhotoUri)
                        }
                        else -> {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    }
                } else {
                    cameraLauncher.launch(cameraPhotoUri)
                }
            },
            onChooseFromGallery = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemovePhoto = if (state.photoUri != null) {
                {
                    viewModel.onEvent(EditContactEvent.PhotoUriChanged(null))
                }
            } else null,
            hasPhoto = state.photoUri != null
        )
    }

    // Birthday Date Picker Dialog
    if (showBirthdayPickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (state.birthday.isNotBlank()) {
                try {
                    java.time.LocalDate.parse(state.birthday).toEpochDay() * 24 * 60 * 60 * 1000
                } catch (e: Exception) {
                    null
                }
            } else null
        )

        DatePickerDialog(
            onDismissRequest = { showBirthdayPickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.onEvent(EditContactEvent.BirthdayChanged(date.toString()))
                        }
                        showBirthdayPickerDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBirthdayPickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
