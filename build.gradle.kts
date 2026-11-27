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
        group = findProperty("INSPECTO_GROUP")?.toString() ?: "io.github.ugurbuga"
        version = findProperty("INSPECTO_VERSION")?.toString() ?: "1.0.0-alpha21"
    }
}
