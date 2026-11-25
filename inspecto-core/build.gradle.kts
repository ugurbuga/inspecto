import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.mavenPublish)
}

group = "io.github.ugurbuga"
version = "1.0.0-alpha18"

compose.resources {
    publicResClass = true
    packageOfResClass = "com.ugurbuga.inspecto.generated.resources"
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Inspecto"
            isStatic = true
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
       namespace = "com.ugurbuga.inspecto"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
    }
    sourceSets {
        androidMain.dependencies {
            api(libs.androidx.activity.compose)
            api(compose.uiTooling)
            api(libs.compose.uiToolingPreview)
            compileOnly(libs.okhttp)
        }
        jvmMain.dependencies {
            compileOnly(libs.okhttp)
        }
        commonMain.dependencies {
            api(compose.components.resources)
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.ui)
            api(libs.compose.uiToolingPreview)
            api(libs.androidx.lifecycle.viewmodelCompose)
            api(libs.androidx.lifecycle.runtimeCompose)
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.datetime)
            api(libs.ktor.client.core)
            api(libs.apollo.runtime)
        }
    }
}

dependencies {
    androidRuntimeClasspath(compose.uiTooling)
}

mavenPublishing {
    // publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    // signAllPublications()
    coordinates("io.github.ugurbuga", "inspecto-core", "1.0.0-alpha17")

    pom {
        name.set("inspecto-core")
        description.set("Kotlin Multiplatform Network Inspection Library - Core")
        url.set("https://github.com/ugurbuga/inspecto")
        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("ugurbuga")
                name.set("Uğur Buğa")
                email.set("ugurbuga00@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:github.com/ugurbuga/inspecto.git")
            developerConnection.set("scm:git:ssh://github.com/ugurbuga/inspecto.git")
            url.set("https://github.com/ugurbuga/inspecto")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ugurbuga/inspecto")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
