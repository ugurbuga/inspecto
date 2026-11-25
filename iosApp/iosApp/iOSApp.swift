import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        URLProtocol.registerClass(InspectoURLProtocol.self)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
