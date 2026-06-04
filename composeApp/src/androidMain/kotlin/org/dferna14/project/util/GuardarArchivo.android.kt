package org.dferna14.project.util

/**
 * En móvil no se usa: el botón de generación del cuaderno solo aparece en Desktop.
 * KMP exige declarar el actual para todos los targets de commonMain.
 */
actual fun guardarPdfEnDisco(bytes: ByteArray, nombreSugerido: String): String? {
    throw UnsupportedOperationException("guardarPdfEnDisco no implementado en Android")
}
