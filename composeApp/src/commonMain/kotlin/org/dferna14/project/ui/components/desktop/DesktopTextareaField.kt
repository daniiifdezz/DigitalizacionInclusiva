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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.dferna14.project.ui.theme.BordeNormal
import org.dferna14.project.ui.theme.CremaPrincipal
import org.dferna14.project.ui.theme.OlivaPrimario
import org.dferna14.project.ui.theme.TextoPrimario
import org.dferna14.project.ui.theme.TextoTerciario
import org.dferna14.project.ui.theme.extraTypography

// ════════════════════════════════════════════════════════════════════════
//  DesktopTextareaField — OutlinedTextField(minLines = rows)
//  Mismo estilo que DesktopFormField pero multilínea.
//  minLines controla la altura mínima visible (por defecto 3 líneas).
// ════════════════════════════════════════════════════════════════════════

@Composable
fun DesktopTextareaField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    minLines: Int = 3,
    modifier: Modifier = Modifier,
) {
    val lineHeight: Dp = 22.dp
    val minHeight: Dp  = lineHeight * minLines + 18.dp   // equivale a rows * 22 + 18 del JSX

    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color      = TextoPrimario,
        lineHeight = lineHeight.value.let { androidx.compose.ui.unit.TextUnit(it * 1.4f, androidx.compose.ui.unit.TextUnitType.Sp) },
    )

    Column(modifier = modifier) {
        Text(
            text  = label.uppercase(),
            style = MaterialTheme.extraTypography.eyebrow,
            color = TextoTerciario,
        )
        Spacer(Modifier.height(5.dp))
        BasicTextField(
            value         = value,
            onValueChange = onValueChange,
            minLines      = minLines,
            textStyle     = textStyle,
            cursorBrush   = SolidColor(OlivaPrimario),
            modifier      = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight)
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
                .padding(horizontal = 12.dp, vertical = 10.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier         = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopStart,
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
