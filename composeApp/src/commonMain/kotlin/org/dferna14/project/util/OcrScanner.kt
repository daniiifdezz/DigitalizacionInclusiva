package org.dferna14.project.util

interface OcrScanner {
    val isAvailable: Boolean
}

expect fun crearOcrScanner(): OcrScanner
