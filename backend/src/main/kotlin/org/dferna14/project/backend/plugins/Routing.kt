package org.dferna14.project.backend.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.routes.actividadRoutes
import org.dferna14.project.backend.routes.authRoutes
import org.dferna14.project.backend.routes.cuadernoRoutes
import org.dferna14.project.backend.routes.cultivoRoutes
import org.dferna14.project.backend.routes.equipoRoutes
import org.dferna14.project.backend.routes.explotacionRoutes
import org.dferna14.project.backend.routes.fertilizacionRoutes
import org.dferna14.project.backend.routes.parcelaRoutes
import org.dferna14.project.backend.routes.productoRoutes
import org.dferna14.project.backend.routes.sigpacRoutes
import org.dferna14.project.backend.routes.titularRoutes
import org.dferna14.project.backend.routes.usuarioRoutes

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
        authRoutes()
        actividadRoutes()
        parcelaRoutes()
        sigpacRoutes()
        fertilizacionRoutes()
        productoRoutes()
        cultivoRoutes()
        explotacionRoutes()
        titularRoutes()
        equipoRoutes()
        usuarioRoutes()
        cuadernoRoutes()
    }
}