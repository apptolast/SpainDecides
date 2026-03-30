package com.apptolast.spaindecides

import android.app.Application
import com.apptolast.spaindecides.di.initKoin
import com.apptolast.spaindecides.notification.NotificationInitializer
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/**
 * Application class for Android.
 * Initializes Koin dependency injection and push notifications.
 */
class SpainDecidesApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger() // Enable Android logging for Koin
            androidContext(this@SpainDecidesApplication) // Provide Android context
        }

        // Initialize KMPNotifier for push notifications
        NotifierManager.initialize(
            configuration = NotificationPlatformConfiguration.Android(
                notificationIconResId = R.drawable.ic_notification,
                showPushNotification = true
            )
        )

        // Subscribe to new_proposals topic to receive notifications
        NotificationInitializer.subscribeToNewProposalsTopic()

        // Set up listener to log incoming notifications
        NotificationInitializer.setNotificationListener { title, body ->
            println("[FCM] Notification callback - title: $title, body: $body")
        }
    }
}
