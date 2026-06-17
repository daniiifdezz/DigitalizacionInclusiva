package org.dferna14.project.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.Usuario
import org.dferna14.project.ui.components.CampoCard
import org.dferna14.project.ui.components.CampoField
import org.dferna14.project.ui.components.CampoPasswordField
import org.dferna14.project.ui.components.CampoTextField
import org.dferna14.project.ui.components.SectionHeader
import org.dferna14.project.ui.theme.*
import org.dferna14.project.ui.viewmodel.AjustesVm
import org.dferna14.project.ui.viewmodel.AuthVm
import org.dferna14.project.ui.viewmodel.UsuarioVm
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesSc(
    mostrarBotonCerrarSesion: Boolean = true,
    authVm: AuthVm = koinViewModel(),
    ajustesVm: AjustesVm = koinViewModel(),
    usuarioVm: UsuarioVm = koinViewModel(),
    onVolver: (() -> Unit)? = null,
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var mostrarDialogoAgricultor by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val mensaje by ajustesVm.mensaje.collectAsState()
    val usuariosResult by usuarioVm.usuarios.collectAsState()
    val mensajeUsuario by usuarioVm.mensajeError.collectAsState()

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            ajustesVm.limpiarMensaje()
        }
    }

    LaunchedEffect(mensajeUsuario) {
        mensajeUsuario?.let {
            snackbarHostState.showSnackbar(it)
            usuarioVm.limpiarMensajeError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CremaPrincipal,
        topBar = {
            TopAppBar(
                title = { Text("Ajustes", style = MaterialTheme.typography.titleLarge, color = TextoPrimario) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SuperficieSepia),
                navigationIcon = {
                    if (onVolver != null) {
                        IconButton(onClick = onVolver) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = OlivaPrimario
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
                .padding(top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (mostrarBotonCerrarSesion) {
                val nombre = ajustesVm.nombreMostrado.ifBlank { ajustesVm.emailUsuario }
                val iniciales = nombre.split(" ")
                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    .take(2)
                    .joinToString("")
                val rol = ajustesVm.rolUsuario.lowercase().replaceFirstChar { it.uppercase() }

                // Avatar + nombre + rol
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(OlivaTint)
                            .border(2.dp, OlivaClaro, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = iniciales,
                            style = MaterialTheme.typography.titleLarge,
                            color = OlivaPrimario,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = nombre,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextoPrimario
                    )
                    Box(
                        modifier = Modifier
                            .background(OlivaTint, RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = rol,
                            style = MaterialTheme.typography.labelSmall,
                            color = OlivaPrimario,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // MI CUENTA
                SectionHeader("MI CUENTA")
                CampoCard {
                    CampoField(label = "Correo", value = ajustesVm.emailUsuario)
                    if (!ajustesVm.explotacionNombre.isNullOrBlank()) {
                        CampoField(label = "Explotación", value = ajustesVm.explotacionNombre!!)
                    }
                }

                // ACERCA DE
                SectionHeader("ACERCA DE")
                CampoCard {
                    CampoField(label = "Aplicación", value = "Cuaderno de Campo Digital v1.0.0")
                    CampoField(label = "Autoría", value = "TFG — Daniel Fernández")
                }
            }

            // MIS AGRICULTORES (solo TECNICO)
            if (ajustesVm.rolUsuario == "TECNICO") {
                val agricultores = (usuariosResult as? Result.Success)?.data
                    ?.filter { it.rol == "AGRICULTOR" } ?: emptyList()

                SectionHeader("MIS AGRICULTORES")
                CampoCard {
                    if (agricultores.isEmpty()) {
                        Text(
                            text = "Sin agricultores registrados",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoTerciario
                        )
                    } else {
                        agricultores.forEachIndexed { i, ag ->
                            if (i > 0) HorizontalDivider(color = BordeSuave, thickness = 0.5.dp)
                            AgricultorFila(
                                usuario    = ag,
                                onEliminar = { usuarioVm.eliminarAplicador(ag.id) }
                            )
                        }
                        HorizontalDivider(color = BordeSuave, thickness = 0.5.dp)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mostrarDialogoAgricultor = true }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = null,
                            tint = OlivaPrimario,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Añadir agricultor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OlivaPrimario,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Botón cerrar sesión
            if (mostrarBotonCerrarSesion) {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { mostrarConfirmacion = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                    border = BorderStroke(1.dp, TerracotaAccent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracotaAccent),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = null,
                        tint = TerracotaAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cerrar sesión",
                            color = TerracotaAccent,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Saldrás de tu cuenta en este dispositivo",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario
                        )
                    }
                }
            }
        }
    }

    // Diálogo confirmar cerrar sesión
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            containerColor = SuperficieSepia,
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

    // Diálogo nuevo agricultor
    if (mostrarDialogoAgricultor) {
        var nombre by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var contrasena by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { mostrarDialogoAgricultor = false },
            containerColor = SuperficieSepia,
            title = { Text("Nuevo agricultor") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoTextField(
                        label = "Nombre",
                        value = nombre,
                        onValueChange = { nombre = it }
                    )
                    CampoTextField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it },
                        keyboardType = KeyboardType.Email
                    )
                    CampoPasswordField(
                        label = "Contraseña",
                        value = contrasena,
                        onValueChange = { contrasena = it }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        usuarioVm.crearAplicador(
                            usuario    = Usuario(nombre = nombre.trim(), email = email.trim(), rol = "AGRICULTOR"),
                            contrasena = contrasena
                        )
                        mostrarDialogoAgricultor = false
                    },
                    enabled = nombre.isNotBlank() && email.isNotBlank() && contrasena.length >= 6
                ) {
                    Text("Crear", color = OlivaPrimario, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoAgricultor = false }) {
                    Text("Cancelar", color = TextoSecundario)
                }
            }
        )
    }
}

@Composable
private fun AgricultorFila(usuario: Usuario, onEliminar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(OlivaTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = OlivaPrimario,
                modifier = Modifier.size(16.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = usuario.nombre,
                style = MaterialTheme.typography.bodyMedium,
                color = TextoPrimario,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = usuario.email,
                style = MaterialTheme.typography.labelSmall,
                color = TextoTerciario
            )
        }
        IconButton(
            onClick = onEliminar,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Eliminar agricultor",
                tint = RojoEliminar,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
