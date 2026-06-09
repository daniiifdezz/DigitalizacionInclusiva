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
    // Imprime las IPs locales al arrancar para que sea fácil configurar la tablet.
    val ips = java.net.NetworkInterface.getNetworkInterfaces()
        ?.asSequence()
        ?.filter { it.isUp && !it.isLoopback }
        ?.flatMap { it.inetAddresses.asSequence() }
        ?.filterIsInstance<java.net.Inet4Address>()
        ?.map { "  http://${it.hostAddress}:8080" }
        ?.joinToString("\n") ?: "  (no detectadas)"

    println("═══════════════════════════════════════════")
    println("  Backend arrancando en http://0.0.0.0:8080")
    println("  Usa en la tablet cualquiera de estas IPs:")
    println(ips)
    println("═══════════════════════════════════════════")

    embeddedServer(
        factory  = Netty,
        port     = 8080,
        host     = "0.0.0.0",
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