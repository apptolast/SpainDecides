package com.apptolast.spaindecides.data.remote

expect object Environment {
    val SUPABASE_URL: String
    val SUPABASE_ANON_KEY: String
    val GOOGLE_WEB_CLIENT_ID: String

    /**
     * iOS OAuth client ID, required only by the native Google sign-in on iOS.
     * Blank on Android and on iOS builds that do not enable Google.
     */
    val GOOGLE_IOS_CLIENT_ID: String
    val N8N_WEBHOOK_PATH: String
    val FCM_TOPIC_NEW_PROPOSALS: String
}
