package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.Cultivos
import org.dferna14.project.backend.model.CultivoRequest
import org.dferna14.project.backend.model.CultivoResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

fun Route.cultivoRoutes() {

    route("/api/cultivos") {

        // GET /api/cultivos
        get {
            val cultivos = transaction {
                Cultivos.selectAll().map { it.toCultivoResponse() }
            }
            call.respond(cultivos)
        }

        // GET /api/cultivos/{id}
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val cultivo = transaction {
                Cultivos.selectAll()
                    .where { Cultivos.id eq id }
                    .singleOrNull()
                    ?.toCultivoResponse()
            }

            if (cultivo == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(cultivo)
            }
        }

        // POST /api/cultivos
        post {
            val request = call.receive<CultivoRequest>()

            val nuevoId = transaction {
                Cultivos.insertAndGetId {
                    it[especie]  = request.especie
                    it[variedad] = request.variedad
                }.value
            }

            val creada = transaction {
                Cultivos.selectAll()
                    .where { Cultivos.id eq nuevoId }
                    .single()
                    .toCultivoResponse()
            }

            call.respond(HttpStatusCode.Created, creada)
        }

        // PUT /api/cultivos/{id}
        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<CultivoRequest>()

            val filasActualizadas = transaction {
                Cultivos.update({ Cultivos.id eq id }) {
                    it[especie]  = request.especie
                    it[variedad] = request.variedad
                }
            }

            if (filasActualizadas == 0) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.OK)
            }
        }

        // DELETE /api/cultivos/{id}
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val filasEliminadas = transaction {
                Cultivos.deleteWhere { Cultivos.id eq id }
            }

            if (filasEliminadas == 0) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun ResultRow.toCultivoResponse() = CultivoResponse(
    id       = this[Cultivos.id].value,
    especie  = this[Cultivos.especie],
    variedad = this[Cultivos.variedad]
)
