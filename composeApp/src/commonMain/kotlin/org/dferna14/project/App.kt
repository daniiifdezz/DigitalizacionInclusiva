package org.dferna14.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import org.dferna14.project.ui.screens.*
import org.dferna14.project.ui.screens.desktop.AjustesTecnicoSc
import org.dferna14.project.ui.screens.desktop.ConfiguracionInicialSc
import org.dferna14.project.ui.screens.desktop.DesktopMainSc
import org.dferna14.project.ui.screens.desktop.PantallaBloqueadaSc
import org.dferna14.project.ui.screens.desktop.ParcelasDesktopSc
import org.dferna14.project.ui.screens.desktop.ValidarActividadSc
import org.dferna14.project.ui.theme.AppTheme
import org.dferna14.project.ui.theme.BlancoPuro
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.theme.OlivaPrimario
import org.dferna14.project.ui.theme.OlivaTint
import org.dferna14.project.ui.theme.RojoEliminar
import org.dferna14.project.ui.theme.SuperficieSepia
import org.dferna14.project.ui.theme.TextoSecundario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.viewmodel.AuthVm
import org.dferna14.project.ui.viewmodel.EstadoSesion
import androidx.compose.ui.Modifier
import org.dferna14.project.util.AppBackHandler
import org.dferna14.project.util.cerrarApp
import org.koin.compose.viewmodel.koinViewModel

sealed class Screen {
    // Auth
    object Login : Screen()
    object Registro : Screen()

    // Mobile tabs
    object MisActividades : Screen()
    object NuevaActividad : Screen()
    object NuevoTipoActividad : Screen()
    object NuevaSemilla : Screen()
    object NuevaFertilizacion : Screen()
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
    object CuadernoPdf : Screen()
    object DesktopAjustes : Screen()
    object Configuracion : Screen()
    data class EditarParcela(val parcelaId: Int) : Screen()
    data class Pendientes(val actividadId: Int) : Screen()
}

@Composable
fun App(isDesktop: Boolean = false) {
    val authVm: AuthVm = koinViewModel()
    val estadoSesion by authVm.estadoSesion.collectAsState()

    LaunchedEffect(Unit) { authVm.intentarRestaurarSesion() }

    AppTheme {
        when (val sesion = estadoSesion) {
            is EstadoSesion.Comprobando -> PantallaCargandoSesion()

            is EstadoSesion.NoAutenticado -> AuthFlow(authVm = authVm)

            is EstadoSesion.Autenticado -> {
                if (isDesktop && sesion.usuario.rol == "AGRICULTOR") {
                    PantallaBloqueadaSc(onCerrarSesion = { authVm.cerrarSesion() })
                } else {
                    AppContent(isDesktop = isDesktop)
                }
            }
        }
    }
}

@Composable
private fun PantallaCargandoSesion() {
    Box(
        modifier = Modifier.fillMaxSize().background(CremaPrincipal),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = OlivaPrimario)
    }
}

@Composable
private fun AuthFlow(authVm: AuthVm) {
    var enRegistro by remember { mutableStateOf(false) }
    if (enRegistro) {
        RegisterScreen(
            onRegistroExitoso = {},
            onIrALogin = { enRegistro = false },
            viewModel = authVm
        )
    } else {
        LoginScreen(
            onLoginExitoso = {},
            onIrARegistro = { enRegistro = true },
            viewModel = authVm
        )
    }
}

/**
 * Contenido autenticado. Mantiene un historial de pantallas visitadas (pila)
**/

@Composable
private fun AppContent(isDesktop: Boolean) {
    val pantallaPrincipal: Screen =
        if (isDesktop) Screen.DesktopHome else Screen.MisActividades

    val historial = remember { mutableStateListOf<Screen>(pantallaPrincipal) }
    val currentScreen = historial.last()

    fun navegarA(destino: Screen) {
        if (historial.isEmpty() || historial.last() != destino) {
            historial.add(destino)
        }
    }

    fun volverAtras(): Boolean {
        return if (historial.size > 1) {
            historial.removeAt(historial.size - 1)
            true
        } else false
    }

    if (isDesktop) {
        DesktopApp(currentScreen) { navegarA(it) }
    } else {
        MobileApp(
            currentScreen = currentScreen,
            volverAtras = ::volverAtras,
            onNavigate = ::navegarA
        )
    }
}

