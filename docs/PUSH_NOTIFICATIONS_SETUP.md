# Push Notifications Setup Guide

This document explains how to configure push notifications for SpainDecides using Firebase Cloud Messaging (FCM).

## Architecture Overview

```
┌─────────────────┐      ┌──────────────────────┐      ┌─────────────────┐
│   Mobile App    │ ───▶ │ Firebase Cloud       │ ───▶ │   FCM Server    │
│ (Creates Prop.) │      │ Function             │      │                 │
└─────────────────┘      └──────────────────────┘      └────────┬────────┘
                                                                │
                                                                ▼
                                                    ┌─────────────────────┐
                                                    │  All subscribed     │
                                                    │  devices (topic:    │
                                                    │  new_proposals)     │
                                                    └─────────────────────┘
```

## Prerequisites

1. Firebase account (free Spark plan is sufficient)
2. Apple Developer account (for iOS)
3. Node.js 18+ installed locally

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project"
3. Name it (e.g., "SpainDecides")
4. Enable Google Analytics (optional)
5. Click "Create project"

## Step 2: Configure Android App

### 2.1 Register Android App

1. In Firebase Console, click "Add app" → Android
2. Enter package name: `com.apptolast.spaindecides`
3. Enter app nickname: "SpainDecides Android"
4. Download `google-services.json`

### 2.2 Add Configuration File

1. Copy `google-services.json` to:
   ```
   composeApp/src/androidMain/
   ```

### 2.3 Note About SHA Certificates

For FCM to work in production, you need to add SHA-1 and SHA-256 fingerprints:

1. Get fingerprints from your keystore:
   ```bash
   keytool -list -v -keystore your-release.keystore -alias your-alias
   ```
2. Add them in Firebase Console → Project Settings → Your Android App → SHA certificate fingerprints

## Step 3: Configure iOS App

### 3.1 Register iOS App

1. In Firebase Console, click "Add app" → iOS
2. Enter bundle ID: `com.apptolast.spaindecides`
3. Download `GoogleService-Info.plist`

### 3.2 Add Configuration File

1. Open Xcode
2. Drag `GoogleService-Info.plist` into `iosApp/iosApp/`
3. Make sure "Copy items if needed" is checked

### 3.3 Configure APNs

1. Go to [Apple Developer Portal](https://developer.apple.com/)
2. Navigate to Certificates, Identifiers & Profiles → Keys
3. Create a new key with "Apple Push Notifications service (APNs)"
4. Download the `.p8` file
5. In Firebase Console → Project Settings → Cloud Messaging → iOS app configuration
6. Upload the APNs authentication key (`.p8` file)
7. Enter your Team ID and Key ID

### 3.4 Enable Push Notifications in Xcode

1. Open the project in Xcode
2. Select the target → Signing & Capabilities
3. Click "+ Capability"
4. Add "Push Notifications"
5. Add "Background Modes" and check "Remote notifications"

### 3.5 Add Firebase SDK via Swift Package Manager

1. In Xcode, go to File → Add Package Dependencies
2. Add: `https://github.com/firebase/firebase-ios-sdk`
3. Select:
    - FirebaseMessaging

## Step 4: Deploy Firebase Cloud Function

### 4.1 Install Firebase CLI

```bash
npm install -g firebase-tools
```

### 4.2 Login to Firebase

```bash
firebase login
```

### 4.3 Initialize Firebase in the project

```bash
cd firebase
firebase init
```

Select:

- Functions
- Use existing project (select your project)
- TypeScript
- Yes to ESLint
- Yes to install dependencies

### 4.4 Set the API Key

```bash
firebase functions:config:set notifications.api_key="YOUR_SECURE_API_KEY"
```

Generate a secure API key:

```bash
openssl rand -base64 32
```

### 4.5 Deploy the Function

```bash
cd functions
npm install
npm run build
firebase deploy --only functions
```

### 4.6 Get the Function URL

After deployment, Firebase will show the function URL:

```
✔ Function sendNewProposalNotification deployed
   https://us-central1-YOUR_PROJECT.cloudfunctions.net/sendNewProposalNotification
```

## Step 5: Configure Mobile App

### 5.1 Add Environment Variables

Add the following to your `local.properties`:

```properties
# Firebase Cloud Function Configuration
FIREBASE_FUNCTION_URL=https://us-central1-YOUR_PROJECT.cloudfunctions.net/sendNewProposalNotification
FIREBASE_FUNCTION_API_KEY=YOUR_SECURE_API_KEY
# For separate dev/prod environments:
FIREBASE_FUNCTION_URL_DEBUG=https://us-central1-YOUR_DEV_PROJECT.cloudfunctions.net/sendNewProposalNotification
FIREBASE_FUNCTION_URL_RELEASE=https://us-central1-YOUR_PROD_PROJECT.cloudfunctions.net/sendNewProposalNotification
FIREBASE_FUNCTION_API_KEY_DEBUG=your_dev_api_key
FIREBASE_FUNCTION_API_KEY_RELEASE=your_prod_api_key
```

## Step 6: Request Notification Permissions (Android 13+)

For Android 13+, you need to request notification permissions at runtime. Add this code where appropriate in your app:

```kotlin
// In a Composable
val context = LocalContext.current
val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        // Permission granted
    }
}

// Request permission
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
}
```

## Testing

### Test Notification from Firebase Console

1. Go to Firebase Console → Engage → Messaging
2. Click "Create your first campaign" or "New campaign"
3. Select "Firebase Notification messages"
4. Enter title and body
5. Select "Topic" → `new_proposals`
6. Send test message

### Test from Command Line

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -H "x-api-key: YOUR_API_KEY" \
  -d '{"title":"Test Proposal","body":"This is a test notification"}' \
  https://us-central1-YOUR_PROJECT.cloudfunctions.net/sendNewProposalNotification
```

## Troubleshooting

### Android

1. **Notifications not received**
    - Check that `google-services.json` is in the correct location
    - Verify the package name matches exactly
    - Check logcat for FCM token generation

2. **App crashes on startup**
    - Ensure `google-services` plugin is applied correctly
    - Check that all Firebase dependencies are compatible

### iOS

1. **Notifications not received**
    - Verify APNs key is correctly uploaded to Firebase
    - Check that Push Notifications capability is enabled
    - Test on a real device (simulator doesn't support push)

2. **Token not generated**
    - Ensure `GoogleService-Info.plist` is included in the bundle
    - Check that Bundle ID matches exactly

### Firebase Function

1. **401 Unauthorized**
    - Verify the API key in `local.properties` matches the one set in Firebase Functions config

2. **Function not found**
    - Check the function URL is correct
    - Verify the function is deployed successfully

## Costs

The Firebase Spark (free) plan includes:

- Cloud Functions: 2 million invocations/month
- Cloud Messaging: Unlimited notifications
- Authentication: 10K verifications/month

This is more than sufficient for most apps. You only pay if you exceed these limits.

## Security Considerations

1. **Never expose** the Firebase Admin SDK key in client code
2. The API key for the Cloud Function should be:
    - Stored securely in `local.properties` (not committed to git)
    - Different for development and production
3. Consider adding rate limiting to the Cloud Function for production
