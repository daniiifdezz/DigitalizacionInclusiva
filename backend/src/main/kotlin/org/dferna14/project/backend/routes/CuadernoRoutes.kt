package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.plugins.tenantId
import org.dferna14.project.backend.service.CuadernoPdfGenerator
import org.dferna14.project.backend.service.CuadernoService
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Endpoints REST para la generación del Cuaderno de Campo Digital (RD 1311/2012).
 *
 *  - GET /api/cuaderno/pdf   → PDF firmado como descarga
 *  - GET /api/cuaderno/datos → JSON con los datos consolidados (debug)
 *
 * Ambos exigen ?desde=YYYY-MM-DD&hasta=YYYY-MM-DD y solo incluyen actividades
 * en estado VALIDADA dentro del periodo (la lógica vive en CuadernoService).
 */
fun Route.cuadernoRoutes() {

    route("/api/cuaderno") {

        get("/pdf") {
            val tenantId = call.tenantId()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val (desde, hasta) = parseFechas(
                desdeStr = call.request.queryParameters["desde"],
                hastaStr = call.request.queryParameters["hasta"]
            ) { status, mensaje ->
                call.respond(status, mapOf("message" to mensaje))
                return@get
            }

            try {
                val cuaderno = CuadernoService.obtenerCuadernoCompleto(desde, hasta, tenantId)
                val pdfBytes = CuadernoPdfGenerator.generar(cuaderno)

                val nombreFichero = "cuaderno_campo_${desde}_a_${hasta}.pdf"
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment
                        .withParameter(ContentDisposition.Parameters.FileName, nombreFichero)
                        .toString()
                )
                call.respondBytes(
                    bytes = pdfBytes,
                    contentType = ContentType.Application.Pdf,
                    status = HttpStatusCode.OK
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("message" to "Error generando el PDF: ${e.message}")
                )
            }
        }

        get("/datos") {
            val tenantId = call.tenantId()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val (desde, hasta) = parseFechas(
                desdeStr = call.request.queryParameters["desde"],
                hastaStr = call.request.queryParameters["hasta"]
            ) { status, mensaje ->
                call.respond(status, mapOf("message" to mensaje))
                return@get
            }

            try {
                val cuaderno = CuadernoService.obtenerCuadernoCompleto(desde, hasta, tenantId)
                call.respond(cuaderno)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("message" to "Error obteniendo el cuaderno: ${e.message}")
                )
            }
        }
    }
}

/**
 * Helper para validar los parámetros `desde` y `hasta`. Llama a `onError`
 * (que debe devolver la response y abortar el handler) si algo no cuadra.
 * Si todo es válido devuelve el par `(desde, hasta)` ya parseado.
 */
private inline fun parseFechas(
    desdeStr: String?,
    hastaStr: String?,
    onError: (HttpStatusCode, String) -> Nothing
): Pair<LocalDate, LocalDate> {
    if (desdeStr.isNullOrBlank()) {
        onError(HttpStatusCode.BadRequest, "Falta el parametro 'desde' (formato YYYY-MM-DD)")
    }
    if (hastaStr.isNullOrBlank()) {
        onError(HttpStatusCode.BadRequest, "Falta el parametro 'hasta' (formato YYYY-MM-DD)")
    }

    val desde = try {
        LocalDate.parse(desdeStr)
    } catch (e: DateTimeParseException) {
        onError(HttpStatusCode.BadRequest, "Formato de fecha invalido en 'desde'. Usa YYYY-MM-DD")
    }
    val hasta = try {
        LocalDate.parse(hastaStr)
    } catch (e: DateTimeParseException) {
        onError(HttpStatusCode.BadRequest, "Formato de fecha invalido en 'hasta'. Usa YYYY-MM-DD")
    }

    if (hasta.isBefore(desde)) {
        onError(HttpStatusCode.BadRequest, "'hasta' no puede ser anterior a 'desde'")
    }

    return desde to hasta
}
