package org.dferna14.project.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dferna14.project.ui.theme.BlancoPuro
import org.dferna14.project.ui.theme.BordeSuave
import org.dferna14.project.ui.theme.CremaSecundario
import org.dferna14.project.ui.theme.NaranjaClaro
import org.dferna14.project.ui.theme.NaranjaPrimario
import org.dferna14.project.ui.theme.RojoEliminar
import org.dferna14.project.ui.theme.RojoFondoEliminar
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoSecundario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.theme.VerdeValidada
import org.dferna14.project.ui.viewmodel.AjustesVm
import org.dferna14.project.ui.viewmodel.AuthVm
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla de Ajustes: configuración de red (URL del backend) y, opcionalmente,
 * el bloque de cerrar sesión.
 *
 * [mostrarBotonCerrarSesion] = true  → versión móvil (con cerrar sesión al final)
 * [mostrarBotonCerrarSesion] = false → versión Desktop (solo configuración de red;
 *   el logout ya está en el menú lateral)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesSc(
    mostrarBotonCerrarSesion: Boolean = true,
    authVm: AuthVm = koinViewModel(),
    ajustesVm: AjustesVm = koinViewModel(),
    onVolver: (() -> Unit)? = null,

) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val mensaje by ajustesVm.mensaje.collectAsState()

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            ajustesVm.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Ajustes", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    if (onVolver != null) {
                        IconButton(onClick = onVolver) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver al menú principal"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            if (mostrarBotonCerrarSesion) {
                AjusteSeccion(titulo = "MI CUENTA") {
                    AjusteFilaPerfil(
                        nombre = ajustesVm.nombreMostrado.ifBlank { ajustesVm.emailUsuario },
                        rol    = ajustesVm.rolUsuario.lowercase().replaceFirstChar { it.uppercase() }
                    )
                }

                AjusteSeccion(titulo = "ACERCA DE") {
                    AjusteFila(
                        icono = Icons.Outlined.Info,
                        texto = "Cuaderno de Campo Digital",
                        valor = "v1.0.0"
                    )
                    HorizontalDivider(color = CremaSecundario, thickness = 0.5.dp)
                    AjusteFila(
                        icono = Icons.Outlined.School,
                        texto = "TFG — Daniel Fernández"
                    )
                }
            }

            if (mostrarBotonCerrarSesion) {
                Spacer(Modifier.height(4.dp ))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostrarConfirmacion = true },
                    colors = CardDefaults.cardColors(containerColor = RojoFondoEliminar),
                    border = BorderStroke(0.5.dp, RojoEliminar.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Logout,
                            contentDescription = null,
                            tint = RojoEliminar,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Cerrar sesión",
                                color = RojoEliminar,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Saldrás de tu cuenta en este dispositivo",
                                color = TextoSecundario,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tendrás que iniciar sesión de nuevo la próxima vez.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacion = false
                    authVm.cerrarSesion()
                }) {
                    Text("Cerrar sesión", color = RojoEliminar, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
    }
}

@Composable
private fun AjusteSeccion(titulo: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = titulo.uppercase(),
            color = TextoTerciario,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlancoPuro),
            border = BorderStroke(0.5.dp, BordeSuave),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun AjusteFila(
    icono: ImageVector,
    texto: String,
    valor: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = NaranjaPrimario,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = texto,
            color = TextoPrimario,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        if (valor != null) {
            Text(text = valor, color = TextoTerciario, fontSize = 13.sp)
        }
    }
}

@Composable
private fun AjusteFilaPerfil(nombre: String, rol: String) {
    val iniciales = nombre.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NaranjaClaro),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iniciales,
                color = NaranjaPrimario,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = nombre, color = TextoPrimario, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = rol, color = TextoTerciario, fontSize = 12.sp)
        }
    }
}
