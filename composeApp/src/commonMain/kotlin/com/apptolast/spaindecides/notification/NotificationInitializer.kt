package com.apptolast.spaindecides.notification

import com.apptolast.spaindecides.data.remote.Environment
import com.mmk.kmpnotifier.KMPNotifier
import com.mmk.kmpnotifier.notification.PayloadData
import com.mmk.kmpnotifier.push.PushListener
import com.mmk.kmpnotifier.push.firebase.addPushListener
import com.mmk.kmpnotifier.push.firebase.firebasePushNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Common notification initializer interface.
 * Platform-specific implementations handle FCM setup.
 */
object NotificationInitializer {

    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Subscribes the device to the environment-specific new proposals topic.
     * Dev builds subscribe to "new_proposals_dev", production to "new_proposals_prod".
     */
    fun subscribeToNewProposalsTopic() {
        val topic = Environment.FCM_TOPIC_NEW_PROPOSALS
        println("[FCM] Requesting subscription to topic: $topic")
        scope.launch {
            try {
                KMPNotifier.firebasePushNotifier.subscribeToTopic(topic)
                println("[FCM] Successfully subscribed to topic: $topic")
            } catch (e: Exception) {
                println("[FCM] ERROR subscribing to topic '$topic': ${e.message}")
            }
        }
    }

    /**
     * Sets up a listener for incoming push notifications.
     * @param onNotificationReceived Callback when a notification is received
     */
    fun setNotificationListener(onNotificationReceived: (title: String?, body: String?) -> Unit) {
        println("[FCM] Setting up notification listener")
        KMPNotifier.addPushListener(object : PushListener {
            override fun onNewToken(token: String) {
                println("[FCM] New token received: $token")
            }

            override fun onPayloadData(data: PayloadData) {
                println("[FCM] Payload data received: $data")
            }

            override fun onPushNotification(title: String?, body: String?) {
                println("[FCM] Push notification received - title: $title, body: $body")
                super.onPushNotification(title, body)
                onNotificationReceived(title, body)
            }
        })
    }
}
