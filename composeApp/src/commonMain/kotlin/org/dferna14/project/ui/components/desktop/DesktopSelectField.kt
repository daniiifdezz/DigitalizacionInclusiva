package org.dferna14.project.ui.components.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import org.dferna14.project.ui.theme.BordeNormal
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.theme.OlivaPrimario
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.theme.extraTypography

// ════════════════════════════════════════════════════════════════════════
//  DesktopSelectField — ExposedDropdownMenuBox (M3)
//  Mismo estilo visual que DesktopFormField pero con chevron y clickable.
//  El manejo del dropdown real (ExposedDropdownMenuBox) lo implementa
//  cada pantalla; este componente es solo la parte visual del trigger.
// ════════════════════════════════════════════════════════════════════════

@Composable
fun DesktopSelectField(
    label: String,
    value: String = "",
    placeholder: String = "Seleccionar…",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text  = label.uppercase(),
            style = MaterialTheme.extraTypography.eyebrow,
            color = TextoTerciario,
        )
        Spacer(Modifier.height(5.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 42.dp)
                .background(CremaPrincipal.copy(alpha = 0.8f))
                .drawBehind {
                    val thin  = 1.dp.toPx()
                    val thick = 2.dp.toPx()
                    val w     = size.width
                    val h     = size.height
                    drawLine(BordeNormal,   Offset(0f, 0f), Offset(w, 0f), thin)
                    drawLine(BordeNormal,   Offset(0f, 0f), Offset(0f, h), thin)
                    drawLine(BordeNormal,   Offset(w, 0f),  Offset(w, h),  thin)
                    drawLine(OlivaPrimario, Offset(0f, h),  Offset(w, h),  thick)
                }
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment    = Alignment.CenterVertically,
        ) {
            val displayText = value.ifBlank { placeholder }
            val textColor   = if (value.isBlank()) TextoTerciario else TextoPrimario

            Text(
                text     = displayText,
                style    = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector      = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint             = TextoTerciario,
                modifier         = Modifier.size(16.dp),
            )
        }
    }
}
