package com.contacts.android.contacts.presentation.screens.contactdetail

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contacts.R
import com.contacts.android.contacts.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    onNavigateBack: () -> Unit,
    onEditContact: (Long) -> Unit,
    onShowQRCode: ((Long) -> Unit)? = null,
    viewModel: ContactDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    // Scroll behavior for collapsible header
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Animations
    val favoriteScale by animateFloatAsState(
        targetValue = if (state.contact?.isFavorite == true) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "favorite_scale"
    )

    // Handle navigation events using IntentHelper
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is ContactDetailViewModel.NavigationEvent.NavigateBack -> {
                    onNavigateBack()
                }
                is ContactDetailViewModel.NavigationEvent.Call -> {
                    com.contacts.android.contacts.presentation.util.IntentHelper.callPhoneNumber(
                        context,
                        event.phoneNumber
                    )
                }
                is ContactDetailViewModel.NavigationEvent.Message -> {
                    com.contacts.android.contacts.presentation.util.IntentHelper.sendSms(
                        context,
                        event.phoneNumber
                    )
                }
                is ContactDetailViewModel.NavigationEvent.Email -> {
                    com.contacts.android.contacts.presentation.util.IntentHelper.sendEmail(
                        context,
                        event.email
                    )
                }
                is ContactDetailViewModel.NavigationEvent.ShareVCard -> {
                    // Share contact as vCard file (with photo if available)
                    val chooserIntent = Intent.createChooser(event.shareIntent, "Share contact")
                    context.startActivity(chooserIntent)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    // Show contact name when collapsed
                    Text(
                        text = state.contact?.displayName ?: stringResource(id = R.string.nav_contacts),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateBack()
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.action_back))
                    }
                },
                actions = {
                    state.contact?.let { contact ->
                        // Favorite button with animation
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onEvent(ContactDetailEvent.ToggleFavorite)
                            }
                        ) {
                            Icon(
                                imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = if (contact.isFavorite) stringResource(id = R.string.remove) else stringResource(id = R.string.favorites_add),
                                tint = if (contact.isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                                modifier = Modifier.scale(favoriteScale)
                            )
                        }

                        // Edit button
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onEditContact(contact.id)
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(id = R.string.action_edit))
                        }

                        // More options
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showMenu = true
                            }
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.more_options))
                        }

                        // Enhanced dropdown menu
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.share_contact)) },
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showMenu = false
                                    viewModel.onEvent(ContactDetailEvent.ShareContact)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                            if (onShowQRCode != null) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.show_qr_code)) },
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showMenu = false
                                        onShowQRCode(contact.id)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.QrCode2,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.contact_delete), color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showMenu = false
                                    viewModel.onEvent(ContactDetailEvent.ShowDeleteDialog)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    ContactDetailSkeletonLoader()
                }
                state.contact != null -> {
                    ContactDetailContent(
                        contact = state.contact!!,
                        onEvent = viewModel::onEvent
                    )
                }
                state.error != null -> {
                    EmptyState(
                        icon = Icons.Default.Error,
                        title = stringResource(R.string.error),
                        description = state.error
                    )
                }
            }
        }
    }

    // Enhanced Delete confirmation dialog
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onEvent(ContactDetailEvent.HideDeleteDialog)
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    stringResource(id = R.string.contact_delete),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.delete_contact_confirmation, state.contact?.displayName ?: ""),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(id = R.string.delete_contact_confirmation_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onEvent(ContactDetailEvent.DeleteContact)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.action_delete))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onEvent(ContactDetailEvent.HideDeleteDialog)
                    }
                ) {
                    Text(stringResource(id = R.string.action_cancel))
                }
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }

    // Full-screen photo dialog
    if (showPhotoDialog && state.contact?.photoUri != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showPhotoDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showPhotoDialog = false },
                contentAlignment = Alignment.Center
            ) {
                coil.compose.AsyncImage(
                    model = state.contact!!.photoUri,
                    contentDescription = stringResource(R.string.contact_details),
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )

                // Close button
                IconButton(
                    onClick = { showPhotoDialog = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_close),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactDetailContent(
    contact: com.contacts.android.contacts.domain.model.Contact,
    onEvent: (ContactDetailEvent) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Fade-in animation for content
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)) +
                slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(durationMillis = 300)
                )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                    // Enhanced Header with avatar, gradient background and badge
                    item {
                        ContactDetailHeader(
                            contact = contact,
                            onEvent = onEvent,
                            onPhotoClick = { showPhotoDialog = true }
                        )
                    }

                    // Phone numbers with card
                    if (contact.phoneNumbers.isNotEmpty()) {
                        item {
                Spacer(modifier = Modifier.height(24.dp))
                ContactSectionCard(
                    title = stringResource(id = R.string.phone),
                    icon = Icons.Default.Phone,
                    iconTint = MaterialTheme.colorScheme.primary
                ) {
                    contact.phoneNumbers.forEachIndexed { index, phone ->
                        PhoneNumberItem(
                            phoneNumber = phone.number,
                            type = phone.displayType,
                            onCallClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onEvent(ContactDetailEvent.CallContact(phone.number))
                            },
                            onMessageClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onEvent(ContactDetailEvent.MessageContact(phone.number))
                            },
                            isPrimary = index == 0
                        )
                        if (index < contact.phoneNumbers.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        // Emails with card
        if (contact.emails.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ContactSectionCard(
                    title = stringResource(id = R.string.email),
                    icon = Icons.Default.Email,
                    iconTint = MaterialTheme.colorScheme.secondary
                ) {
                    contact.emails.forEachIndexed { index, email ->
                        EmailItem(
                            email = email.email,
                            type = email.displayType,
                            onEmailClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onEvent(ContactDetailEvent.EmailContact(email.email))
                            },
                            isPrimary = index == 0
                        )
                        if (index < contact.emails.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        // Addresses with card
        if (contact.addresses.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ContactSectionCard(
                    title = stringResource(id = R.string.address),
                    icon = Icons.Default.LocationOn,
                    iconTint = MaterialTheme.colorScheme.tertiary
                ) {
                    contact.addresses.filter { it.isNotEmpty }.forEachIndexed { index, address ->
                        AddressItem(
                            address = address.fullAddress,
                            type = address.displayType,
                            onMapClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                com.contacts.android.contacts.presentation.util.IntentHelper.openAddressInMaps(
                                    context,
                                    address.fullAddress
                                )
                            }
                        )
                        if (index < contact.addresses.filter { it.isNotEmpty }.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        // Websites with card
        if (contact.websites.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ContactSectionCard(
                    title = stringResource(R.string.websites),
                    icon = Icons.Default.Language,
                    iconTint = MaterialTheme.colorScheme.secondary
                ) {
                    contact.websites.forEachIndexed { index, website ->
                        WebsiteItem(
                            url = website.url,
                            type = stringResource(website.type.displayNameRes),
                            onWebsiteClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(
                                    if (!website.url.startsWith("http")) "https://${website.url}" else website.url
                                ))
                                context.startActivity(intent)
                            }
                        )
                        if (index < contact.websites.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        // Instant Messages with card and social app launch
        if (contact.instantMessages.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ContactSectionCard(
                    title = stringResource(R.string.instant_messages),
                    icon = Icons.Default.Forum,
                    iconTint = MaterialTheme.colorScheme.tertiary
                ) {
                    contact.instantMessages.forEachIndexed { index, im ->
                        InstantMessageItem(
                            handle = im.handle,
                            protocol = im.protocol,
                            onMessageClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                launchSocialApp(context, im.protocol, im.handle)
                            }
                        )
                        if (index < contact.instantMessages.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        // Events (Anniversaries, etc.) with card
        if (contact.events.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ContactSectionCard(
                    title = stringResource(R.string.important_dates),
                    icon = Icons.Default.Event,
                    iconTint = MaterialTheme.colorScheme.primary
                ) {
                    contact.events.forEachIndexed { index, event ->
                        EventItem(
                            date = event.date,
                            type = stringResource(event.type.displayNameRes)
                        )
                        if (index < contact.events.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        // Notes with card
        if (!contact.notes.isNullOrBlank()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ContactSectionCard(
                    title = stringResource(id = R.string.notes),
                    icon = Icons.Default.Note,
                    iconTint = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = contact.notes,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f
                    )
                }
            }
        }

        // Ringtone with card
        if (!contact.ringtone.isNullOrBlank()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ContactSectionCard(
                    title = stringResource(R.string.ringtone),
                    icon = Icons.Default.MusicNote,
                    iconTint = MaterialTheme.colorScheme.secondary
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.custom_ringtone_set),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Groups with card
        if (contact.groups.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ContactSectionCard(
                    title = stringResource(id = R.string.groups_title),
                    icon = Icons.Default.Group,
                    iconTint = MaterialTheme.colorScheme.secondary
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        contact.groups.forEachIndexed { index, group ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        // Future: Navigate to group details
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (index < contact.groups.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }

            }

            // Fixed AdMob Banner at the bottom with edge-to-edge support
            AdMobBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                adUnitId = com.contacts.android.contacts.ads.AdMobManager.BANNER_DETAIL_CONTACT_AD_UNIT_ID
            )
        }
    }
}

// Helper composable for info cards
@Composable
private fun ContactSectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Section content
            content()
        }
    }
}

@Composable
private fun ContactDetailHeader(
    contact: com.contacts.android.contacts.domain.model.Contact,
    onEvent: (ContactDetailEvent) -> Unit,
    onPhotoClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar with favorite badge
        Box(contentAlignment = Alignment.Center) {
            ContactAvatar(
                name = contact.displayName,
                photoUri = contact.photoUri,
                size = AvatarSize.ExtraLarge,
                modifier = Modifier
                    .size(100.dp)
                    .clickable(onClick = onPhotoClick)
            )
            // Favorite badge
            if (contact.isFavorite) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-4).dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = stringResource(id = R.string.favorites_title),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Organization subtitle
        if (contact.organization != null || contact.title != null) {
            Text(
                text = listOfNotNull(contact.title, contact.organization).joinToString(" • "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Action Chips (replacing FAB)
        QuickActionChipsRow(
            contact = contact,
            onEvent = onEvent
        )
    }
}

@Composable
private fun QuickActionChipsRow(
    contact: com.contacts.android.contacts.domain.model.Contact,
    onEvent: (ContactDetailEvent) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Call chip
        if (contact.phoneNumbers.isNotEmpty()) {
            FilterChip(
                selected = false,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    contact.primaryPhone?.let { onEvent(ContactDetailEvent.CallContact(it.number)) }
                },
                label = { Text(stringResource(R.string.action_call)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    iconColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        // Message chip
        if (contact.phoneNumbers.isNotEmpty()) {
            FilterChip(
                selected = false,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    contact.primaryPhone?.let { onEvent(ContactDetailEvent.MessageContact(it.number)) }
                },
                label = { Text(stringResource(R.string.quick_action_message)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Message,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    iconColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }

        // Email chip
        if (contact.emails.isNotEmpty()) {
            FilterChip(
                selected = false,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    contact.primaryEmail?.let { onEvent(ContactDetailEvent.EmailContact(it.email)) }
                },
                label = { Text(stringResource(R.string.quick_action_email)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    iconColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(imageVector = icon, contentDescription = text)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Website Item Component
@Composable
private fun WebsiteItem(
    url: String,
    type: String,
    onWebsiteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onWebsiteClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = url,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = type,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Default.OpenInBrowser,
            contentDescription = stringResource(R.string.open_website),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

// Instant Message Item Component with Social App Icons
@Composable
private fun InstantMessageItem(
    handle: String,
    protocol: com.contacts.android.contacts.domain.model.IMProtocol,
    onMessageClick: () -> Unit
) {
    val icon = when (protocol) {
        com.contacts.android.contacts.domain.model.IMProtocol.WHATSAPP -> Icons.Default.Chat
        com.contacts.android.contacts.domain.model.IMProtocol.TELEGRAM -> Icons.Default.Send
        com.contacts.android.contacts.domain.model.IMProtocol.SIGNAL -> Icons.Default.Security
        com.contacts.android.contacts.domain.model.IMProtocol.MESSENGER -> Icons.Default.Facebook
        com.contacts.android.contacts.domain.model.IMProtocol.INSTAGRAM -> Icons.Default.PhotoCamera
        com.contacts.android.contacts.domain.model.IMProtocol.DISCORD -> Icons.Default.Forum
        com.contacts.android.contacts.domain.model.IMProtocol.SLACK -> Icons.Default.Work
        else -> Icons.Default.Chat
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onMessageClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = handle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = protocol.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.Default.Launch,
            contentDescription = stringResource(R.string.open_app),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

// Event Item Component
@Composable
private fun EventItem(
    date: String,
    type: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = type,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = try {
                    val parsedDate = java.time.LocalDate.parse(date)
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")
                    parsedDate.format(formatter)
                } catch (e: Exception) {
                    date
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Default.EventAvailable,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

// Social App Launcher Function
private fun launchSocialApp(
    context: android.content.Context,
    protocol: com.contacts.android.contacts.domain.model.IMProtocol,
    handle: String
) {
    val intent = when (protocol) {
        com.contacts.android.contacts.domain.model.IMProtocol.WHATSAPP -> {
            // WhatsApp intent - opens chat with phone number
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/${handle.replace("+", "").replace(" ", "")}")
                setPackage("com.whatsapp")
            }
        }
        com.contacts.android.contacts.domain.model.IMProtocol.TELEGRAM -> {
            // Telegram intent
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("tg://resolve?domain=$handle")
                setPackage("org.telegram.messenger")
            }
        }
        com.contacts.android.contacts.domain.model.IMProtocol.SIGNAL -> {
            // Signal intent
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sgnl://signal.me/#p/$handle")
                setPackage("org.thoughtcrime.securesms")
            }
        }
        com.contacts.android.contacts.domain.model.IMProtocol.MESSENGER -> {
            // Facebook Messenger intent
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("fb://messaging/$handle")
                setPackage("com.facebook.orca")
            }
        }
        com.contacts.android.contacts.domain.model.IMProtocol.INSTAGRAM -> {
            // Instagram intent
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("instagram://user?username=$handle")
                setPackage("com.instagram.android")
            }
        }
        com.contacts.android.contacts.domain.model.IMProtocol.DISCORD -> {
            // Discord intent
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("discord://discord.com/users/$handle")
                setPackage("com.discord")
            }
        }
        com.contacts.android.contacts.domain.model.IMProtocol.SLACK -> {
            // Slack intent
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("slack://user?team=&id=$handle")
                setPackage("com.slack")
            }
        }
        com.contacts.android.contacts.domain.model.IMProtocol.VIBER -> {
            // Viber intent
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("viber://chat?number=$handle")
                setPackage("com.viber.voip")
            }
        }
        com.contacts.android.contacts.domain.model.IMProtocol.SNAPCHAT -> {
            // Snapchat intent
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("snapchat://add/$handle")
                setPackage("com.snapchat.android")
            }
        }
        com.contacts.android.contacts.domain.model.IMProtocol.SKYPE -> {
            // Skype intent
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("skype:$handle?chat")
                setPackage("com.skype.raider")
            }
        }
        else -> {
            // Generic fallback - try to open as web URL
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://$handle")
            }
        }
    }

    try {
        context.startActivity(intent)
    } catch (e: android.content.ActivityNotFoundException) {
        // If app is not installed, try to open in Play Store or web
        val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
            data = when (protocol) {
                com.contacts.android.contacts.domain.model.IMProtocol.WHATSAPP ->
                    Uri.parse("market://details?id=com.whatsapp")
                com.contacts.android.contacts.domain.model.IMProtocol.TELEGRAM ->
                    Uri.parse("market://details?id=org.telegram.messenger")
                com.contacts.android.contacts.domain.model.IMProtocol.SIGNAL ->
                    Uri.parse("market://details?id=org.thoughtcrime.securesms")
                else -> Uri.parse("https://wa.me/${handle.replace("+", "").replace(" ", "")}")
            }
        }
        try {
            context.startActivity(fallbackIntent)
        } catch (e: Exception) {
            // Silently fail if Play Store is also not available
        }
    }
}
