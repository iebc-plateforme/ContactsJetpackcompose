package com.contacts.android.contacts.presentation.screens.contactdetail

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contacts.android.contacts.domain.usecase.contact.DeleteContactUseCase
import com.contacts.android.contacts.domain.usecase.contact.GetContactByIdUseCase
import com.contacts.android.contacts.domain.usecase.contact.ToggleFavoriteUseCase
import com.contacts.android.contacts.domain.usecase.vcf.ExportSingleContactUseCase
import com.contacts.android.contacts.util.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getContactByIdUseCase: GetContactByIdUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val exportSingleContactUseCase: ExportSingleContactUseCase,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val contactId: Long = savedStateHandle.get<Long>("contactId") ?: 0L

    private val _state = MutableStateFlow(ContactDetailState())
    val state: StateFlow<ContactDetailState> = _state.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        loadContact()
    }

    fun onEvent(event: ContactDetailEvent) {
        when (event) {
            ContactDetailEvent.ToggleFavorite -> {
                toggleFavorite()
            }
            ContactDetailEvent.ShowDeleteDialog -> {
                _state.update { it.copy(showDeleteDialog = true) }
            }
            ContactDetailEvent.HideDeleteDialog -> {
                _state.update { it.copy(showDeleteDialog = false) }
            }
            ContactDetailEvent.DeleteContact -> {
                deleteContact()
            }
            ContactDetailEvent.ShareContact -> {
                shareContact()
            }
            is ContactDetailEvent.CallContact -> {
                analyticsManager.logContactCalled()
                viewModelScope.launch {
                    _navigationEvent.emit(NavigationEvent.Call(event.phoneNumber))
                }
            }
            is ContactDetailEvent.MessageContact -> {
                analyticsManager.logContactMessaged()
                viewModelScope.launch {
                    _navigationEvent.emit(NavigationEvent.Message(event.phoneNumber))
                }
            }
            is ContactDetailEvent.EmailContact -> {
                analyticsManager.logContactEmailed()
                viewModelScope.launch {
                    _navigationEvent.emit(NavigationEvent.Email(event.email))
                }
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
                    _state.update {
                        it.copy(
                            contact = contact,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun toggleFavorite() {
        val contact = _state.value.contact ?: return
        val newFavoriteState = !contact.isFavorite
        analyticsManager.logFavoriteToggled(newFavoriteState)
        viewModelScope.launch {
            toggleFavoriteUseCase(contact.id, newFavoriteState)
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to update favorite")
                    }
                }
        }
    }

    private fun deleteContact() {
        viewModelScope.launch {
            _state.update { it.copy(showDeleteDialog = false) }
            deleteContactUseCase(contactId)
                .onSuccess {
                    analyticsManager.logContactDeleted()
                    _navigationEvent.emit(NavigationEvent.NavigateBack)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to delete contact")
                    }
                }
        }
    }

    private fun shareContact() {
        val contact = _state.value.contact ?: return
        viewModelScope.launch {
            exportSingleContactUseCase.exportContact(contact, includePhoto = true)
                .onSuccess { shareIntent ->
                    analyticsManager.logContactShared()
                    _navigationEvent.emit(NavigationEvent.ShareVCard(shareIntent))
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to share contact")
                    }
                }
        }
    }

    sealed class NavigationEvent {
        object NavigateBack : NavigationEvent()
        data class Call(val phoneNumber: String) : NavigationEvent()
        data class Message(val phoneNumber: String) : NavigationEvent()
        data class Email(val email: String) : NavigationEvent()
        data class ShareVCard(val shareIntent: Intent) : NavigationEvent()
    }
}
