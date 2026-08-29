package com.apptolast.spaindecides

import android.app.Application
import com.apptolast.spaindecides.di.initKoin
import com.apptolast.spaindecides.notification.NotificationInitializer
import com.mmk.kmpnotifier.KMPNotifier
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import com.mmk.kmpnotifier.push.firebase.FirebasePush
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

        // Initialize KMPNotifier for push notifications (2.0 API with Firebase extension)
        KMPNotifier.initialize(
            NotificationPlatformConfiguration.Android(
                notificationIconResId = R.drawable.ic_notification,
                showPushNotification = true
            ),
            FirebasePush
        )

        // Subscribe to new_proposals topic to receive notifications
        NotificationInitializer.subscribeToNewProposalsTopic()

        // Set up listener to log incoming notifications
        NotificationInitializer.setNotificationListener { title, body ->
            println("[FCM] Notification callback - title: $title, body: $body")
        }
    }
}
