package com.contacts.android.contacts.presentation.screens.editcontact

import android.app.Activity
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contacts.R
import com.contacts.android.contacts.ads.AdMobManager
import com.contacts.android.contacts.data.preferences.UserPreferences
import com.contacts.android.contacts.presentation.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(
    onNavigateBack: () -> Unit,
    onContactSaved: (Long) -> Unit,
    onNavigateToPremium: () -> Unit = {},
    viewModel: EditContactViewModel = hiltViewModel(),
    adMobManager: AdMobManager? = null,
    userPreferences: UserPreferences? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showPhotoPickerDialog by remember { mutableStateOf(false) }
    var showBirthdayPickerDialog by remember { mutableStateOf(false) }
    var showPremiumUpsellDialog by remember { mutableStateOf(false) }
    var showTagInputDialog by remember { mutableStateOf(false) }
    var savedContactId by remember { mutableStateOf<Long?>(null) }
    val isPremium by userPreferences?.isPremium?.collectAsState(initial = false) ?: remember { mutableStateOf(false) }

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

    // AdMob Manager for interstitial ads
    val activity = context as? Activity

    // Handle navigation events
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is EditContactViewModel.NavigationEvent.ContactSaved -> {
                    android.widget.Toast.makeText(
                        context,
                        context.getString(R.string.contact_saved),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()

                    // For premium users, just navigate
                    if (isPremium) {
                        onContactSaved(event.contactId)
                        return@collect
                    }

                    // Check if we should show premium dialog (every 3rd save) instead of ad
                    val shouldShowPremium = userPreferences?.shouldShowPremiumDialogOnSave() ?: false

                    if (shouldShowPremium) {
                        // Show premium upsell dialog instead of ad
                        savedContactId = event.contactId
                        showPremiumUpsellDialog = true
                    } else {
                        // Show interstitial ad
                        if (adMobManager != null && activity != null) {
                            adMobManager.showInterstitialAd(
                                activity = activity,
                                onAdDismissed = { onContactSaved(event.contactId) },
                                onAdFailed = { onContactSaved(event.contactId) }
                            )
                        } else {
                            onContactSaved(event.contactId)
                        }
                    }
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
                            targetState = if (state.isEditMode) stringResource(R.string.contact_edit) else stringResource(R.string.contact_add),
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
                            text = if (state.isValid) stringResource(R.string.action_save) else "Fill required fields",
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
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel))
                    }
                },
                actions = {
                    // Favorite toggle button with animation
                    val favoriteScale by animateFloatAsState(
                        targetValue = if (state.isFavorite) 1.3f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "favorite_scale"
                    )

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onEvent(EditContactEvent.ToggleFavorite)
                        }
                    ) {
                        Icon(
                            imageVector = if (state.isFavorite)
                                Icons.Default.Star
                            else
                                Icons.Default.StarBorder,
                            contentDescription = if (state.isFavorite)
                                stringResource(R.string.remove)
                            else
                                stringResource(R.string.favorites_add),
                            tint = if (state.isFavorite)
                                MaterialTheme.colorScheme.primary
                            else
                                LocalContentColor.current,
                            modifier = Modifier.scale(favoriteScale)
                        )
                    }

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
                            Text(stringResource(R.string.action_save))
                        } else {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.action_save))
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
                                        contentDescription = if (hasPhoto) "Change photo" else stringResource(R.string.contact_add),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Name preview with animation
                        AnimatedContent(
                            targetState = (state.firstName + " " + state.lastName).trim()
                                .takeIf { it.isNotBlank() } ?: stringResource(R.string.contact_add),
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
                                text = stringResource(R.string.personal_information),
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
                            label = { Text(stringResource(R.string.contact_first_name)) },
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
                            label = { Text(stringResource(R.string.contact_last_name)) },
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
                SectionHeader(text = stringResource(R.string.phone))
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
                    canRemove = state.phoneNumbers.size > 1,
                    errorResId = state.phoneValidationErrors[index]
                )
            }

            item {
                AddFieldButton(
                    text = stringResource(R.string.add_phone_number),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onEvent(EditContactEvent.AddPhoneNumber)
                    }
                )
            }

            // Email section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(text = stringResource(R.string.email))
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
                    canRemove = state.emails.size > 1,
                    errorResId = state.emailValidationErrors[index]
                )
            }

            item {
                AddFieldButton(
                    text = stringResource(R.string.add_email),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onEvent(EditContactEvent.AddEmail)
                    }
                )
            }

            // Address section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(text = stringResource(R.string.address))
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
                    text = stringResource(R.string.add_address),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onEvent(EditContactEvent.AddAddress)
                    }
                )
            }

            // Organization section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(text = stringResource(R.string.organization))
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
                        label = { Text(stringResource(R.string.contact_organization)) },
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
                        label = { Text(stringResource(R.string.contact_title)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.birthday,
                        onValueChange = {
                            viewModel.onEvent(EditContactEvent.BirthdayChanged(it))
                        },
                        label = { Text(stringResource(R.string.birthday)) },
                        placeholder = { Text(stringResource(R.string.birthday_format)) },
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
                                    contentDescription = stringResource(R.string.pick_date),
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
                SectionHeader(text = stringResource(R.string.notes))
            }

            item {
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = {
                        viewModel.onEvent(EditContactEvent.NotesChanged(it))
                    },
                    label = { Text(stringResource(R.string.notes)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .heightIn(min = 120.dp),
                    maxLines = 5
                )
            }

            // Tags section (Premium feature)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(
                    text = stringResource(R.string.tags),
                    trailing = if (!isPremium) {
                        {
                            AssistChip(
                                onClick = onNavigateToPremium,
                                label = { Text("Premium", style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    } else null
                )
            }

            item {
                if (isPremium) {
                    TagChipsRow(
                        tags = state.tags,
                        onRemoveTag = { viewModel.onEvent(EditContactEvent.RemoveTag(it)) },
                        onAddTagClick = { showTagInputDialog = true }
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.tags_premium_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                        Text(stringResource(R.string.dismiss))
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
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBirthdayPickerDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Tag input dialog
    if (showTagInputDialog) {
        TagInputDialog(
            availableTags = state.availableTags,
            currentTags = state.tags,
            onAddTag = { viewModel.onEvent(EditContactEvent.AddTag(it)) },
            onDismiss = { showTagInputDialog = false }
        )
    }

    // Premium upsell dialog after saving contact (shown every 3rd save)
    if (showPremiumUpsellDialog) {
        PremiumUpsellAfterSaveDialog(
            onUpgrade = {
                showPremiumUpsellDialog = false
                savedContactId?.let { onContactSaved(it) }
                onNavigateToPremium()
            },
            onDismiss = {
                showPremiumUpsellDialog = false
                savedContactId?.let { onContactSaved(it) }
            }
        )
    }
}

/**
 * Premium upsell dialog shown after saving a contact
 * More friendly than an interstitial ad
 */
@Composable
private fun PremiumUpsellAfterSaveDialog(
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.premium_after_save_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.premium_after_save_message),
                    style = MaterialTheme.typography.bodyMedium
                )

                // Benefits
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.premium_benefit_no_ads),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.premium_benefit_themes),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Price highlight
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.premium_prompt_price, "$4.99"),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onUpgrade) {
                Icon(
                    Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.see_premium))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.maybe_later))
            }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}