@Composable
private fun DesktopApp(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    when (val screen = currentScreen) {
        is Screen.DesktopHome -> {
            DesktopMainSc(
                onVerActividades   = { onNavigate(Screen.MisActividades) },
                onVerParcelas      = { onNavigate(Screen.Parcelas) },
                onVerProductos     = { onNavigate(Screen.Productos) },
                onVerAjustes       = { onNavigate(Screen.DesktopAjustes) },
                onVerConfiguracion = { onNavigate(Screen.Configuracion) },
                onVerValidar       = { id -> onNavigate(Screen.Validar(id)) },
                onNuevaEntrada     = { onNavigate(Screen.NuevaActividad) },
                onExportarPdf      = { onNavigate(Screen.CuadernoPdf) },
            )
        }
        is Screen.Configuracion -> {
            ConfiguracionInicialSc(
                onVerInicio        = { onNavigate(Screen.DesktopHome) },
                onVerActividades   = { onNavigate(Screen.MisActividades) },
                onVerParcelas      = { onNavigate(Screen.Parcelas) },
                onVerProductos     = { onNavigate(Screen.Productos) },
                onVerAjustes       = { onNavigate(Screen.DesktopAjustes) },
            )
        }
        is Screen.Validar -> {
            ValidarActividadSc(
                actividadId        = screen.actividadId,
                onVolver           = { onNavigate(Screen.MisActividades) },
                onVerInicio        = { onNavigate(Screen.DesktopHome) },
                onVerActividades   = { onNavigate(Screen.MisActividades) },
                onVerParcelas      = { onNavigate(Screen.Parcelas) },
                onVerProductos     = { onNavigate(Screen.Productos) },
                onVerAjustes       = { onNavigate(Screen.DesktopAjustes) },
                onVerConfiguracion = { onNavigate(Screen.Configuracion) },
            )
        }
        is Screen.Parcelas -> {
            ParcelasDesktopSc(
                onVerInicio        = { onNavigate(Screen.DesktopHome) },
                onVerActividades   = { onNavigate(Screen.MisActividades) },
                onVerProductos     = { onNavigate(Screen.Productos) },
                onVerAjustes       = { onNavigate(Screen.DesktopAjustes) },
                onVerConfiguracion = { onNavigate(Screen.Configuracion) },
            )
        }
        is Screen.DesktopAjustes -> {
            AjustesTecnicoSc(
                onVerInicio        = { onNavigate(Screen.DesktopHome) },
                onVerActividades   = { onNavigate(Screen.MisActividades) },
                onVerParcelas      = { onNavigate(Screen.Parcelas) },
                onVerProductos     = { onNavigate(Screen.Productos) },
                onVerConfiguracion = { onNavigate(Screen.Configuracion) },
                onCerrarSesion     = {},
            )
        }
        is Screen.MisActividades -> {
            ActividadListadoSc(
                onNuevaActividad   = { onNavigate(Screen.NuevaActividad) },
                onVerDetalle       = { id -> onNavigate(Screen.Detalle(id)) },
                isDesktop          = true,
                onVolver           = { onNavigate(Screen.DesktopHome) },
                onVerInicio        = { onNavigate(Screen.DesktopHome) },
                onVerParcelas      = { onNavigate(Screen.Parcelas) },
                onVerProductos     = { onNavigate(Screen.Productos) },
                onVerAjustes       = { onNavigate(Screen.DesktopAjustes) },
                onVerConfiguracion = { onNavigate(Screen.Configuracion) }
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
                onVolver    = { onNavigate(Screen.MisActividades) },
                onEditar    = { id -> onNavigate(Screen.Editar(id)) }
            )
        }
        is Screen.Editar -> {
            EditarActividadSc(
                actividadId = screen.actividadId,
                onVolver    = { onNavigate(Screen.MisActividades) }
            )
        }
        is Screen.Productos -> {
            ProductosSc(
                onVolver           = { onNavigate(Screen.DesktopHome) },
                isDesktop          = true,
                onVerInicio        = { onNavigate(Screen.DesktopHome) },
                onVerActividades   = { onNavigate(Screen.MisActividades) },
                onVerParcelas      = { onNavigate(Screen.Parcelas) },
                onVerAjustes       = { onNavigate(Screen.DesktopAjustes) },
                onVerConfiguracion = { onNavigate(Screen.Configuracion) }
            )
        }
        is Screen.CuadernoPdf -> {
            CuadernoPdfSc(
                onVolver = { onNavigate(Screen.DesktopHome) }
            )
        }
        else -> {}
    }
}

