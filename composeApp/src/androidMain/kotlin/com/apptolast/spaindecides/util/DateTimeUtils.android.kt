package com.apptolast.spaindecides.util

import java.time.Instant

/**
 * Returns the current timestamp in ISO 8601 format.
 */
actual fun getCurrentTimestamp(): String = Instant.now().toString()
