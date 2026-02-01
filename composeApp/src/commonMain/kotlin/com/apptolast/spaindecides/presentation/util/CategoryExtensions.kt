package com.apptolast.spaindecides.presentation.util

import androidx.compose.runtime.Composable
import com.apptolast.spaindecides.data.model.Category
import org.jetbrains.compose.resources.stringResource
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.category_economy
import spaindecides.composeapp.generated.resources.category_economy_desc
import spaindecides.composeapp.generated.resources.category_education
import spaindecides.composeapp.generated.resources.category_education_desc
import spaindecides.composeapp.generated.resources.category_environment
import spaindecides.composeapp.generated.resources.category_environment_desc
import spaindecides.composeapp.generated.resources.category_foreign_policy
import spaindecides.composeapp.generated.resources.category_foreign_policy_desc
import spaindecides.composeapp.generated.resources.category_health
import spaindecides.composeapp.generated.resources.category_health_desc
import spaindecides.composeapp.generated.resources.category_housing
import spaindecides.composeapp.generated.resources.category_housing_desc
import spaindecides.composeapp.generated.resources.category_justice
import spaindecides.composeapp.generated.resources.category_justice_desc
import spaindecides.composeapp.generated.resources.category_science
import spaindecides.composeapp.generated.resources.category_science_desc
import spaindecides.composeapp.generated.resources.category_social_policies
import spaindecides.composeapp.generated.resources.category_social_policies_desc
import spaindecides.composeapp.generated.resources.category_taxes
import spaindecides.composeapp.generated.resources.category_taxes_desc
import spaindecides.composeapp.generated.resources.category_unknown
import spaindecides.composeapp.generated.resources.empty

/**
 * Extension functions for Category model to resolve localized strings.
 *
 * These functions use Compose Multiplatform's string resources system to provide
 * internationalized (i18n) category names and descriptions based on the category key.
 */

/**
 * Returns the localized name of the category based on its key.
 *
 * @return Translated category name for the current locale
 */
@Composable
fun Category.getLocalizedName(): String {
    return stringResource(
        when (key) {
            "economy" -> Res.string.category_economy
            "health" -> Res.string.category_health
            "education" -> Res.string.category_education
            "environment" -> Res.string.category_environment
            "foreign_policy" -> Res.string.category_foreign_policy
            "justice" -> Res.string.category_justice
            "housing" -> Res.string.category_housing
            "science" -> Res.string.category_science
            "social_policies" -> Res.string.category_social_policies
            "taxes" -> Res.string.category_taxes
            else -> Res.string.category_unknown
        }
    )
}

/**
 * Returns the localized description of the category based on its key.
 *
 * @return Translated category description for the current locale
 */
@Composable
fun Category.getLocalizedDescription(): String {
    return stringResource(
        when (key) {
            "economy" -> Res.string.category_economy_desc
            "health" -> Res.string.category_health_desc
            "education" -> Res.string.category_education_desc
            "environment" -> Res.string.category_environment_desc
            "foreign_policy" -> Res.string.category_foreign_policy_desc
            "justice" -> Res.string.category_justice_desc
            "housing" -> Res.string.category_housing_desc
            "science" -> Res.string.category_science_desc
            "social_policies" -> Res.string.category_social_policies_desc
            "taxes" -> Res.string.category_taxes_desc
            else -> Res.string.empty
        }
    )
}

/**
 * Returns the localized category name for a given category key.
 * This is useful when only the key is available (e.g., from navigation routes).
 *
 * @param categoryKey The i18n key for the category (e.g., "economy", "health")
 * @return Translated category name for the current locale
 */
@Composable
fun getLocalizedCategoryName(categoryKey: String): String {
    return stringResource(
        when (categoryKey) {
            "economy" -> Res.string.category_economy
            "health" -> Res.string.category_health
            "education" -> Res.string.category_education
            "environment" -> Res.string.category_environment
            "foreign_policy" -> Res.string.category_foreign_policy
            "justice" -> Res.string.category_justice
            "housing" -> Res.string.category_housing
            "science" -> Res.string.category_science
            "social_policies" -> Res.string.category_social_policies
            "taxes" -> Res.string.category_taxes
            else -> Res.string.category_unknown
        }
    )
}
