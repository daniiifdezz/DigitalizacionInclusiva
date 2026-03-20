package org.dferna14.project.backend.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

/**
 * Configura la serialización JSON del servidor.
 * Usa kotlinx.serialization con opciones flexibles para desarrollo.
 */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint        = true   // JSON legible en desarrollo
            isLenient          = true   // acepta JSON no estricto
            ignoreUnknownKeys  = true   // no falla si llegan campos extra
            encodeDefaults     = true   // incluye valores por defecto en respuesta
        })
    }
}