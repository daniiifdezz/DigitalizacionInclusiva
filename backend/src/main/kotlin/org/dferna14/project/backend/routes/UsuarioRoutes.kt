package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.Actividades
import org.dferna14.project.backend.db.Parcelas
import org.dferna14.project.backend.db.Usuarios
import org.mindrot.jbcrypt.BCrypt
import org.dferna14.project.backend.mapper.toUsuarioResponse
import org.dferna14.project.backend.model.CambioRolRequest
import org.dferna14.project.backend.model.UsuarioRequest
import org.dferna14.project.backend.plugins.tenantId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

private val VALORES_VALIDOS_CARNET = setOf("BASICO", "CUALIFICADO", "FUMIGADOR", "PILOTO")

/**
 * Lectura y mantenimiento de usuarios. El alta vía POST aquí cubre el caso de
 * "aplicador dado de alta por el técnico desde Desktop": el usuario no tiene
 * password hasta que se registre por sí mismo desde la pantalla de auth.
 */
fun Route.usuarioRoutes() {

    route("/api/usuarios") {

        // GET /api/usuarios
        // Filtro opcional: ?rol=AGRICULTOR|TECNICO|ADMIN
        get {
            val tenantId = call.tenantId()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val rolParam = call.request.queryParameters["rol"]
            val usuarios = transaction {
                val base = Usuarios.selectAll().where { Usuarios.explotacionId eq tenantId }
                val query = if (rolParam != null) base.andWhere { Usuarios.rol eq rolParam } else base
                query.map { it.toUsuarioResponse() }
            }
            call.respond(usuarios)
        }

        // GET /api/usuarios/{id}
        get("{id}") {
            val tenantId = call.tenantId()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val usuario = transaction {
                Usuarios.selectAll()
                    .where { (Usuarios.id eq id) and (Usuarios.explotacionId eq tenantId) }
                    .singleOrNull()
                    ?.toUsuarioResponse()
            }

            if (usuario == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(usuario)
        }

        // POST /api/usuarios
        // Crea un usuario sin contraseña — se usa para dar de alta aplicadores
        // desde la pantalla de configuración del técnico. La contraseña se
        // establece después por el propio usuario vía /api/auth/register.
        post {
            val tenantId = call.tenantId()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val request = call.receive<UsuarioRequest>()
            if (request.nombre.isBlank() || request.email.isBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "nombre y email son obligatorios")
                )
            }
            if (request.tipoCarnetRopo != null && request.tipoCarnetRopo !in VALORES_VALIDOS_CARNET) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "Tipo de carné inválido. Valores permitidos: BASICO, CUALIFICADO, FUMIGADOR, PILOTO")
                )
            }
            val emailNorm = request.email.trim().lowercase()

            val resultado = transaction {
                val yaExiste = Usuarios.selectAll()
                    .where { Usuarios.email eq emailNorm }
                    .any()
                if (yaExiste) return@transaction null

                val nuevoId = Usuarios.insertAndGetId {
                    it[nombre]         = request.nombre.trim()
                    it[apellidos]      = request.apellidos?.trim()
                    it[email]          = emailNorm
                    it[passwordHash]   = request.contrasena
                        ?.takeIf { p -> p.isNotBlank() }
                        ?.let { p -> BCrypt.hashpw(p, BCrypt.gensalt(12)) }
                    it[rol]            = request.rol?.takeIf { r -> r.isNotBlank() } ?: "AGRICULTOR"
                    it[explotacionId]  = tenantId
                    it[fechaAlta]      = LocalDate.now()
                    it[tipoCarnetRopo] = request.tipoCarnetRopo
                }.value

                Usuarios.selectAll()
                    .where { Usuarios.id eq nuevoId }
                    .single()
                    .toUsuarioResponse()
            }

            if (resultado == null) {
                call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("message" to "Ya existe un usuario con ese email")
                )
            } else {
                call.respond(HttpStatusCode.Created, resultado)
            }
        }

        // DELETE /api/usuarios/{id}
        // 409 si el usuario está asignado como aplicador en alguna actividad.
        delete("{id}") {
            val tenantId = call.tenantId()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val esDelTenant = transaction {
                Usuarios.selectAll()
                    .where { (Usuarios.id eq id) and (Usuarios.explotacionId eq tenantId) }
                    .any()
            }
            if (!esDelTenant) return@delete call.respond(HttpStatusCode.NotFound)

            val tieneActividades = transaction {
                !Actividades.selectAll()
                    .where { Actividades.aplicadorId eq id }
                    .empty()
            }
            if (tieneActividades) {
                return@delete call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("message" to "No se puede eliminar el usuario porque está asignado a actividades")
                )
            }

            val tieneParcelas = transaction {
                !Parcelas.selectAll()
                    .where { Parcelas.creadorId eq id }
                    .empty()
            }
            if (tieneParcelas) {
                return@delete call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("message" to "No se puede eliminar el agricultor porque tiene parcelas asociadas")
                )
            }

            val filas = transaction { Usuarios.deleteWhere { Usuarios.id eq id } }
            if (filas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.NoContent)
        }

        // PUT /api/usuarios/{id}/rol — promoción/degradación de roles (solo TECNICO).
        // Reglas: solo TECNICO puede cambiar roles; un TECNICO no puede degradarse a sí mismo.
        put("{id}/rol") {
            val principal = call.principal<JWTPrincipal>()!!
            val rolSolicitante = principal.payload.getClaim("rol").asString()
            val userIdSolicitante = principal.payload.getClaim("userId").asInt()
            val tenantId = call.tenantId()

            if (rolSolicitante != "TECNICO") {
                return@put call.respond(
                    HttpStatusCode.Forbidden,
                    mapOf("message" to "Solo los tecnicos pueden modificar roles")
                )
            }

            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "ID de usuario invalido")
                )

            val request = call.receive<CambioRolRequest>()
            if (request.nuevoRol !in setOf("AGRICULTOR", "TECNICO")) {
                return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "Rol invalido. Permitidos: AGRICULTOR, TECNICO")
                )
            }

            if (id == userIdSolicitante && request.nuevoRol == "AGRICULTOR") {
                return@put call.respond(
                    HttpStatusCode.Forbidden,
                    mapOf("message" to "No puedes degradarte a ti mismo. Pide a otro tecnico que lo haga.")
                )
            }

            val esDelTenant = tenantId != null && transaction {
                Usuarios.selectAll()
                    .where { (Usuarios.id eq id) and (Usuarios.explotacionId eq tenantId) }
                    .any()
            }
            if (!esDelTenant) return@put call.respond(HttpStatusCode.NotFound, mapOf("message" to "Usuario no encontrado"))

            val filasAfectadas = transaction {
                Usuarios.update({ Usuarios.id eq id }) {
                    it[Usuarios.rol] = request.nuevoRol
                }
            }

            if (filasAfectadas == 0) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Usuario no encontrado"))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Rol actualizado correctamente"))
            }
        }
    }
}

