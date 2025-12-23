"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.sendNewProposalNotification = void 0;
const https_1 = require("firebase-functions/v2/https");
const v2_1 = require("firebase-functions/v2");
const params_1 = require("firebase-functions/params");
const admin = require("firebase-admin");
// Initialize Firebase Admin SDK
admin.initializeApp();
// Set global options for all functions
(0, v2_1.setGlobalOptions)({ region: "europe-west1" });
// Define the secret reference for webhook authentication
const webhookSecret = (0, params_1.defineSecret)("SUPABASE_WEBHOOK_SECRET");
/**
 * Extracts the first sentence from text.
 * A sentence ends with '.', '!' or '?'
 */
function extractFirstSentence(text) {
    const trimmed = text.trim();
    const match = trimmed.match(/^[^.!?]*[.!?]/);
    if (match) {
        return match[0];
    }
    // If no sentence ending found, take first 100 characters
    return trimmed.length > 100 ? trimmed.substring(0, 100) + "..." : trimmed;
}
/**
 * Cloud Function triggered by Supabase Database Webhook.
 *
 * Sends push notifications when a new proposal is inserted.
 *
 * Headers:
 * - x-webhook-secret: Secret for authentication (configured in Supabase)
 */
exports.sendNewProposalNotification = (0, https_1.onRequest)({ secrets: [webhookSecret] }, async (req, res) => {
    // Only allow POST requests
    if (req.method !== "POST") {
        res.status(405).json({ error: "Method not allowed" });
        return;
    }
    // Validate webhook secret
    const SECRET = webhookSecret.value();
    const providedSecret = req.headers["x-webhook-secret"];
    if (!SECRET || providedSecret !== SECRET) {
        console.error("Unauthorized webhook call");
        res.status(401).json({ error: "Unauthorized" });
        return;
    }
    // Parse Supabase webhook payload
    const payload = req.body;
    // Only process INSERT events on proposals table
    if (payload.type !== "INSERT" || payload.table !== "proposals") {
        res.status(200).json({ message: "Ignored - not an INSERT on proposals" });
        return;
    }
    const record = payload.record;
    if (!record) {
        res.status(400).json({ error: "Missing record in payload" });
        return;
    }
    const title = record.title;
    const body = extractFirstSentence(record.description);
    try {
        // Build the notification message
        const message = {
            topic: "new_proposals",
            notification: {
                title: title,
                body: body,
            },
            data: {
                type: "new_proposal",
                proposalId: record.id,
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
    }
    catch (error) {
        console.error("Error sending notification:", error);
        res.status(500).json({
            error: "Failed to send notification",
            details: error instanceof Error ? error.message : "Unknown error"
        });
    }
});
//# sourceMappingURL=index.js.map