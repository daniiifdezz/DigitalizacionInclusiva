package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.Usuarios
import org.dferna14.project.backend.model.UsuarioResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Lectura de usuarios para poblar dropdowns (aplicador en una actividad, etc.).
 * El alta/edición/baja se delegará al sprint de autenticación, que añadirá
 * password_hash, login y validación serias.
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
    }
}

private fun ResultRow.toUsuarioResponse() = UsuarioResponse(
    id            = this[Usuarios.id].value,
    nombre        = this[Usuarios.nombre],
    apellidos     = this[Usuarios.apellidos],
    email         = this[Usuarios.email],
    rol           = this[Usuarios.rol],
    explotacionId = this[Usuarios.explotacionId],
    fechaAlta     = this[Usuarios.fechaAlta]?.toString()
)
