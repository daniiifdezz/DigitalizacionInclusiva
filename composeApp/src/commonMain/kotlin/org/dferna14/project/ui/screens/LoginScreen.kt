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
import org.dferna14.project.ui.components.CampoPasswordField
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
 * Pantalla de login. Reutiliza los componentes de CampoComponents para mantener
 * la identidad visual del resto de la app. El éxito de autenticación se notifica
 * vía onLoginExitoso(usuario) — la navegación la decide App.kt.
 */
@Composable
fun LoginScreen(
    onLoginExitoso: (UsuarioDto) -> Unit,
    onIrARegistro: () -> Unit,
    viewModel: AuthVm = koinViewModel()
) {
    val estado by viewModel.estado.collectAsState()

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(estado) {
        val actual = estado
        if (actual is Result.Success) {
            onLoginExitoso(actual.data)
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
            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(NaranjaClaro),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CC",
                    color = NaranjaPrimario,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Cuaderno de Campo",
                color = TextoPrimario,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Inicia sesión para continuar",
                color = TextoSecundario,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(8.dp))

            CampoTextField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "usuario@ejemplo.com",
                keyboardType = KeyboardType.Email
            )

            CampoPasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMsg != null) {
                Text(
                    text = errorMsg,
                    color = RojoEliminar,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(4.dp))

            CampoPrimaryButton(
                text = if (cargando) "Entrando…" else "Entrar",
                enabled = !cargando && email.isNotBlank() && password.isNotBlank(),
                onClick = { viewModel.login(email.trim(), password) }
            )

            CampoSecondaryButton(
                text = "Crear una cuenta",
                onClick = onIrARegistro
            )
        }
    }
}
