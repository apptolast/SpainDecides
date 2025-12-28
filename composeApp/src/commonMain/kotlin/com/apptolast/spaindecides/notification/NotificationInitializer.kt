package com.apptolast.spaindecides.notification

import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.PayloadData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Common notification initializer interface.
 * Platform-specific implementations handle FCM setup.
 */
object NotificationInitializer {

    private const val TOPIC_NEW_PROPOSALS = "new_proposals"
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Subscribes the device to the new proposals topic.
     * All users subscribed to this topic will receive notifications
     * when new proposals are created.
     */
    fun subscribeToNewProposalsTopic() {
        scope.launch {
            NotifierManager.getPushNotifier().subscribeToTopic(TOPIC_NEW_PROPOSALS)
        }
    }

    /**
     * Sets up a listener for incoming notifications.
     * @param onNotificationReceived Callback when a notification is received
     */
    fun setNotificationListener(onNotificationReceived: (title: String?, body: String?) -> Unit) {
        NotifierManager.addListener(object : NotifierManager.Listener {
            override fun onNewToken(token: String) {
                println("FCM Token: $token")
            }

            override fun onPayloadData(data: PayloadData) {
                println("Notification payload received: $data")
            }

            override fun onPushNotification(title: String?, body: String?) {
                super.onPushNotification(title, body)
                onNotificationReceived(title, body)
            }
        })
    }
}
