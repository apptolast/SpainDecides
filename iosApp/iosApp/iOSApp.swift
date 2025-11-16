import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
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
                }
        }
    }
}