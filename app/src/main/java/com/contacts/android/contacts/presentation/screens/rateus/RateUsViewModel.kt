package com.contacts.android.contacts.presentation.screens.rateus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contacts.android.contacts.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RateUsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _showRateDialog = MutableStateFlow(false)
    val showRateDialog = _showRateDialog.asStateFlow()

    private val _showThankYouDialog = MutableStateFlow(false)
    val showThankYouDialog = _showThankYouDialog.asStateFlow()

    fun onAppStart() {
        viewModelScope.launch {
            val isRated = userPreferences.isRatingCompleted.first()
            if (!isRated) {
                userPreferences.incrementAppOpenCount()
                val count = userPreferences.appOpenCount.first()

                // Déclencher au 3ème lancement (ou multiples si vous voulez insister plus tard : count % 3 == 0)
                if (count == 3) {
                    _showRateDialog.value = true
                }
            }
        }
    }

    fun onRateSubmit(stars: Int, openPlayStore: () -> Unit) {
        viewModelScope.launch {
            _showRateDialog.value = false
            userPreferences.setRatingCompleted(true)

            if (stars >= 4) {
                // Redirection vers le Play Store
                openPlayStore()
            } else {
                // Afficher le message de remerciement interne
                _showThankYouDialog.value = true
            }
        }
    }

    fun dismissRateDialog() {
        // L'utilisateur a annulé, on pourra lui redemander plus tard
        // ou on considère ça comme fait selon votre préférence.
        // Ici, on ferme juste le dialogue.
        _showRateDialog.value = false
    }

    fun dismissThankYouDialog() {
        _showThankYouDialog.value = false
    }
}