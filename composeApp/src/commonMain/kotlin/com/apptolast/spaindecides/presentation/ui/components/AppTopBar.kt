package com.apptolast.spaindecides.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptolast.spaindecides.presentation.ui.theme.SpainDecidesTheme
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.settings

/**
 * Common TopAppBar for the application with title, subtitle and action buttons.
 *
 * @param title Main title text
 * @param subtitle Optional subtitle text shown below the title
 * @param onSettingsClick Callback when settings button is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            // Settings button
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(Res.string.settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Preview
@Composable
private fun AppTopBarPreview() {
    SpainDecidesTheme {
        AppTopBar(
            title = "Spain Decides",
            subtitle = "Democracia participativa",
            onSettingsClick = {}
        )
    }
}

@Preview
@Composable
private fun AppTopBarNoSubtitlePreview() {
    SpainDecidesTheme {
        AppTopBar(
            title = "Spain Decides",
            subtitle = null,
            onSettingsClick = {}
        )
    }
}
