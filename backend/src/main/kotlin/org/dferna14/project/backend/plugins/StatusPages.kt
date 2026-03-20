package org.dferna14.project.backend.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

/**
 * Manejo centralizado de errores.
 * Cualquier excepción no controlada devuelve un JSON con el mensaje de error
 * en lugar de un stack trace crudo.
 */
fun Application.configureStatusPages() {
    install(StatusPages) {

        // Error genérico no controlado
        exception<Throwable> { call, cause ->
            call.respond(
                status  = HttpStatusCode.InternalServerError,
                message = ErrorResponse(
                    code    = 500,
                    message = cause.localizedMessage ?: "Error interno del servidor"
                )
            )
        }

        // Recurso no encontrado
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                status  = HttpStatusCode.NotFound,
                message = ErrorResponse(code = 404, message = "Recurso no encontrado")
            )
        }

        // Petición mal formada
        status(HttpStatusCode.BadRequest) { call, _ ->
            call.respond(
                status  = HttpStatusCode.BadRequest,
                message = ErrorResponse(code = 400, message = "Petición no válida")
            )
        }
    }
}

/**
 * Estructura estándar de respuesta de error.
 */
@Serializable
data class ErrorResponse(
    val code    : Int,
    val message : String
)