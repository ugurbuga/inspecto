# Inspecto 🔍

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin_Multiplatform-KMP-blue?logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![Compose Multiplatform](https://img.shields.io/badge/Compose_Multiplatform-CMP-purple?logo=jetbrains)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![GitHub Packages](https://img.shields.io/badge/GitHub_Packages-v1.0.0--alpha17-orange)](https://github.com/ugurbuga/inspecto/packages)

**Inspecto** is a lightweight, modular, cross-platform network and event inspection library built specifically for **Kotlin Multiplatform (KMP)** and Jetpack Compose (Android, iOS, Desktop JVM, Web JS/WasmJS).

Inspired by Chucker, **Inspecto** enables real-time in-app inspection for HTTP and Apollo GraphQL network traffic with zero setup friction.

### 🌟 Key Highlights

- **🧩 Fully Modular Architecture**: Zero bloat! Core UI & Storage (`inspecto-core`) is completely decoupled from network client SDKs. Add only the interceptors you use (`inspecto-ktor`, `inspecto-okhttp`, `inspecto-apollo`, `inspecto-ios`).
- **⚡ Zero-Config Android Setup**: Automatically captures context on startup via `InspectoInitProvider`—no `Application` subclassing or `MainActivity` code modifications required.
- **📱 In-App Overlay (`InspectoOverlay`)**: Renders directly inside your Compose app on all platforms with back gesture/button navigation.
- **🎨 Beautiful Developer Experience**: Real-time HTTP & Apollo GraphQL interceptors, JSON syntax highlighting, in-page search, word wrap / horizontal scroll toggling, cURL command export, and smart multi-field filtering.

---

## 📱 Visual Showcase & Screenshots

<table>
  <tr>
    <th align="center">Request List (Light)</th>
    <th align="center">Request List (Dark)</th>
    <th align="center">Filter (Light)</th>
    <th align="center">Filter (Dark)</th>
  </tr>
  <tr>
    <td align="center"><img src="images/list_light.png" width="200"/></td>
    <td align="center"><img src="images/list_dark.png" width="200"/></td>
    <td align="center"><img src="images/filter_light.png" width="200"/></td>
    <td align="center"><img src="images/filter_dark.png" width="200"/></td>
  </tr>
  <tr>
    <th align="center">Sort (Light)</th>
    <th align="center">Sort (Dark)</th>
    <th align="center">Overview (Light)</th>
    <th align="center">Overview (Dark)</th>
  </tr>
  <tr>
    <td align="center"><img src="images/sort_light.png" width="200"/></td>
    <td align="center"><img src="images/sort_dark.png" width="200"/></td>
    <td align="center"><img src="images/overview_light.png" width="200"/></td>
    <td align="center"><img src="images/overview_dark.png" width="200"/></td>
  </tr>
  <tr>
    <th align="center">Request (Light)</th>
    <th align="center">Request (Dark)</th>
    <th align="center">Response (Light)</th>
    <th align="center">Response (Dark)</th>
  </tr>
  <tr>
    <td align="center"><img src="images/request_light.png" width="200"/></td>
    <td align="center"><img src="images/request_dark.png" width="200"/></td>
    <td align="center"><img src="images/response_light.png" width="200"/></td>
    <td align="center"><img src="images/response_dark.png" width="200"/></td>
  </tr>
</table>

---

## 🚀 Step-by-Step Integration & Usage Guide

### 1. Installation

Add Inspecto dependencies to your multiplatform module (`commonMain` & `iosMain`):

```kotlin
commonMain.dependencies {
    // Core UI, models & storage (Required)
    implementation("io.github.ugurbuga:inspecto-core:1.0.0-alpha17")

    // Optional Network Add-ons (Add only what you use)
    implementation("io.github.ugurbuga:inspecto-ktor:1.0.0-alpha17")
    implementation("io.github.ugurbuga:inspecto-okhttp:1.0.0-alpha17")
    implementation("io.github.ugurbuga:inspecto-apollo:1.0.0-alpha17")
}

iosMain.dependencies {
    implementation("io.github.ugurbuga:inspecto-ios:1.0.0-alpha17")
}
```

---

### 2. Launching Inspecto UI

Wrap your root composable with `InspectoOverlay` and launch the inspector anywhere:

```kotlin
import com.ugurbuga.inspecto.InspectoOverlay
import com.ugurbuga.inspecto.Inspecto

@Composable
fun App() {
    InspectoOverlay {
        Button(onClick = { Inspecto.launch() }) {
            Text("Open Inspecto Inspector")
        }
    }
}
```

---

### 3. Custom Font Configuration (Optional)

Inspecto is font-agnostic by default (`FontFamily.Default`). You can supply your own custom font via `InspectoOverlay`:

```kotlin
InspectoOverlay(fontFamily = FontFamily(Font(Res.font.my_custom_font))) {
    // App UI
}
```

---

## 🔌 Network Stack Integrations

### A. Ktor Client Integration (Cross-Platform / All)
Install `InspectoKtorLogging` plugin into your Ktor `HttpClient`:

```kotlin
import com.ugurbuga.inspecto.InspectoKtorLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

val httpClient = HttpClient {
    install(InspectoKtorLogging)
    install(ContentNegotiation) {
        json()
    }
}
```
*✨ Automatically intercepts all HTTP requests/responses across Android, iOS, Desktop, and Web with a purple **`Ktor`** tag.*

---

### B. OkHttp & Retrofit Integration (Android & JVM)
Add `InspectoOkHttpLoggingInterceptor` to your `OkHttpClient.Builder`:

```kotlin
import com.ugurbuga.inspecto.InspectoOkHttpLoggingInterceptor
import okhttp3.OkHttpClient

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(InspectoOkHttpLoggingInterceptor())
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```
*✨ Intercepts OkHttp & Retrofit network calls with an amber **`OkHttp`** tag.*

---

### C. Native iOS `NSURLSession` Integration (iOS & KMP)

#### Option 1: Automated Kotlin Extension (`dataTaskWithRequestAndLog`)
Use the `dataTaskWithRequestAndLog` extension function on `NSURLSession` in Kotlin (`iosMain`) for zero-boilerplate logging:

```kotlin
import com.ugurbuga.inspecto.dataTaskWithRequestAndLog

val task = session.dataTaskWithRequestAndLog(request) { data, response, error ->
    // Your completion handler — logging is 100% automated!
}
task.resume()
```

#### Option 2: Delegate Wrapper (`InspectoURLSessionDataDelegate`)
Wrap your `NSURLSession` delegate with `InspectoURLSessionDataDelegate`:

```kotlin
import com.ugurbuga.inspecto.InspectoURLSessionDataDelegate

val session = NSURLSession.sessionWithConfiguration(
    configuration = config,
    delegate = InspectoURLSessionDataDelegate(myExistingDelegate),
    delegateQueue = null
)
```

#### Option 3: Swift Native `URLProtocol` Registration
Register `InspectoURLProtocol.self` in your Swift `iOSApp.swift` to automatically intercept all app-wide `URLSession` calls:

```swift
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
```
*✨ Automatically intercepts and logs native iOS `NSURLSession` network traffic with an emerald **`iOS NSURLSession`** tag.*

---

### D. Apollo Kotlin GraphQL Integration (Cross-Platform)
Add `InspectoApolloInterceptor` to your `ApolloClient.Builder`, explicitly providing your endpoint URL:

```kotlin
import com.apollographql.apollo.ApolloClient
import com.ugurbuga.inspecto.InspectoApolloInterceptor

val url = "https://api.example.com/graphql"

val apolloClient = ApolloClient.Builder()
    .serverUrl(url)
    .addInterceptor(InspectoApolloInterceptor(url))
    .build()
```
*✨ Automatically intercepts GraphQL queries and mutations, pretty-prints responses, and displays them with a magenta **`GraphQL`** tag.*

---

### E. Manual Logging API
For custom protocols, WebSockets, or third-party SDKs:

```kotlin
import com.ugurbuga.inspecto.Inspecto

Inspecto.addLog(
    url = "https://api.inspecto.local/v1/custom-action",
    method = "POST",
    statusCode = 201,
    durationMs = 45L,
    requestHeaders = mapOf("Authorization" to "Bearer token"),
    requestBody = "{\"action\": \"ping\"}",
    responseBody = "{\"status\": \"pong\"}",
    tag = "CustomTag"
)
```

---

## ✨ Key Features

- **Multiplatform (KMP)**: Full support for Android, iOS, Desktop JVM, and Web.
- **Advanced Filtering & Search**: Filter requests by HTTP Method (`GET`, `POST`, `PUT`, `DELETE`, `GraphQL`), Status (`2XX`, `4XX`, `5XX`), or search by URL/tag.
- **JSON Beautifier**: Automatically pretty-prints and indents JSON request and response bodies.
- **cURL Export**: One-tap copy cURL command for any captured network transaction.
- **Theme-Aware Status Bar**: Dynamically synchronizes Android system status bar icon contrast with theme appearance.

---

## 📄 License
Licensed under the Apache License 2.0.
