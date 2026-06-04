package org.dferna14.project.data.remote

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.dferna14.project.data.local.SessionStorage

/**
 * Cliente HTTP Ktor configurado para conectar con el backend.
 *
 * La URL base varía según la plataforma:
 * - Desktop  : http://localhost:8080      (mismo PC)
 * - Android  : http://10.0.2.2:8080      (emulador → localhost del PC)
 * - Android  : http://192.168.x.x:8080   (dispositivo físico → IP del PC en WiFi)
 *
 * expect/actual permite tener implementaciones distintas por plataforma.
 */

// URL base — se define por plataforma en androidMain y jvmMain
expect val BASE_URL: String

fun createHttpClient(sessionStorage: SessionStorage): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient         = true
            })
        }
        install(Logging) {
            level  = LogLevel.BODY  // En producción cambiar a LogLevel.INFO
            logger = Logger.DEFAULT
        }
        // Adjunta el JWT en cada petición si hay sesión guardada. Se lee de forma
        // dinámica para que tras login/logout las peticiones usen el token correcto.
        defaultRequest {
            sessionStorage.obtenerToken()?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }
}