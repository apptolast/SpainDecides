package com.apptolast.spaindecides.util

import platform.Foundation.NSDate
import platform.Foundation.NSISO8601DateFormatter

/**
 * Returns the current timestamp in ISO 8601 format.
 */
actual fun getCurrentTimestamp(): String {
    val formatter = NSISO8601DateFormatter()
    return formatter.stringFromDate(NSDate())
}