private data class TabItem(val titulo: String, val icono: ImageVector)

@Composable
private fun MobileApp(
    currentScreen: Screen,
    volverAtras: () -> Boolean,
    onNavigate: (Screen) -> Unit
) {
    var mostrarDialogoSalida by remember { mutableStateOf(false) }

    val selectedTab = when (currentScreen) {
        is Screen.MisParcelas -> 1
        is Screen.Productos   -> 2
        is Screen.Ajustes     -> 3
        else                  -> 0  // MisActividades y todas las pantallas de flujo
    }

    // Intercepta el botón/gesto atrás del sistema (no-op silencioso en Desktop).
    AppBackHandler(enabled = true) {
        val pudoVolver = volverAtras()
        if (!pudoVolver) {
            // Estamos en la raíz — preguntar al usuario si quiere salir
            mostrarDialogoSalida = true
        }
    }

    val tabs = listOf(
        TabItem("Actividades", Icons.Outlined.Assignment),
        TabItem("Parcelas",    Icons.Outlined.Map),
        TabItem("Productos",   Icons.Outlined.Science),
        TabItem("Ajustes",     Icons.Outlined.Settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = SuperficieSepia) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
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
                            selectedIconColor   = OlivaPrimario,
                            selectedTextColor   = OlivaPrimario,
                            unselectedIconColor = TextoTerciario,
                            unselectedTextColor = TextoTerciario,
                            indicatorColor      = OlivaTint
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val screen = currentScreen) {
                is Screen.MisActividades -> {
                    ActividadListadoSc(
                        onNuevaActividad = { onNavigate(Screen.NuevoTipoActividad) },
                        onVerDetalle     = { id -> onNavigate(Screen.Detalle(id)) },
                        isDesktop        = false
                    )
                }
                is Screen.NuevoTipoActividad -> {
                    NuevoTipoActividadSc(
                        onTratamiento    = { onNavigate(Screen.NuevaActividad) },
                        onSemilla        = { onNavigate(Screen.NuevaSemilla) },
                        onFertilizacion  = { onNavigate(Screen.NuevaFertilizacion) },
                        onVolver         = { onNavigate(Screen.MisActividades) }
                    )
                }
                is Screen.NuevaActividad -> {
                    NuevaActividadSc(
                        onVolver = { onNavigate(Screen.MisActividades) }
                    )
                }
                is Screen.NuevaSemilla -> {
                    NuevaSemillaSc(
                        onVolver = { onNavigate(Screen.MisActividades) }
                    )
                }
                is Screen.NuevaFertilizacion -> {
                    NuevaFertilizacionSc(
                        onVolver = { onNavigate(Screen.MisActividades) }
                    )
                }
                is Screen.Detalle -> {
                    ActividadDetalleSc(
                        actividadId = screen.actividadId,
                        onVolver    = { onNavigate(Screen.MisActividades) },
                        onEditar    = { id -> onNavigate(Screen.Editar(id)) }
                    )
                }
                is Screen.Editar -> {
                    EditarActividadSc(
                        actividadId = screen.actividadId,
                        onVolver    = { onNavigate(Screen.MisActividades) }
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
                else -> {}
            }
        }
    }

    if (mostrarDialogoSalida) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSalida = false },
            title = { Text("¿Salir de la aplicación?") },
            text = { Text("Si sales, tendrás que volver a abrir la app para continuar.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoSalida = false
                    cerrarApp()
                }) {
                    Text(
                        "Salir",
                        color = RojoEliminar,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoSalida = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
    }
}
