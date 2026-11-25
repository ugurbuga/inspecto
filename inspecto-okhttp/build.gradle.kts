import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.mavenPublish)
}

group = "io.github.ugurbuga"
version = "1.0.0-alpha19"

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()
    js { browser() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    androidLibrary {
        namespace = "com.ugurbuga.inspecto.okhttp"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":inspecto-core"))
        }
        androidMain.dependencies {
            api(libs.okhttp)
        }
        jvmMain.dependencies {
            api(libs.okhttp)
        }
    }
}

mavenPublishing {
    coordinates("io.github.ugurbuga", "inspecto-okhttp", "1.0.0-alpha19")

    pom {
        name.set("Inspecto OkHttp")
        description.set("OkHttp logging interceptor for Inspecto")
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
