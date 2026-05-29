package org.dferna14.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dferna14.project.data.remote.UsuarioDto
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.CampoPrimaryButton
import org.dferna14.project.ui.components.CampoSecondaryButton
import org.dferna14.project.ui.components.CampoTextField
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.theme.NaranjaClaro
import org.dferna14.project.ui.theme.NaranjaPrimario
import org.dferna14.project.ui.theme.RojoEliminar
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoSecundario
import org.dferna14.project.ui.viewmodel.AuthVm
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla de registro. Al recibir Result.Success notifica vía
 * onRegistroExitoso(usuario) y la navegación la decide App.kt.
 */
@Composable
fun RegisterScreen(
    onRegistroExitoso: (UsuarioDto) -> Unit,
    onIrALogin: () -> Unit,
    viewModel: AuthVm = koinViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    var nombre        by remember { mutableStateOf("") }
    var email         by remember { mutableStateOf("") }
    var password      by remember { mutableStateOf("") }
    var confirmPass   by remember { mutableStateOf("") }

    val passwordsCoinciden = password == confirmPass
    val passwordValida     = password.length >= 6

    LaunchedEffect(estado) {
        val actual = estado
        if (actual is Result.Success) {
            onRegistroExitoso(actual.data)
            viewModel.resetEstado()
        }
    }

    val cargando = estado is Result.Loading
    val errorMsg = (estado as? Result.Error)?.message

    Scaffold(containerColor = CremaPrincipal) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(NaranjaClaro),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = NaranjaPrimario,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Crear cuenta",
                color = TextoPrimario,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Regístrate para empezar a usar la app",
                color = TextoSecundario,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(4.dp))

            CampoTextField(
                label = "Nombre",
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = "Tu nombre"
            )

            CampoTextField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "usuario@ejemplo.com",
                keyboardType = KeyboardType.Email
            )

            CampoTextField(
                label = "Contraseña (mín. 6 caracteres)",
                value = password,
                onValueChange = { password = it },
                placeholder = "Crea una contraseña",
                keyboardType = KeyboardType.Password
            )

            CampoTextField(
                label = "Repetir contraseña",
                value = confirmPass,
                onValueChange = { confirmPass = it },
                placeholder = "Vuelve a escribirla",
                keyboardType = KeyboardType.Password
            )

            val avisoLocal = when {
                password.isNotBlank() && !passwordValida    -> "La contraseña debe tener al menos 6 caracteres"
                confirmPass.isNotBlank() && !passwordsCoinciden -> "Las contraseñas no coinciden"
                else -> null
            }

            if (avisoLocal != null) {
                Text(
                    text = avisoLocal,
                    color = RojoEliminar,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (errorMsg != null) {
                Text(
                    text = errorMsg,
                    color = RojoEliminar,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(4.dp))

            val puedeEnviar = !cargando
                && nombre.isNotBlank()
                && email.isNotBlank()
                && passwordValida
                && passwordsCoinciden

            CampoPrimaryButton(
                text = if (cargando) "Creando cuenta…" else "Registrarme",
                enabled = puedeEnviar,
                onClick = {
                    viewModel.register(
                        email    = email.trim(),
                        password = password,
                        nombre   = nombre.trim()
                    )
                }
            )

            CampoSecondaryButton(
                text = "Ya tengo cuenta",
                onClick = onIrALogin
            )
        }
    }
}
