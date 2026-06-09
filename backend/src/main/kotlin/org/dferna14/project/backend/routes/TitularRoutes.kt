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
            val tenantId = call.tenantId()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val request = call.receive<TitularRequest>()
            if (request.nombre.isBlank() || request.nif.isBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "nombre y nif son obligatorios")
                )
            }

            val creado = transaction {
                // Upsert por NIF: si ya existe un Titular con ese NIF (estado roto previo),
                // simplemente lo vinculamos en lugar de duplicar.
                val existente = Titulares.selectAll()
                    .where { Titulares.nif eq request.nif }
                    .singleOrNull()

                val resolvedId = if (existente != null) {
                    // Actualizar datos y reutilizar el registro existente
                    Titulares.update({ Titulares.nif eq request.nif }) {
                        it[nombre]       = request.nombre
                        it[apellidos]    = request.apellidos
                        it[direccion]    = request.direccion
                        it[localidad]    = request.localidad
                        it[codigoPostal] = request.codigoPostal
                        it[provincia]    = request.provincia
                        it[telefono]     = request.telefono
                        it[email]        = request.email
                    }
                    existente[Titulares.id].value
                } else {
                    Titulares.insertAndGetId {
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
                }

                // Vincular el Titular a la Explotacion del TECNICO
                Explotaciones.update({ Explotaciones.id eq tenantId }) {
                    it[titularId] = resolvedId
                }

                Titulares.selectAll()
                    .where { Titulares.id eq resolvedId }
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

            val actualizado = transaction {
                val filas = Titulares.update({ Titulares.id eq id }) {
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
                if (filas > 0) Titulares.selectAll().where { Titulares.id eq id }.singleOrNull()?.toTitularResponse()
                else null
            }

            if (actualizado == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK, actualizado)
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

