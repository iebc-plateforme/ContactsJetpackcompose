package com.contacts.android.contacts.presentation.screens.businesscard

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class BusinessCardScanViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(BusinessCardScanState())
    val state: StateFlow<BusinessCardScanState> = _state.asStateFlow()

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun onEvent(event: BusinessCardEvent) {
        when (event) {
            is BusinessCardEvent.ImageSelected -> {
                processImage(event.uri, event.context)
            }
            is BusinessCardEvent.UpdateField -> {
                updateField(event.field, event.value)
            }
            BusinessCardEvent.SaveContact -> {
                _state.update { it.copy(showSaveConfirmation = true) }
            }
            BusinessCardEvent.ResetState -> {
                _state.update { BusinessCardScanState() }
            }
            BusinessCardEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    fun createTempImageUri(context: Context): Uri {
        val tempFile = File(context.cacheDir, "business_card_${System.currentTimeMillis()}.jpg")
        _state.update { it.copy(tempImageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            tempFile
        ))}
        return _state.value.tempImageUri!!
    }

    private fun processImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, imageUri = uri, error = null) }

            try {
                val image = InputImage.fromFilePath(context, uri)

                textRecognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val extractedData = extractBusinessCardData(visionText.text)
                        _state.update {
                            it.copy(
                                isLoading = false,
                                extractedData = extractedData
                            )
                        }
                    }
                    .addOnFailureListener { e ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to scan: ${e.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Error processing image: ${e.message}"
                    )
                }
            }
        }
    }

    private fun extractBusinessCardData(text: String): BusinessCardData {
        val lines = text.lines().filter { it.isNotBlank() }

        var name = ""
        var phone = ""
        var email = ""
        var company = ""
        var title = ""
        var address = ""

        val emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        val phonePattern = Pattern.compile("\\+?[0-9\\s\\-()]{10,}")

        lines.forEach { line ->
            when {
                // Email
                emailPattern.matcher(line).find() && email.isEmpty() -> {
                    email = emailPattern.matcher(line).run {
                        if (find()) group() else ""
                    }
                }
                // Phone
                phonePattern.matcher(line).find() && phone.isEmpty() -> {
                    phone = phonePattern.matcher(line).run {
                        if (find()) group() else ""
                    }
                }
                // Potential title (contains keywords)
                (line.contains("CEO", ignoreCase = true) ||
                 line.contains("Manager", ignoreCase = true) ||
                 line.contains("Director", ignoreCase = true) ||
                 line.contains("Engineer", ignoreCase = true) ||
                 line.contains("Developer", ignoreCase = true)) && title.isEmpty() -> {
                    title = line.trim()
                }
                // First non-empty line is likely the name
                name.isEmpty() && line.length < 50 -> {
                    name = line.trim()
                }
                // Company name (usually second or after title)
                company.isEmpty() && line.length < 50 && line != name && line != title -> {
                    company = line.trim()
                }
            }
        }

        // Address is usually multi-line, take remaining lines
        val addressLines = lines.filter { line ->
            line != name && line != phone && line != email && line != company && line != title
        }
        if (addressLines.isNotEmpty()) {
            address = addressLines.joinToString(", ")
        }

        return BusinessCardData(
            name = name,
            phone = phone,
            email = email,
            company = company,
            title = title,
            address = address
        )
    }

    private fun updateField(field: String, value: String) {
        _state.update { currentState ->
            val updatedData = currentState.extractedData?.copy(
                name = if (field == "name") value else currentState.extractedData.name,
                phone = if (field == "phone") value else currentState.extractedData.phone,
                email = if (field == "email") value else currentState.extractedData.email,
                company = if (field == "company") value else currentState.extractedData.company,
                title = if (field == "title") value else currentState.extractedData.title,
                address = if (field == "address") value else currentState.extractedData.address
            )
            currentState.copy(extractedData = updatedData)
        }
    }

    override fun onCleared() {
        super.onCleared()
        textRecognizer.close()
    }
}

data class BusinessCardScanState(
    val imageUri: Uri? = null,
    val tempImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val extractedData: BusinessCardData? = null,
    val error: String? = null,
    val showSaveConfirmation: Boolean = false
)

data class BusinessCardData(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val company: String = "",
    val title: String = "",
    val address: String = ""
)

sealed class BusinessCardEvent {
    data class ImageSelected(val uri: Uri, val context: Context) : BusinessCardEvent()
    data class UpdateField(val field: String, val value: String) : BusinessCardEvent()
    object SaveContact : BusinessCardEvent()
    object ResetState : BusinessCardEvent()
    object ClearError : BusinessCardEvent()
}
