import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(project(":inspecto-ios"))
            export(project(":inspecto-core"))
        }
    }
    jvm()
    js {
        browser()
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    android {
       namespace = "com.ugurbuga.inspectodemo.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    sourceSets {
        androidMain.dependencies {
            api(libs.compose.uiTooling)
            api(libs.compose.uiToolingPreview)
            api(libs.androidx.activity.compose)
            api(libs.ktor.client.cio)
            api(libs.okhttp)
        }
        commonMain.dependencies {
            api(project(":inspecto-core"))
            api(project(":inspecto-ktor"))
            api(project(":inspecto-okhttp"))
            api(project(":inspecto-apollo"))
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material3)
            api(libs.compose.material.icons.extended)
            api(libs.compose.ui)
            api(libs.compose.uiToolingPreview)
            api(libs.compose.components.resources)
            api(libs.androidx.lifecycle.viewmodelCompose)
            api(libs.androidx.lifecycle.runtimeCompose)
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.datetime)
            api(libs.ktor.client.core)
            api(libs.apollo.runtime)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.serialization.kotlinx.json)
            api(libs.coil.compose)
            api(libs.coil.network.ktor3)
            api(libs.coil.svg)
        }
        iosMain.dependencies {
            api(project(":inspecto-ios"))
            api(libs.ktor.client.darwin)
        }
        jvmMain.dependencies {
            api(libs.compose.uiTooling)
            api(libs.compose.uiToolingPreview)
            api(libs.ktor.client.cio)
            api(libs.okhttp)
        }
        jsMain.dependencies {
            api(libs.wrappers.browser)
            api(libs.ktor.client.js)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(compose.uiTooling)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.ugurbuga.inspectodemo.shared.generated.resources"
}
