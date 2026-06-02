package org.dferna14.project.backend.service

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.dferna14.project.backend.model.ActividadCompletaDto
import org.dferna14.project.backend.model.CuadernoCompletoDto
import java.io.ByteArrayOutputStream

/**
 * Genera el PDF del Cuaderno de Campo Digital conforme al RD 1311/2012.
 * Estructura inspirada en el formato oficial del cuaderno agrícola español.
 *
 * Las fuentes Standard14 de PDFBox no soportan UTF-8 completo. Todo el texto
 * que se dibuje DEBE pasar por [sanitizar] antes de ser enviado al stream;
 * los helpers de este object ya lo hacen internamente.
 */
object CuadernoPdfGenerator {

    private val FUENTE_TITULO_PRINCIPAL  = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
    private val FUENTE_TITULO_SECCION    = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
    private val FUENTE_TITULO_SUBSECCION = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
    private val FUENTE_CUERPO_NORMAL     = PDType1Font(Standard14Fonts.FontName.HELVETICA)
    private val FUENTE_CUERPO_BOLD       = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)

    private const val MARGEN_IZQ = 40f
    private const val MARGEN_DCH = 40f
    private const val MARGEN_SUP = 40f
    private const val MARGEN_INF = 50f
    private val PAGINA = PDRectangle.A4

    /**
     * Punto de entrada principal. Genera el PDF completo y devuelve los bytes.
     */
    fun generar(cuaderno: CuadernoCompletoDto): ByteArray {
        val documento = PDDocument()
        try {
            generarPortada(documento, cuaderno)
            generarSeccionInformacionGeneral(documento, cuaderno)
            generarSeccionParcelas(documento, cuaderno)
            generarSeccionTratamientosFitosanitarios(documento, cuaderno)
            generarSeccionSemillasTratadas(documento, cuaderno)
            generarSeccionFertilizacion(documento, cuaderno)
            generarResumen(documento, cuaderno)

            val output = ByteArrayOutputStream()
            documento.save(output)
            return output.toByteArray()
        } finally {
            documento.close()
        }
    }

    // ===================================================================
    // PORTADA
    // ===================================================================
    private fun generarPortada(doc: PDDocument, cuaderno: CuadernoCompletoDto) {
        val pagina = PDPage(PAGINA)
        doc.addPage(pagina)
        PDPageContentStream(doc, pagina).use { cs ->
            var y = PAGINA.height - 100f

            dibujarTextoCentrado(cs, "CUADERNO DE CAMPO DIGITAL", FUENTE_TITULO_PRINCIPAL, 22f, y)
            y -= 30f
            dibujarTextoCentrado(cs, "RD 1311/2012", FUENTE_CUERPO_NORMAL, 11f, y)
            y -= 16f
            dibujarTextoCentrado(cs, "Uso sostenible de productos fitosanitarios", FUENTE_CUERPO_NORMAL, 11f, y)
            y -= 50f

            y = dibujarSubseccion(cs, "TITULAR DE LA EXPLOTACION", y)
            cuaderno.titular?.let { t ->
                val nombreCompleto = listOfNotNull(t.nombre, t.apellidos)
                    .joinToString(" ").ifBlank { "—" }
                y = dibujarCampo(cs, "Nombre y apellidos / Razon social:", nombreCompleto, y)
                y = dibujarCampo(cs, "NIF:", t.nif, y)
                y = dibujarCampo(cs, "Direccion:", t.direccion ?: "—", y)
                y = dibujarCampo(cs, "Localidad:", t.localidad ?: "—", y)
                y = dibujarCampo(cs, "Provincia:", t.provincia ?: "—", y)
                y = dibujarCampo(cs, "Codigo Postal:", t.codigoPostal ?: "—", y)
                y = dibujarCampo(cs, "Telefono:", t.telefono ?: "—", y)
                y = dibujarCampo(cs, "Email:", t.email ?: "—", y)
            } ?: run {
                y = dibujarTexto(cs, "Sin datos de titular registrados", FUENTE_CUERPO_NORMAL, 10f, MARGEN_IZQ, y)
            }
            y -= 30f

            y = dibujarSubseccion(cs, "DATOS DE LA EXPLOTACION", y)
            cuaderno.explotacion?.let { e ->
                y = dibujarCampo(cs, "Nombre:", e.nombre, y)
                y = dibujarCampo(cs, "NIF empresa:", e.nifEmpresa ?: "—", y)
                y = dibujarCampo(cs, "Registro Nacional:", e.registroNacional ?: "—", y)
                y = dibujarCampo(cs, "Registro Autonomico:", e.registroAutonomico ?: "—", y)
                y = dibujarCampo(cs, "Direccion:", e.direccion ?: "—", y)
                y = dibujarCampo(cs, "Municipio:", e.municipio ?: "—", y)
                y = dibujarCampo(cs, "Provincia:", e.provincia ?: "—", y)
            } ?: run {
                y = dibujarTexto(cs, "Sin datos de explotacion registrados", FUENTE_CUERPO_NORMAL, 10f, MARGEN_IZQ, y)
            }
            y -= 30f

            y = dibujarSubseccion(cs, "PERIODO DEL CUADERNO", y)
            y = dibujarCampo(cs, "Desde:", cuaderno.periodo.fechaInicio, y)
            y = dibujarCampo(cs, "Hasta:", cuaderno.periodo.fechaFin, y)
            dibujarCampo(cs, "Fecha de generacion:", cuaderno.fechaGeneracion, y)

            dibujarPiePagina(cs, cuaderno, doc.numberOfPages)
        }
    }

    // ===================================================================
    // SECCION 1 — INFORMACION GENERAL (aplicadores + equipos)
    // ===================================================================
    private fun generarSeccionInformacionGeneral(doc: PDDocument, cuaderno: CuadernoCompletoDto) {
        val pagina = PDPage(PAGINA)
        doc.addPage(pagina)
        PDPageContentStream(doc, pagina).use { cs ->
            var y = PAGINA.height - MARGEN_SUP
            y = dibujarTituloSeccion(cs, "1. INFORMACION GENERAL", y)

            y = dibujarSubseccion(cs, "1.2 Personas que intervienen en el tratamiento", y)
            y = dibujarTablaAplicadores(cs, cuaderno.actividades, y)
            y -= 20f

            dibujarSubseccion(cs, "1.3 Equipos de aplicacion", y).let { yEquipos ->
                dibujarTablaEquipos(cs, cuaderno.actividades, yEquipos)
            }

            dibujarPiePagina(cs, cuaderno, doc.numberOfPages)
        }
    }

    private fun dibujarTablaAplicadores(
        cs: PDPageContentStream,
        actividades: List<ActividadCompletaDto>,
        yInicial: Float
    ): Float {
        var y = yInicial

        val aplicadores = actividades
            .mapNotNull { it.aplicador }
            .distinctBy { it.id }

        if (aplicadores.isEmpty()) {
            return dibujarTexto(cs, "No hay aplicadores registrados en el periodo",
                FUENTE_CUERPO_NORMAL, 9f, MARGEN_IZQ + 5f, y) - 10f
        }

        val anchos = floatArrayOf(180f, 180f, 140f)
        val cabeceras = arrayOf("Nombre y apellidos", "Email", "Tipo de carne ROPO")
        y = dibujarFilaTabla(cs, cabeceras, anchos, y, cabecera = true)

        aplicadores.forEach { aplicador ->
            val nombreCompleto = listOfNotNull(aplicador.nombre, aplicador.apellidos)
                .joinToString(" ").ifBlank { "—" }
            val tipoCarnet = aplicador.tipoCarnetRopo?.let { etiquetaCarnet(it) } ?: "—"
            y = dibujarFilaTabla(cs, arrayOf(nombreCompleto, aplicador.email, tipoCarnet), anchos, y)
        }
        return y
    }

    private fun dibujarTablaEquipos(
        cs: PDPageContentStream,
        actividades: List<ActividadCompletaDto>,
        yInicial: Float
    ): Float {
        var y = yInicial

        val equipos = actividades
            .mapNotNull { it.equipoUsado }
            .distinctBy { it.id }

        if (equipos.isEmpty()) {
            return dibujarTexto(cs, "No hay equipos registrados en el periodo",
                FUENTE_CUERPO_NORMAL, 9f, MARGEN_IZQ + 5f, y) - 10f
        }

        val anchos = floatArrayOf(220f, 140f, 140f)
        val cabeceras = arrayOf("Descripcion", "N. inscripcion ROMA", "Ultima inspeccion")
        y = dibujarFilaTabla(cs, cabeceras, anchos, y, cabecera = true)

        equipos.forEach { equipo ->
            val descripcion = listOfNotNull(
                equipo.tipo.ifBlank { null },
                equipo.marca?.takeIf { it.isNotBlank() },
                equipo.modelo?.takeIf { it.isNotBlank() }
            ).joinToString(" ").ifBlank { "—" }

            y = dibujarFilaTabla(
                cs,
                arrayOf(
                    descripcion,
                    equipo.numeroRoma ?: "—",
                    equipo.fechaUltimaInspeccion ?: "—"
                ),
                anchos, y
            )
        }
        return y
    }

    // ===================================================================
    // SECCION 2 — IDENTIFICACION DE PARCELAS
    // ===================================================================
    private fun generarSeccionParcelas(doc: PDDocument, cuaderno: CuadernoCompletoDto) {
        val pagina = PDPage(PAGINA)
        doc.addPage(pagina)
        PDPageContentStream(doc, pagina).use { cs ->
            var y = PAGINA.height - MARGEN_SUP
            y = dibujarTituloSeccion(cs, "2. IDENTIFICACION DE LAS PARCELAS", y)
            y = dibujarSubseccion(cs, "2.1 Datos identificativos SIGPAC y agronomicos", y)

            if (cuaderno.parcelas.isEmpty()) {
                dibujarTexto(cs, "No hay parcelas registradas",
                    FUENTE_CUERPO_NORMAL, 10f, MARGEN_IZQ + 5f, y)
            } else {
                val anchos = floatArrayOf(25f, 110f, 50f, 50f, 50f, 60f, 95f, 75f)
                val cabeceras = arrayOf("N.", "Municipio", "Poligono", "Parcela", "Recinto", "Sup. (ha)", "Cultivo", "Eco-reg.")
                y = dibujarFilaTabla(cs, cabeceras, anchos, y, cabecera = true)

                for ((index, parcelaCompleta) in cuaderno.parcelas.withIndex()) {
                    // Si no cabe otra fila, marca truncado y termina.
                    if (y - 14f < MARGEN_INF + 14f) {
                        dibujarTexto(cs,
                            "... ${cuaderno.parcelas.size - index} parcelas mas no caben en el listado",
                            FUENTE_CUERPO_NORMAL, 8f, MARGEN_IZQ, y - 6f)
                        break
                    }

                    val sigpac = parcelaCompleta.referenciaSigpac
                    val agro = parcelaCompleta.datosAgronomicos
                    y = dibujarFilaTabla(
                        cs,
                        arrayOf(
                            (index + 1).toString(),
                            sigpac?.terminoMunicipal ?: parcelaCompleta.parcela.alias ?: "—",
                            sigpac?.numeroPoligono ?: "—",
                            sigpac?.numeroParcela ?: "—",
                            sigpac?.numeroRecinto ?: "—",
                            sigpac?.superficieHa?.let { "%.2f".format(it) } ?: "—",
                            agro?.especieVariedad ?: "—",
                            agro?.ecoregimenPractica ?: "—"
                        ),
                        anchos, y
                    )
                }
            }

            dibujarPiePagina(cs, cuaderno, doc.numberOfPages)
        }
    }

    // ===================================================================
    // SECCION 3.1 — REGISTRO DE ACTUACIONES FITOSANITARIAS
    // ===================================================================
    private fun generarSeccionTratamientosFitosanitarios(doc: PDDocument, cuaderno: CuadernoCompletoDto) {
        val pagina = PDPage(PAGINA)
        doc.addPage(pagina)
        PDPageContentStream(doc, pagina).use { cs ->
            var y = PAGINA.height - MARGEN_SUP
            y = dibujarTituloSeccion(cs, "3. INFORMACION SOBRE TRATAMIENTOS FITOSANITARIOS", y)
            y = dibujarSubseccion(cs, "3.1 Registro de actuaciones fitosanitarias", y)

            val actividadesConProductos = cuaderno.actividades.filter { it.productosAplicados.isNotEmpty() }

            if (actividadesConProductos.isEmpty()) {
                dibujarTexto(cs, "No hay tratamientos fitosanitarios registrados",
                    FUENTE_CUERPO_NORMAL, 10f, MARGEN_IZQ + 5f, y)
            } else {
                var pendientes = actividadesConProductos.size
                for (act in actividadesConProductos) {
                    val altoBloque = 60f + act.productosAplicados.size * 14f
                    if (y - altoBloque < MARGEN_INF) {
                        dibujarTexto(cs, "... $pendientes actividades mas no caben en esta pagina",
                            FUENTE_CUERPO_NORMAL, 8f, MARGEN_IZQ, y - 6f)
                        break
                    }
                    y = dibujarBloqueActividadFitosanitaria(cs, act, y)
                    y -= 10f
                    pendientes--
                }
            }

            dibujarPiePagina(cs, cuaderno, doc.numberOfPages)
        }
    }

    private fun dibujarBloqueActividadFitosanitaria(
        cs: PDPageContentStream,
        act: ActividadCompletaDto,
        yInicial: Float
    ): Float {
        var y = yInicial

        val cabecera = "${act.actividad.fechaInicio}  -  " +
                (act.actividad.parcelaAlias ?: "Parcela ${act.actividad.parcelaId}")
        y = dibujarTexto(cs, cabecera, FUENTE_CUERPO_BOLD, 10f, MARGEN_IZQ, y)
        y -= 2f

        y = dibujarCampo(cs, "Superficie tratada:",
            act.actividad.superficieTratada?.let { "%.2f ha".format(it) } ?: "— ha", y)
        act.actividad.problemaFitosanitario?.takeIf { it.isNotBlank() }?.let {
            y = dibujarCampo(cs, "Problema:", it, y)
        }
        act.actividad.eficacia?.takeIf { it.isNotBlank() }?.let {
            y = dibujarCampo(cs, "Eficacia:", it, y)
        }

        y -= 4f

        val anchos = floatArrayOf(280f, 110f, 110f)
        val cabeceras = arrayOf("Producto", "ID catalogo", "Dosis (kg/ha o l/ha)")
        y = dibujarFilaTabla(cs, cabeceras, anchos, y, cabecera = true)

        act.productosAplicados.forEach { prodAct ->
            y = dibujarFilaTabla(
                cs,
                arrayOf(
                    "Producto #${prodAct.productoId}",
                    prodAct.productoId.toString(),
                    "%.2f".format(prodAct.dosis)
                ),
                anchos, y
            )
        }
        return y
    }

    // ===================================================================
    // SECCION 3.2 — SEMILLA TRATADA
    // ===================================================================
    private fun generarSeccionSemillasTratadas(doc: PDDocument, cuaderno: CuadernoCompletoDto) {
        val pagina = PDPage(PAGINA)
        doc.addPage(pagina)
        PDPageContentStream(doc, pagina).use { cs ->
            var y = PAGINA.height - MARGEN_SUP
            y = dibujarTituloSeccion(cs, "3.2 REGISTRO DE USO DE SEMILLA TRATADA", y)

            val actividadesConSemilla = cuaderno.actividades.filter {
                it.semillaTratada?.aplica == true
            }

            if (actividadesConSemilla.isEmpty()) {
                dibujarTexto(cs, "No hay registros de uso de semilla tratada",
                    FUENTE_CUERPO_NORMAL, 10f, MARGEN_IZQ + 5f, y)
            } else {
                val anchos = floatArrayOf(80f, 100f, 70f, 80f, 90f, 95f)
                val cabeceras = arrayOf("Fecha siembra", "Parcela", "Superficie (ha)", "Cantidad (kg)", "Producto", "Variedad")
                y = dibujarFilaTabla(cs, cabeceras, anchos, y, cabecera = true)

                for ((index, act) in actividadesConSemilla.withIndex()) {
                    if (y - 14f < MARGEN_INF + 14f) {
                        dibujarTexto(cs,
                            "... ${actividadesConSemilla.size - index} registros mas no caben en el listado",
                            FUENTE_CUERPO_NORMAL, 8f, MARGEN_IZQ, y - 6f)
                        break
                    }
                    val s = act.semillaTratada!!
                    y = dibujarFilaTabla(
                        cs,
                        arrayOf(
                            s.fechaSiembra ?: act.actividad.fechaInicio,
                            act.actividad.parcelaAlias ?: "—",
                            s.superficieHa?.let { "%.2f".format(it) } ?: "—",
                            s.cantidadSemillaKg?.let { "%.2f".format(it) } ?: "—",
                            s.productoId?.let { "#$it" } ?: "—",
                            s.variedadSemilla ?: "—"
                        ),
                        anchos, y
                    )
                }
            }

            dibujarPiePagina(cs, cuaderno, doc.numberOfPages)
        }
    }

    // ===================================================================
    // SECCION 6 — FERTILIZACION
    // ===================================================================
    private fun generarSeccionFertilizacion(doc: PDDocument, cuaderno: CuadernoCompletoDto) {
        val pagina = PDPage(PAGINA)
        doc.addPage(pagina)
        PDPageContentStream(doc, pagina).use { cs ->
            var y = PAGINA.height - MARGEN_SUP
            y = dibujarTituloSeccion(cs, "6. REGISTRO DE FERTILIZACION", y)

            val actividadesConFertilizacion = cuaderno.actividades.filter {
                it.fertilizacion?.aplica == true
            }

            if (actividadesConFertilizacion.isEmpty()) {
                dibujarTexto(cs, "No hay registros de fertilizacion",
                    FUENTE_CUERPO_NORMAL, 10f, MARGEN_IZQ + 5f, y)
            } else {
                val anchos = floatArrayOf(70f, 90f, 70f, 70f, 80f, 70f, 70f)
                val cabeceras = arrayOf("Fecha", "Parcela", "Tipo abono", "Albaran", "Riqueza NPK", "Dosis", "Tipo fert.")
                y = dibujarFilaTabla(cs, cabeceras, anchos, y, cabecera = true)

                for ((index, act) in actividadesConFertilizacion.withIndex()) {
                    if (y - 14f < MARGEN_INF + 14f) {
                        dibujarTexto(cs,
                            "... ${actividadesConFertilizacion.size - index} registros mas no caben en el listado",
                            FUENTE_CUERPO_NORMAL, 8f, MARGEN_IZQ, y - 6f)
                        break
                    }
                    val f = act.fertilizacion!!
                    y = dibujarFilaTabla(
                        cs,
                        arrayOf(
                            f.fechaInicio ?: act.actividad.fechaInicio,
                            act.actividad.parcelaAlias ?: "—",
                            f.tipoProducto ?: "—",
                            f.numeroAlbaran ?: "—",
                            f.riquezaNPK ?: "—",
                            f.dosis?.let { "%.2f".format(it) } ?: "—",
                            f.tipoFertilizacion ?: "—"
                        ),
                        anchos, y
                    )
                }
            }

            dibujarPiePagina(cs, cuaderno, doc.numberOfPages)
        }
    }

    // ===================================================================
    // RESUMEN
    // ===================================================================
    private fun generarResumen(doc: PDDocument, cuaderno: CuadernoCompletoDto) {
        val pagina = PDPage(PAGINA)
        doc.addPage(pagina)
        PDPageContentStream(doc, pagina).use { cs ->
            var y = PAGINA.height - MARGEN_SUP
            y = dibujarTituloSeccion(cs, "RESUMEN DEL PERIODO", y)
            y -= 20f

            val r = cuaderno.resumen
            y = dibujarCampo(cs, "Total actividades validadas:", r.totalActividadesValidadas.toString(), y)
            y = dibujarCampo(cs, "Total parcelas registradas:", r.totalParcelas.toString(), y)
            y = dibujarCampo(cs, "Superficie total tratada:", "%.2f ha".format(r.superficieTotalTratada), y)
            dibujarCampo(cs, "Productos diferentes usados:", r.productosUnicosUsados.toString(), y)

            dibujarPiePagina(cs, cuaderno, doc.numberOfPages)
        }
    }

    // ===================================================================
    // HELPERS DE DIBUJO
    // ===================================================================

    private fun dibujarTexto(
        cs: PDPageContentStream,
        texto: String,
        fuente: PDType1Font,
        tamano: Float,
        x: Float,
        y: Float
    ): Float {
        cs.beginText()
        cs.setFont(fuente, tamano)
        cs.newLineAtOffset(x, y)
        cs.showText(sanitizar(texto))
        cs.endText()
        return y - (tamano + 4f)
    }

    private fun dibujarTextoCentrado(
        cs: PDPageContentStream,
        texto: String,
        fuente: PDType1Font,
        tamano: Float,
        y: Float
    ): Float {
        val anchoTexto = fuente.getStringWidth(sanitizar(texto)) / 1000f * tamano
        val x = (PAGINA.width - anchoTexto) / 2f
        return dibujarTexto(cs, texto, fuente, tamano, x, y)
    }

    private fun dibujarTituloSeccion(cs: PDPageContentStream, titulo: String, y: Float): Float {
        val resultado = dibujarTexto(cs, titulo, FUENTE_TITULO_SECCION, 14f, MARGEN_IZQ, y) - 4f
        cs.setLineWidth(1f)
        cs.moveTo(MARGEN_IZQ, resultado + 6f)
        cs.lineTo(PAGINA.width - MARGEN_DCH, resultado + 6f)
        cs.stroke()
        return resultado - 6f
    }

    private fun dibujarSubseccion(cs: PDPageContentStream, titulo: String, y: Float): Float {
        return dibujarTexto(cs, titulo, FUENTE_TITULO_SUBSECCION, 11f, MARGEN_IZQ, y) - 2f
    }

    private fun dibujarCampo(cs: PDPageContentStream, etiqueta: String, valor: String, y: Float): Float {
        cs.beginText()
        cs.setFont(FUENTE_CUERPO_BOLD, 9f)
        cs.newLineAtOffset(MARGEN_IZQ + 10f, y)
        cs.showText(sanitizar(etiqueta))
        cs.endText()

        cs.beginText()
        cs.setFont(FUENTE_CUERPO_NORMAL, 9f)
        cs.newLineAtOffset(MARGEN_IZQ + 170f, y)
        cs.showText(sanitizar(valor))
        cs.endText()
        return y - 13f
    }

    private fun dibujarFilaTabla(
        cs: PDPageContentStream,
        celdas: Array<String>,
        anchos: FloatArray,
        y: Float,
        cabecera: Boolean = false
    ): Float {
        val alturaFila = 14f
        var x = MARGEN_IZQ
        val fuente = if (cabecera) FUENTE_CUERPO_BOLD else FUENTE_CUERPO_NORMAL
        val tamanoFuente = 8.5f

        cs.setLineWidth(0.5f)
        celdas.forEachIndexed { i, _ ->
            cs.addRect(x, y - alturaFila, anchos[i], alturaFila)
            x += anchos[i]
        }
        cs.stroke()

        x = MARGEN_IZQ
        celdas.forEachIndexed { i, contenido ->
            val truncado = truncarTexto(contenido, fuente, tamanoFuente, anchos[i] - 4f)
            cs.beginText()
            cs.setFont(fuente, tamanoFuente)
            cs.newLineAtOffset(x + 2f, y - alturaFila + 4f)
            cs.showText(sanitizar(truncado))
            cs.endText()
            x += anchos[i]
        }
        return y - alturaFila
    }

    private fun dibujarPiePagina(cs: PDPageContentStream, cuaderno: CuadernoCompletoDto, numeroPagina: Int) {
        cs.beginText()
        cs.setFont(FUENTE_CUERPO_NORMAL, 7f)
        cs.newLineAtOffset(MARGEN_IZQ, 25f)
        cs.showText(sanitizar(
            "Cuaderno de Campo Digital - Generado el ${cuaderno.fechaGeneracion} - Pagina $numeroPagina"
        ))
        cs.endText()
    }

    /**
     * PDFBox usa fuentes Standard14 que no soportan acentos ni eñes.
     * Sanitizamos el texto reemplazando caracteres problemáticos.
     */
    private fun sanitizar(texto: String): String {
        return texto
            .replace("á", "a").replace("Á", "A")
            .replace("é", "e").replace("É", "E")
            .replace("í", "i").replace("Í", "I")
            .replace("ó", "o").replace("Ó", "O")
            .replace("ú", "u").replace("Ú", "U")
            .replace("ñ", "n").replace("Ñ", "N")
            .replace("ü", "u").replace("Ü", "U")
            .replace("—", "-")
            .replace("·", ".")
            .replace("\n", " ")
            .replace("\r", "")
    }

    /**
     * Trunca texto si excede el ancho disponible añadiendo "..." al final.
     */
    private fun truncarTexto(texto: String, fuente: PDType1Font, tamano: Float, anchoMax: Float): String {
        val anchoTexto = fuente.getStringWidth(sanitizar(texto)) / 1000f * tamano
        if (anchoTexto <= anchoMax) return texto

        var truncado = texto
        while (truncado.length > 3) {
            truncado = truncado.dropLast(1)
            val ancho = fuente.getStringWidth(sanitizar("$truncado...")) / 1000f * tamano
            if (ancho <= anchoMax) return "$truncado..."
        }
        return truncado
    }

    private fun etiquetaCarnet(codigo: String): String = when (codigo) {
        "BASICO"      -> "Basico"
        "CUALIFICADO" -> "Cualificado"
        "FUMIGADOR"   -> "Fumigador"
        "PILOTO"      -> "Piloto"
        else          -> codigo
    }
}
