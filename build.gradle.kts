plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.mavenPublish) apply false
}

subprojects {
    if (name.startsWith("inspecto-")) {
        group = rootProject.property("INSPECTO_GROUP") as String
        version = rootProject.property("INSPECTO_VERSION") as String

        plugins.withId("com.vanniktech.maven.publish") {
            configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
                coordinates(
                    rootProject.property("INSPECTO_GROUP") as String,
                    name,
                    rootProject.property("INSPECTO_VERSION") as String
                )
            }
        }
    }
}
