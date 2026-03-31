package org.dferna14.project

import androidx.compose.ui.window.application
import org.dferna14.project.di.appModule
import org.koin.core.context.startKoin

/**
 * Punto de entrada de la aplicación Desktop.
 * Inicializa Koin antes de arrancar la ventana Compose.
 */
fun main() {
    // Iniciamos Koin con el mismo módulo que Android
    startKoin {
        modules(appModule)
    }

    // Arranca la ventana Desktop
    application {
        // Aquí irá la ventana principal — la añadiremos en el siguiente paso
    }
}