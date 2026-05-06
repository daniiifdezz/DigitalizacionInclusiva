package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.Titulares
import org.dferna14.project.backend.model.TitularRequest
import org.dferna14.project.backend.model.TitularResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.titularRoutes() {

    route("/api/titulares") {

        // GET /api/titulares
        get {
            val titulares = transaction {
                Titulares.selectAll().map { it.toTitularResponse() }
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

private fun ResultRow.toTitularResponse() = TitularResponse(
    id           = this[Titulares.id].value,
    nombre       = this[Titulares.nombre],
    apellidos    = this[Titulares.apellidos],
    nif          = this[Titulares.nif],
    direccion    = this[Titulares.direccion],
    localidad    = this[Titulares.localidad],
    codigoPostal = this[Titulares.codigoPostal],
    provincia    = this[Titulares.provincia],
    telefono     = this[Titulares.telefono],
    email        = this[Titulares.email]
)
