package org.dferna14.project.backend

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.dferna14.project.backend.db.DatabaseFactory
import org.dferna14.project.backend.plugins.configureCors
import org.dferna14.project.backend.plugins.configureRouting
import org.dferna14.project.backend.plugins.configureSecurity
import org.dferna14.project.backend.plugins.configureSerialization
import org.dferna14.project.backend.plugins.configureStatusPages

/**
 * Punto de entrada del servidor Ktor.
 * Arranca en el puerto 8080 y configura todos los plugins.
 *
 * Para ejecutar: ./gradlew :backend:run
 */
fun main() {
    embeddedServer(
        factory  = Netty,
        port     = 8080,
        host     = "0.0.0.0", // acepta conexiones desde móvil en la misma red WiFi
        module   = Application::module
    ).start(wait = true)
}

fun Application.module() {
    // 1. Conectar con PostgreSQL
    DatabaseFactory.init()

    // 2. Configurar plugins
    configureSerialization()
    configureCors()
    configureStatusPages()
    configureSecurity()   // instalar Authentication antes de las rutas que usan authenticate()
    configureRouting()
}