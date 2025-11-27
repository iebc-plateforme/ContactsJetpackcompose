package com.contacts.android.contacts.presentation.screens.qrcode

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.contacts.android.contacts.R
import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.presentation.util.VCardUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeGenerateScreen(
    contact: Contact,
    onNavigateBack: () -> Unit,
    onShareQRCode: (Bitmap) -> Unit
) {
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(contact) {
        try {
            isLoading = true
            error = null
            val bitmap = generateQRCode(contact)
            qrCodeBitmap = bitmap
        } catch (e: Exception) {
            error = e.message ?: "Failed to generate QR code"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.qr_code_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    if (qrCodeBitmap != null) {
                        IconButton(onClick = { qrCodeBitmap?.let { onShareQRCode(it) } }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = stringResource(R.string.share_contact)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                error != null -> {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                qrCodeBitmap != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = contact.displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.qr_code_scan_instruction),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        Box(
                            modifier = Modifier
                                .size(300.dp)
                                .background(Color.White)
                                .padding(16.dp)
                        ) {
                            Image(
                                bitmap = qrCodeBitmap!!.asImageBitmap(),
                                contentDescription = stringResource(R.string.qr_code_description),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun generateQRCode(contact: Contact): Bitmap = withContext(Dispatchers.Default) {
    val vCardData = VCardUtils.encodeToVCard(contact)
    val writer = QRCodeWriter()
    val size = 512
    val bitMatrix = writer.encode(vCardData, BarcodeFormat.QR_CODE, size, size)

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    bitmap
}
