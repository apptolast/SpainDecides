import SwiftUI
import ComposeApp
import FirebaseCore
import FirebaseMessaging
import UserNotifications

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        // Configure Firebase
        FirebaseApp.configure()

        // Set up push notification delegates
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self

        // Request notification permissions
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, error in
            if granted {
                DispatchQueue.main.async {
                    application.registerForRemoteNotifications()
                }
            }
        }

        // Initialize KMPNotifier
        NotifierManager.shared.initialize(
            configuration: NotificationPlatformConfigurationIos(
                showPushNotification: true,
                askNotificationPermissionOnStart: false, // We handle permissions manually above
                notificationSoundName: nil
            )
        )

        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }

    // Handle FCM token refresh
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        if let token = fcmToken {
            print("FCM Token: \(token)")
            // Subscribe to environment-specific topic via KMP
            let topic = Environment.shared.FCM_TOPIC_NEW_PROPOSALS
            Messaging.messaging().subscribe(toTopic: topic) { error in
                if let error = error {
                    print("Error subscribing to topic: \(error)")
                } else {
                    print("Subscribed to \(topic) topic")
                }
            }
        }
    }

    // Handle foreground notifications
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.banner, .sound, .badge])
    }

    // Handle notification tap (cold start and warm start)
    // Called when user taps on a notification to open the app
    // Note: categoryKey is looked up client-side from CategoryRepository
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo

        print("[DeepLink-Swift] userNotificationCenter didReceive called")
        print("[DeepLink-Swift] userInfo: \(userInfo)")

        // Extract deep link data from notification payload
        if let type = userInfo["type"] as? String, type == "new_proposal",
           let proposalId = userInfo["proposalId"] as? String,
           let categoryId = userInfo["categoryId"] as? String {

            print("[DeepLink-Swift] Extracted type: \(type)")
            print("[DeepLink-Swift] Extracted proposalId: \(proposalId)")
            print("[DeepLink-Swift] Extracted categoryId: \(categoryId)")

            // Trigger navigation via Kotlin bridge
            print("[DeepLink-Swift] Calling NotificationDeepLinkHandlerKt.handleNotificationTap...")
            NotificationDeepLinkHandlerKt.handleNotificationTap(
                proposalId: proposalId,
                categoryId: categoryId
            )
            print("[DeepLink-Swift] handleNotificationTap completed")
        } else {
            print("[DeepLink-Swift] Could not extract required fields from userInfo")
            print("[DeepLink-Swift] type: \(userInfo["type"] ?? "nil")")
            print("[DeepLink-Swift] proposalId: \(userInfo["proposalId"] ?? "nil")")
            print("[DeepLink-Swift] categoryId: \(userInfo["categoryId"] ?? "nil")")
        }

        completionHandler()
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    init() {
        KoinInitializerKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    // Handle deep link for OAuth callback
                    // Supabase ComposeAuth will automatically handle the URL
                    print("Deep link received: \(url)")

                    DeepLinkHandlerKt.handleDeepLinkUrl(url: url)
                }
        }
    }
}
