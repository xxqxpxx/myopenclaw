import SwiftUI
import ComposeApp
import FirebaseCore
import GoogleSignIn
import UserNotifications

// MARK: - Google Sign-In Bridge (implements Kotlin GoogleSignInHandler interface)

class SwiftGoogleSignInHandler: GoogleSignInHandler {
    func performSignIn(callback: any GoogleSignInCallback) {
        DispatchQueue.main.async {
            guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                  let rootViewController = windowScene.windows.first?.rootViewController else {
                callback.onResult(idToken: nil, error: "No root view controller available")
                return
            }

            // Find the topmost presented view controller
            var topVC = rootViewController
            while let presented = topVC.presentedViewController {
                topVC = presented
            }

            GIDSignIn.sharedInstance.signIn(withPresenting: topVC) { result, error in
                if let error = error {
                    let nsError = error as NSError
                    if nsError.code == GIDSignInError.canceled.rawValue {
                        callback.onResult(idToken: nil, error: "Sign-in was cancelled")
                    } else {
                        callback.onResult(idToken: nil, error: error.localizedDescription)
                    }
                    return
                }

                guard let idToken = result?.user.idToken?.tokenString else {
                    callback.onResult(idToken: nil, error: "Failed to get ID token from Google")
                    return
                }

                callback.onResult(idToken: idToken, error: nil)
            }
        }
    }
}

// MARK: - App Delegate

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Set notification center delegate FIRST to handle any notification callbacks
        // that may be triggered by Firebase or RevenueCat during initialization.
        // Without this, notification callbacks on background queues can cause
        // uncaught Kotlin/Native exceptions leading to crashes (SIGABRT).
        UNUserNotificationCenter.current().delegate = self

        // Initialize Firebase FIRST before any Firebase APIs are used
        FirebaseApp.configure()

        // Request notification permission (shows the system "Allow Notifications?" dialog)
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
            if granted {
                DispatchQueue.main.async {
                    application.registerForRemoteNotifications()
                }
            }
        }

        // Initialize Koin for iOS (DI container must be ready before SDK calls)
        KoinInitializerKt.doInitKoin()

        // Configure Google Sign-In and wire the Kotlin bridge
        GoogleSignInBridgeKt.setGoogleSignInHandler(handler: SwiftGoogleSignInHandler())

        // Initialize RevenueCat SDK LAST (after Firebase and Koin are ready)
        MainViewControllerKt.initializeRevenueCat()

        return true
    }

    // MARK: - URL Handling (Google Sign-In callback)

    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        return GIDSignIn.sharedInstance.handle(url)
    }

    // MARK: - Remote Notification Registration

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        // Pass the APNS token to Firebase Auth for phone auth / verification
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        // Silently handle registration failure - app works without push notifications
    }

    // MARK: - UNUserNotificationCenterDelegate

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // Show notifications as banners when the app is in the foreground
        completionHandler([.banner, .sound])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        // Handle notification tap
        completionHandler()
    }
}

// MARK: - App Entry Point

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
