package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.Fertilizaciones
import org.dferna14.project.backend.mapper.toFertilizacionResponse
import org.dferna14.project.backend.model.FertilizacionRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.fertilizacionRoutes() {

    // CRUD por id compatible con vistas que no manejan actividad.
    route("/api/fertilizaciones") {

        get {
            val fertilizaciones = transaction {
                Fertilizaciones.selectAll().map { it.toFertilizacionResponse() }
            }
            call.respond(fertilizaciones)
        }

        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val fertilizacion = transaction {
                Fertilizaciones.selectAll()
                    .where { Fertilizaciones.id eq id }
                    .singleOrNull()
                    ?.toFertilizacionResponse()
            }

            if (fertilizacion == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(fertilizacion)
        }

        post {
            val request = call.receive<FertilizacionRequest>()

            val fechaInicioLocalDate = request.fechaInicio?.let { java.time.LocalDate.parse(it) }
            val fechaFinLocalDate = request.fechaFin?.let { java.time.LocalDate.parse(it) }

            val creada = transaction {
                val nuevaId = Fertilizaciones.insertAndGetId {
                    it[actividadId] = request.actividadId
                    it[productoId] = request.productoId
                    it[cultivoId] = request.cultivoId
                    it[aplica] = request.aplica
                    it[fechaInicio] = fechaInicioLocalDate
                    it[fechaFin] = fechaFinLocalDate
                    it[tipoProducto] = request.tipoProducto
                    it[numeroAlbaran] = request.numeroAlbaran
                    it[riquezaNpk] = request.riquezaNpk
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

        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<FertilizacionRequest>()
            val fechaInicioLocalDate = request.fechaInicio?.let { java.time.LocalDate.parse(it) }
            val fechaFinLocalDate = request.fechaFin?.let { java.time.LocalDate.parse(it) }

            val filasActualizadas = transaction {
                Fertilizaciones.update({ Fertilizaciones.id eq id }) {
                    it[actividadId] = request.actividadId
                    it[productoId] = request.productoId
                    it[cultivoId] = request.cultivoId
                    it[aplica] = request.aplica
                    it[fechaInicio] = fechaInicioLocalDate
                    it[fechaFin] = fechaFinLocalDate
                    it[tipoProducto] = request.tipoProducto
                    it[numeroAlbaran] = request.numeroAlbaran
                    it[riquezaNpk] = request.riquezaNpk
                    it[dosis] = request.dosis
                    it[tipoFertilizacion] = request.tipoFertilizacion
                    it[observaciones] = request.observaciones
                }
            }

            if (filasActualizadas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK)
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val filasEliminadas = transaction {
                Fertilizaciones.deleteWhere { Fertilizaciones.id eq id }
            }

            if (filasEliminadas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.NoContent)
        }
    }

    // Endpoints anidados por actividad.
    // GET devuelve la fertilización asociada;
    // POST upsert para evitar duplicados al regresar al formulario.
    route("/api/actividades/{actividadId}/fertilizacion") {

        get {
            val actId = call.parameters["actividadId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val fertilizacion = transaction {
                Fertilizaciones.selectAll()
                    .where { Fertilizaciones.actividadId eq actId }
                    .orderBy(Fertilizaciones.id to SortOrder.DESC)
                    .firstOrNull()
                    ?.toFertilizacionResponse()
            }

            if (fertilizacion == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(fertilizacion)
        }

        post {
            val actId = call.parameters["actividadId"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<FertilizacionRequest>()
            val fechaInicioLocalDate = request.fechaInicio?.let { java.time.LocalDate.parse(it) }
            val fechaFinLocalDate = request.fechaFin?.let { java.time.LocalDate.parse(it) }

            val (fertId, esNueva) = transaction {
                val existente = Fertilizaciones.selectAll()
                    .where { Fertilizaciones.actividadId eq actId }
                    .orderBy(Fertilizaciones.id to SortOrder.DESC)
                    .firstOrNull()

                if (existente != null) {
                    val idExistente = existente[Fertilizaciones.id].value
                    Fertilizaciones.update({ Fertilizaciones.id eq idExistente }) {
                        it[productoId]        = request.productoId
                        it[cultivoId]         = request.cultivoId
                        it[aplica]            = request.aplica
                        it[fechaInicio]       = fechaInicioLocalDate
                        it[fechaFin]          = fechaFinLocalDate
                        it[tipoProducto]      = request.tipoProducto
                        it[numeroAlbaran]     = request.numeroAlbaran
                        it[riquezaNpk]        = request.riquezaNpk
                        it[dosis]             = request.dosis
                        it[tipoFertilizacion] = request.tipoFertilizacion
                        it[observaciones]     = request.observaciones
                    }
                    idExistente to false
                } else {
                    val nuevoId = Fertilizaciones.insertAndGetId {
                        it[Fertilizaciones.actividadId] = actId
                        it[productoId]                  = request.productoId
                        it[cultivoId]                   = request.cultivoId
                        it[aplica]                      = request.aplica
                        it[fechaInicio]                 = fechaInicioLocalDate
                        it[fechaFin]                    = fechaFinLocalDate
                        it[tipoProducto]                = request.tipoProducto
                        it[numeroAlbaran]               = request.numeroAlbaran
                        it[riquezaNpk]                  = request.riquezaNpk
                        it[dosis]                       = request.dosis
                        it[tipoFertilizacion]           = request.tipoFertilizacion
                        it[observaciones]               = request.observaciones
                    }.value
                    nuevoId to true
                }
            }

            val respuesta = transaction {
                Fertilizaciones.selectAll()
                    .where { Fertilizaciones.id eq fertId }
                    .single()
                    .toFertilizacionResponse()
            }

            call.respond(if (esNueva) HttpStatusCode.Created else HttpStatusCode.OK, respuesta)
        }
    }
}

