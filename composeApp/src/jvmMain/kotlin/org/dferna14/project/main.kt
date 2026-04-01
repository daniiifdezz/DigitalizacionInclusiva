package org.dferna14.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.dferna14.project.di.appModule
import org.dferna14.project.ui.screens.ActividadListSc
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
    //arrancamos pantalla desktop
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Cuaderno de Campo"
        ) {
            ActividadListSc(
                onNuevaActividad = { //navegacion a pantalla nueva actividad
                }
            )
        }
    }
}