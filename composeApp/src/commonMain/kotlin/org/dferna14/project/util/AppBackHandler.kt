package org.dferna14.project.util

import androidx.compose.runtime.Composable


@Composable
expect fun AppBackHandler(enabled: Boolean = true, onBack: () -> Unit)
