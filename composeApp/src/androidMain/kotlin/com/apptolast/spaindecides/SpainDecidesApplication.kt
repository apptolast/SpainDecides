package com.apptolast.spaindecides

import android.app.Application
import com.apptolast.spaindecides.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/**
 * Application class for Android.
 * Initializes Koin dependency injection.
 */
class SpainDecidesApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger() // Enable Android logging for Koin
            androidContext(this@SpainDecidesApplication) // Provide Android context
        }
    }
}
