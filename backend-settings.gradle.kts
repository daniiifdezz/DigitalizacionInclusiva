rootProject.name = "DigitalizacionInclusiva"
rootProject.buildFileName = "backend-build.gradle.kts"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":backend")
