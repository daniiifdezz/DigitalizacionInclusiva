package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.Fertilizaciones
import org.dferna14.project.backend.model.FertilizacionRequest
import org.dferna14.project.backend.model.FertilizacionResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

fun Route.fertilizacionRoutes() {

    route("/api/fertilizaciones") {

        // GET /api/fertilizaciones
        get {
            val fertilizaciones = transaction {
                Fertilizaciones.selectAll().map { it.toFertilizacionResponse() }
            }
            call.respond(fertilizaciones)
        }

        // GET /api/fertilizaciones/{id}
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val fertilizacion = transaction {
                Fertilizaciones.selectAll()
                    .where { Fertilizaciones.id eq id }
                    .singleOrNull()
                    ?.toFertilizacionResponse()
            }

            if (fertilizacion == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(fertilizacion)
            }
        }

        // POST /api/fertilizaciones
        post {
            val request = call.receive<FertilizacionRequest>()

            val fechaInicioLocalDate = request.fechaInicio?.let { java.time.LocalDate.parse(it) }
            val fechaFinLocalDate = request.fechaFin?.let { java.time.LocalDate.parse(it) }

            val creada = transaction {
                val nuevaId = Fertilizaciones.insertAndGetId {
                    it[cultivoId] = request.cultivoId
                    it[aplica] = request.aplica
                    it[fechaInicio] = fechaInicioLocalDate
                    it[fechaFin] = fechaFinLocalDate
                    it[tipoProducto] = request.tipoProducto
                    it[numeroAlbaran] = request.numeroAlbaran
                    it[riquezaNPK] = request.riquezaNPK
                    it[dosis] = request.dosis
                    it[tipoFertilizacion] = request.tipoFertilizacion
                    it[observaciones] = request.observaciones
                }.value

                Fertilizaciones.selectAll()
                    .where { Fertilizaciones.id eq nuevaId }
                    .single()
                    .toFertilizacionResponse()
            }

            call.respond(HttpStatusCode.Created, creada)
        }

        // PUT /api/fertilizaciones/{id}
        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<FertilizacionRequest>()
            val fechaInicioLocalDate = request.fechaInicio?.let { java.time.LocalDate.parse(it) }
            val fechaFinLocalDate = request.fechaFin?.let { java.time.LocalDate.parse(it) }

            val filasActualizadas = transaction {
                Fertilizaciones.update({ Fertilizaciones.id eq id }) {
                    it[cultivoId] = request.cultivoId
                    it[aplica] = request.aplica
                    it[fechaInicio] = fechaInicioLocalDate
                    it[fechaFin] = fechaFinLocalDate
                    it[tipoProducto] = request.tipoProducto
                    it[numeroAlbaran] = request.numeroAlbaran
                    it[riquezaNPK] = request.riquezaNPK
                    it[dosis] = request.dosis
                    it[tipoFertilizacion] = request.tipoFertilizacion
                    it[observaciones] = request.observaciones
                }
            }

            if (filasActualizadas == 0) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.OK)
            }
        }

        // DELETE /api/fertilizaciones/{id}
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val filasEliminadas = transaction {
                Fertilizaciones.deleteWhere { Fertilizaciones.id eq id }
            }

            if (filasEliminadas == 0) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun ResultRow.toFertilizacionResponse() = FertilizacionResponse(
    id               = this[Fertilizaciones.id].value,
    cultivoId        = this[Fertilizaciones.cultivoId],
    aplica           = this[Fertilizaciones.aplica],
    fechaInicio      = this[Fertilizaciones.fechaInicio]?.toString(),
    fechaFin         = this[Fertilizaciones.fechaFin]?.toString(),
    tipoProducto     = this[Fertilizaciones.tipoProducto],
    numeroAlbaran    = this[Fertilizaciones.numeroAlbaran],
    riquezaNPK       = this[Fertilizaciones.riquezaNPK],
    dosis            = this[Fertilizaciones.dosis],
    tipoFertilizacion= this[Fertilizaciones.tipoFertilizacion],
    observaciones    = this[Fertilizaciones.observaciones]
)
