package com.apptolast.spaindecides.data.remote

import com.apptolast.spaindecides.BuildConfig

actual object Environment {
    actual val SUPABASE_URL: String = BuildConfig.SUPABASE_URL
    actual val SUPABASE_ANON_KEY: String = BuildConfig.SUPABASE_ANON_KEY
    actual val GOOGLE_WEB_CLIENT_ID: String = BuildConfig.GOOGLE_WEB_CLIENT_ID
}
