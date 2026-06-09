package org.dferna14.project.backend.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import java.util.Date

/**
 * Configuración JWT. El token se firma con HMAC256 y expira a los 30 días.
 * El cliente lo guarda en disco y lo envía en `Authorization: Bearer <token>`.
 *
 * NOTA: en producción el SECRET debe cargarse desde una variable de entorno,
 * nunca quedar hardcodeado en el repositorio.
 */
object JwtConfig {
    private val SECRET = System.getenv("JWT_SECRET")
        ?: "digitalizacion-inclusiva-tfg-secret-key-cambiar-en-produccion"
    const val ISSUER = "DigitalizacionInclusiva"
    const val AUDIENCE = "DigitalizacionInclusivaUsers"
    const val REALM = "DigitalizacionInclusiva App"

    private const val DURACION_TOKEN_MS = 30L * 24 * 60 * 60 * 1000

    val algorithm: Algorithm get() = Algorithm.HMAC256(SECRET)

    fun generarToken(userId: Int, email: String, rol: String, explotacionId: Int?): String {
        return JWT.create()
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withClaim("rol", rol)
            .withClaim("explotacionId", explotacionId)
            .withExpiresAt(Date(System.currentTimeMillis() + DURACION_TOKEN_MS))
            .sign(algorithm)
    }
}

fun ApplicationCall.tenantId(): Int? =
    principal<JWTPrincipal>()?.payload?.getClaim("explotacionId")?.let {
        if (it.isNull) null else it.asInt()
    }
fun ApplicationCall.currentUserId(): Int =
    principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asInt()
        ?: throw IllegalArgumentException("No se encontró userId en el token")
fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.REALM
            verifier(
                JWT.require(JwtConfig.algorithm)
                    .withIssuer(JwtConfig.ISSUER)
                    .withAudience(JwtConfig.AUDIENCE)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asInt() != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("message" to "Token invalido o expirado")
                )
            }
        }
    }
}
