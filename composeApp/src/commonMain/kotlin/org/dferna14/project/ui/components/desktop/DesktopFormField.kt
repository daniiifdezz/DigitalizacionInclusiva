package org.dferna14.project.ui.components.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.dferna14.project.ui.theme.BordeNormal
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.theme.OlivaPrimario
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.theme.extraTypography

// ════════════════════════════════════════════════════════════════════════
//  DesktopFormField — OutlinedTextField con borde inferior remarcado
//  Estilo "underline": esquinas redondeadas solo arriba (4dp), borde
//  inferior 2dp OlivaPrimario, resto de bordes 1dp BordeNormal.
//
//  readOnly = true → campo de solo lectura (cursor invisible)
// ════════════════════════════════════════════════════════════════════════

@Composable
fun DesktopFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit = {},
    placeholder: String = "",
    readOnly: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextoPrimario)

    Column(modifier = modifier) {
        Text(
            text  = label.uppercase(),
            style = MaterialTheme.extraTypography.eyebrow,
            color = TextoTerciario,
        )
        Spacer(Modifier.height(5.dp))
        BasicTextField(
            value            = value,
            onValueChange    = onValueChange,
            readOnly         = readOnly,
            singleLine       = true,
            textStyle        = textStyle,
            cursorBrush      = SolidColor(OlivaPrimario),
            modifier         = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 42.dp)
                .background(CremaPrincipal.copy(alpha = 0.8f))
                .drawBehind {
                    val thin   = 1.dp.toPx()
                    val thick  = 2.dp.toPx()
                    val w      = size.width
                    val h      = size.height
                    drawLine(BordeNormal,   Offset(0f, 0f),  Offset(w, 0f),  thin)   // top
                    drawLine(BordeNormal,   Offset(0f, 0f),  Offset(0f, h),  thin)   // left
                    drawLine(BordeNormal,   Offset(w, 0f),   Offset(w, h),   thin)   // right
                    drawLine(OlivaPrimario, Offset(0f, h),   Offset(w, h),   thick)  // bottom
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier         = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty() && placeholder.isNotBlank()) {
                        Text(placeholder, style = textStyle.copy(color = TextoTerciario))
                    }
                    innerTextField()
                }
            },
        )
    }
}
