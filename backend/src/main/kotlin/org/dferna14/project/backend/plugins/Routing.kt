package org.dferna14.project.backend.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.routes.actividadRoutes
import org.dferna14.project.backend.routes.parcelaRoutes

/**
 * Registro central de todas las rutas de la API.
 * Añade aquí los nuevos routers cuando los crees.
 */
fun Application.configureRouting() {
    routing {

        // Health check --> verificar si el servidor esta activo
        get("/health") {
            call.respondText("OK")
        }

        // Rutas de la API
        actividadRoutes()
        parcelaRoutes()
    }
}