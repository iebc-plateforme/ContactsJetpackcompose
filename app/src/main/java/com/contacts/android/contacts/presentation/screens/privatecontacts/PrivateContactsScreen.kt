package com.contacts.android.contacts.presentation.screens.privatecontacts

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contacts.R
import com.contacts.android.contacts.presentation.components.ContactAvatar
import com.contacts.android.contacts.presentation.components.AvatarSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateContactsScreen(
    onNavigateBack: () -> Unit,
    onContactClick: (Long) -> Unit,
    viewModel: PrivateContactsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Try biometric on launch if available
    LaunchedEffect(Unit) {
        if (state.showPinEntry && viewModel.isBiometricEnabled()) {
            val activity = context as? FragmentActivity ?: return@LaunchedEffect
            val biometricManager = BiometricManager.from(context)
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                val executor = ContextCompat.getMainExecutor(context)
                val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        viewModel.onBiometricSuccess()
                    }
                })
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(context.getString(R.string.unlock_private_contacts))
                    .setNegativeButtonText(context.getString(R.string.enter_pin))
                    .build()
                prompt.authenticate(promptInfo)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.private_contacts)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_cancel))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.showPinSetup -> {
                    PinSetupContent(
                        error = state.error,
                        onSetupPin = { pin, confirm ->
                            viewModel.setupPin(pin, confirm)
                        },
                        onClearError = { viewModel.clearError() }
                    )
                }
                state.showPinEntry -> {
                    PinEntryContent(
                        error = state.error,
                        onVerifyPin = { viewModel.verifyPin(it) },
                        onClearError = { viewModel.clearError() }
                    )
                }
                state.isAuthenticated -> {
                    if (state.contacts.isEmpty() && !state.isLoading) {
                        EmptyPrivateContacts()
                    } else {
                        PrivateContactsList(
                            contacts = state.contacts,
                            isLoading = state.isLoading,
                            onContactClick = onContactClick,
                            onMakePublic = { viewModel.makeContactPublic(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PinSetupContent(
    error: String?,
    onSetupPin: (String, String) -> Boolean,
    onClearError: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.set_pin),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.setup_pin_first),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = {
                pin = it.filter { c -> c.isDigit() }.take(6)
                onClearError()
            },
            label = { Text(stringResource(R.string.enter_pin)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPin,
            onValueChange = {
                confirmPin = it.filter { c -> c.isDigit() }.take(6)
                onClearError()
            },
            label = { Text(stringResource(R.string.confirm_pin)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = error != null
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (error) {
                    "pin_mismatch" -> stringResource(R.string.pin_mismatch)
                    "pin_too_short" -> stringResource(R.string.pin_too_short)
                    else -> error
                },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onSetupPin(pin, confirmPin) },
            enabled = pin.length >= 4,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.set_pin))
        }
    }
}

@Composable
private fun PinEntryContent(
    error: String?,
    onVerifyPin: (String) -> Boolean,
    onClearError: () -> Unit
) {
    var pin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.unlock_private_contacts),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = {
                pin = it.filter { c -> c.isDigit() }.take(6)
                onClearError()
            },
            label = { Text(stringResource(R.string.enter_pin)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = error != null
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (error) {
                    "wrong_pin" -> stringResource(R.string.wrong_pin)
                    else -> error
                },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onVerifyPin(pin)
                if (error != null) pin = ""
            },
            enabled = pin.length >= 4,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.unlock_private_contacts))
        }
    }
}

@Composable
private fun EmptyPrivateContacts() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.VisibilityOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_private_contacts),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.no_private_contacts_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PrivateContactsList(
    contacts: List<com.contacts.android.contacts.domain.model.Contact>,
    isLoading: Boolean,
    onContactClick: (Long) -> Unit,
    onMakePublic: (Long) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(contacts, key = { it.id }) { contact ->
            var showMenu by remember { mutableStateOf(false) }

            ListItem(
                headlineContent = {
                    Text(
                        text = contact.displayName,
                        fontWeight = FontWeight.Medium
                    )
                },
                supportingContent = if (contact.phoneNumbers.isNotEmpty()) {
                    { Text(contact.phoneNumbers.first().number) }
                } else null,
                leadingContent = {
                    ContactAvatar(
                        name = contact.displayName,
                        photoUri = contact.photoUri,
                        size = AvatarSize.Small
                    )
                },
                trailingContent = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.make_public)) },
                                onClick = {
                                    showMenu = false
                                    onMakePublic(contact.id)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.LockOpen, contentDescription = null)
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.animateItem()
            )
        }
    }
}
