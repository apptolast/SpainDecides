package com.apptolast.spaindecides.data.remote

import com.apptolast.spaindecides.BuildKonfig

actual object Environment {
    actual val SUPABASE_URL: String = BuildKonfig.SUPABASE_URL
    actual val SUPABASE_ANON_KEY: String = BuildKonfig.SUPABASE_ANON_KEY
    actual val GOOGLE_WEB_CLIENT_ID: String = BuildKonfig.GOOGLE_WEB_CLIENT_ID
    actual val N8N_WEBHOOK_PATH: String = BuildKonfig.N8N_WEBHOOK_PATH
    actual val FCM_TOPIC_NEW_PROPOSALS: String = BuildKonfig.FCM_TOPIC_NEW_PROPOSALS
}
