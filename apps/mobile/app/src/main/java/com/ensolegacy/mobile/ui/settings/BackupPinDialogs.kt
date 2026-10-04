package com.ensolegacy.mobile.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

/**
 * PIN dialogs for the backup flow: [ExportPinDialog] sets the 4–6 digit PIN a
 * new backup is encrypted with; [RestorePinDialog] asks for it back. Shared by
 * Settings and the fresh-install restore prompt (both live in the activity).
 */
@Composable
private fun PinField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    enabled: Boolean,
) {
    OutlinedTextField(
        value = value,
        // Digits only, capped at 6 — the PIN rule (4–6 digits).
        onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) onValueChange(it) },
        label = { Text(label) },
        isError = isError,
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun ExportPinDialog(
    working: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val mismatch = confirm.isNotEmpty() && pin != confirm
    val valid = pin.length in 4..6 && pin == confirm

    AlertDialog(
        onDismissRequest = { if (!working) onDismiss() },
        title = { Text("Protect this backup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Set a 4–6 digit PIN. The backup is encrypted with it and restoring " +
                        "asks for it — it's how Ensō knows the collection is yours. " +
                        "There's no recovery if it's forgotten.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                PinField(pin, { pin = it }, "PIN", isError = false, enabled = !working)
                PinField(confirm, { confirm = it }, "Confirm PIN", isError = mismatch, enabled = !working)
                if (mismatch) {
                    Text(
                        text = "PINs don't match",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pin) }, enabled = valid && !working) {
                Text("Export")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !working) { Text("Cancel") }
        },
    )
}

@Composable
fun RestorePinDialog(
    displayName: String,
    error: String?,
    working: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    // Clear the field on a failed attempt so the retry starts clean.
    LaunchedEffect(error) { if (error != null) pin = "" }

    AlertDialog(
        onDismissRequest = { if (!working) onDismiss() },
        title = { Text("Backup PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "$displayName is protected. Enter the 4–6 digit PIN set when it was exported.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                PinField(pin, { pin = it }, "PIN", isError = error != null, enabled = !working)
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pin) }, enabled = pin.length in 4..6 && !working) {
                Text("Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !working) { Text("Cancel") }
        },
    )
}
