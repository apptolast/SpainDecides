package com.apptolast.spaindecides.data.model

/**
 * Enum representing the reasons a user can report a proposal.
 *
 * @property displayName User-visible reason text in Spanish
 */
enum class ReportReason(val displayName: String) {
    SPAM("Spam o contenido no relevante"),
    INAPPROPRIATE("Contenido inapropiado"),
    CSAE("Abuso o explotación sexual infantil (CSAE)"),
    VIOLENCE("Contenido violento o que incita al odio"),
    MISINFORMATION("Información falsa o engañosa"),
    OTHER("Otro motivo")
}
