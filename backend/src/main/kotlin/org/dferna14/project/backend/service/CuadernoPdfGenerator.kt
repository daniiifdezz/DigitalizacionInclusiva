package org.dferna14.project.backend.service

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.dferna14.project.backend.model.ActividadCompletaDto
import org.dferna14.project.backend.model.ActividadProductoResponse
import org.dferna14.project.backend.model.CuadernoCompletoDto
import org.dferna14.project.backend.model.EquipoResponse
import org.dferna14.project.backend.model.ParcelaCompletaDto
import org.dferna14.project.backend.model.UsuarioResponse
import java.io.ByteArrayOutputStream

/**
 * Genera el PDF del Cuaderno de Campo Digital conforme al formato oficial
 * del Ministerio de Agricultura (RD 1311/2012).
 *
 * Diseño:
 *   - Apaisado A4 (el oficial siempre va horizontal).
 *   - Cada página lleva cabecera con titular + año y pie con
 *     "Hoja n° X de la sección n° Y".
 *   - Subsecciones marcadas con barra gris oscuro y texto blanco en
 *     mayúsculas, tal y como aparece en las hojas oficiales escaneadas.
 *   - Tablas con cabecera gris clara, celdas con borde negro, texto
 *     centrado en cabecera y a la izquierda en datos. Las celdas en
 *     blanco se quedan en blanco — el cuaderno oficial NO rellena los
 *     huecos con "-" ni "Sin especificar".
 *
 * Las fuentes Standard14 de PDFBox no soportan UTF-8 completo, por eso
 * todo el texto pasa por [sanitizar] antes de escribirse al stream.
 *
 * Datos que el cuaderno oficial pide y que el modelo de dominio actual
 * NO ofrece (se renderizan en blanco y quedan como trabajo futuro):
 *   - "Nº de Asesor" en la tabla 1.2 de personas que intervienen.
 *   - "Superficie Cultivada (ha)" diferenciada de la superficie SIGPAC
 *     en la tabla 2.1 de parcelas.
 *   - "Cultivo principal/secundario" como literal — la tabla expone
 *     "Especie/Variedad" como dato libre porque DatosAgronomicos no
 *     desagrega ese matiz.
 *   - "Fecha de adquisición" del equipo — la tabla la muestra en blanco.
 */
object CuadernoPdfGenerator {

