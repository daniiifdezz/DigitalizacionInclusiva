package org.dferna14.project

import androidx.compose.runtime.*
import org.dferna14.project.ui.screens.ActividadDetalleSc
import org.dferna14.project.ui.screens.ActividadListadoSc
import org.dferna14.project.ui.screens.DesktopMainSc
import org.dferna14.project.ui.screens.EditarActividadSc
import org.dferna14.project.ui.screens.NuevaActividadSc
import org.dferna14.project.ui.screens.PendientesSc
import org.dferna14.project.ui.screens.ValidarActividadSc
import org.dferna14.project.ui.theme.AppTheme

sealed class Screen {
    object Listado : Screen()
    object NuevaActividad : Screen()
    data class Detalle(val actividadId: Int) : Screen()
    data class Editar(val actividadId: Int) : Screen()
    data class Validar(val actividadId: Int) : Screen()

    // Desktop screens
    object DesktopHome : Screen()
    data class Pendientes(val actividadId: Int) : Screen()
}

@Composable
fun App(isDesktop: Boolean = false) {
    var currentScreen by remember { mutableStateOf<Screen>(
        if (isDesktop) Screen.DesktopHome else Screen.Listado
    ) }

    val volverAListado = if (isDesktop) {
        { currentScreen = Screen.DesktopHome }
    } else {
        { currentScreen = Screen.Listado }
    }

    val volverADetalle = if (isDesktop) {
        { id: Int -> currentScreen = Screen.DesktopHome }
    } else {
        { id: Int -> currentScreen = Screen.Listado }
    }

    AppTheme {
        when (val screen = currentScreen) {
            is Screen.Listado -> {
                ActividadListadoSc(
                    onNuevaActividad = { currentScreen = Screen.NuevaActividad },
                    onVerDetalle = { id -> currentScreen = Screen.Detalle(id) },
                    isDesktop = isDesktop,
                    onVolver = { currentScreen = Screen.DesktopHome }
                )
            }
            is Screen.NuevaActividad -> {
                NuevaActividadSc(
                    onVolver = volverAListado
                )
            }
            is Screen.Detalle -> {
                ActividadDetalleSc(
                    actividadId = screen.actividadId,
                    onVolver = volverAListado,
                    onEditar = { id -> currentScreen = Screen.Editar(id) }
                )
            }
            is Screen.Editar -> {
                EditarActividadSc(
                    actividadId = screen.actividadId,
                    onVolver = volverAListado
                )
            }
            is Screen.Validar -> {
                ValidarActividadSc(
                    actividadId = screen.actividadId,
                    onVolver = { currentScreen = if (isDesktop) Screen.DesktopHome else Screen.Listado }
                )
            }
            is Screen.DesktopHome -> {
                DesktopMainSc(
                    onVerListado = { currentScreen = Screen.Listado },
                    onVerPendientes = { currentScreen = Screen.Pendientes(0) },
                    onVerParcelas = { /* TODO */ },
                    onVerProductos = { /* TODO */ },
                    onVerValidar = { id -> currentScreen = Screen.Validar(id) }
                )
            }
            is Screen.Pendientes -> {
                PendientesSc(
                    onVolver = { currentScreen = Screen.DesktopHome },
                    onVerActividad = { id -> currentScreen = Screen.Validar(id) }
                )
            }
        }
    }
}
