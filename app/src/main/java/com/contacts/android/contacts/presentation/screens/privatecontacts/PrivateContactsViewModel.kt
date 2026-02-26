package com.contacts.android.contacts.presentation.screens.privatecontacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contacts.android.contacts.data.local.dao.ContactDao
import com.contacts.android.contacts.data.mapper.toDomain
import com.contacts.android.contacts.data.security.PrivateContactsAuthManager
import com.contacts.android.contacts.domain.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrivateContactsState(
    val isAuthenticated: Boolean = false,
    val isPinSet: Boolean = false,
    val contacts: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
    val showPinSetup: Boolean = false,
    val showPinEntry: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PrivateContactsViewModel @Inject constructor(
    private val contactDao: ContactDao,
    private val authManager: PrivateContactsAuthManager
) : ViewModel() {

    private val _state = MutableStateFlow(PrivateContactsState())
    val state: StateFlow<PrivateContactsState> = _state.asStateFlow()

    init {
        val isPinSet = authManager.isPinSet()
        _state.update {
            it.copy(
                isPinSet = isPinSet,
                showPinSetup = !isPinSet,
                showPinEntry = isPinSet
            )
        }
    }

    fun setupPin(pin: String, confirmPin: String): Boolean {
        if (pin.length < 4) {
            _state.update { it.copy(error = "pin_too_short") }
            return false
        }
        if (pin != confirmPin) {
            _state.update { it.copy(error = "pin_mismatch") }
            return false
        }
        authManager.setPin(pin)
        _state.update {
            it.copy(
                isPinSet = true,
                isAuthenticated = true,
                showPinSetup = false,
                showPinEntry = false,
                error = null
            )
        }
        loadPrivateContacts()
        return true
    }

    fun verifyPin(pin: String): Boolean {
        if (authManager.verifyPin(pin)) {
            _state.update {
                it.copy(
                    isAuthenticated = true,
                    showPinEntry = false,
                    error = null
                )
            }
            loadPrivateContacts()
            return true
        } else {
            _state.update { it.copy(error = "wrong_pin") }
            return false
        }
    }

    fun onBiometricSuccess() {
        _state.update {
            it.copy(
                isAuthenticated = true,
                showPinEntry = false,
                error = null
            )
        }
        loadPrivateContacts()
    }

    fun isBiometricEnabled(): Boolean = authManager.isBiometricEnabled()

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun makeContactPublic(contactId: Long) {
        viewModelScope.launch {
            contactDao.togglePrivate(contactId, false)
        }
    }

    private fun loadPrivateContacts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            contactDao.getPrivateContacts().collect { contactsWithDetails ->
                val contacts = contactsWithDetails.map { it.toDomain() }
                _state.update {
                    it.copy(
                        contacts = contacts,
                        isLoading = false
                    )
                }
            }
        }
    }
}
