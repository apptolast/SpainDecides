package com.apptolast.spaindecides.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.apptolast.spaindecides.presentation.ui.theme.SpainDecidesTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Reusable loading dialog component
 * Shows a centered circular progress indicator with an optional message
 *
 * @param message Optional message to display below the progress indicator
 * @param onDismissRequest Called when the user tries to dismiss the dialog (should be empty for loading)
 */
@Composable
fun LoadingDialog(
    message: String? = null,
    onDismissRequest: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Box(
                modifier = Modifier.padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )

                    message?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun LoadingDialogPreview() {
    SpainDecidesTheme {
        LoadingDialog(
            message = "Cargando...",
            onDismissRequest = {}
        )
    }
}

@Preview
@Composable
private fun LoadingDialogNoMessagePreview() {
    SpainDecidesTheme {
        LoadingDialog(
            message = null,
            onDismissRequest = {}
        )
    }
}
