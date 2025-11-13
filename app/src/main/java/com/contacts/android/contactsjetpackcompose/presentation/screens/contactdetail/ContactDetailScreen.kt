package com.contacts.android.contactsjetpackcompose.presentation.screens.contactdetail

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contactsjetpackcompose.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    onNavigateBack: () -> Unit,
    onEditContact: (Long) -> Unit,
    viewModel: ContactDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }
    var showQuickActions by remember { mutableStateOf(false) }

    // Animations
    val favoriteScale by animateFloatAsState(
        targetValue = if (state.contact?.isFavorite == true) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "favorite_scale"
    )

    // Handle navigation events
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is ContactDetailViewModel.NavigationEvent.NavigateBack -> {
                    onNavigateBack()
                }
                is ContactDetailViewModel.NavigationEvent.Call -> {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${event.phoneNumber}")
                    }
                    context.startActivity(intent)
                }
                is ContactDetailViewModel.NavigationEvent.Message -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:${event.phoneNumber}")
                    }
                    context.startActivity(intent)
                }
                is ContactDetailViewModel.NavigationEvent.Email -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${event.email}")
                    }
                    context.startActivity(intent)
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
        topBar = {
            LargeTopAppBar(
                title = {
                    AnimatedContent(
                        targetState = state.contact?.displayName ?: "Contact",
                        transitionSpec = {
                            (slideInVertically { it } + fadeIn()).togetherWith(
                                slideOutVertically { -it } + fadeOut()
                            )
                        },
                        label = "title_animation"
                    ) { title ->
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            state.contact?.let { contact ->
                                if (contact.organization != null || contact.title != null) {
                                    Text(
                                        text = listOfNotNull(contact.title, contact.organization)
                                            .joinToString(" • "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateBack()
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                                contentDescription = if (contact.isFavorite) "Remove from favorites" else "Add to favorites",
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
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }

                        // More options
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showMenu = true
                            }
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }

                        // Enhanced dropdown menu
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Share contact") },
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
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Delete contact", color = MaterialTheme.colorScheme.error) },
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
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            // Quick action FABs
            state.contact?.let { contact ->
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Show extended actions
                    AnimatedVisibility(
                        visible = showQuickActions,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Message FAB
                            contact.primaryPhone?.let { phone ->
                                SmallFloatingActionButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.onEvent(ContactDetailEvent.MessageContact(phone.number))
                                    },
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ) {
                                    Icon(Icons.Default.Message, contentDescription = "Message")
                                }
                            }

                            // Email FAB
                            contact.primaryEmail?.let { email ->
                                SmallFloatingActionButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.onEvent(ContactDetailEvent.EmailContact(email.email))
                                    },
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = "Email")
                                }
                            }
                        }
                    }

                    // Main Call FAB
                    contact.primaryPhone?.let { phone ->
                        FloatingActionButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showQuickActions = !showQuickActions
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 10.dp
                            )
                        ) {
                            AnimatedContent(
                                targetState = showQuickActions,
                                transitionSpec = {
                                    (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
                                },
                                label = "fab_icon"
                            ) { expanded ->
                                Icon(
                                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Phone,
                                    contentDescription = if (expanded) "Close" else "Call"
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    LoadingIndicator()
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
                        title = "Error",
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
                    "Delete Contact",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Are you sure you want to delete ${state.contact?.displayName}?",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "This action cannot be undone.",
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
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onEvent(ContactDetailEvent.HideDeleteDialog)
                    }
                ) {
                    Text("Cancel")
                }
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }
}

@Composable
private fun ContactDetailContent(
    contact: com.contacts.android.contactsjetpackcompose.domain.model.Contact,
    onEvent: (ContactDetailEvent) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // Enhanced Header with avatar, gradient background and badge
        item {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Gradient background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                )

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar with scale animation
                    var avatarScale by remember { mutableStateOf(0.8f) }
                    LaunchedEffect(Unit) {
                        avatarScale = 1f
                    }

                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        ContactAvatar(
                            name = contact.displayName,
                            photoUri = contact.photoUri,
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

                        // Favorite badge
                        if (contact.isFavorite) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 8.dp, y = (-8).dp),
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Favorite",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Name with fade-in animation
                    var nameVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(100)
                        nameVisible = true
                    }

                    AnimatedVisibility(
                        visible = nameVisible,
                        enter = fadeIn() + slideInVertically { it / 2 }
                    ) {
                        Text(
                            text = contact.displayName,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Organization/Title
                    if (contact.organization != null || contact.title != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AnimatedVisibility(
                            visible = nameVisible,
                            enter = fadeIn() + slideInVertically { it / 2 }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Work,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = listOfNotNull(contact.title, contact.organization)
                                        .joinToString(" • "),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Phone numbers with card
        if (contact.phoneNumbers.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                InfoCard(
                    title = "Phone",
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
                            }
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
                InfoCard(
                    title = "Email",
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
                            }
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
                InfoCard(
                    title = "Address",
                    icon = Icons.Default.LocationOn,
                    iconTint = MaterialTheme.colorScheme.tertiary
                ) {
                    contact.addresses.filter { it.isNotEmpty }.forEachIndexed { index, address ->
                        AddressItem(
                            address = address.fullAddress,
                            type = address.displayType
                        )
                        if (index < contact.addresses.filter { it.isNotEmpty }.size - 1) {
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
                InfoCard(
                    title = "Notes",
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

        // Groups with card
        if (contact.groups.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                InfoCard(
                    title = "Groups",
                    icon = Icons.Default.Group,
                    iconTint = MaterialTheme.colorScheme.secondary
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        contact.groups.forEach { group ->
                            SuggestionChip(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                label = { Text(group.name, fontWeight = FontWeight.Medium) },
                                icon = {
                                    Icon(
                                        Icons.Default.Group,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    iconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper composable for info cards
@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Card header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Card content
            content()
        }
    }
}
