package org.dferna14.project.util

class OcrScannerJvm : OcrScanner {
    override val isAvailable: Boolean = false
}

actual fun crearOcrScanner(): OcrScanner = OcrScannerJvm()
