package com.contacts.android.contacts.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.contacts.android.contacts.R

@Composable
fun RatingDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int) -> Unit
) {
    var rating by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.rate_us_title),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.rate_us_description),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Star Row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(5) { index ->
                        val starIndex = index + 1
                        Icon(
                            imageVector = if (starIndex <= rating) Icons.Filled.Star else Icons.Filled.StarOutline,
                            contentDescription = null,
                            tint = if (starIndex <= rating) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant, // Amber color for stars
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { rating = starIndex }
                                .padding(4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(rating) },
                enabled = rating > 0
            ) {
                Text(stringResource(R.string.action_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_not_now))
            }
        }
    )
}

@Composable
fun ThankYouDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.thank_you_title)) },
        text = { Text(stringResource(R.string.thank_you_feedback_desc)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}