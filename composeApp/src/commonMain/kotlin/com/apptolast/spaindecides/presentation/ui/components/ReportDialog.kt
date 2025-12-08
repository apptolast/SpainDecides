package com.apptolast.spaindecides.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.apptolast.spaindecides.data.model.ReportReason
import org.jetbrains.compose.resources.stringResource
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.close
import spaindecides.composeapp.generated.resources.report_cancel
import spaindecides.composeapp.generated.resources.report_dialog_proposal
import spaindecides.composeapp.generated.resources.report_dialog_select_reason
import spaindecides.composeapp.generated.resources.report_dialog_title
import spaindecides.composeapp.generated.resources.report_submit

/**
 * Dialog for selecting a reason to report a proposal.
 *
 * @param proposalTitle Title of the proposal being reported
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when report is confirmed with selected reason
 * @param isLoading Whether the report is being submitted
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDialog(
    proposalTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (ReportReason) -> Unit,
    isLoading: Boolean = false
) {
    var selectedReason by remember { mutableStateOf<ReportReason?>(null) }

    BasicAlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.report_dialog_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(Res.string.close)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Proposal title (truncated if too long)
                val truncatedTitle = if (proposalTitle.length > 50) {
                    "${proposalTitle.take(50)}..."
                } else {
                    proposalTitle
                }
                Text(
                    text = stringResource(Res.string.report_dialog_proposal, truncatedTitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.report_dialog_select_reason),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Radio buttons for each reason
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ReportReason.entries.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedReason == reason,
                                    onClick = { selectedReason = reason },
                                    role = Role.RadioButton,
                                    enabled = !isLoading
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = null,
                                enabled = !isLoading
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = reason.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text(stringResource(Res.string.report_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedReason?.let { onConfirm(it) }
                        },
                        enabled = selectedReason != null && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(Res.string.report_submit))
                        }
                    }
                }
            }
        }
    }
}
