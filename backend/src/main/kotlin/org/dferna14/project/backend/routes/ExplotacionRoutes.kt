package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.EquiposAplicacion
import org.dferna14.project.backend.db.Explotaciones
import org.dferna14.project.backend.db.Parcelas
import org.dferna14.project.backend.db.Usuarios
import org.dferna14.project.backend.model.ExplotacionRequest
import org.dferna14.project.backend.model.ExplotacionResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.explotacionRoutes() {

    route("/api/explotaciones") {

        // GET /api/explotaciones
        get {
            val explotaciones = transaction {
                Explotaciones.selectAll().map { it.toExplotacionResponse() }
            }
            call.respond(explotaciones)
        }

        // GET /api/explotaciones/{id}
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val explotacion = transaction {
                Explotaciones.selectAll()
                    .where { Explotaciones.id eq id }
                    .singleOrNull()
                    ?.toExplotacionResponse()
            }

            if (explotacion == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(explotacion)
        }

        // POST /api/explotaciones
        post {
            val request = call.receive<ExplotacionRequest>()
            if (request.nombre.isBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "nombre es obligatorio")
                )
            }

            val creada = transaction {
                val nuevoId = Explotaciones.insertAndGetId {
                    it[nombre]       = request.nombre
                    it[titularId]    = request.titularId
                    it[direccion]    = request.direccion
                    it[municipio]    = request.municipio
                    it[provincia]    = request.provincia
                    it[codigoPostal] = request.codigoPostal
                    it[nifEmpresa]   = request.nifEmpresa
                }.value

                Explotaciones.selectAll()
                    .where { Explotaciones.id eq nuevoId }
                    .single()
                    .toExplotacionResponse()
            }

            call.respond(HttpStatusCode.Created, creada)
        }

        // PUT /api/explotaciones/{id}
        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<ExplotacionRequest>()

            val filas = transaction {
                Explotaciones.update({ Explotaciones.id eq id }) {
                    it[nombre]       = request.nombre
                    it[titularId]    = request.titularId
                    it[direccion]    = request.direccion
                    it[municipio]    = request.municipio
                    it[provincia]    = request.provincia
                    it[codigoPostal] = request.codigoPostal
                    it[nifEmpresa]   = request.nifEmpresa
                }
            }

            if (filas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK)
        }

        // DELETE /api/explotaciones/{id}
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val tieneDependencias = transaction {
                !Parcelas.selectAll().where { Parcelas.explotacionId eq id }.empty() ||
                !EquiposAplicacion.selectAll().where { EquiposAplicacion.explotacionId eq id }.empty() ||
                !Usuarios.selectAll().where { Usuarios.explotacionId eq id }.empty()
            }

            if (tieneDependencias) {
                return@delete call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("message" to "No se puede eliminar la explotación porque tiene parcelas, equipos o usuarios asociados")
                )
            }

            val filas = transaction {
                Explotaciones.deleteWhere { Explotaciones.id eq id }
            }

            if (filas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun ResultRow.toExplotacionResponse() = ExplotacionResponse(
    id           = this[Explotaciones.id].value,
    nombre       = this[Explotaciones.nombre],
    titularId    = this[Explotaciones.titularId],
    direccion    = this[Explotaciones.direccion],
    municipio    = this[Explotaciones.municipio],
    provincia    = this[Explotaciones.provincia],
    codigoPostal = this[Explotaciones.codigoPostal],
    nifEmpresa   = this[Explotaciones.nifEmpresa]
)
