package org.dferna14.project

import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.dferna14.project.ui.screens.ActividadListadoSc
import org.dferna14.project.ui.screens.NuevaActividadSc
import org.dferna14.project.ui.theme.AppTheme

sealed class Screen {
    object Listado : Screen()
    object NuevaActividad : Screen()
}

@Composable
fun App() {
    // Empezamos en el listado
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Listado) }

    val platform = remember { getPlatform() }
    val isDesktop = platform.name.contains("Java", ignoreCase = true)

    AppTheme {
        when (currentScreen) {
            is Screen.Listado -> {
                ActividadListadoSc(
                    onNuevaActividad = {
                        currentScreen = Screen.NuevaActividad
                    }
                )
            }
            is Screen.NuevaActividad -> {
                NuevaActividadSc(
                    onVolver = {
                        currentScreen = Screen.Listado
                    }
                )
            }
        }
    }
}