package org.dferna14.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.dferna14.project.data.remote.UsuarioDto
import org.dferna14.project.domain.model.Result
import org.dferna14.project.ui.components.CampoPasswordField
import org.dferna14.project.ui.components.CampoPrimaryButton
import org.dferna14.project.ui.components.CampoSecondaryButton
import org.dferna14.project.ui.components.CampoTextField
import org.dferna14.project.ui.theme.BordeNormal
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.theme.OlivaPrimario
import org.dferna14.project.ui.theme.RojoEliminar
import org.dferna14.project.ui.theme.SuperficieSepia
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.viewmodel.AuthVm
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onLoginExitoso: (UsuarioDto) -> Unit,
    onIrARegistro: (() -> Unit)? = null,
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
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //marca
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(SuperficieSepia)
                    .border(2.dp, BordeNormal, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MenuBook,
                    contentDescription = null,
                    tint = OlivaPrimario,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Cuaderno de Campo",
                style = MaterialTheme.typography.titleLarge,
                color = TextoPrimario
            )
            Text(
                text = "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodySmall,
                color = TextoTerciario
            )

            Spacer(Modifier.height(32.dp))

            // forms
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CampoTextField(
                    label = "Correo electrónico",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "nombre@dominio.es",
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
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            //acciones
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CampoPrimaryButton(
                    text = if (cargando) "Entrando…" else "Entrar",
                    enabled = !cargando && email.isNotBlank() && password.isNotBlank(),
                    onClick = { viewModel.login(email.trim(), password) }
                )

                if (onIrARegistro != null) {
                    CampoSecondaryButton(
                        text = "Crear una cuenta",
                        onClick = onIrARegistro
                    )
                }
            }
        }
    }
}
