package com.apptolast.spaindecides.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.apptolast.spaindecides.data.model.Category
import com.apptolast.spaindecides.presentation.ui.preview.SampleData
import com.apptolast.spaindecides.presentation.ui.theme.SpainDecidesTheme
import com.apptolast.spaindecides.presentation.util.getLocalizedDescription
import com.apptolast.spaindecides.presentation.util.getLocalizedName
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Maps icon name strings to Material Icons.
 * Icon names match those stored in the database (snake_case).
 */
fun getIconForCategory(iconName: String): ImageVector {
    return when (iconName) {
        "account_balance" -> Icons.Default.AccountBalance
        "medical_services" -> Icons.Default.Shield
        "school" -> Icons.Default.School
        "park" -> Icons.Default.Park
        "public" -> Icons.Default.Public
        "gavel" -> Icons.Default.Gavel
        "apartment" -> Icons.Default.Apartment
        "science" -> Icons.Default.Science
        "groups" -> Icons.Default.Groups
        "receipt" -> Icons.Default.Receipt
        else -> Icons.Default.Category
    }
}

/**
 * Card component for displaying a category.
 * Shows category icon, name, description, and proposal count.
 *
 * @param category The category to display
 * @param proposalCount Number of proposals in this category (null if not loaded)
 * @param onClick Callback when the card is clicked
 * @param modifier Optional modifier
 */
@Composable
fun CategoryCard(
    category: Category,
    proposalCount: Int? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon in a colored container with proposal count below
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = getIconForCategory(category.iconName),
                            contentDescription = category.getLocalizedName(),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Proposal count below the icon
                if (proposalCount != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = proposalCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.getLocalizedName(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = category.getLocalizedDescription(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Chevron right icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver propuestas",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview
@Composable
private fun CategoryCardPreview() {
    SpainDecidesTheme {
        CategoryCard(
            category = SampleData.sampleCategory,
            proposalCount = 42,
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun CategoryCardNoCountPreview() {
    SpainDecidesTheme {
        CategoryCard(
            category = SampleData.sampleCategory,
            proposalCount = null,
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun CategoryCardHealthPreview() {
    SpainDecidesTheme {
        CategoryCard(
            category = SampleData.sampleCategories[1], // Health category
            proposalCount = 128,
            onClick = {}
        )
    }
}
