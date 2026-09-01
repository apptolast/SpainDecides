package com.apptolast.spaindecides.presentation.ui.screens.auth

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import com.apptolast.baselogin.presentation.screens.components.HeaderContent
import com.apptolast.baselogin.presentation.slots.AuthScreenSlots
import com.apptolast.baselogin.presentation.slots.ForgotPasswordScreenSlots
import com.apptolast.baselogin.presentation.slots.LoginScreenSlots
import com.apptolast.baselogin.presentation.slots.RegisterScreenSlots
import com.apptolast.baselogin.presentation.slots.ResetPasswordScreenSlots
import org.jetbrains.compose.resources.stringResource
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.app_name
import spaindecides.composeapp.generated.resources.login_subtitle
import spaindecides.composeapp.generated.resources.logo_spain_decides
import spaindecides.composeapp.generated.resources.register_csae_link
import spaindecides.composeapp.generated.resources.register_csae_prefix
import spaindecides.composeapp.generated.resources.register_csae_url
import spaindecides.composeapp.generated.resources.register_privacy_policy_link
import spaindecides.composeapp.generated.resources.register_privacy_policy_prefix
import spaindecides.composeapp.generated.resources.register_privacy_policy_url
import spaindecides.composeapp.generated.resources.register_subtitle

/**
 * España Decide branding for BaseLogin's auth screens.
 *
 * Everything here goes through [MaterialTheme], so the screens follow `SpainDecidesTheme` in both
 * light and dark mode without any hardcoded colour.
 */

/**
 * Brand header: the app logo on a primary-coloured tile, the app name and a per-screen subtitle.
 */
@Composable
private fun SpainDecidesHeader(subtitle: String) {
    HeaderContent(
        drawableResource = Res.drawable.logo_spain_decides,
        appName = stringResource(Res.string.app_name),
        appSubtitle = subtitle
    )
}

/**
 * Policy acceptance checkbox carried over from the previous register screen.
 *
 * The privacy policy and CSAE links are a store requirement, so they are reproduced here instead of
 * falling back to BaseLogin's generic terms checkbox.
 */
@Composable
private fun PolicyCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val linkStyle = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline
        )
    )

    val privacyPrefix = stringResource(Res.string.register_privacy_policy_prefix)
    val privacyLinkText = stringResource(Res.string.register_privacy_policy_link)
    val privacyUrl = stringResource(Res.string.register_privacy_policy_url)
    val csaePrefix = stringResource(Res.string.register_csae_prefix)
    val csaeLinkText = stringResource(Res.string.register_csae_link)
    val csaeUrl = stringResource(Res.string.register_csae_url)

    val policyText = buildAnnotatedString {
        append(privacyPrefix)
        withLink(LinkAnnotation.Url(url = privacyUrl, styles = linkStyle)) {
            append(privacyLinkText)
        }
        append(csaePrefix)
        withLink(LinkAnnotation.Url(url = csaeUrl, styles = linkStyle)) {
            append(csaeLinkText)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(
            text = policyText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Builds the slot set handed to `authRoutesFlow`.
 *
 * Only the slots that carry branding are overridden; every other component keeps BaseLogin's
 * default, which already reads its colours and typography from the ambient Material theme.
 */
@Composable
fun rememberSpainDecidesAuthSlots(): AuthScreenSlots {
    val loginSubtitle = stringResource(Res.string.login_subtitle)
    val registerSubtitle = stringResource(Res.string.register_subtitle)

    return AuthScreenSlots(
        login = LoginScreenSlots(
            header = { SpainDecidesHeader(subtitle = loginSubtitle) }
        ),
        register = RegisterScreenSlots(
            header = { SpainDecidesHeader(subtitle = registerSubtitle) },
            termsCheckbox = { checked, onCheckedChange ->
                PolicyCheckbox(checked = checked, onCheckedChange = onCheckedChange)
            }
        ),
        forgotPassword = ForgotPasswordScreenSlots(
            header = { SpainDecidesHeader(subtitle = loginSubtitle) }
        ),
        resetPassword = ResetPasswordScreenSlots(
            header = { SpainDecidesHeader(subtitle = loginSubtitle) }
        )
    )
}
