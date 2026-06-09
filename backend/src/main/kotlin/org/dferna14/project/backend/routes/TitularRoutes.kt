package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.Explotaciones
import org.dferna14.project.backend.db.Titulares
import org.dferna14.project.backend.mapper.toTitularResponse
import org.dferna14.project.backend.model.TitularRequest
import org.dferna14.project.backend.plugins.tenantId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.titularRoutes() {

    route("/api/titulares") {

        // GET /api/titulares
        get {
            val tenantId = call.tenantId()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val titulares = transaction {
                val titularId = Explotaciones.selectAll()
                    .where { Explotaciones.id eq tenantId }
                    .firstOrNull()
                    ?.get(Explotaciones.titularId)
                if (titularId != null) {
                    Titulares.selectAll()
                        .where { Titulares.id eq titularId }
                        .map { it.toTitularResponse() }
                } else emptyList()
            }
            call.respond(titulares)
        }

        // GET /api/titulares/{id}
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val titular = transaction {
                Titulares.selectAll()
                    .where { Titulares.id eq id }
                    .singleOrNull()
                    ?.toTitularResponse()
            }

            if (titular == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(titular)
        }

        // POST /api/titulares
        post {
            val request = call.receive<TitularRequest>()
            if (request.nombre.isBlank() || request.nif.isBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "nombre y nif son obligatorios")
                )
            }

            val creado = transaction {
                val nuevoId = Titulares.insertAndGetId {
                    it[nombre]       = request.nombre
                    it[apellidos]    = request.apellidos
                    it[nif]          = request.nif
                    it[direccion]    = request.direccion
                    it[localidad]    = request.localidad
                    it[codigoPostal] = request.codigoPostal
                    it[provincia]    = request.provincia
                    it[telefono]     = request.telefono
                    it[email]        = request.email
                }.value

                Titulares.selectAll()
                    .where { Titulares.id eq nuevoId }
                    .single()
                    .toTitularResponse()
            }

            call.respond(HttpStatusCode.Created, creado)
        }

        // PUT /api/titulares/{id}
        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<TitularRequest>()

            val filas = transaction {
                Titulares.update({ Titulares.id eq id }) {
                    it[nombre]       = request.nombre
                    it[apellidos]    = request.apellidos
                    it[nif]          = request.nif
                    it[direccion]    = request.direccion
                    it[localidad]    = request.localidad
                    it[codigoPostal] = request.codigoPostal
                    it[provincia]    = request.provincia
                    it[telefono]     = request.telefono
                    it[email]        = request.email
                }
            }

            if (filas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK)
        }

        // DELETE /api/titulares/{id}
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val filas = transaction {
                Titulares.deleteWhere { Titulares.id eq id }
            }

            if (filas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.NoContent)
        }
    }
}

