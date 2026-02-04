package com.apptolast.spaindecides.data.remote

expect object Environment {
    val SUPABASE_URL: String
    val SUPABASE_ANON_KEY: String
    val GOOGLE_WEB_CLIENT_ID: String
    val N8N_WEBHOOK_PATH: String
}
