package org.dferna14.project

import androidx.compose.runtime.*
import org.dferna14.project.ui.screens.ActividadDetalleSc
import org.dferna14.project.ui.screens.ActividadListadoSc
import org.dferna14.project.ui.screens.EditarActividadSc
import org.dferna14.project.ui.screens.NuevaActividadSc
import org.dferna14.project.ui.screens.ValidarActividadSc
import org.dferna14.project.ui.theme.AppTheme

sealed class Screen {
    object Listado : Screen()
    object NuevaActividad : Screen()
    data class Detalle(val actividadId: Int) : Screen()
    data class Editar(val actividadId: Int) : Screen()
    data class Validar(val actividadId: Int) : Screen()
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Listado) }

    AppTheme {
        when (val screen = currentScreen) {
            is Screen.Listado -> {
                ActividadListadoSc(
                    onNuevaActividad = { currentScreen = Screen.NuevaActividad },
                    onVerDetalle = { id -> currentScreen = Screen.Detalle(id) }
                )
            }
            is Screen.NuevaActividad -> {
                NuevaActividadSc(
                    onVolver = { currentScreen = Screen.Listado }
                )
            }
is Screen.Detalle -> {
    ActividadDetalleSc(
        actividadId = screen.actividadId,
        onVolver = { currentScreen = Screen.Listado },
        onEditar = { id -> currentScreen = Screen.Editar(id) }
    )
}
            is Screen.Editar -> {
                EditarActividadSc(
                    actividadId = screen.actividadId,
                    onVolver = { currentScreen = Screen.Listado }
                )
            }
            is Screen.Validar -> {
                ValidarActividadSc(
                    actividadId = screen.actividadId,
                    onVolver = { currentScreen = Screen.Listado }
                )
            }
        }
    }
}
