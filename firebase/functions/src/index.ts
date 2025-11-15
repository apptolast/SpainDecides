import { onRequest } from "firebase-functions/v2/https";
import { setGlobalOptions } from "firebase-functions/v2";
import { defineSecret } from "firebase-functions/params";
import * as admin from "firebase-admin";

// Initialize Firebase Admin SDK
admin.initializeApp();

// Set global options for all functions
setGlobalOptions({ region: "europe-west1" });

// Define the secret reference for webhook authentication
const webhookSecret = defineSecret("SUPABASE_WEBHOOK_SECRET");

/**
 * Supabase Webhook payload structure for INSERT events
 */
interface SupabaseWebhookPayload {
    type: "INSERT" | "UPDATE" | "DELETE";
    table: string;
    schema: string;
    record: {
        id: string;
        title: string;
        description: string;
        short_description: string | null;
        category_id: string;
        user_id: string;
        created_at: string;
    } | null;
    old_record: unknown;
}

/**
 * Extracts the first sentence from text.
 * A sentence ends with '.', '!' or '?'
 */
function extractFirstSentence(text: string): string {
    const trimmed = text.trim();
    const match = trimmed.match(/^[^.!?]*[.!?]/);

    if (match) {
        return match[0];
    }
    // If no sentence ending found, take first 100 characters
    return trimmed.length > 100 ? trimmed.substring(0, 100) + "..." : trimmed;
}

/**
 * Gets the notification body text from the proposal record.
 * Prioritizes short_description, falls back to extracting from description.
 */
function getNotificationBody(record: NonNullable<SupabaseWebhookPayload["record"]>): string {
    if (record.short_description && record.short_description.trim().length > 0) {
        return record.short_description;
    }
    // Fallback to first sentence of description
    return extractFirstSentence(record.description);
}

/**
 * Cloud Function triggered by Supabase Database Webhook.
 *
 * Sends push notifications when a new proposal is inserted.
 * Includes proposalId and categoryId for deep linking navigation.
 *
 * Headers:
 * - x-webhook-secret: Secret for authentication (configured in Supabase)
 */
export const sendNewProposalNotification = onRequest(
    { secrets: [webhookSecret] },
    async (req, res) => {

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

    // Determine target FCM topic from environment query parameter
    const env = req.query.env as string | undefined;
    if (!env || !["dev", "prod"].includes(env)) {
        res.status(400).json({ error: "Missing or invalid 'env' query parameter. Must be 'dev' or 'prod'." });
        return;
    }
    const targetTopic = `new_proposals_${env}`;

    // Parse Supabase webhook payload
    const payload = req.body as SupabaseWebhookPayload;

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
    const body = getNotificationBody(record);

    // Log notification data for debugging
    console.log("=== Preparing notification ===");
    console.log("environment:", env);
    console.log("targetTopic:", targetTopic);
    console.log("proposalId:", record.id);
    console.log("categoryId:", record.category_id);
    console.log("title:", title);
    console.log("body:", body);

    try {
        // Build the notification message
        // Note: categoryKey lookup is done client-side to keep this function simple
        const message: admin.messaging.Message = {
            topic: targetTopic,
            notification: {
                title: title,
                body: body,
            },
            data: {
                type: "new_proposal",
                proposalId: record.id,
                categoryId: record.category_id,
                title: title,
                body: body,
            },
            // Android-specific configuration
            android: {
                priority: "high",
                notification: {
                    icon: "ic_notification",
                    color: "#FABD00",
                    channelId: targetTopic,
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
