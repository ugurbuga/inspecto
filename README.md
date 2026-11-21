# Inspecto 🔍

**Inspecto** is a powerful, lightweight, cross-platform network and event inspection library for **Kotlin Multiplatform (KMP)** and Jetpack Compose (Android, iOS, Desktop JVM, Web JS/WasmJS). Inspired by Chucker, it provides real-time HTTP/GraphQL request & response inspection, search filtering, JSON beautification, cURL export, and custom font configuration.

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

Add Inspecto dependency to your multiplatform module (`commonMain`):

```kotlin
commonMain.dependencies {
    implementation("io.github.ugurbuga:inspecto:1.0.0-alpha11")
}
```

---

### 2. Launching Inspecto UI

Wrap your root composable with `InspectoApp` and launch the inspector drawer/activity anywhere:

```kotlin
import io.github.ugurbuga.inspecto.InspectoApp
import io.github.ugurbuga.inspecto.launchInspecto

@Composable
fun App() {
    InspectoApp {
        Button(onClick = { launchInspecto() }) {
            Text("Open Inspecto Inspector")
        }
    }
}
```

---

### 3. Custom Font Configuration (Optional)

Inspecto is font-agnostic by default (`FontFamily.Default`). You can supply your own custom font globally or via `InspectoApp`:

```kotlin
// Option A: Global assignment
Inspecto.customFontFamily = FontFamily(Font(Res.font.my_custom_font))

// Option B: Composable parameter
InspectoApp(fontFamily = FontFamily(Font(Res.font.my_custom_font))) {
    // App UI
}
```

---

## 🔌 Network Stack Integrations

### A. Ktor Client Integration (Cross-Platform / All)
Install `InspectoKtorLogging` plugin into your Ktor `HttpClient`:

```kotlin
import io.github.ugurbuga.inspecto.InspectoKtorLogging
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
import io.github.ugurbuga.inspecto.InspectoOkHttpLoggingInterceptor
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

### C. Native iOS `NSURLSession` Integration (iOS Swift / Kotlin)
For native iOS networking (`URLSession`), use the provided logging helper in your Swift callback or `URLSession` extension:

```swift
import Foundation
import Shared

extension URLSession {
    func inspectoDataTask(
        with request: URLRequest,
        completionHandler: @escaping (Data?, URLResponse?, Error?) -> Void
    ) -> URLSessionDataTask {
        let startTime = Date().timeIntervalSince1970
        return self.dataTask(with: request) { data, response, error in
            let duration = Int64((Date().timeIntervalSince1970 - startTime) * 1000)
            InspectoNSURLSessionKt.logNSURLSessionDataTask(
                request: request as NSURLRequest,
                response: response as? NSHTTPURLResponse,
                data: data as NSData?,
                error: error as NSError?,
                durationMs: duration
            )
            completionHandler(data, response, error)
        }
    }
}
```
*✨ Logs native iOS network traffic with an emerald **`iOS NSURLSession`** tag.*

---

### D. Apollo Kotlin GraphQL Integration (Cross-Platform)
Add `InspectoApolloInterceptor` to your `ApolloClient.Builder`, explicitly providing your endpoint URL:

```kotlin
import com.apollographql.apollo.ApolloClient
import io.github.ugurbuga.inspecto.InspectoApolloInterceptor

val apolloClient = ApolloClient.Builder()
    .serverUrl("https://rickandmortyapi.com/graphql")
    .addInterceptor(InspectoApolloInterceptor("https://rickandmortyapi.com/graphql"))
    .build()
```
*✨ Automatically intercepts GraphQL queries and mutations, pretty-prints responses, and displays them with a magenta **`GraphQL`** tag.*

---

### E. Manual Logging API
For custom protocols, WebSockets, or third-party SDKs:

```kotlin
import io.github.ugurbuga.inspecto.Inspecto

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
- **Theme-Aware Status Bar**: Dynamically synchronizes Android system status bar color with the app's topbar/surface color.

---

## 📄 License
Licensed under the Apache License 2.0.
