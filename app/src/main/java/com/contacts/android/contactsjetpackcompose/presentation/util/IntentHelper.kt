package com.contacts.android.contactsjetpackcompose.presentation.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.contacts.android.contactsjetpackcompose.domain.model.Contact
import java.io.File

/**
 * Utility class for creating and launching intents for contact actions
 * Centralizes all intent creation logic with proper error handling
 */
object IntentHelper {

    /**
     * Launch phone dialer with the given phone number
     */
    fun callPhoneNumber(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${phoneNumber.trim()}")
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                showNoAppToast(context, "No phone app found")
            }
        } catch (e: Exception) {
            showErrorToast(context, "Failed to launch dialer")
        }
    }

    /**
     * Launch SMS app with the given phone number
     */
    fun sendSms(context: Context, phoneNumber: String, message: String = "") {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${phoneNumber.trim()}")
                if (message.isNotEmpty()) {
                    putExtra("sms_body", message)
                }
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                showNoAppToast(context, "No messaging app found")
            }
        } catch (e: Exception) {
            showErrorToast(context, "Failed to launch messaging app")
        }
    }

    /**
     * Launch email app with the given email address
     */
    fun sendEmail(context: Context, emailAddress: String, subject: String = "", body: String = "") {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${emailAddress.trim()}")
                if (subject.isNotEmpty()) {
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                }
                if (body.isNotEmpty()) {
                    putExtra(Intent.EXTRA_TEXT, body)
                }
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                showNoAppToast(context, "No email app found")
            }
        } catch (e: Exception) {
            showErrorToast(context, "Failed to launch email app")
        }
    }

    /**
     * Launch email app with multiple email addresses
     */
    fun sendEmailToMultiple(
        context: Context,
        emailAddresses: List<String>,
        subject: String = "",
        body: String = ""
    ) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, emailAddresses.toTypedArray())
                if (subject.isNotEmpty()) {
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                }
                if (body.isNotEmpty()) {
                    putExtra(Intent.EXTRA_TEXT, body)
                }
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(intent, "Send email"))
            } else {
                showNoAppToast(context, "No email app found")
            }
        } catch (e: Exception) {
            showErrorToast(context, "Failed to launch email app")
        }
    }

    /**
     * Open address in maps application
     */
    fun openAddressInMaps(context: Context, address: String) {
        try {
            val encodedAddress = Uri.encode(address.trim())
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:0,0?q=$encodedAddress")
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                showNoAppToast(context, "No maps app found")
            }
        } catch (e: Exception) {
            showErrorToast(context, "Failed to open maps")
        }
    }

    /**
     * Share contact as VCF file
     */
    fun shareContact(context: Context, vcfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                vcfFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/x-vcard"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(intent, "Share contact"))
            } else {
                showNoAppToast(context, "No app found to share contact")
            }
        } catch (e: Exception) {
            showErrorToast(context, "Failed to share contact")
        }
    }

    /**
     * Share contact as text
     */
    fun shareContactAsText(context: Context, contact: Contact) {
        try {
            val shareText = buildString {
                appendLine(contact.displayName)

                contact.phoneNumbers.forEach { phone ->
                    appendLine("${phone.type}: ${phone.number}")
                }

                contact.emails.forEach { email ->
                    appendLine("${email.type}: ${email.email}")
                }

                contact.addresses.forEach { address ->
                    appendLine("${address.type}: ${address.fullAddress}")
                }

                contact.organization?.let {
                    appendLine("Organization: $it")
                }

                contact.title?.let {
                    appendLine("Title: $it")
                }
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(intent, "Share contact"))
            } else {
                showNoAppToast(context, "No app found to share contact")
            }
        } catch (e: Exception) {
            showErrorToast(context, "Failed to share contact")
        }
    }

    /**
     * Open system settings for the app
     */
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            showErrorToast(context, "Failed to open settings")
        }
    }

    /**
     * Launch video call (using default video call handler)
     */
    fun makeVideoCall(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("tel:${phoneNumber.trim()}")
                putExtra("android.phone.extra.VIDEO_CALL", true)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Fallback to regular call
                callPhoneNumber(context, phoneNumber)
            }
        } catch (e: Exception) {
            showErrorToast(context, "Failed to make video call")
        }
    }

    private fun showNoAppToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showErrorToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
