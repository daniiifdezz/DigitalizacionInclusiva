package org.dferna14.project.ui.screens.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.theme.TerracotaAccent
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoSecundario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.theme.extraTypography

// Colores locales para el icono de candado (terracota tint)
private val TerracotaTint = Color(0xFFFCEAE4)
private val TerracotaLight = Color(0xFFF08860)

@Composable
fun PantallaBloqueadaSc(onCerrarSesion: () -> Unit) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(CremaPrincipal),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier            = Modifier
                .widthIn(max = 460.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0x1A2C1A0E))
                .background(Color(0xFFF5ECD7), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFDFCFB0), RoundedCornerShape(16.dp))
                .padding(horizontal = 40.dp, vertical = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icono candado
            Box(
                modifier         = Modifier
                    .size(72.dp)
                    .background(TerracotaTint, CircleShape)
                    .border(2.dp, TerracotaLight, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint               = TerracotaAccent,
                    modifier           = Modifier.size(32.dp),
                )
            }

            Spacer(Modifier.height(22.dp))

            // Título
            Text(
                text      = "Acceso no autorizado",
                style     = MaterialTheme.extraTypography.display.copy(fontSize = 26.sp),
                color     = TextoPrimario,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            // Descripción principal
            Text(
                text = buildAnnotatedString {
                    append("Esta plataforma está reservada exclusivamente para ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextoPrimario)) {
                        append("técnicos y asesores agrícolas")
                    }
                    append(".")
                },
                fontSize  = 15.sp,
                color     = TextoSecundario,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
            )

            Spacer(Modifier.height(10.dp))

            // Descripción secundaria
            Text(
                text      = "Si eres agricultor, accede a través de la aplicación " +
                        "móvil Android para registrar tus actividades y consultar " +
                        "tu cuaderno de campo.",
                fontSize  = 13.5.sp,
                color     = TextoTerciario,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp,
            )

            Spacer(Modifier.height(30.dp))

            // Botón cerrar sesión
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(2.dp, TerracotaAccent, RoundedCornerShape(8.dp))
                    .clickable(onClick = onCerrarSesion),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Close,
                    contentDescription = null,
                    tint               = TerracotaAccent,
                    modifier           = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text       = "Cerrar sesión",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TerracotaAccent,
                )
            }
        }
    }
}
