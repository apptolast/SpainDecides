package com.apptolast.spaindecides.presentation.ui.screens.proposals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apptolast.spaindecides.presentation.viewmodel.ProposalViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.close
import spaindecides.composeapp.generated.resources.create_proposal_description_counter
import spaindecides.composeapp.generated.resources.create_proposal_description_label
import spaindecides.composeapp.generated.resources.create_proposal_description_placeholder
import spaindecides.composeapp.generated.resources.create_proposal_publish
import spaindecides.composeapp.generated.resources.create_proposal_title
import spaindecides.composeapp.generated.resources.create_proposal_title_counter
import spaindecides.composeapp.generated.resources.create_proposal_title_label
import spaindecides.composeapp.generated.resources.create_proposal_title_placeholder

/**
 * Create proposal screen - Form to create a new proposal.
 *
 * @param categoryId ID of the category to create the proposal in (UUID from database)
 * @param categoryKey i18n key for resolving localized name (e.g., "economy", "health")
 * @param onClose Callback to close the screen
 * @param onProposalCreated Callback when proposal is successfully created
 * @param viewModel Proposal ViewModel (injected via Koin with categoryId parameter)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProposalScreen(
    categoryId: String,
    categoryKey: String,
    viewModel: ProposalViewModel = koinViewModel { parametersOf(categoryId) },
    onClose: () -> Unit,
    onProposalCreated: () -> Unit,
) {
//    // Reconstruct minimal Category object for using extension functions
//    val category = Category(
//        id = categoryId,
//        key = categoryKey,
//        iconName = "", // Not needed for CreateProposalScreen
//        sortOrder = 0  // Not needed for CreateProposalScreen
//    )

    val proposalTitle by viewModel.newProposalTitle.collectAsStateWithLifecycle()
    val proposalDescription by viewModel.newProposalDescription.collectAsStateWithLifecycle()
    val isCreating by viewModel.isCreating.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    // ===== NUEVO: Estados para duplicados =====
    val showDuplicatesDialog by viewModel.showDuplicatesDialog.collectAsStateWithLifecycle()
    val duplicates by viewModel.duplicatesFound.collectAsStateWithLifecycle()
    // ==========================================

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Clear fields when screen is opened
    LaunchedEffect(Unit) {
        viewModel.clearNewProposalFields()
    }

    // ===== NUEVO: Diálogo de duplicados =====
    if (showDuplicatesDialog && duplicates.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDuplicatesDialog() },
            title = {
                Text("Propuestas similares encontradas")
            },
            text = {
                Column {
                    Text(
                        text = "Hemos encontrado propuestas parecidas a la tuya. ¿Quieres apoyar alguna existente?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    duplicates.forEach { duplicate ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.selectExistingProposal(duplicate.id)
                                    onProposalCreated() // Cerrar pantalla
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = duplicate.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = duplicate.description.take(100) + if (duplicate.description.length > 100) "..." else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${(duplicate.similarity * 100).toInt()}% similar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.createIgnoringDuplicates()
                        onProposalCreated() // Cerrar pantalla después de crear
                    }
                ) {
                    Text("Crear de todos modos")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDuplicatesDialog() }) {
                    Text("Cancelar")
                }
            }
        )
    }
    // =========================================

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.create_proposal_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(Res.string.close)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val success = viewModel.createProposal()
                                if (success) {
                                    onProposalCreated()
                                } else if (error != null && !showDuplicatesDialog) {
                                    // Solo mostrar snackbar si hay error Y no es por duplicados
                                    snackbarHostState.showSnackbar(error!!)
                                }
                                // Si hay duplicados, el diálogo se muestra automáticamente
                            }
                        },
                        enabled = !isCreating && proposalTitle.isNotBlank() && proposalDescription.isNotBlank()
                    ) {
                        Text(stringResource(Res.string.create_proposal_publish))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
//            // Category chip
//            Surface(
//                shape = MaterialTheme.shapes.small,
//                color = MaterialTheme.colorScheme.primaryContainer
//            ) {
//                Text(
//                    text = stringResource(
//                        Res.string.create_proposal_category,
//                        category.getLocalizedName()
//                    ),
//                    style = MaterialTheme.typography.labelMedium,
//                    color = MaterialTheme.colorScheme.onPrimaryContainer,
//                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))

            // Title label
            Text(
                text = stringResource(Res.string.create_proposal_title_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Title input field
            OutlinedTextField(
                value = proposalTitle,
                onValueChange = viewModel::updateNewProposalTitle,
                placeholder = { Text(stringResource(Res.string.create_proposal_title_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCreating,
                singleLine = true,
                maxLines = 1,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Title character counter
            Text(
                text = stringResource(
                    Res.string.create_proposal_title_counter,
                    viewModel.titleCharacterCount
                ),
                style = MaterialTheme.typography.bodySmall,
                color = if (viewModel.titleCharacterCount > 100) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description label
            Text(
                text = stringResource(Res.string.create_proposal_description_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description input field
            OutlinedTextField(
                value = proposalDescription,
                onValueChange = viewModel::updateNewProposalDescription,
                placeholder = { Text(stringResource(Res.string.create_proposal_description_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                enabled = !isCreating,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description character counter
            Text(
                text = stringResource(
                    Res.string.create_proposal_description_counter,
                    viewModel.descriptionCharacterCount
                ),
                style = MaterialTheme.typography.bodySmall,
                color = if (viewModel.descriptionCharacterCount > 1000) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.align(Alignment.End)
            )

            if (isCreating) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
