package org.dferna14.project

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import org.dferna14.project.ui.screens.*
import org.dferna14.project.ui.theme.AppTheme
import org.dferna14.project.ui.theme.BlancoPuro
import org.dferna14.project.ui.theme.NaranjaClaro
import org.dferna14.project.ui.theme.NaranjaPrimario
import org.dferna14.project.ui.theme.TextoTerciario
import androidx.compose.ui.Modifier

sealed class Screen {
    // Auth
    object Login : Screen()
    object Registro : Screen()

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
    data class Semillas(val actividadId: Int) : Screen()
    // Fertilización: almacenamos parcelaId (referencia) y actividadId (para volver atrás)
    data class Fertilizacion(val parcelaId: Int, val actividadId: Int) : Screen()

    // Desktop screens
    object DesktopHome : Screen()
    object Parcelas : Screen()
    object Configuracion : Screen()
    data class EditarParcela(val parcelaId: Int) : Screen()
    data class Pendientes(val actividadId: Int) : Screen()
}

@Composable
fun App(isDesktop: Boolean = false) {
    // Arranca SIEMPRE en Login (móvil y desktop) hasta que el usuario se autentique
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

    AppTheme {
        when (currentScreen) {
            is Screen.Login -> {
                LoginScreen(
                    onLoginExitoso = {
                        currentScreen = if (isDesktop) Screen.DesktopHome else Screen.NuevaActividad
                    },
                    onIrARegistro = { currentScreen = Screen.Registro }
                )
            }
            is Screen.Registro -> {
                RegisterScreen(
                    onRegistroExitoso = {
                        currentScreen = if (isDesktop) Screen.DesktopHome else Screen.NuevaActividad
                    },
                    onIrALogin = { currentScreen = Screen.Login }
                )
            }
            else -> {
                if (isDesktop) {
                    DesktopApp(currentScreen) { currentScreen = it }
                } else {
                    MobileApp(currentScreen) { currentScreen = it }
                }
            }
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
                onVerValidar = { id -> onNavigate(Screen.Validar(id)) },
                onVerConfiguracion = { onNavigate(Screen.Configuracion) }
            )
        }
        is Screen.Configuracion -> {
            ConfiguracionSc(
                onVolver = { onNavigate(Screen.DesktopHome) }
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
                onEditar = { id -> onNavigate(Screen.Editar(id)) },
                onVerSemillas = { id -> onNavigate(Screen.Semillas(id)) },
                onVerFertilizacion = { parcelaId -> onNavigate(Screen.Fertilizacion(parcelaId, screen.actividadId)) }
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
                onVolver = { onNavigate(Screen.DesktopHome) },
                onIrAEditarParcela = { parcelaId -> onNavigate(Screen.EditarParcela(parcelaId)) }
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
                onVolver = { onNavigate(Screen.DesktopHome) },
                onEditarParcela = { id -> onNavigate(Screen.EditarParcela(id)) }
            )
        }
        is Screen.EditarParcela -> {
            EditarParcelaSc(
                parcelaId = screen.parcelaId,
                onVolver = { onNavigate(Screen.Parcelas) }
            )
        }
        is Screen.Semillas -> {
            SemillasTratadasSc(
                actividadId = screen.actividadId,
                onVolver = { onNavigate(Screen.Detalle(screen.actividadId)) }
            )
        }
        is Screen.Fertilizacion -> {
            FertilizacionSc(
                parcelaId = screen.parcelaId,
                actividadId = screen.actividadId,
                onVolver = { onNavigate(Screen.Detalle(screen.actividadId)) }
            )
        }
        is Screen.Productos -> {
            ProductosSc(
                onVolver = { onNavigate(Screen.DesktopHome) }
            )
        }
        else -> {}
    }
}

private data class TabItem(val titulo: String, val icono: ImageVector)

@Composable
private fun MobileApp(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        TabItem("Actividades", Icons.Outlined.Assignment),
        TabItem("Parcelas",    Icons.Outlined.Map),
        TabItem("Productos",   Icons.Outlined.Science),
        TabItem("Ajustes",     Icons.Outlined.Settings)
    )
    val screen = currentScreen

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = BlancoPuro
            ) {
                tabs.forEachIndexed { index, tab ->
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
                        icon  = { Icon(tab.icono, contentDescription = tab.titulo) },
                        label = { Text(tab.titulo) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = NaranjaPrimario,
                            selectedTextColor   = NaranjaPrimario,
                            unselectedIconColor = TextoTerciario,
                            unselectedTextColor = TextoTerciario,
                            indicatorColor      = NaranjaClaro
                        )
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
                        onEditar = { id -> onNavigate(Screen.Editar(id)) },
                        onVerSemillas = { id -> onNavigate(Screen.Semillas(id)) },
                onVerFertilizacion = { parcelaId -> onNavigate(Screen.Fertilizacion(parcelaId, screen.actividadId)) }
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
                    ProductosSc(
                        onVolver = { onNavigate(Screen.MisActividades) }
                    )
                }
                is Screen.Ajustes -> {
                    AjustesSc()
                }
                is Screen.Semillas -> {
                    SemillasTratadasSc(
                        actividadId = screen.actividadId,
                        onVolver = { onNavigate(Screen.Detalle(screen.actividadId)) }
                    )
                }
                is Screen.Fertilizacion -> {
                    FertilizacionSc(
                        parcelaId = screen.parcelaId,
                        actividadId = screen.actividadId,
                        onVolver = { onNavigate(Screen.Detalle(screen.actividadId)) }
                    )
                }
                else -> {}
            }
        }
    }
}