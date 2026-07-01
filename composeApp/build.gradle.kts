import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

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

            // androidx.startup — captura el Context de app para SessionStorage
            implementation("androidx.startup:startup-runtime:1.1.1")

            // CameraX — vista previa y análisis de imagen en tiempo real
            val cameraxVersion = "1.3.4"
            implementation("androidx.camera:camera-core:$cameraxVersion")
            implementation("androidx.camera:camera-camera2:$cameraxVersion")
            implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
            implementation("androidx.camera:camera-view:$cameraxVersion")

            // ML Kit — reconocimiento de texto (OCR) offline
            implementation("com.google.mlkit:text-recognition:16.0.1")
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
            implementation(compose.materialIconsExtended)

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

// ── Firma Android (release) ────────────────────────────────────────────────────
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
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

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                keyAlias      = keystoreProperties["keyAlias"]      as String
                keyPassword   = keystoreProperties["keyPassword"]   as String
                storeFile     = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig   = signingConfigs.getByName("release")
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

        buildTypes {
            release {
                proguard {
                    isEnabled.set(false)
                }
            }
        }

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb)
            packageName    = "DigitalizacionInclusiva"
            packageVersion = "1.0.0"
            description    = "Cuaderno de Campo Digital"
            vendor         = "Daniel Fernández"

            modules(
                "java.net.http",    // Ktor JVM HTTP client
                "java.sql",         // SQLDelight JDBC driver
                "java.naming",      // required by some logging/crypto libs
                "jdk.unsupported",  // sun.misc.Unsafe (needed by Netty / coroutines internals)
            )

            windows {
                iconFile.set(project.file("src/jvmMain/resources/imgico.ico"))
                menuGroup   = "DigitalizacionInclusiva"
                upgradeUuid = "3F2A1B4C-5D6E-7F8A-9B0C-1D2E3F4A5B6C"
            }
            linux {
                iconFile.set(project.file("src/jvmMain/resources/imgpng.png"))
            }
        }
    }
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.androidx.savedstate:savedstate:1.3.2")
    }
}
