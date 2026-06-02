package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.Actividades
import org.dferna14.project.backend.db.Usuarios
import org.dferna14.project.backend.mapper.toUsuarioResponse
import org.dferna14.project.backend.model.UsuarioRequest
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
            val rolParam = call.request.queryParameters["rol"]
            val usuarios = transaction {
                val query = if (rolParam != null) {
                    Usuarios.selectAll().where { Usuarios.rol eq rolParam }
                } else {
                    Usuarios.selectAll()
                }
                query.map { it.toUsuarioResponse() }
            }
            call.respond(usuarios)
        }

        // GET /api/usuarios/{id}
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val usuario = transaction {
                Usuarios.selectAll()
                    .where { Usuarios.id eq id }
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
                    it[rol]            = request.rol?.takeIf { r -> r.isNotBlank() } ?: "AGRICULTOR"
                    it[explotacionId]  = request.explotacionId
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
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val tieneActividades = transaction {
                !Actividades.selectAll()
                    .where { Actividades.aplicadorId eq id }
                    .empty()
            }
            if (tieneActividades) {
                return@delete call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("message" to "No se puede eliminar el aplicador porque está asignado a actividades")
                )
            }

            val filas = transaction { Usuarios.deleteWhere { Usuarios.id eq id } }
            if (filas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.NoContent)
        }
    }
}

