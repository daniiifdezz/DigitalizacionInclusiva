import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)  // nuevo
    alias(libs.plugins.sqldelight)           // nuevo
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()

    sourceSets {

        // ── Android ───────────────────────────────────────────────────────────
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)

            // Motor HTTP para Android
            implementation(libs.ktor.client.android)

            // Driver SQLite para Android
            implementation(libs.sqldelight.androidDriver)

            // Koin para Android
            implementation(libs.koin.android)
        }

        // ── Común (Android + Desktop) ─────────────────────────────────────────
        // Aquí vive toda la lógica: UI, ViewModels, dominio, datos, red, DI
        commonMain.dependencies {
            // UI Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)

            // ViewModel + Lifecycle (KMP)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)



            // Coroutines
            implementation(libs.kotlinx.coroutinesCore)

            // Serialización JSON
            implementation(libs.kotlinx.serialization.json)

            // Fechas multiplataforma
            implementation(libs.kotlinx.datetime)

            // Ktor Client — llamadas REST al backend
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNeg)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.json)

            // SQLDelight — base de datos local offline
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)

            // Koin — inyección de dependencias
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.viewmodel)

            // NavigationCompose
            implementation(libs.androidx.navigation.compose)



        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        // ── Desktop ───────────────────────────────────────────────────────────
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)

            // Coroutines con dispatcher para Swing
            implementation(libs.kotlinx.coroutinesSwing)

            // Motor HTTP para JVM
            implementation(libs.ktor.client.java)

            // Driver SQLite para Desktop
            implementation(libs.sqldelight.jvmDriver)
        }
    }
}

// ── Configuración Android ──────────────────────────────────────────────────────
android {
    namespace  = "org.dferna14.project"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.dferna14.project"
        minSdk        = libs.versions.android.minSdk.get().toInt()
        targetSdk     = libs.versions.android.targetSdk.get().toInt()
        versionCode   = 1
        versionName   = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

// ── SQLDelight — generación de código SQL tipado ───────────────────────────────
sqldelight {
    databases {
        create("CuadernoCampoDatabase") {
            packageName.set("org.dferna14.project.db")
        }
    }
}

// ── Desktop ────────────────────────────────────────────────────────────────────
compose.desktop {
    application {
        mainClass = "org.dferna14.project.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName    = "DigitalizacionInclusiva"
            packageVersion = "1.0.0"
        }
    }
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.androidx.savedstate:savedstate:1.3.2")
    }
}
