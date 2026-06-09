package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.plugins.tenantId
import org.dferna14.project.backend.db.Actividades
import org.dferna14.project.backend.db.EquiposAplicacion
import org.dferna14.project.backend.mapper.toEquipoResponse
import org.dferna14.project.backend.model.EquipoRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.equipoRoutes() {

    route("/api/equipos") {

        // GET /api/equipos
        get {
            val tenantId = call.tenantId()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val equipos = transaction {
                EquiposAplicacion.selectAll()
                    .where { EquiposAplicacion.explotacionId eq tenantId }
                    .map { it.toEquipoResponse() }
            }
            call.respond(equipos)
        }

        // GET /api/equipos/{id}
        get("{id}") {
            val tenantId = call.tenantId()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val equipo = transaction {
                EquiposAplicacion.selectAll()
                    .where { (EquiposAplicacion.id eq id) and (EquiposAplicacion.explotacionId eq tenantId) }
                    .singleOrNull()
                    ?.toEquipoResponse()
            }

            if (equipo == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(equipo)
        }

        // POST /api/equipos
        post {
            val tenantId = call.tenantId()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val request = call.receive<EquipoRequest>()
            if (request.tipo.isBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "tipo es obligatorio")
                )
            }
            val fechaInsp = request.fechaUltimaInspeccion?.let { java.time.LocalDate.parse(it) }

            val creado = transaction {
                val nuevoId = EquiposAplicacion.insertAndGetId {
                    it[explotacionId]         = tenantId
                    it[tipo]                  = request.tipo
                    it[marca]                 = request.marca
                    it[modelo]                = request.modelo
                    it[numeroRoma]            = request.numeroRoma
                    it[anyoFabricacion]       = request.anyoFabricacion
                    it[fechaUltimaInspeccion] = fechaInsp
                }.value

                EquiposAplicacion.selectAll()
                    .where { EquiposAplicacion.id eq nuevoId }
                    .single()
                    .toEquipoResponse()
            }

            call.respond(HttpStatusCode.Created, creado)
        }

        // PUT /api/equipos/{id}
        put("{id}") {
            val tenantId = call.tenantId()
                ?: return@put call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<EquipoRequest>()
            val fechaInsp = request.fechaUltimaInspeccion?.let { java.time.LocalDate.parse(it) }

            val filas = transaction {
                EquiposAplicacion.update({ (EquiposAplicacion.id eq id) and (EquiposAplicacion.explotacionId eq tenantId) }) {
                    it[explotacionId]         = tenantId
                    it[tipo]                  = request.tipo
                    it[marca]                 = request.marca
                    it[modelo]                = request.modelo
                    it[numeroRoma]            = request.numeroRoma
                    it[anyoFabricacion]       = request.anyoFabricacion
                    it[fechaUltimaInspeccion] = fechaInsp
                }
            }

            if (filas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK)
        }

        // DELETE /api/equipos/{id}
        delete("{id}") {
            val tenantId = call.tenantId()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val esDelTenant = transaction {
                EquiposAplicacion.selectAll()
                    .where { (EquiposAplicacion.id eq id) and (EquiposAplicacion.explotacionId eq tenantId) }
                    .any()
            }
            if (!esDelTenant) return@delete call.respond(HttpStatusCode.NotFound)

            val tieneActividades = transaction {
                !Actividades.selectAll().where { Actividades.equipoId eq id }.empty()
            }
            if (tieneActividades) {
                return@delete call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("message" to "No se puede eliminar el equipo porque tiene actividades asociadas")
                )
            }

            val filas = transaction {
                EquiposAplicacion.deleteWhere { EquiposAplicacion.id eq id }
            }

            if (filas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.NoContent)
        }
    }
}

