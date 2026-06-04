package org.dferna14.project.util

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Implementación Desktop: abre el diálogo nativo "Guardar como" (Swing JFileChooser),
 * fuerza la extensión .pdf y escribe los bytes en el fichero elegido.
 */
actual fun guardarPdfEnDisco(bytes: ByteArray, nombreSugerido: String): String? {
    val chooser = JFileChooser().apply {
        dialogTitle = "Guardar Cuaderno de Campo PDF"
        selectedFile = File(nombreSugerido)
        fileFilter = FileNameExtensionFilter("Documentos PDF (*.pdf)", "pdf")
    }

    val resultado = chooser.showSaveDialog(null)
    if (resultado != JFileChooser.APPROVE_OPTION) return null

    var fichero = chooser.selectedFile
    if (!fichero.name.endsWith(".pdf", ignoreCase = true)) {
        fichero = File(fichero.parentFile, "${fichero.name}.pdf")
    }

    fichero.writeBytes(bytes)
    return fichero.absolutePath
}
