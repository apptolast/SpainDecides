import SwiftUI
import ComposeApp
import FirebaseCore
import FirebaseAuth
import FirebaseMessaging
import GoogleSignIn
import UserNotifications

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        // Configure Firebase
        FirebaseApp.configure()

        // Install the Google sign-in handlers BaseLogin calls into, before any Composable renders.
        configureGoogleSignIn()

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

    /// Routes an incoming URL to whoever is waiting for it: Firebase Auth or Google Sign-In.
    static func handle(_ url: URL) -> Bool {
        if Auth.auth().canHandle(url) {
            return true
        }
        return GIDSignIn.sharedInstance.handle(url)
    }

    func application(_ app: UIApplication,
                     open url: URL,
                     options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
        return AppDelegate.handle(url)
    }

    /// Wires the two callbacks BaseLogin's iOS Google provider suspends on.
    ///
    /// The client ID arrives from `GoogleSignInConfig.iosClientId`, which is fed by the
    /// `GOOGLE_IOS_CLIENT_ID` entry in local.properties.
    private func configureGoogleSignIn() {
        // Firebase's signOut() does not touch GIDSignIn: its currentUser lives in the keychain, and
        // without this the next sign-in reuses the same account and nobody can switch.
        GoogleSignInProviderIOS.shared.signOutHandler = {
            GIDSignIn.sharedInstance.signOut()
        }

        GoogleSignInProviderIOS.shared.signInHandler = { clientId, completion in
            // The Kotlin lambda is exported as `(String?) -> KotlinUnit`, so its result is unused
            // on purpose at every call site below.
            guard let clientId = clientId else {
                _ = completion(nil)
                return
            }

            guard let requiredScheme = Self.reversedGoogleClientId(clientId),
                  Self.bundleSupportsURLScheme(requiredScheme)
            else {
                NSLog("%@", "[GoogleSignIn] Missing URL scheme for iOS client ID: \(clientId)")
                _ = completion(nil)
                return
            }

            GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientId)

            // The scene that is actually on screen, not whichever one comes first in the set.
            let scenes = UIApplication.shared.connectedScenes.compactMap { $0 as? UIWindowScene }
            let scene = scenes.first { $0.activationState == .foregroundActive } ?? scenes.first
            guard let rootViewController = (scene?.keyWindow ?? scene?.windows.first)?.rootViewController else {
                NSLog("%@", "[GoogleSignIn] No root view controller to present from.")
                _ = completion(nil)
                return
            }

            var topController = rootViewController
            while let presented = topController.presentedViewController {
                topController = presented
            }

            GIDSignIn.sharedInstance.signIn(withPresenting: topController) { result, error in
                if let error = error {
                    NSLog("%@", "[GoogleSignIn] Failed or cancelled: \(error.localizedDescription)")
                    _ = completion(nil)
                    return
                }

                guard let user = result?.user,
                      let idToken = user.idToken?.tokenString
                else {
                    NSLog("%@", "[GoogleSignIn] No ID token in the result.")
                    _ = completion(nil)
                    return
                }

                // Firebase needs the access token too, so both travel in one string.
                let accessToken = user.accessToken.tokenString
                _ = completion("\(idToken)|||accessToken|||\(accessToken)")
            }
        }
    }

    private static func reversedGoogleClientId(_ clientId: String) -> String? {
        let suffix = ".apps.googleusercontent.com"
        guard clientId.hasSuffix(suffix) else {
            return nil
        }
        let prefix = clientId.dropLast(suffix.count)
        return "com.googleusercontent.apps.\(prefix)"
    }

    private static func bundleSupportsURLScheme(_ scheme: String) -> Bool {
        guard let urlTypes = Bundle.main.object(forInfoDictionaryKey: "CFBundleURLTypes") as? [[String: Any]] else {
            return false
        }
        return urlTypes.contains { urlType in
            let schemes = urlType["CFBundleURLSchemes"] as? [String]
            return schemes?.contains(scheme) == true
        }
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
        KoinInitializerKt_.doInitKoin(appDeclaration: nil)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                // Where the Google and Firebase callbacks land under the SwiftUI lifecycle:
                // the UIApplicationDelegate `open url:` method is not called when scenes are used.
                .onOpenURL { url in
                    _ = AppDelegate.handle(url)
                }
        }
    }
}
