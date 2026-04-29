package org.dferna14.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import org.dferna14.project.ui.screens.*
import org.dferna14.project.ui.theme.AppTheme
import androidx.compose.ui.Modifier

sealed class Screen {
    // Mobile tabs
    object MisActividades : Screen()
    object NuevaActividad : Screen()
    object MisParcelas : Screen()
    object Productos : Screen()
    object Ajustes : Screen()

    // Activity flow
    data class Detalle(val actividadId: Int) : Screen()
    data class Editar(val actividadId: Int) : Screen()
    data class Validar(val actividadId: Int) : Screen()

    // Desktop screens
    object DesktopHome : Screen()
    object Parcelas : Screen()
    data class Pendientes(val actividadId: Int) : Screen()
}

@Composable
fun App(isDesktop: Boolean = false) {
    var currentScreen by remember { mutableStateOf<Screen>(
        if (isDesktop) Screen.DesktopHome else Screen.MisActividades
    ) }

    AppTheme {
        if (isDesktop) {
            DesktopApp(currentScreen) { currentScreen = it }
        } else {
            MobileApp(currentScreen) { currentScreen = it }
        }
    }
}

@Composable
private fun DesktopApp(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    when (val screen = currentScreen) {
        is Screen.DesktopHome -> {
            DesktopMainSc(
                onVerListado = { onNavigate(Screen.MisActividades) },
                onVerPendientes = { onNavigate(Screen.Pendientes(0)) },
                onVerParcelas = { onNavigate(Screen.Parcelas) },
                onVerProductos = { onNavigate(Screen.Productos) },
                onVerValidar = { id -> onNavigate(Screen.Validar(id)) }
            )
        }
        is Screen.MisActividades -> {
            ActividadListadoSc(
                onNuevaActividad = { onNavigate(Screen.NuevaActividad) },
                onVerDetalle = { id -> onNavigate(Screen.Detalle(id)) },
                isDesktop = true,
                onVolver = { onNavigate(Screen.DesktopHome) }
            )
        }
        is Screen.NuevaActividad -> {
            NuevaActividadSc(
                onVolver = { onNavigate(Screen.MisActividades) }
            )
        }
        is Screen.Detalle -> {
            ActividadDetalleSc(
                actividadId = screen.actividadId,
                onVolver = { onNavigate(Screen.MisActividades) },
                onEditar = { id -> onNavigate(Screen.Editar(id)) }
            )
        }
        is Screen.Editar -> {
            EditarActividadSc(
                actividadId = screen.actividadId,
                onVolver = { onNavigate(Screen.MisActividades) }
            )
        }
        is Screen.Validar -> {
            ValidarActividadSc(
                actividadId = screen.actividadId,
                onVolver = { onNavigate(Screen.DesktopHome) }
            )
        }
        is Screen.Pendientes -> {
            PendientesSc(
                onVolver = { onNavigate(Screen.DesktopHome) },
                onVerActividad = { id -> onNavigate(Screen.Validar(id)) }
            )
        }
        is Screen.Parcelas -> {
            ParcelasSc(
                onVolver = { onNavigate(Screen.DesktopHome) }
            )
        }
        is Screen.Productos -> {
            ProductosSc()
        }
        else -> {}
    }
}

@Composable
private fun MobileApp(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Actividades", "Parcelas", "Productos", "Ajustes")
    val screen = currentScreen

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            when (index) {
                                0 -> onNavigate(Screen.MisActividades)
                                1 -> onNavigate(Screen.MisParcelas)
                                2 -> onNavigate(Screen.Productos)
                                3 -> onNavigate(Screen.Ajustes)
                            }
                        },
                        icon = { Text(if (index == 0) "A" else if (index == 1) "P" else if (index == 2) "Pr" else "Aj") },
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (screen) {
                is Screen.MisActividades -> {
                    ActividadListadoSc(
                        onNuevaActividad = { onNavigate(Screen.NuevaActividad) },
                        onVerDetalle = { id -> onNavigate(Screen.Detalle(id)) },
                        isDesktop = false
                    )
                }
                is Screen.NuevaActividad -> {
                    NuevaActividadSc(
                        onVolver = { onNavigate(Screen.MisActividades) }
                    )
                }
                is Screen.Detalle -> {
                    ActividadDetalleSc(
                        actividadId = screen.actividadId,
                        onVolver = { onNavigate(Screen.MisActividades) },
                        onEditar = { id -> onNavigate(Screen.Editar(id)) }
                    )
                }
                is Screen.Editar -> {
                    EditarActividadSc(
                        actividadId = screen.actividadId,
                        onVolver = { onNavigate(Screen.MisActividades) }
                    )
                }
                is Screen.MisParcelas -> {
                    MisParcelasSc()
                }
                is Screen.Productos -> {
                    ProductosSc()
                }
                is Screen.Ajustes -> {
                    AjustesSc()
                }
                else -> {}
            }
        }
    }
}