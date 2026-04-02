package org.dferna14.project.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

val MyWarmColorScheme = lightColorScheme(
    primary = WarmPrimary,
    onPrimary = WarmOnPrimary,
    primaryContainer = WarmPrimaryContainer,
    secondary = WarmSecondary,
    onSecondary = WarmOnSecondary,
    tertiary = WarmTertiary,
    background = WarmBackground,
    surface = WarmSurface
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MyWarmColorScheme,
        content = content
    )
}