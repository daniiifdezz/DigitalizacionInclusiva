package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.Usuarios
import org.dferna14.project.backend.mapper.toUsuarioResponse
import org.dferna14.project.backend.model.LoginRequest
import org.dferna14.project.backend.model.LoginResponse
import org.dferna14.project.backend.model.RegisterRequest
import org.dferna14.project.backend.plugins.JwtConfig
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDate

/**
 * Rutas de autenticación: registro y login.
 * Las contraseñas se cifran SIEMPRE con BCrypt antes de persistirse.
 * No se emite ningún token JWT en este sprint — el login simplemente devuelve
 * el UsuarioDto correspondiente cuando las credenciales son válidas.
 */
fun Route.authRoutes() {

    route("/api/auth") {

        // POST /api/auth/register
        post("register") {
            val req = call.receive<RegisterRequest>()

            if (req.email.isBlank() || req.password.isBlank() || req.nombre.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, "email, password y nombre son obligatorios")
            }

            val emailNorm = req.email.trim().lowercase()
            val hash      = BCrypt.hashpw(req.password, BCrypt.gensalt(12))

            val resultado = transaction {
                val yaExiste = Usuarios.selectAll()
                    .where { Usuarios.email eq emailNorm }
                    .any()
                if (yaExiste) return@transaction null

                val newId = Usuarios.insertAndGetId {
                    it[nombre]       = req.nombre.trim()
                    it[email]        = emailNorm
                    it[passwordHash] = hash
                    it[rol]          = req.rol?.takeIf { r -> r.isNotBlank() } ?: "AGRICULTOR"
                    it[fechaAlta]    = LocalDate.now()
                }.value

                Usuarios.selectAll()
                    .where { Usuarios.id eq newId }
                    .single()
                    .toUsuarioResponse()
            }

            if (resultado == null) {
                call.respond(HttpStatusCode.Conflict, "Ya existe un usuario con ese email")
            } else {
                val token = JwtConfig.generarToken(
                    userId = resultado.id,
                    email  = resultado.email,
                    rol    = resultado.rol
                )
                call.respond(HttpStatusCode.Created, LoginResponse(token = token, usuario = resultado))
            }
        }

        // POST /api/auth/login
        post("login") {
            val req = call.receive<LoginRequest>()

            if (req.email.isBlank() || req.password.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, "email y password son obligatorios")
            }

            val emailNorm = req.email.trim().lowercase()

            val (hashGuardado, usuarioRow) = transaction {
                val row = Usuarios.selectAll()
                    .where { Usuarios.email eq emailNorm }
                    .singleOrNull()
                val hash = row?.get(Usuarios.passwordHash)
                hash to row
            }

            if (hashGuardado.isNullOrBlank() || usuarioRow == null) {
                return@post call.respond(HttpStatusCode.Unauthorized, "Credenciales inválidas")
            }

            val coincide = try {
                BCrypt.checkpw(req.password, hashGuardado)
            } catch (e: IllegalArgumentException) {
                false
            }

            if (!coincide) {
                call.respond(HttpStatusCode.Unauthorized, "Credenciales inválidas")
            } else {
                val usuarioResponse = usuarioRow.toUsuarioResponse()
                val token = JwtConfig.generarToken(
                    userId = usuarioResponse.id,
                    email  = usuarioResponse.email,
                    rol    = usuarioResponse.rol
                )
                call.respond(HttpStatusCode.OK, LoginResponse(token = token, usuario = usuarioResponse))
            }
        }

        // GET /api/auth/me - Valida el JWT guardado y devuelve el usuario actual.
        // El cliente lo llama al arrancar para restaurar la sesión persistida.
        // Ktor verifica automáticamente el token dentro de authenticate("auth-jwt").
        authenticate("auth-jwt") {
            get("me") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("userId").asInt()

                val usuario = transaction {
                    Usuarios.selectAll()
                        .where { Usuarios.id eq userId }
                        .singleOrNull()
                        ?.toUsuarioResponse()
                }

                if (usuario != null) {
                    call.respond(usuario)
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("message" to "Usuario no encontrado")
                    )
                }
            }
        }
    }
}

