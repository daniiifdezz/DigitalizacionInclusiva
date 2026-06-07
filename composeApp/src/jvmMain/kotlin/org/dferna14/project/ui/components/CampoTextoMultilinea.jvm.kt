package org.dferna14.project.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun CampoTextoMultilinea(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
    minLines: Int,
    maxLines: Int
) {
    CampoTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        minLines = minLines
    )
}
