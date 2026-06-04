package org.dferna14.project.util

/**
 * Guarda los bytes de un PDF en disco usando el mecanismo nativo de cada plataforma.
 *
 * @return la ruta absoluta donde se guardó, o null si el usuario canceló.
 *
 * Solo tiene implementación real en Desktop (JVM), que abre el file picker del
 * sistema. En Android lanza UnsupportedOperationException porque el botón de
 * generación del cuaderno solo existe en el escritorio del técnico.
 */
expect fun guardarPdfEnDisco(bytes: ByteArray, nombreSugerido: String): String?
