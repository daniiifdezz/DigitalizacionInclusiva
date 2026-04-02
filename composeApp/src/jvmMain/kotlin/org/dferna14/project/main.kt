package org.dferna14.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.dferna14.project.di.appModule
import org.dferna14.project.ui.screens.ActividadListadoSc
import org.koin.core.context.startKoin

/**
 * Punto de entrada de la aplicación Desktop.
 * Inicializa Koin antes de arrancar la ventana Compose.
 */
fun main() {
    try {
        startKoin {
            modules(appModule)
        }
    } catch (e: Exception) {
        // Ya estaba iniciado
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Cuaderno de Campo - Digitalización Inclusiva"
        ) {
            App()
        }
    }
}