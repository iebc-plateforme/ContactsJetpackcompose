package com.contacts.android.contactsjetpackcompose.domain.usecase.validation

import android.util.Patterns
import javax.inject.Inject

class ValidateEmailUseCase @Inject constructor() {
    operator fun invoke(email: String): Boolean {
        if (email.isBlank()) return false
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
