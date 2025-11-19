package com.contacts.android.contacts.presentation.screens.editcontact

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contacts.android.contacts.domain.model.*
import com.contacts.android.contacts.domain.usecase.contact.GetContactByIdUseCase
import com.contacts.android.contacts.domain.usecase.contact.SaveContactUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditContactViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getContactByIdUseCase: GetContactByIdUseCase,
    private val saveContactUseCase: SaveContactUseCase
) : ViewModel() {

    private val contactId: Long = savedStateHandle.get<Long>("contactId") ?: 0L

    private val _state = MutableStateFlow(EditContactState())
    val state: StateFlow<EditContactState> = _state.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        if (contactId != 0L) {
            loadContact()
        }
    }

    fun onEvent(event: EditContactEvent) {
        when (event) {
            is EditContactEvent.FirstNameChanged -> {
                _state.update { it.copy(firstName = event.value) }
            }
            is EditContactEvent.LastNameChanged -> {
                _state.update { it.copy(lastName = event.value) }
            }
            is EditContactEvent.PhotoUriChanged -> {
                _state.update { it.copy(photoUri = event.value) }
            }
            is EditContactEvent.OrganizationChanged -> {
                _state.update { it.copy(organization = event.value) }
            }
            is EditContactEvent.TitleChanged -> {
                _state.update { it.copy(title = event.value) }
            }
            is EditContactEvent.NotesChanged -> {
                _state.update { it.copy(notes = event.value) }
            }
            is EditContactEvent.BirthdayChanged -> {
                _state.update { it.copy(birthday = event.value) }
            }

            // Phone number events
            EditContactEvent.AddPhoneNumber -> {
                _state.update {
                    it.copy(phoneNumbers = it.phoneNumbers + PhoneNumberInput())
                }
            }
            is EditContactEvent.RemovePhoneNumber -> {
                _state.update {
                    it.copy(phoneNumbers = it.phoneNumbers.filterIndexed { index, _ -> index != event.index })
                }
            }
            is EditContactEvent.PhoneNumberChanged -> {
                _state.update {
                    it.copy(
                        phoneNumbers = it.phoneNumbers.mapIndexed { index, phone ->
                            if (index == event.index) phone.copy(number = event.number) else phone
                        }
                    )
                }
            }
            is EditContactEvent.PhoneTypeChanged -> {
                _state.update {
                    it.copy(
                        phoneNumbers = it.phoneNumbers.mapIndexed { index, phone ->
                            if (index == event.index) phone.copy(type = event.type) else phone
                        }
                    )
                }
            }

            // Email events
            EditContactEvent.AddEmail -> {
                _state.update {
                    it.copy(emails = it.emails + EmailInput())
                }
            }
            is EditContactEvent.RemoveEmail -> {
                _state.update {
                    it.copy(emails = it.emails.filterIndexed { index, _ -> index != event.index })
                }
            }
            is EditContactEvent.EmailChanged -> {
                _state.update {
                    it.copy(
                        emails = it.emails.mapIndexed { index, email ->
                            if (index == event.index) email.copy(email = event.email) else email
                        }
                    )
                }
            }
            is EditContactEvent.EmailTypeChanged -> {
                _state.update {
                    it.copy(
                        emails = it.emails.mapIndexed { index, email ->
                            if (index == event.index) email.copy(type = event.type) else email
                        }
                    )
                }
            }

            // Address events
            EditContactEvent.AddAddress -> {
                _state.update {
                    it.copy(addresses = it.addresses + AddressInput())
                }
            }
            is EditContactEvent.RemoveAddress -> {
                _state.update {
                    it.copy(addresses = it.addresses.filterIndexed { index, _ -> index != event.index })
                }
            }
            is EditContactEvent.AddressStreetChanged -> {
                _state.update {
                    it.copy(
                        addresses = it.addresses.mapIndexed { index, address ->
                            if (index == event.index) address.copy(street = event.value) else address
                        }
                    )
                }
            }
            is EditContactEvent.AddressCityChanged -> {
                _state.update {
                    it.copy(
                        addresses = it.addresses.mapIndexed { index, address ->
                            if (index == event.index) address.copy(city = event.value) else address
                        }
                    )
                }
            }
            is EditContactEvent.AddressStateChanged -> {
                _state.update {
                    it.copy(
                        addresses = it.addresses.mapIndexed { index, address ->
                            if (index == event.index) address.copy(state = event.value) else address
                        }
                    )
                }
            }
            is EditContactEvent.AddressPostalCodeChanged -> {
                _state.update {
                    it.copy(
                        addresses = it.addresses.mapIndexed { index, address ->
                            if (index == event.index) address.copy(postalCode = event.value) else address
                        }
                    )
                }
            }
            is EditContactEvent.AddressCountryChanged -> {
                _state.update {
                    it.copy(
                        addresses = it.addresses.mapIndexed { index, address ->
                            if (index == event.index) address.copy(country = event.value) else address
                        }
                    )
                }
            }
            is EditContactEvent.AddressTypeChanged -> {
                _state.update {
                    it.copy(
                        addresses = it.addresses.mapIndexed { index, address ->
                            if (index == event.index) address.copy(type = event.type) else address
                        }
                    )
                }
            }

            // Favorite toggle event
            EditContactEvent.ToggleFavorite -> {
                _state.update { it.copy(isFavorite = !it.isFavorite) }
            }

            EditContactEvent.SaveContact -> {
                saveContact()
            }
        }
    }

    private fun loadContact() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getContactByIdUseCase(contactId)
                .catch { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load contact"
                        )
                    }
                }
                .collect { contact ->
                    contact?.let {
                        _state.update { state ->
                            state.copy(
                                contactId = it.id,
                                firstName = it.firstName,
                                lastName = it.lastName,
                                photoUri = it.photoUri,
                                phoneNumbers = if (it.phoneNumbers.isEmpty()) {
                                    listOf(PhoneNumberInput())
                                } else {
                                    it.phoneNumbers.map { phone ->
                                        PhoneNumberInput(
                                            id = phone.id,
                                            number = phone.number,
                                            type = phone.type,
                                            label = phone.label
                                        )
                                    }
                                },
                                emails = if (it.emails.isEmpty()) {
                                    listOf(EmailInput())
                                } else {
                                    it.emails.map { email ->
                                        EmailInput(
                                            id = email.id,
                                            email = email.email,
                                            type = email.type,
                                            label = email.label
                                        )
                                    }
                                },
                                addresses = if (it.addresses.isEmpty()) {
                                    listOf(AddressInput())
                                } else {
                                    it.addresses.map { address ->
                                        AddressInput(
                                            id = address.id,
                                            street = address.street ?: "",
                                            city = address.city ?: "",
                                            state = address.state ?: "",
                                            postalCode = address.postalCode ?: "",
                                            country = address.country ?: "",
                                            type = address.type,
                                            label = address.label
                                        )
                                    }
                                },
                                organization = it.organization ?: "",
                                title = it.title ?: "",
                                notes = it.notes ?: "",
                                birthday = it.birthday ?: "",
                                isFavorite = it.isFavorite, // Load favorite status
                                isLoading = false
                            )
                        }
                    }
                }
        }
    }

    private fun saveContact() {
        val currentState = _state.value

        if (!currentState.isValid) {
            _state.update {
                it.copy(error = "Please enter at least a first or last name")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val contact = Contact(
                id = currentState.contactId,
                firstName = currentState.firstName.trim(),
                lastName = currentState.lastName.trim(),
                photoUri = currentState.photoUri,
                phoneNumbers = currentState.phoneNumbers
                    .filter { it.number.isNotBlank() }
                    .map { PhoneNumber(it.id, it.number.trim(), it.type, it.label) },
                emails = currentState.emails
                    .filter { it.email.isNotBlank() }
                    .map { Email(it.id, it.email.trim(), it.type, it.label) },
                addresses = currentState.addresses
                    .filter { input ->
                        input.street.isNotBlank() || input.city.isNotBlank() ||
                        input.state.isNotBlank() || input.postalCode.isNotBlank() ||
                        input.country.isNotBlank()
                    }
                    .map {
                        Address(
                            id = it.id,
                            street = it.street.trim().ifBlank { null },
                            city = it.city.trim().ifBlank { null },
                            state = it.state.trim().ifBlank { null },
                            postalCode = it.postalCode.trim().ifBlank { null },
                            country = it.country.trim().ifBlank { null },
                            type = it.type,
                            label = it.label
                        )
                    },
                organization = currentState.organization.trim().ifBlank { null },
                title = currentState.title.trim().ifBlank { null },
                notes = currentState.notes.trim().ifBlank { null },
                birthday = currentState.birthday.trim().ifBlank { null },
                isFavorite = currentState.isFavorite // Save favorite status
            )

            saveContactUseCase(contact)
                .onSuccess { savedId ->
                    _navigationEvent.emit(NavigationEvent.NavigateBack(savedId))
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            error = error.message ?: "Failed to save contact"
                        )
                    }
                }
        }
    }

    sealed class NavigationEvent {
        data class NavigateBack(val contactId: Long) : NavigationEvent()
    }
}
