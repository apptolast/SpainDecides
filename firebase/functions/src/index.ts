import { onRequest } from "firebase-functions/v2/https";
import { setGlobalOptions } from "firebase-functions/v2";
import { defineSecret } from "firebase-functions/params";
import * as admin from "firebase-admin";

// Initialize Firebase Admin SDK
admin.initializeApp();

// Set global options for all functions
setGlobalOptions({ region: "europe-west1" });

// Define the secret reference for API key authentication
const notificationsApiKey = defineSecret("NOTIFICATIONS_API_KEY");

/**
 * Cloud Function to send push notifications when a new proposal is created.
 * 
 * This function receives a request from the mobile app and sends a notification
 * to all devices subscribed to the "new_proposals" topic.
 * 
 * Request body:
 * {
 *   "title": "Proposal title",
 *   "body": "First sentence of description"
 * }
 * 
 * Headers:
 * - x-api-key: API key for authentication
 */
export const sendNewProposalNotification = onRequest(
    { secrets: [notificationsApiKey] },
    async (req, res) => {
    // Enable CORS
    res.set("Access-Control-Allow-Origin", "*");
    res.set("Access-Control-Allow-Methods", "POST, OPTIONS");
    res.set("Access-Control-Allow-Headers", "Content-Type, x-api-key");

    // Handle preflight requests
    if (req.method === "OPTIONS") {
        res.status(204).send("");
        return;
    }

    // Only allow POST requests
    if (req.method !== "POST") {
        res.status(405).json({ error: "Method not allowed" });
        return;
    }

    // Validate API key using the secret
    const API_KEY = notificationsApiKey.value();
    const providedApiKey = req.headers["x-api-key"];
    if (!API_KEY || providedApiKey !== API_KEY) {
        res.status(401).json({ error: "Unauthorized" });
        return;
    }

    // Validate request body
    const { title, body } = req.body;
    if (!title || !body) {
        res.status(400).json({ error: "Missing title or body" });
        return;
    }

    try {
        // Build the notification message
        const message: admin.messaging.Message = {
            topic: "new_proposals",
            notification: {
                title: title,
                body: body,
            },
            data: {
                type: "new_proposal",
                title: title,
                body: body,
            },
            // Android-specific configuration
            android: {
                priority: "high",
                notification: {
                    icon: "ic_notification",
                    color: "#FABD00",
                    channelId: "new_proposals",
                },
            },
            // iOS-specific configuration
            apns: {
                payload: {
                    aps: {
                        alert: {
                            title: title,
                            body: body,
                        },
                        badge: 1,
                        sound: "default",
                    },
                },
            },
        };

        // Send the notification
        const response = await admin.messaging().send(message);
        
        console.log("Notification sent successfully:", response);
        res.status(200).json({ 
            success: true, 
            messageId: response 
        });
    } catch (error) {
        console.error("Error sending notification:", error);
        res.status(500).json({ 
            error: "Failed to send notification",
            details: error instanceof Error ? error.message : "Unknown error"
        });
    }
});
