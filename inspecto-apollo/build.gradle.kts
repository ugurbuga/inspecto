plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.mavenPublish)
}

group = "io.github.ugurbuga"
version = "1.0.0-alpha14"

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()
    js { browser() }
    wasmJs { browser() }

    androidLibrary {
        namespace = "com.ugurbuga.inspecto.apollo"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":inspecto"))
            api(libs.apollo.runtime)
        }
    }
}

mavenPublishing {
    // publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    // signAllPublications()
    coordinates("io.github.ugurbuga", "inspecto-apollo", "1.0.0-alpha14")

    pom {
        name.set("Inspecto Apollo")
        description.set("Apollo Kotlin GraphQL interceptor for Inspecto")
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
                name.set("Ugur Buga")
                email.set("ugurbuga@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:github.com/ugurbuga/inspecto.git")
            developerConnection.set("scm:git:ssh://github.com/ugurbuga/inspecto.git")
            url.set("https://github.com/ugurbuga/inspecto")
        }
    }
}
