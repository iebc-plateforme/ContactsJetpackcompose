package com.contacts.android.contacts.presentation.screens.editcontact

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contacts.android.contacts.data.local.dao.TagDao
import com.contacts.android.contacts.data.local.entity.TagEntity
import com.contacts.android.contacts.domain.model.*
import com.contacts.android.contacts.domain.usecase.contact.GetContactByIdUseCase
import com.contacts.android.contacts.domain.usecase.contact.SaveContactUseCase
import com.contacts.android.contacts.domain.usecase.validation.ValidateEmailUseCase
import com.contacts.android.contacts.domain.usecase.validation.ValidatePhoneNumberUseCase
import com.contacts.android.contacts.domain.usecase.validation.ValidationResult
import com.contacts.android.contacts.util.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditContactViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getContactByIdUseCase: GetContactByIdUseCase,
    private val saveContactUseCase: SaveContactUseCase,
    private val validatePhoneNumberUseCase: ValidatePhoneNumberUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val analyticsManager: AnalyticsManager,
    private val tagDao: TagDao
) : ViewModel() {

    private val contactId: Long = savedStateHandle.get<Long>("contactId") ?: 0L
    private val prefillPhoneNumber: String? = savedStateHandle.get<String>("phoneNumber")
        ?.trim()
        ?.takeIf { it.isNotBlank() }

    private val _state = MutableStateFlow(EditContactState())
    val state: StateFlow<EditContactState> = _state.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        if (contactId != 0L) {
            loadContact()
        } else {
            prefillPhoneNumber?.let { phone ->
                _state.update { state ->
                    val updatedPhones = if (state.phoneNumbers.isEmpty()) {
                        listOf(PhoneNumberInput(number = phone))
                    } else {
                        state.phoneNumbers.mapIndexed { index, input ->
                            if (index == 0 && input.number.isBlank()) input.copy(number = phone) else input
                        }
                    }
                    state.copy(phoneNumbers = updatedPhones)
                }
                // Validate the prefilled phone number
                validatePhoneNumber(0, phone)
            }
        }
        loadAvailableTags()
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
                _state.update { currentState ->
                    // Remove the phone number and update validation errors
                    val newPhones = currentState.phoneNumbers.filterIndexed { index, _ -> index != event.index }
                    val newErrors = currentState.phoneValidationErrors
                        .filterKeys { it != event.index }
                        .mapKeys { (key, _) -> if (key > event.index) key - 1 else key }
                    currentState.copy(
                        phoneNumbers = newPhones,
                        phoneValidationErrors = newErrors
                    )
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
                // Validate the phone number
                validatePhoneNumber(event.index, event.number)
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
                _state.update { currentState ->
                    // Remove the email and update validation errors
                    val newEmails = currentState.emails.filterIndexed { index, _ -> index != event.index }
                    val newErrors = currentState.emailValidationErrors
                        .filterKeys { it != event.index }
                        .mapKeys { (key, _) -> if (key > event.index) key - 1 else key }
                    currentState.copy(
                        emails = newEmails,
                        emailValidationErrors = newErrors
                    )
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
                // Validate the email
                validateEmail(event.index, event.email)
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
                val newFavoriteState = !_state.value.isFavorite
                analyticsManager.logFavoriteToggled(newFavoriteState)
                _state.update { it.copy(isFavorite = newFavoriteState) }
            }

            // Tag events
            is EditContactEvent.AddTag -> {
                val tag = event.tag.trim()
                if (tag.isNotBlank() && tag !in _state.value.tags) {
                    _state.update { it.copy(tags = it.tags + tag) }
                }
            }
            is EditContactEvent.RemoveTag -> {
                _state.update { it.copy(tags = it.tags - event.tag) }
            }

            EditContactEvent.SaveContact -> {
                saveContact()
            }
        }
    }

    /**
     * Validate a phone number at the given index and update the error state.
     */
    private fun validatePhoneNumber(index: Int, number: String) {
        val result = validatePhoneNumberUseCase(number)
        val errorResId = when (result) {
            is ValidationResult.Invalid -> result.errorResId
            ValidationResult.Valid -> null
        }
        _state.update { currentState ->
            val newErrors = currentState.phoneValidationErrors.toMutableMap()
            if (errorResId != null) {
                newErrors[index] = errorResId
            } else {
                newErrors.remove(index)
            }
            currentState.copy(phoneValidationErrors = newErrors)
        }
    }

    /**
     * Validate an email at the given index and update the error state.
     */
    private fun validateEmail(index: Int, email: String) {
        val result = validateEmailUseCase(email)
        val errorResId = when (result) {
            is ValidationResult.Invalid -> result.errorResId
            ValidationResult.Valid -> null
        }
        _state.update { currentState ->
            val newErrors = currentState.emailValidationErrors.toMutableMap()
            if (errorResId != null) {
                newErrors[index] = errorResId
            } else {
                newErrors.remove(index)
            }
            currentState.copy(emailValidationErrors = newErrors)
        }
    }

    /**
     * Validate all fields before saving.
     */
    private fun validateAllFields() {
        val currentState = _state.value

        // Validate all phone numbers
        currentState.phoneNumbers.forEachIndexed { index, phone ->
            validatePhoneNumber(index, phone.number)
        }

        // Validate all emails
        currentState.emails.forEachIndexed { index, email ->
            validateEmail(index, email.email)
        }

        // Mark that save was attempted (to show all errors)
        _state.update { it.copy(hasAttemptedSave = true) }
    }

    private fun loadAvailableTags() {
        viewModelScope.launch {
            tagDao.getAllUniqueTags().collect { tags ->
                _state.update { it.copy(availableTags = tags) }
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
                                isFavorite = it.isFavorite,
                                tags = it.tags.map { tag -> tag.tag },
                                isLoading = false
                            )
                        }
                    }
                }
        }
    }

    private fun saveContact() {
        // Validate all fields first
        validateAllFields()

        val currentState = _state.value

        // Check if name is provided
        if (!currentState.hasRequiredName) {
            _state.update {
                it.copy(error = "Please enter at least a first or last name")
            }
            return
        }

        // Check if there are validation errors
        if (!currentState.isValid) {
            _state.update {
                it.copy(error = "Please fix the validation errors before saving")
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
                    // Save tags
                    saveTags(savedId, currentState.tags)
                    _state.update { it.copy(isSaving = false) }
                    // Log analytics event
                    if (contactId == 0L) {
                        analyticsManager.logContactCreated("manual")
                    } else {
                        analyticsManager.logEvent("contact_updated")
                    }
                    _navigationEvent.emit(NavigationEvent.ContactSaved(savedId))
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

    private suspend fun saveTags(contactId: Long, tags: List<String>) {
        tagDao.deleteAllTagsForContact(contactId)
        if (tags.isNotEmpty()) {
            tagDao.insertTags(tags.map { TagEntity(contactId = contactId, tag = it) })
        }
    }

    sealed class NavigationEvent {
        data class ContactSaved(val contactId: Long) : NavigationEvent()
    }
}
