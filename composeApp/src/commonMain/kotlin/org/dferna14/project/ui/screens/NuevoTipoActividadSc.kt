package org.dferna14.project.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.dferna14.project.ui.theme.*

@Composable
fun NuevoTipoActividadSc(
    onTratamiento: () -> Unit,
    onSemilla: () -> Unit,
    onFertilizacion: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(containerColor = CremaPrincipal) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavBarFormulario(titulo = "¿Qué quieres registrar?", onVolver = onVolver)
            HorizontalDivider(color = BordeSuave, thickness = 0.5.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text  = "Selecciona el tipo de operación que vas a registrar en el cuaderno de campo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario
                )

                Spacer(Modifier.height(4.dp))

                TipoActividadBoton(
                    titulo    = "Tratamiento fitosanitario",
                    subtitulo = "Sección 3.1 — Aplicación de productos fitosanitarios",
                    icono     = Icons.Outlined.Science,
                    onClick   = onTratamiento
                )

                TipoActividadBoton(
                    titulo    = "Siembra de semilla tratada",
                    subtitulo = "Sección 3.2 — Registro de siembra con semilla tratada",
                    icono     = Icons.Outlined.Grass,
                    onClick   = onSemilla
                )

                TipoActividadBoton(
                    titulo    = "Fertilización",
                    subtitulo = "Sección 6 — Aplicación de fertilizantes",
                    icono     = Icons.Outlined.WaterDrop,
                    onClick   = onFertilizacion
                )
            }
        }
    }
}

@Composable
private fun TipoActividadBoton(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth().heightIn(min = 100.dp),
        colors    = CardDefaults.cardColors(containerColor = BlancoPuro),
        border    = BorderStroke(1.dp, BordeMedio),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = icono,
                contentDescription = null,
                tint               = OlivaPrimario,
                modifier           = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextoPrimario
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoTerciario
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector        = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint               = TextoTerciario,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}
