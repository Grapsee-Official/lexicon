//  LexiconBackgroundTaskHandler.swift
//  Lexicon
//
//  Per spec risk matrix: "iOS background app suspension — when the user selects
//  text in the iOS Kindle app, Lexicon must wake from a suspended state to process
//  the request. iOS gives background apps milliseconds to respond before killing them.
//  If Lexicon does not use background execution tasks properly, the menu item will be dead on tap."
//
//  Solution: "Do use BGProcessingTaskRequest for execution time."
//

import UIKit
import BackgroundTasks

/// LexiconBackgroundTaskHandler — manages background execution for the
/// UIEditMenuInteraction hook. When the app is suspended and the user
/// taps "Lexicon" from a text selection in another app, this handler
/// requests background execution time to process the lookup.
@available(iOS 16.0, *)
class LexiconBackgroundTaskHandler {

    static let shared = LexiconBackgroundTaskHandler()

    private var backgroundTask: UIBackgroundTaskIdentifier = .invalid
    private var engine: LexiconEngine?

    private init() {}

    func setEngine(_ engine: LexiconEngine) {
        self.engine = engine
    }

    /// Register background task identifiers.
    /// Call this in AppDelegate's didFinishLaunchingWithOptions.
    func registerBackgroundTasks() {
        // Register for background processing task
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: "com.lexicon.lookup",
            using: nil
        ) { task in
            self.handleBackgroundLookup(task: task as! BGProcessingTask)
        }
    }

    /// Schedule a background processing task for an upcoming lookup.
    /// Call this when the app is about to be suspended.
    func scheduleBackgroundLookup() {
        let request = BGProcessingTaskRequest(identifier: "com.lexicon.lookup")
        request.requiresNetworkConnectivity = false
        request.requiresExternalPower = false
        request.earliestBeginDate = Date(timeIntervalSinceNow: 1.0)

        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("[Lexicon] Failed to schedule background task: \(error)")
        }
    }

    /// Handle a background lookup request.
    /// The system gives us ~30 seconds of background execution time.
    /// We must complete the lookup and present the result within this window.
    private func handleBackgroundLookup(task: BGProcessingTask) {
        // Schedule the next background task
        scheduleBackgroundLookup()

        // Set up the task completion handler
        var completed = false

        task.expirationHandler = {
            // The system is about to kill us — clean up immediately
            completed = true
            task.setTaskCompleted(success: false)
        }

        // Start a background task to keep the app alive
        backgroundTask = UIApplication.shared.beginBackgroundTask(withName: "LexiconLookup") {
            // Expiration — the system is about to suspend us
            completed = true
            if self.backgroundTask != .invalid {
                UIApplication.shared.endBackgroundTask(self.backgroundTask)
                self.backgroundTask = .invalid
            }
            task.setTaskCompleted(success: false)
        }

        // The actual lookup was already triggered by UIEditMenuInteraction.
        // Here we just keep the app alive long enough for the result to be
        // presented (either as a notification or by bringing the app to foreground).
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            if !completed {
                task.setTaskCompleted(success: true)
                if self.backgroundTask != .invalid {
                    UIApplication.shared.endBackgroundTask(self.backgroundTask)
                    self.backgroundTask = .invalid
                }
            }
        }
    }

    /// Begin a short background task for immediate lookup.
    /// Use this when the app is in the foreground but needs a few extra
    /// seconds to complete the engine lookup.
    func beginShortBackgroundTask(completion: @escaping () -> Void) {
        let taskId = UIApplication.shared.beginBackgroundTask(withName: "LexiconQuickLookup") {
            // Expiration
            completion()
        }

        DispatchQueue.global(qos: .userInitiated).async {
            completion()
            if taskId != .invalid {
                UIApplication.shared.endBackgroundTask(taskId)
            }
        }
    }

    /// End the background task.
    func endBackgroundTask() {
        if backgroundTask != .invalid {
            UIApplication.shared.endBackgroundTask(backgroundTask)
            backgroundTask = .invalid
        }
    }
}