    // ── Tipografía ─────────────────────────────────────────────────────────────
    private val FUENTE_TITULO_PRINCIPAL  = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
    private val FUENTE_TITULO_SECCION    = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
    private val FUENTE_TITULO_SUBSECCION = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
    private val FUENTE_CUERPO_NORMAL     = PDType1Font(Standard14Fonts.FontName.HELVETICA)
    private val FUENTE_CUERPO_BOLD       = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)

    // ── Geometría ──────────────────────────────────────────────────────────────
    // Apaisado A4: el cuaderno oficial siempre se imprime en horizontal.
    private val PAGINA = PDRectangle(PDRectangle.A4.height, PDRectangle.A4.width)
    private const val MARGEN_IZQ = 30f
    private const val MARGEN_DCH = 30f
    private const val MARGEN_SUP = 40f
    private const val MARGEN_INF = 40f
    private val ANCHO_UTIL get() = PAGINA.width - MARGEN_IZQ - MARGEN_DCH

    // ── Colores (escala de grises) ─────────────────────────────────────────────
    private const val GRIS_OSCURO   = 0.35f   // barra de subsección
    private const val GRIS_CABECERA = 0.88f   // cabecera de tabla

    /**
     * Mapas de "número de orden" — el PDF oficial referencia aplicadores,
     * equipos y parcelas por número de orden en lugar de por nombre. Se
     * construye al inicio para que las secciones 1.2/1.3/2.1 los asignen
     * y las secciones 3.1/3.2/6 los usen.
     */
    private data class Ordenes(
        val aplicadores: List<UsuarioResponse>,
        val equipos: List<EquipoResponse>,
        val parcelas: List<ParcelaCompletaDto>,
        val ordenAplicador: Map<Int, Int>,
        val ordenEquipo: Map<Int, Int>,
        val ordenParcela: Map<Int, Int>,
        val parcelaPorId: Map<Int, ParcelaCompletaDto>
    )

    // ===================================================================
    // PUNTO DE ENTRADA
    // ===================================================================
    fun generar(cuaderno: CuadernoCompletoDto): ByteArray {
        val ordenes = construirOrdenes(cuaderno)
        val documento = PDDocument()
        try {
            generarSeccion1(documento, cuaderno, ordenes)
            generarSeccion2(documento, cuaderno, ordenes)
            generarSeccion3(documento, cuaderno, ordenes)
            generarSeccion6(documento, cuaderno, ordenes)

            val output = ByteArrayOutputStream()
            documento.save(output)
            return output.toByteArray()
        } finally {
            documento.close()
        }
    }

    private fun construirOrdenes(cuaderno: CuadernoCompletoDto): Ordenes {
        val aplicadores = cuaderno.actividades
            .mapNotNull { it.aplicador }
            .distinctBy { it.id }
        val equipos = cuaderno.actividades
            .mapNotNull { it.equipoUsado }
            .distinctBy { it.id }
        val parcelas = cuaderno.parcelas
        return Ordenes(
            aplicadores      = aplicadores,
            equipos          = equipos,
            parcelas         = parcelas,
            ordenAplicador   = aplicadores.mapIndexed { i, u -> u.id to (i + 1) }.toMap(),
            ordenEquipo      = equipos.mapIndexed { i, e -> e.id to (i + 1) }.toMap(),
            ordenParcela     = parcelas.mapIndexed { i, p -> p.parcela.id to (i + 1) }.toMap(),
            parcelaPorId     = parcelas.associateBy { it.parcela.id }
        )
    }

    // ===================================================================
    // SECCIÓN 1 — INFORMACIÓN GENERAL
    // ===================================================================
    private fun generarSeccion1(doc: PDDocument, cuaderno: CuadernoCompletoDto, ordenes: Ordenes) {
        // Página 1.A: explotación + titular
        nuevaPagina(doc, cuaderno, numeroSeccion = 1, numeroHoja = 1) { cs, y0 ->
            var y = dibujarTituloSeccionOficial(cs, "1. INFORMACION GENERAL", y0)
            y -= 6f
            y = dibujarBarraSubseccion(cs, "1.1 Datos generales de la explotacion", y)
            y = dibujarFichaExplotacion(cs, cuaderno, y)
            y -= 14f
            y = dibujarBarraSubseccion(cs, "Titular o representante de la explotacion", y)
            dibujarFichaTitular(cs, cuaderno, y)
        }
        // Página 1.B: personas (1.2) + equipos (1.3)
        nuevaPagina(doc, cuaderno, numeroSeccion = 1, numeroHoja = 2) { cs, y0 ->
            var y = dibujarBarraSubseccion(
                cs,
                "1.2 Personas o empresas que intervienen en el tratamiento con productos fitosanitarios",
                y0
            )
            y = dibujarTablaPersonas(cs, ordenes.aplicadores, y)
            y -= 18f
            y = dibujarBarraSubseccion(
                cs,
                "1.3 Equipos de aplicacion de productos fitosanitarios propios de la explotacion",
                y
            )
            dibujarTablaEquipos(cs, ordenes.equipos, y)
        }
    }

    private fun dibujarFichaExplotacion(
        cs: PDPageContentStream,
        cuaderno: CuadernoCompletoDto,
        yInicial: Float
    ): Float {
        val e = cuaderno.explotacion
        // Fila 1: razón social (ancho completo)
        var y = dibujarFilaForm(cs, listOf(
            "Nombre y apellidos o razon social:" to (e?.nombre ?: "")
        ), floatArrayOf(ANCHO_UTIL), yInicial)
        // Fila 2: registro nacional + NIF
        y = dibujarFilaForm(cs, listOf(
            "Nº Registro Explotaciones Nacional:" to (e?.registroNacional ?: ""),
            "NIF:" to (e?.nifEmpresa ?: "")
        ), floatArrayOf(ANCHO_UTIL * 0.65f, ANCHO_UTIL * 0.35f), y)
        // Fila 3: registro autonómico (solo)
        y = dibujarFilaForm(cs, listOf(
            "Nº Registro Explotaciones Autonomico:" to (e?.registroAutonomico ?: "")
        ), floatArrayOf(ANCHO_UTIL), y)
        // Fila 4: dirección
        y = dibujarFilaForm(cs, listOf(
            "Direccion:" to (e?.direccion ?: "")
        ), floatArrayOf(ANCHO_UTIL), y)
        // Fila 5: localidad + cp + provincia
        y = dibujarFilaForm(cs, listOf(
            "Localidad:" to (e?.municipio ?: ""),
            "C.Postal:" to (e?.codigoPostal ?: ""),
            "Provincia:" to (e?.provincia ?: "")
        ), floatArrayOf(ANCHO_UTIL * 0.45f, ANCHO_UTIL * 0.20f, ANCHO_UTIL * 0.35f), y)
        // Fila 6: teléfonos + email
        return dibujarFilaForm(cs, listOf(
            "Telefono fijo:" to (e?.telefonoFijo ?: ""),
            "Telefono movil:" to (e?.telefonoMovil ?: ""),
            "e-mail:" to (e?.email ?: "")
        ), floatArrayOf(ANCHO_UTIL * 0.28f, ANCHO_UTIL * 0.28f, ANCHO_UTIL * 0.44f), y)
    }

    private fun dibujarFichaTitular(
        cs: PDPageContentStream,
        cuaderno: CuadernoCompletoDto,
        yInicial: Float
    ): Float {
        val t = cuaderno.titular
        val nombreCompleto = t?.let { listOfNotNull(it.nombre, it.apellidos).joinToString(" ") } ?: ""
        var y = dibujarFilaForm(cs, listOf(
            "Nombre y apellidos:" to nombreCompleto,
            "NIF:" to (t?.nif ?: "")
        ), floatArrayOf(ANCHO_UTIL * 0.70f, ANCHO_UTIL * 0.30f), yInicial)
        y = dibujarFilaForm(cs, listOf(
            "Direccion:" to (t?.direccion ?: "")
        ), floatArrayOf(ANCHO_UTIL), y)
        y = dibujarFilaForm(cs, listOf(
            "Localidad:" to (t?.localidad ?: ""),
            "C.Postal:" to (t?.codigoPostal ?: ""),
            "Provincia:" to (t?.provincia ?: "")
        ), floatArrayOf(ANCHO_UTIL * 0.45f, ANCHO_UTIL * 0.20f, ANCHO_UTIL * 0.35f), y)
        return dibujarFilaForm(cs, listOf(
            "Telefono:" to (t?.telefono ?: ""),
            "e-mail:" to (t?.email ?: "")
        ), floatArrayOf(ANCHO_UTIL * 0.35f, ANCHO_UTIL * 0.65f), y)
    }

    /**
     * Tabla 1.2 con cabecera de dos filas porque "Tipo de carné" agrupa
     * cuatro sub-columnas (Básico / Cualificado / Fumigador / Piloto).
     * Marca una "X" debajo del tipo que tiene cada aplicador.
     */
    private fun dibujarTablaPersonas(
        cs: PDPageContentStream,
        aplicadores: List<UsuarioResponse>,
        yInicial: Float
    ): Float {
        // Layout: Nº | Nombre / Empresa | NIF | Nº ROPO | [Bas|Cua|Fum|Pil] | Asesor
        val anchoNum   = 50f
        val anchoNombre= 220f
        val anchoNif   = 70f
        val anchoRopo  = 80f
        val anchoCarne = 45f
        val anchoCarneTotal = anchoCarne * 4
        val anchoAsesor= 60f
        val anchoTotal = anchoNum + anchoNombre + anchoNif + anchoRopo + anchoCarneTotal + anchoAsesor

        val altoFila = 14f
        val altoCabezaTotal = 30f

        var y = yInicial
        // Cabecera fila 1 (los 4 fijos llegan hasta abajo)
        val xNum    = MARGEN_IZQ
        val xNombre = xNum + anchoNum
        val xNif    = xNombre + anchoNombre
        val xRopo   = xNif + anchoNif
        val xCarne  = xRopo + anchoRopo
        val xAsesor = xCarne + anchoCarneTotal

        // Fondo gris claro de toda la cabecera
        cs.setNonStrokingColor(GRIS_CABECERA, GRIS_CABECERA, GRIS_CABECERA)
        cs.addRect(MARGEN_IZQ, y - altoCabezaTotal, anchoTotal, altoCabezaTotal)
        cs.fill()
        cs.setNonStrokingColor(0f, 0f, 0f)

        // Bordes: 5 cajas altas (Nº, Nombre, NIF, ROPO, Asesor) + 1 caja superior "Tipo de carné" + 4 cajas inferiores
        cs.setLineWidth(0.4f)
        cs.setStrokingColor(0f, 0f, 0f)
        cs.addRect(xNum,    y - altoCabezaTotal, anchoNum,    altoCabezaTotal)
        cs.addRect(xNombre, y - altoCabezaTotal, anchoNombre, altoCabezaTotal)
        cs.addRect(xNif,    y - altoCabezaTotal, anchoNif,    altoCabezaTotal)
        cs.addRect(xRopo,   y - altoCabezaTotal, anchoRopo,   altoCabezaTotal)
        cs.addRect(xCarne,  y - altoFila,        anchoCarneTotal, altoFila)
        var xc = xCarne
        for (i in 0 until 4) {
            cs.addRect(xc, y - altoCabezaTotal, anchoCarne, altoFila)
            xc += anchoCarne
        }
        cs.addRect(xAsesor, y - altoCabezaTotal, anchoAsesor, altoCabezaTotal)
        cs.stroke()

        // Textos
        textoEnCelda(cs, "Nº de orden",       xNum,    y - altoCabezaTotal, anchoNum,    altoCabezaTotal, FUENTE_CUERPO_BOLD, 6.5f, centrar = true)
        textoEnCelda(cs, "Nombre y apellidos / Empresa de servicios", xNombre, y - altoCabezaTotal, anchoNombre, altoCabezaTotal, FUENTE_CUERPO_BOLD, 6.5f, centrar = true)
        textoEnCelda(cs, "NIF",               xNif,    y - altoCabezaTotal, anchoNif,    altoCabezaTotal, FUENTE_CUERPO_BOLD, 6.5f, centrar = true)
        textoEnCelda(cs, "Nº Inscripcion ROPO", xRopo, y - altoCabezaTotal, anchoRopo,   altoCabezaTotal, FUENTE_CUERPO_BOLD, 6.5f, centrar = true)
        textoEnCelda(cs, "Tipo de carne",     xCarne,  y - altoFila,        anchoCarneTotal, altoFila, FUENTE_CUERPO_BOLD, 6.5f, centrar = true)
        xc = xCarne
        listOf("Basico", "Cualif.", "Fumig.", "Piloto").forEach { etiqueta ->
            textoEnCelda(cs, etiqueta, xc, y - altoCabezaTotal, anchoCarne, altoFila, FUENTE_CUERPO_BOLD, 6.5f, centrar = true)
            xc += anchoCarne
        }
        textoEnCelda(cs, "Asesor", xAsesor, y - altoCabezaTotal, anchoAsesor, altoCabezaTotal, FUENTE_CUERPO_BOLD, 6.5f, centrar = true)

        y -= altoCabezaTotal

        if (aplicadores.isEmpty()) {
            y = dibujarFilaAviso(cs, "Sin aplicadores registrados en el periodo", anchoTotal, y)
            return y
        }

        aplicadores.forEachIndexed { idx, u ->
            if (y - altoFila < MARGEN_INF + 14f) {
                dibujarTextoLibre(cs, "... ${aplicadores.size - idx} aplicadores mas no caben",
                    FUENTE_CUERPO_NORMAL, 7f, MARGEN_IZQ, y - 8f)
                return y
            }
            // Fila bordes
            cs.setLineWidth(0.3f)
            cs.addRect(xNum,    y - altoFila, anchoNum,    altoFila)
            cs.addRect(xNombre, y - altoFila, anchoNombre, altoFila)
            cs.addRect(xNif,    y - altoFila, anchoNif,    altoFila)
            cs.addRect(xRopo,   y - altoFila, anchoRopo,   altoFila)
            xc = xCarne
            for (i in 0 until 4) {
                cs.addRect(xc, y - altoFila, anchoCarne, altoFila)
                xc += anchoCarne
            }
            cs.addRect(xAsesor, y - altoFila, anchoAsesor, altoFila)
            cs.stroke()

            val nombreCompleto = listOfNotNull(u.nombre, u.apellidos).joinToString(" ")
            textoEnCelda(cs, (idx + 1).toString(), xNum,    y - altoFila, anchoNum,    altoFila, FUENTE_CUERPO_NORMAL, 7.5f, centrar = true)
            textoEnCelda(cs, nombreCompleto,       xNombre, y - altoFila, anchoNombre, altoFila, FUENTE_CUERPO_NORMAL, 7.5f, centrar = false)
            // NIF: el modelo de dominio no tiene NIF por usuario, queda en blanco
            textoEnCelda(cs, u.email,              xRopo,   y - altoFila, anchoRopo,   altoFila, FUENTE_CUERPO_NORMAL, 6.5f, centrar = false)
            // Marca X en la columna del carné que tenga
            val carnetIdx = when (u.tipoCarnetRopo) {
                "BASICO"      -> 0
                "CUALIFICADO" -> 1
                "FUMIGADOR"   -> 2
                "PILOTO"      -> 3
                else          -> -1
            }
            if (carnetIdx >= 0) {
                textoEnCelda(cs, "X", xCarne + anchoCarne * carnetIdx, y - altoFila, anchoCarne, altoFila, FUENTE_CUERPO_BOLD, 8f, centrar = true)
            }
            y -= altoFila
        }
        return y
    }

    /**
     * Tabla 1.3 — equipos de aplicación. Cabecera simple (sin colspan).
     */
    private fun dibujarTablaEquipos(
        cs: PDPageContentStream,
        equipos: List<EquipoResponse>,
        yInicial: Float
    ): Float {
        val anchos = floatArrayOf(50f, 270f, 110f, 110f, 110f)
        val cabeceras = arrayOf(
            "Nº de orden",
            "Descripcion del equipo",
            "Nº Inscripcion ROMA",
            "Fecha de adquisicion",
            "Fecha ultima inspeccion"
        )
        var y = dibujarFilaTablaOficial(cs, cabeceras, anchos, yInicial, cabecera = true)
        if (equipos.isEmpty()) {
            return dibujarFilaAviso(cs, "Sin equipos registrados en el periodo", anchos.sum(), y)
        }
        equipos.forEachIndexed { idx, eq ->
            if (y - 14f < MARGEN_INF + 14f) {
                dibujarTextoLibre(cs, "... ${equipos.size - idx} equipos mas no caben",
                    FUENTE_CUERPO_NORMAL, 7f, MARGEN_IZQ, y - 8f)
                return y
            }
            val descripcion = listOfNotNull(
                eq.tipo.ifBlank { null },
                eq.marca?.takeIf { it.isNotBlank() },
                eq.modelo?.takeIf { it.isNotBlank() }
            ).joinToString(" ")
            y = dibujarFilaTablaOficial(
                cs,
                arrayOf(
                    (idx + 1).toString(),
                    descripcion,
                    eq.numeroRoma ?: "",
                    // Adquisición no existe en el modelo → blanco
                    "",
                    eq.fechaUltimaInspeccion ?: ""
                ),
                anchos, y
            )
        }
        return y
    }

    // ===================================================================
    // SECCIÓN 2 — IDENTIFICACIÓN DE PARCELAS
    // ===================================================================
    private fun generarSeccion2(doc: PDDocument, cuaderno: CuadernoCompletoDto, ordenes: Ordenes) {
        nuevaPagina(doc, cuaderno, numeroSeccion = 2, numeroHoja = 1) { cs, y0 ->
            var y = dibujarTituloSeccionOficial(cs, "2. IDENTIFICACION DE LAS PARCELAS", y0)
            y -= 6f
            y = dibujarBarraSubseccion(cs, "2.1 Datos identificativos y agronomicos de las parcelas", y)
            dibujarTablaParcelas(cs, ordenes.parcelas, y)
        }
    }

    /**
     * Tabla 2.1 con cabecera de dos niveles:
     *   Nº ORDEN | REFERENCIAS SIGPAC (8 subcols) | DATOS AGRONOMICOS (9 subcols)
     */
    private fun dibujarTablaParcelas(
        cs: PDPageContentStream,
        parcelas: List<ParcelaCompletaDto>,
        yInicial: Float
    ): Float {
        // Anchos calibrados para que cada cabecera quepa entera con la fuente
        // de subcolumna a 6f bold. Suma total = 35 + 335 + 405 = 775pt
        // (margen útil apaisado = 782pt).
        val anchoNum = 35f
        val anchosSigpac = floatArrayOf(50f, 65f, 45f, 25f, 35f, 35f, 35f, 45f) // 8 cols → 335
        val anchosAgro = floatArrayOf(60f, 70f, 40f, 30f, 40f, 40f, 40f, 50f, 35f) // 9 cols → 405
        val anchoSigpacTotal = anchosSigpac.sum()
        val anchoAgroTotal = anchosAgro.sum()
        val anchoTotal = anchoNum + anchoSigpacTotal + anchoAgroTotal

        val altoFila = 14f
        val altoEtiquetas = 24f // un pelín más alta porque algunos textos rompen
        val altoCabezaTotal = altoFila + altoEtiquetas

        var y = yInicial
        val xNum = MARGEN_IZQ
        val xSig = xNum + anchoNum
        val xAgr = xSig + anchoSigpacTotal

        // Fondo gris claro toda la cabecera
        cs.setNonStrokingColor(GRIS_CABECERA, GRIS_CABECERA, GRIS_CABECERA)
        cs.addRect(MARGEN_IZQ, y - altoCabezaTotal, anchoTotal, altoCabezaTotal)
        cs.fill()
        cs.setNonStrokingColor(0f, 0f, 0f)

        // Bordes
        cs.setLineWidth(0.4f)
        // Nº ORDEN cell — span completo
        cs.addRect(xNum, y - altoCabezaTotal, anchoNum, altoCabezaTotal)
        // Grupo SIGPAC: cabecera ancha arriba + 8 sub-cells abajo
        cs.addRect(xSig, y - altoFila, anchoSigpacTotal, altoFila)
        var xs = xSig
        for (a in anchosSigpac) {
            cs.addRect(xs, y - altoCabezaTotal, a, altoEtiquetas)
            xs += a
        }
        // Grupo AGRO: cabecera ancha arriba + 9 sub-cells abajo
        cs.addRect(xAgr, y - altoFila, anchoAgroTotal, altoFila)
        xs = xAgr
        for (a in anchosAgro) {
            cs.addRect(xs, y - altoCabezaTotal, a, altoEtiquetas)
            xs += a
        }
        cs.stroke()

        // Textos cabecera
        textoEnCelda(cs, "Nº DE\nORDEN", xNum, y - altoCabezaTotal, anchoNum, altoCabezaTotal, FUENTE_CUERPO_BOLD, 6.5f, centrar = true)
        textoEnCelda(cs, "REFERENCIAS SIGPAC", xSig, y - altoFila, anchoSigpacTotal, altoFila, FUENTE_CUERPO_BOLD, 8f, centrar = true)
        textoEnCelda(cs, "DATOS AGRONOMICOS", xAgr, y - altoFila, anchoAgroTotal, altoFila, FUENTE_CUERPO_BOLD, 8f, centrar = true)

        val etiquetasSigpac = arrayOf("Provincia", "Termino Mun.", "Cod.Agreg.", "Zona", "Poligono", "Parcela", "Recinto", "Uso SIGPAC")
        xs = xSig
        etiquetasSigpac.forEachIndexed { i, etiqueta ->
            textoEnCelda(cs, etiqueta, xs, y - altoCabezaTotal, anchosSigpac[i], altoEtiquetas, FUENTE_CUERPO_BOLD, 6f, centrar = true)
            xs += anchosSigpac[i]
        }
        val etiquetasAgro = arrayOf("Sup. SIGPAC (ha)", "Especie/Variedad", "Eco-regimen", "S/R", "F. inicio", "F. fin", "Aire/Prot.", "Sist. Ases. GIP", "Zona Nitr.")
        xs = xAgr
        etiquetasAgro.forEachIndexed { i, etiqueta ->
            textoEnCelda(cs, etiqueta, xs, y - altoCabezaTotal, anchosAgro[i], altoEtiquetas, FUENTE_CUERPO_BOLD, 6f, centrar = true)
            xs += anchosAgro[i]
        }
        y -= altoCabezaTotal

        // Filas de datos
        if (parcelas.isEmpty()) {
            return dibujarFilaAviso(cs, "Sin parcelas registradas", anchoTotal, y)
        }
        parcelas.forEachIndexed { idx, pc ->
            if (y - altoFila < MARGEN_INF + 14f) {
                dibujarTextoLibre(cs, "... ${parcelas.size - idx} parcelas mas no caben",
                    FUENTE_CUERPO_NORMAL, 7f, MARGEN_IZQ, y - 8f)
                return y
            }
            // Bordes de la fila
            cs.setLineWidth(0.3f)
            cs.addRect(xNum, y - altoFila, anchoNum, altoFila)
            xs = xSig
            for (a in anchosSigpac) {
                cs.addRect(xs, y - altoFila, a, altoFila); xs += a
            }
            xs = xAgr
            for (a in anchosAgro) {
                cs.addRect(xs, y - altoFila, a, altoFila); xs += a
            }
            cs.stroke()

            val sigpac = pc.referenciaSigpac
            val agro = pc.datosAgronomicos
            val zonaNitratos = pc.parcela.zonaNitratos?.let { if (it) "Si" else "No" } ?: ""

            // Datos SIGPAC
            textoEnCelda(cs, (idx + 1).toString(), xNum, y - altoFila, anchoNum, altoFila, FUENTE_CUERPO_NORMAL, 7.5f, centrar = true)
            val datosSigpac = arrayOf(
                sigpac?.provincia ?: "",
                sigpac?.terminoMunicipal ?: "",
                sigpac?.codigoAgregado ?: "",
                sigpac?.zona ?: "",
                sigpac?.numeroPoligono ?: "",
                sigpac?.numeroParcela ?: "",
                sigpac?.numeroRecinto ?: "",
                sigpac?.usoSigpac ?: ""
            )
            xs = xSig
            datosSigpac.forEachIndexed { i, v ->
                textoEnCelda(cs, v, xs, y - altoFila, anchosSigpac[i], altoFila, FUENTE_CUERPO_NORMAL, 6.5f, centrar = false)
                xs += anchosSigpac[i]
            }
            val datosAgro = arrayOf(
                sigpac?.superficieHa?.let { "%.2f".format(it) } ?: "",
                agro?.especieVariedad ?: "",
                agro?.ecoregimenPractica ?: "",
                agro?.secanoRegadio ?: "",
                agro?.fechaInicio ?: "",
                agro?.fechaFin ?: "",
                agro?.aireLibreProtegido ?: "",
                pc.parcela.sistemaAsesoramiento ?: "",
                zonaNitratos
            )
            xs = xAgr
            datosAgro.forEachIndexed { i, v ->
                textoEnCelda(cs, v, xs, y - altoFila, anchosAgro[i], altoFila, FUENTE_CUERPO_NORMAL, 6.5f, centrar = false)
                xs += anchosAgro[i]
            }
            y -= altoFila
        }
        return y
    }

    // ===================================================================
    // SECCIÓN 3 — INFORMACIÓN SOBRE TRATAMIENTOS FITOSANITARIOS
    // ===================================================================
    private fun generarSeccion3(doc: PDDocument, cuaderno: CuadernoCompletoDto, ordenes: Ordenes) {
        // 3.1 — actuaciones fitosanitarias
        nuevaPagina(doc, cuaderno, numeroSeccion = 3, numeroHoja = 1) { cs, y0 ->
            var y = dibujarTituloSeccionOficial(cs, "3. INFORMACION SOBRE TRATAMIENTOS FITOSANITARIOS", y0)
            y -= 6f
            y = dibujarBarraSubseccion(cs, "3.1 Registro de actuaciones fitosanitarias", y)
            y = dibujarTablaFitosanitarios(cs, cuaderno.actividades, ordenes, y)
            y -= 6f
            dibujarNotasAlPie(cs, listOf(
                "[1] Indicar el numero de orden de identificacion de las parcelas tratadas (si se trata a todas las parcelas indicar \"TODAS\").",
                "[2] Indicar el intervalo de fechas o la fecha concreta del tratamiento.",
                "[3] Numero de orden segun la relacion indicada en el apartado correspondiente de informacion general.",
                "[4] Numero de orden segun la relacion indicada en el apartado correspondiente de informacion general.",
                "[5] Indicar buena, regular o mala. Mapeo: A=ALTA, M=MEDIA, Ba=BAJA, N=NULA."
            ), y)
        }
        // 3.2 — semilla tratada
        nuevaPagina(doc, cuaderno, numeroSeccion = 3, numeroHoja = 2) { cs, y0 ->
            var y = dibujarBarraSubseccion(cs, "3.2 Registro de uso de semilla tratada", y0)
            y = dibujarFranjaSiNo(cs, cuaderno.actividades.any { it.semillaTratada?.aplica == true }, y)
            y -= 4f
            y = dibujarTablaSemilla(cs, cuaderno.actividades, ordenes, y)
            y -= 6f
            dibujarNotasAlPie(cs, listOf(
                "[1] Marcar \"SI\" si en el periodo se ha utilizado semilla tratada, \"NO\" en caso contrario.",
                "[2] Numero de orden de la parcela segun la relacion indicada en la seccion 2.1."
            ), y)
        }
    }

    /**
     * Si una actividad tiene N productos genera N filas (una por producto).
     * Si no tiene ninguno, genera una sola fila con las columnas de producto
     * en blanco.
     */
    private fun dibujarTablaFitosanitarios(
        cs: PDPageContentStream,
        actividades: List<ActividadCompletaDto>,
        ordenes: Ordenes,
        yInicial: Float
    ): Float {
        val anchos = floatArrayOf(60f, 90f, 80f, 45f, 90f, 40f, 40f, 125f, 70f, 35f, 40f, 65f)
        val cabeceras = arrayOf(
            "Id. parcelas [1]",
            "Cultivo (Esp./Var.)",
            "Intervalo fechas [2]",
            "Sup. (ha)",
            "Problema fitosanitario",
            "Aplic. [3]",
            "Equipo [4]",
            "Producto - Nombre comercial",
            "Nº Registro",
            "Dosis",
            "Efic. [5]",
            "Observaciones"
        )
        var y = dibujarFilaTablaOficial(cs, cabeceras, anchos, yInicial, cabecera = true)

        // Solo se emite una fila por (actividad × producto). Las actividades
        // sin productos asignados NO aparecen en este registro: el cuaderno
        // oficial recoge tratamientos fitosanitarios, y sin producto no hay
        // tratamiento que registrar.
        data class FilaFito(val act: ActividadCompletaDto, val producto: ActividadProductoResponse)
        val filas = actividades.flatMap { a ->
            a.productosAplicados.map { FilaFito(a, it) }
        }

        if (filas.isEmpty()) {
            return dibujarFilaAviso(cs, "Sin actuaciones fitosanitarias registradas en el periodo", anchos.sum(), y)
        }

        filas.forEachIndexed { idx, f ->
            if (y - 14f < MARGEN_INF + 50f) { // dejar hueco para notas al pie
                dibujarTextoLibre(cs, "... ${filas.size - idx} registros mas no caben",
                    FUENTE_CUERPO_NORMAL, 7f, MARGEN_IZQ, y - 8f)
                return y
            }
            val parcelaCompleta = ordenes.parcelaPorId[f.act.actividad.parcelaId]
            val cultivo = parcelaCompleta?.datosAgronomicos?.especieVariedad ?: ""
            val ordenParcela = ordenes.ordenParcela[f.act.actividad.parcelaId]?.toString() ?: ""
            val ordenAplicador = f.act.aplicador?.id?.let { ordenes.ordenAplicador[it]?.toString() } ?: ""
            val ordenEquipo = f.act.equipoUsado?.id?.let { ordenes.ordenEquipo[it]?.toString() } ?: ""
            val nombreProducto = f.producto.productoNombreComercial?.takeIf { it.isNotBlank() }
                ?: "Producto #${f.producto.productoId}"
            val numeroRegistro = f.producto.productoNumeroRegistro ?: ""
            val dosis = "%.2f".format(f.producto.dosis)

            y = dibujarFilaTablaOficial(
                cs,
                arrayOf(
                    ordenParcela,
                    cultivo,
                    intervaloFechas(f.act),
                    f.act.actividad.superficieTratada?.let { "%.2f".format(it) } ?: "",
                    f.act.actividad.problemaFitosanitario ?: "",
                    ordenAplicador,
                    ordenEquipo,
                    nombreProducto,
                    numeroRegistro,
                    dosis,
                    abreviarEficacia(f.act.actividad.eficacia),
                    f.act.actividad.observaciones ?: ""
                ),
                anchos, y
            )
        }
        return y
    }

    private fun dibujarTablaSemilla(
        cs: PDPageContentStream,
        actividades: List<ActividadCompletaDto>,
        ordenes: Ordenes,
        yInicial: Float
    ): Float {
        val anchos = floatArrayOf(70f, 55f, 100f, 80f, 95f, 200f, 65f)
        val cabeceras = arrayOf(
            "Fecha siembra",
            "Id. Parc. [2]",
            "Cultivo (Esp./Var.)",
            "Sup. sembrada (ha)",
            "Cantidad semilla (kg)",
            "Producto - Mat. activa / Nombre comercial",
            "Nº Registro"
        )
        var y = dibujarFilaTablaOficial(cs, cabeceras, anchos, yInicial, cabecera = true)

        val semillas = actividades.filter { it.semillaTratada?.aplica == true }
        if (semillas.isEmpty()) {
            return dibujarFilaAviso(cs, "Sin registros de uso de semilla tratada en el periodo", anchos.sum(), y)
        }
        semillas.forEachIndexed { idx, act ->
            if (y - 14f < MARGEN_INF + 50f) {
                dibujarTextoLibre(cs, "... ${semillas.size - idx} registros mas no caben",
                    FUENTE_CUERPO_NORMAL, 7f, MARGEN_IZQ, y - 8f)
                return y
            }
            val s = act.semillaTratada!!
            val parcelaCompleta = ordenes.parcelaPorId[act.actividad.parcelaId]
            val cultivo = parcelaCompleta?.datosAgronomicos?.especieVariedad ?: ""
            val ordenParcela = ordenes.ordenParcela[act.actividad.parcelaId]?.toString() ?: ""
            val producto = listOfNotNull(
                s.productoMateriaActiva?.takeIf { it.isNotBlank() },
                s.productoNombreComercial?.takeIf { it.isNotBlank() }
            ).joinToString(" / ").ifBlank { s.productoId?.let { "Producto #$it" } ?: "" }

            y = dibujarFilaTablaOficial(
                cs,
                arrayOf(
                    s.fechaSiembra ?: act.actividad.fechaInicio,
                    ordenParcela,
                    cultivo,
                    s.superficieHa?.let { "%.2f".format(it) } ?: "",
                    s.cantidadSemillaKg?.let { "%.2f".format(it) } ?: "",
                    producto,
                    s.productoNumeroRegistro ?: ""
                ),
                anchos, y
            )
        }
        return y
    }

    // ===================================================================
    // SECCIÓN 6 — FERTILIZACIÓN
    // ===================================================================
    private fun generarSeccion6(doc: PDDocument, cuaderno: CuadernoCompletoDto, ordenes: Ordenes) {
        nuevaPagina(doc, cuaderno, numeroSeccion = 6, numeroHoja = 1) { cs, y0 ->
            // La sección 6 no tiene subsecciones — el título largo ya
            // incorpora el matiz "(OPCIONAL [EXCEPTO ZONAS VULNERABLES])"
            // y bajo él va directamente la tabla.
            var y = dibujarTituloSeccionOficial(cs, "6. REGISTRO DE FERTILIZACION (OPCIONAL [EXCEPTO ZONAS VULNERABLES])", y0)
            y -= 6f
            y = dibujarTablaFertilizacion(cs, cuaderno.actividades, ordenes, y)
            y -= 6f
            dibujarNotasAlPie(cs, listOf(
                "[1] Nº de orden segun la relacion correspondiente en la hoja de identificacion de parcelas.",
                "[2] En caso de abonos organicos, indicar (EB) estiercol de bovino, (EO) estiercol de ovino, (EP) estiercol de porcino, (PP) purines de porcino, (G) gallinaza, (L) lodos de depuradora, (C) compost de RSU, (O) otros.",
                "[3] Indicar (F) fertirrigacion, (AF) abonado de fondo o (AC) abonado de cobertera."
            ), y)
        }
    }

    private fun dibujarTablaFertilizacion(
        cs: PDPageContentStream,
        actividades: List<ActividadCompletaDto>,
        ordenes: Ordenes,
        yInicial: Float
    ): Float {
        val anchos = floatArrayOf(80f, 50f, 95f, 115f, 70f, 65f, 35f, 60f, 75f)
        val cabeceras = arrayOf(
            "Intervalo fechas",
            "Parc. [1]",
            "Cultivo (Esp./Var.)",
            "Tipo abono / Producto [2]",
            "Nº albaran",
            "Riqueza NPK",
            "Dosis",
            "Tipo fert. [3]",
            "Observaciones"
        )
        var y = dibujarFilaTablaOficial(cs, cabeceras, anchos, yInicial, cabecera = true)

        val ferts = actividades.filter { it.fertilizacion?.aplica == true }
        if (ferts.isEmpty()) {
            return dibujarFilaAviso(cs, "Sin registros de fertilizacion en el periodo", anchos.sum(), y)
        }
        ferts.forEachIndexed { idx, act ->
            if (y - 14f < MARGEN_INF + 50f) {
                dibujarTextoLibre(cs, "... ${ferts.size - idx} registros mas no caben",
                    FUENTE_CUERPO_NORMAL, 7f, MARGEN_IZQ, y - 8f)
                return y
            }
            val f = act.fertilizacion!!
            val parcelaCompleta = ordenes.parcelaPorId[act.actividad.parcelaId]
            val cultivo = parcelaCompleta?.datosAgronomicos?.especieVariedad ?: ""
            val ordenParcela = ordenes.ordenParcela[act.actividad.parcelaId]?.toString() ?: ""

            y = dibujarFilaTablaOficial(
                cs,
                arrayOf(
                    f.fechaInicio ?: act.actividad.fechaInicio,
                    ordenParcela,
                    cultivo,
                    f.tipoProducto ?: "",
                    f.numeroAlbaran ?: "",
                    f.riquezaNpk ?: "",
                    f.dosis?.let { "%.2f".format(it) } ?: "",
                    f.tipoFertilizacion ?: "",
                    f.observaciones ?: ""
                ),
                anchos, y
            )
        }
        return y
    }

    // ===================================================================
    // PRIMITIVAS DE PÁGINA Y BLOQUES VISUALES
    // ===================================================================

    private fun nuevaPagina(
        doc: PDDocument,
        cuaderno: CuadernoCompletoDto,
        numeroSeccion: Int,
        numeroHoja: Int,
        contenido: (PDPageContentStream, Float) -> Unit
    ) {
        val pagina = PDPage(PAGINA)
        doc.addPage(pagina)
        PDPageContentStream(doc, pagina).use { cs ->
            dibujarCabeceraPagina(cs, cuaderno)
            dibujarPiePaginaOficial(cs, numeroHoja, numeroSeccion)
            contenido(cs, PAGINA.height - MARGEN_SUP)
        }
    }

    private fun dibujarCabeceraPagina(cs: PDPageContentStream, cuaderno: CuadernoCompletoDto) {
        val nombreTitular = cuaderno.titular
            ?.let { listOfNotNull(it.nombre, it.apellidos).joinToString(" ") }
            ?: ""
        val anyoPeriodo = cuaderno.periodo.fechaInicio.take(4)

        // Izquierda
        cs.beginText()
        cs.setFont(FUENTE_CUERPO_BOLD, 9f)
        cs.newLineAtOffset(MARGEN_IZQ, PAGINA.height - 22f)
        cs.showText(sanitizar("Explotacion/Titular de la explotacion:"))
        cs.endText()
        cs.beginText()
        cs.setFont(FUENTE_CUERPO_NORMAL, 9f)
        cs.newLineAtOffset(MARGEN_IZQ + 175f, PAGINA.height - 22f)
        cs.showText(sanitizar(nombreTitular))
        cs.endText()

        // Derecha: AÑO
        val textoAno = "AÑO  $anyoPeriodo"
        val anchoAno = FUENTE_CUERPO_BOLD.getStringWidth(sanitizar(textoAno)) / 1000f * 9f
        cs.beginText()
        cs.setFont(FUENTE_CUERPO_BOLD, 9f)
        cs.newLineAtOffset(PAGINA.width - MARGEN_DCH - anchoAno, PAGINA.height - 22f)
        cs.showText(sanitizar(textoAno))
        cs.endText()

        // Línea horizontal separadora
        cs.setLineWidth(0.5f)
        cs.setStrokingColor(0f, 0f, 0f)
        cs.moveTo(MARGEN_IZQ, PAGINA.height - 30f)
        cs.lineTo(PAGINA.width - MARGEN_DCH, PAGINA.height - 30f)
        cs.stroke()
    }

    private fun dibujarPiePaginaOficial(
        cs: PDPageContentStream,
        numeroHoja: Int,
        numeroSeccion: Int
    ) {
        // Línea separadora
        cs.setLineWidth(0.5f)
        cs.setStrokingColor(0f, 0f, 0f)
        cs.moveTo(MARGEN_IZQ, 28f)
        cs.lineTo(PAGINA.width - MARGEN_DCH, 28f)
        cs.stroke()

        val texto = "Hoja n  $numeroHoja  de la seccion n  $numeroSeccion"
        val ancho = FUENTE_CUERPO_NORMAL.getStringWidth(sanitizar(texto)) / 1000f * 8f
        cs.beginText()
        cs.setFont(FUENTE_CUERPO_NORMAL, 8f)
        cs.newLineAtOffset(PAGINA.width - MARGEN_DCH - ancho, 16f)
        cs.showText(sanitizar(texto))
        cs.endText()
    }

    private fun dibujarTituloSeccionOficial(
        cs: PDPageContentStream,
        titulo: String,
        y: Float
    ): Float {
        val anchoTitulo = FUENTE_TITULO_SECCION.getStringWidth(sanitizar(titulo)) / 1000f * 13f
        val x = (PAGINA.width - anchoTitulo) / 2f
        cs.beginText()
        cs.setFont(FUENTE_TITULO_SECCION, 13f)
        cs.newLineAtOffset(x, y)
        cs.showText(sanitizar(titulo))
        cs.endText()
        return y - 22f
    }

    private fun dibujarBarraSubseccion(
        cs: PDPageContentStream,
        titulo: String,
        y: Float
    ): Float {
        val alturaBarra = 16f
        // Fondo gris oscuro a lo largo de todo el ancho útil
        cs.setNonStrokingColor(GRIS_OSCURO, GRIS_OSCURO, GRIS_OSCURO)
        cs.addRect(MARGEN_IZQ, y - alturaBarra, ANCHO_UTIL, alturaBarra)
        cs.fill()
        // Texto en blanco en mayúsculas
        cs.setNonStrokingColor(1f, 1f, 1f)
        cs.beginText()
        cs.setFont(FUENTE_TITULO_SUBSECCION, 9f)
        cs.newLineAtOffset(MARGEN_IZQ + 8f, y - alturaBarra + 5f)
        cs.showText(sanitizar(titulo.uppercase()))
        cs.endText()
        // Restaurar color de relleno
        cs.setNonStrokingColor(0f, 0f, 0f)
        return y - alturaBarra - 8f
    }

    /**
     * Dibuja "APLICA TRATAMIENTO:  [X] SI   [ ] NO" para la cabecera de la
     * sección 3.2. La X aparece bajo el SI cuando hay registros y bajo el
     * NO cuando no.
     */
    private fun dibujarFranjaSiNo(
        cs: PDPageContentStream,
        aplicaSi: Boolean,
        y: Float
    ): Float {
        val alto = 18f
        cs.beginText()
        cs.setFont(FUENTE_CUERPO_BOLD, 9f)
        cs.newLineAtOffset(MARGEN_IZQ, y - 12f)
        cs.showText(sanitizar("APLICA TRATAMIENTO:"))
        cs.endText()

        // Casilla SI
        val xCajaSi = MARGEN_IZQ + 130f
        cs.setLineWidth(0.5f)
        cs.addRect(xCajaSi, y - 14f, 12f, 12f)
        cs.stroke()
        if (aplicaSi) textoEnCelda(cs, "X", xCajaSi, y - 14f, 12f, 12f, FUENTE_CUERPO_BOLD, 9f, centrar = true)
        cs.beginText()
        cs.setFont(FUENTE_CUERPO_NORMAL, 9f)
        cs.newLineAtOffset(xCajaSi + 18f, y - 12f)
        cs.showText(sanitizar("SI"))
        cs.endText()

        // Casilla NO
        val xCajaNo = xCajaSi + 60f
        cs.addRect(xCajaNo, y - 14f, 12f, 12f)
        cs.stroke()
        if (!aplicaSi) textoEnCelda(cs, "X", xCajaNo, y - 14f, 12f, 12f, FUENTE_CUERPO_BOLD, 9f, centrar = true)
        cs.beginText()
        cs.setFont(FUENTE_CUERPO_NORMAL, 9f)
        cs.newLineAtOffset(xCajaNo + 18f, y - 12f)
        cs.showText(sanitizar("NO  (1)"))
        cs.endText()

        return y - alto - 4f
    }

    private fun dibujarNotasAlPie(
        cs: PDPageContentStream,
        notas: List<String>,
        yInicial: Float
    ) {
        var y = yInicial
        notas.forEach { nota ->
            if (y < MARGEN_INF + 12f) return
            cs.beginText()
            cs.setFont(FUENTE_CUERPO_NORMAL, 6.5f)
            cs.newLineAtOffset(MARGEN_IZQ, y)
            cs.showText(sanitizar(truncarTexto(nota, FUENTE_CUERPO_NORMAL, 6.5f, ANCHO_UTIL - 4f)))
            cs.endText()
            y -= 9f
        }
    }

    private fun dibujarFilaAviso(
        cs: PDPageContentStream,
        mensaje: String,
        anchoTotal: Float,
        y: Float
    ): Float {
        val alto = 16f
        cs.setLineWidth(0.3f)
        cs.addRect(MARGEN_IZQ, y - alto, anchoTotal, alto)
        cs.stroke()
        textoEnCelda(cs, mensaje, MARGEN_IZQ, y - alto, anchoTotal, alto, FUENTE_CUERPO_NORMAL, 7.5f, centrar = true)
        return y - alto
    }

    // ===================================================================
    // PRIMITIVAS DE TABLA Y FORMULARIO
    // ===================================================================

    /**
     * Una fila de tabla "oficial": cabecera con fondo gris claro + texto
     * centrado, datos con texto a la izquierda. Todas las celdas bordeadas.
     */
    private fun dibujarFilaTablaOficial(
        cs: PDPageContentStream,
        celdas: Array<String>,
        anchos: FloatArray,
        y: Float,
        cabecera: Boolean = false
    ): Float {
        val alturaFila = if (cabecera) 22f else 14f
        val fuente = if (cabecera) FUENTE_CUERPO_BOLD else FUENTE_CUERPO_NORMAL
        // Cabeceras a 6.5pt para que entren textos largos sin truncar; los
        // datos siguen a 7.5pt para que se lean cómodos.
        val tamano = if (cabecera) 6.5f else 7.5f

        // Fondo gris para cabecera
        if (cabecera) {
            cs.setNonStrokingColor(GRIS_CABECERA, GRIS_CABECERA, GRIS_CABECERA)
            cs.addRect(MARGEN_IZQ, y - alturaFila, anchos.sum(), alturaFila)
            cs.fill()
            cs.setNonStrokingColor(0f, 0f, 0f)
        }
        // Bordes
        cs.setLineWidth(0.4f)
        cs.setStrokingColor(0f, 0f, 0f)
        var x = MARGEN_IZQ
        anchos.forEach { a ->
            cs.addRect(x, y - alturaFila, a, alturaFila)
            x += a
        }
        cs.stroke()
        // Textos
        x = MARGEN_IZQ
        celdas.forEachIndexed { i, contenido ->
            textoEnCelda(cs, contenido, x, y - alturaFila, anchos[i], alturaFila, fuente, tamano, centrar = cabecera)
            x += anchos[i]
        }
        return y - alturaFila
    }

    /**
     * Fila de formulario tipo "Etiqueta: valor" — usada en las fichas de
     * explotación y titular en sección 1.1.
     */
    private fun dibujarFilaForm(
        cs: PDPageContentStream,
        campos: List<Pair<String, String>>,
        anchos: FloatArray,
        y: Float,
        altura: Float = 18f
    ): Float {
        // Bordes
        cs.setLineWidth(0.4f)
        cs.setStrokingColor(0f, 0f, 0f)
        var x = MARGEN_IZQ
        anchos.forEach { a ->
            cs.addRect(x, y - altura, a, altura)
            x += a
        }
        cs.stroke()

        // Etiqueta en negrita + valor a la derecha de la etiqueta
        x = MARGEN_IZQ
        campos.forEachIndexed { i, (etiqueta, valor) ->
            val etiquetaSan = sanitizar(etiqueta)
            val anchoEtiqueta = FUENTE_CUERPO_BOLD.getStringWidth(etiquetaSan) / 1000f * 7.5f
            // Etiqueta
            cs.beginText()
            cs.setFont(FUENTE_CUERPO_BOLD, 7.5f)
            cs.newLineAtOffset(x + 4f, y - altura / 2f - 2f)
            cs.showText(etiquetaSan)
            cs.endText()
            // Valor (truncado al hueco que queda en la celda)
            if (valor.isNotBlank()) {
                val hueco = anchos[i] - anchoEtiqueta - 12f
                val valorTrunc = sanitizar(truncarTexto(valor, FUENTE_CUERPO_NORMAL, 8f, hueco))
                cs.beginText()
                cs.setFont(FUENTE_CUERPO_NORMAL, 8f)
                cs.newLineAtOffset(x + 4f + anchoEtiqueta + 4f, y - altura / 2f - 2f)
                cs.showText(valorTrunc)
                cs.endText()
            }
            x += anchos[i]
        }
        return y - altura
    }

    /**
     * Dibuja [texto] dentro del rectángulo (x, y, ancho, alto) con la fuente
     * y tamaño dados. Si [centrar] es true se centra horizontal y verticalmente.
     */
    private fun textoEnCelda(
        cs: PDPageContentStream,
        texto: String,
        x: Float, y: Float, ancho: Float, alto: Float,
        fuente: PDType1Font, tamano: Float,
        centrar: Boolean
    ) {
        if (texto.isBlank()) return
        // Soporte multilínea con "\n" — solo dibujamos cada línea apilada
        val lineas = texto.split("\n")
        val padding = 2f
        val alturaLinea = tamano + 1f
        val alturaBloque = alturaLinea * lineas.size
        val yTopBloque = (y + alto) - (alto - alturaBloque) / 2f
        lineas.forEachIndexed { i, linea ->
            val truncado = sanitizar(truncarTexto(linea, fuente, tamano, ancho - 2 * padding))
            val anchoTexto = fuente.getStringWidth(truncado) / 1000f * tamano
            val offsetX = if (centrar) (ancho - anchoTexto) / 2f else padding
            val baseline = yTopBloque - (i + 1) * alturaLinea + (tamano * 0.25f)
            cs.beginText()
            cs.setFont(fuente, tamano)
            cs.newLineAtOffset(x + offsetX, baseline)
            cs.showText(truncado)
            cs.endText()
        }
    }

    private fun dibujarTextoLibre(
        cs: PDPageContentStream,
        texto: String,
        fuente: PDType1Font,
        tamano: Float,
        x: Float,
        y: Float
    ) {
        cs.beginText()
        cs.setFont(fuente, tamano)
        cs.newLineAtOffset(x, y)
        cs.showText(sanitizar(texto))
        cs.endText()
    }

    // ===================================================================
    // HELPERS DE DATOS Y SANEAMIENTO
    // ===================================================================

    /**
     * PDFBox Standard14 no soporta acentos ni eñes. Sustituye los caracteres
     * problemáticos por equivalentes ASCII.
     */
    private fun sanitizar(texto: String): String =
        texto
            .replace("á", "a").replace("Á", "A")
            .replace("é", "e").replace("É", "E")
            .replace("í", "i").replace("Í", "I")
            .replace("ó", "o").replace("Ó", "O")
            .replace("ú", "u").replace("Ú", "U")
            .replace("ñ", "n").replace("Ñ", "N")
            .replace("ü", "u").replace("Ü", "U")
            .replace("—", "-")
            .replace("–", "-")
            .replace("·", ".")
            .replace("\r", "")

    private fun truncarTexto(texto: String, fuente: PDType1Font, tamano: Float, anchoMax: Float): String {
        if (texto.isEmpty()) return texto
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

    private fun intervaloFechas(act: ActividadCompletaDto): String {
        val ini = act.actividad.fechaInicio
        val fin = act.actividad.fechaFin
        return when {
            fin.isNullOrBlank() -> ini
            fin == ini          -> ini
            else                -> "$ini - $fin"
        }
    }

    private fun abreviarEficacia(eficacia: String?): String = when (eficacia?.uppercase()) {
        "ALTA"  -> "A"
        "MEDIA" -> "M"
        "BAJA"  -> "Ba"
        "NULA"  -> "N"
        else    -> ""
    }
}
