package org.dferna14.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import org.dferna14.project.ui.components.CampoPrimaryButton
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.theme.NaranjaPrimario
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoSecundario
import androidx.compose.foundation.background

/**
 * Pantalla mostrada cuando un AGRICULTOR intenta entrar desde Desktop.
 * El escritorio está reservado al personal técnico.
 */
@Composable
fun PantallaBloqueada(onCerrarSesion: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CremaPrincipal)
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = NaranjaPrimario
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Acceso restringido",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextoPrimario,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "La aplicación de escritorio está reservada para personal técnico. " +
                "Por favor, utiliza la aplicación móvil para acceder a tus actividades agrícolas.",
            fontSize = 16.sp,
            color = TextoSecundario,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 500.dp)
        )
        Spacer(Modifier.height(40.dp))
        CampoPrimaryButton(
            text = "Cerrar sesión",
            onClick = onCerrarSesion,
            modifier = Modifier.widthIn(max = 300.dp)
        )
    }
}
