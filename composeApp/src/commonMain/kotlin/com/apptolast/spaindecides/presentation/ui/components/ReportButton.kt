package com.apptolast.spaindecides.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apptolast.spaindecides.presentation.ui.theme.SpainDecidesTheme
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.report_content

/**
 * Icon button for reporting inappropriate content.
 *
 * @param onClick Callback when the button is clicked
 * @param modifier Optional modifier
 */
@Composable
fun ReportButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Outlined.Flag,
            contentDescription = stringResource(Res.string.report_content),
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Preview
@Composable
private fun ReportButtonPreview() {
    SpainDecidesTheme {
        ReportButton(onClick = {})
    }
}
