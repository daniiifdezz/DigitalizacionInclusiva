package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.toKotlinLocalDate
import org.dferna14.project.backend.db.Actividades
import org.dferna14.project.backend.db.ActividadProductos
import org.dferna14.project.backend.db.SemillasTratadas
import org.dferna14.project.backend.model.ActividadProductoRequest
import org.dferna14.project.backend.model.ActividadProductoResponse
import org.dferna14.project.backend.model.ActividadRequest
import org.dferna14.project.backend.model.ActividadResponse
import org.dferna14.project.backend.model.SemillaTratadaRequest
import org.dferna14.project.backend.model.SemillaTratadaResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

/**
 * Endpoints REST para el recurso Actividad.
 *
 * GET    /api/actividades              → lista todas las actividades
 * GET    /api/actividades/{id}         → detalle de una actividad
 * POST   /api/actividades              → crear nueva actividad
 * PUT    /api/actividades/{id}         → actualizar actividad existente
 * DELETE /api/actividades/{id}         → eliminar actividad
 *
 * GET    /api/actividades/{id}/productos  → productos de una actividad
 * POST   /api/actividades/{id}/productos  → añadir producto a actividad
 *
 * GET    /api/actividades/{id}/semilla    → semilla tratada de una actividad
 * POST   /api/actividades/{id}/semilla    → registrar semilla tratada
 */
fun Route.actividadRoutes() {

    route("/api/actividades") {

        // GET /api/actividades
        get {
            val actividades = transaction {
                Actividades.selectAll().map { it.toActividadResponse() }
            }
            call.respond(actividades)
        }

        // GET /api/actividades/{id}
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val actividad = transaction {
                Actividades.selectAll()
                    .where { Actividades.id eq id }
                    .singleOrNull()
                    ?.toActividadResponse()
            }

            if (actividad == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(actividad)
        }

        // POST /api/actividades
        post {
            val request = call.receive<ActividadRequest>()

            val nuevaId = transaction {
                Actividades.insertAndGetId {
                    it[parcelaId]             = request.parcelaId
                    it[equipoId]              = request.equipoId
                    it[aplicadorId]           = request.aplicadorId
                    it[fechaInicio]           = LocalDate.parse(request.fechaInicio)
                        .toKotlinLocalDate()
                    it[fechaFin]              = request.fechaFin?.let { f ->
                        LocalDate.parse(f).toKotlinLocalDate()
                    }
                    it[superficieTratada]     = request.superficieTratada
                    it[problemaFitosanitario] = request.problemaFitosanitario
                    it[eficacia]              = request.eficacia
                    it[observaciones]         = request.observaciones
                }.value
            }

            val creada = transaction {
                Actividades.selectAll()
                    .where { Actividades.id eq nuevaId }
                    .single()
                    .toActividadResponse()
            }

            call.respond(HttpStatusCode.Created, creada)
        }

        // PUT /api/actividades/{id}
        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<ActividadRequest>()

            val filasActualizadas = transaction {
                Actividades.update({ Actividades.id eq id }) {
                    it[parcelaId]             = request.parcelaId
                    it[equipoId]              = request.equipoId
                    it[aplicadorId]           = request.aplicadorId
                    it[fechaInicio]           = LocalDate.parse(request.fechaInicio)
                        .toKotlinLocalDate()
                    it[fechaFin]              = request.fechaFin?.let { f ->
                        LocalDate.parse(f).toKotlinLocalDate()
                    }
                    it[superficieTratada]     = request.superficieTratada
                    it[problemaFitosanitario] = request.problemaFitosanitario
                    it[eficacia]              = request.eficacia
                    it[observaciones]         = request.observaciones
                }
            }

            if (filasActualizadas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK)
        }

        // DELETE /api/actividades/{id}
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val filasEliminadas = transaction {
                Actividades.deleteWhere { Actividades.id eq id }
            }

            if (filasEliminadas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.NoContent)
        }

        // Productos de una actividad
        route("{id}/productos") {

            get {
                val actividadId = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

                val productos = transaction {
                    ActividadProductos.selectAll()
                        .where { ActividadProductos.actividadId eq actividadId }
                        .map {
                            ActividadProductoResponse(
                                id          = it[ActividadProductos.id].value,
                                actividadId = it[ActividadProductos.actividadId],
                                productoId  = it[ActividadProductos.productoId],
                                dosis       = it[ActividadProductos.dosis]
                            )
                        }
                }
                call.respond(productos)
            }

            post {
                val actividadId = call.parameters["id"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

                val request = call.receive<ActividadProductoRequest>()

                val nuevoId = transaction {
                    ActividadProductos.insertAndGetId {
                        it[ActividadProductos.actividadId] = actividadId
                        it[ActividadProductos.productoId]  = request.productoId
                        it[ActividadProductos.dosis]       = request.dosis
                    }.value
                }

                call.respond(
                    HttpStatusCode.Created,
                    ActividadProductoResponse(
                        id          = nuevoId,
                        actividadId = actividadId,
                        productoId  = request.productoId,
                        dosis       = request.dosis
                    )
                )
            }
        }

        // Semilla tratada de una actividad
        route("{id}/semilla") {

            get {
                val actividadId = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

                val semilla = transaction {
                    SemillasTratadas.selectAll()
                        .where { SemillasTratadas.actividadId eq actividadId }
                        .singleOrNull()
                        ?.toSemillaResponse()
                }

                if (semilla == null) call.respond(HttpStatusCode.NotFound)
                else call.respond(semilla)
            }

            post {
                val actividadId = call.parameters["id"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

                val request = call.receive<SemillaTratadaRequest>()

                val nuevoId = transaction {
                    SemillasTratadas.insertAndGetId {
                        it[SemillasTratadas.actividadId]       = actividadId
                        it[SemillasTratadas.parcelaId]         = request.parcelaId
                        it[SemillasTratadas.aplica]            = request.aplica
                        it[SemillasTratadas.fechaSiembra]      = request.fechaSiembra?.let { f ->
                            LocalDate.parse(f).toKotlinLocalDate()
                        }
                        it[SemillasTratadas.superficieHa]      = request.superficieHa
                        it[SemillasTratadas.cantidadSemillaKg] = request.cantidadSemillaKg
                        it[SemillasTratadas.productoId]        = request.productoId
                    }.value
                }

                call.respond(
                    HttpStatusCode.Created,
                    SemillaTratadaResponse(
                        id                = nuevoId,
                        actividadId       = actividadId,
                        parcelaId         = request.parcelaId,
                        aplica            = request.aplica,
                        fechaSiembra      = request.fechaSiembra,
                        superficieHa      = request.superficieHa,
                        cantidadSemillaKg = request.cantidadSemillaKg,
                        productoId        = request.productoId
                    )
                )
            }
        }
    }
}

// Extensiones de mapeo ResultRow → DTO

private fun ResultRow.toActividadResponse() = ActividadResponse(
    id                    = this[Actividades.id].value,
    parcelaId             = this[Actividades.parcelaId],
    equipoId              = this[Actividades.equipoId],
    aplicadorId           = this[Actividades.aplicadorId],
    fechaInicio           = this[Actividades.fechaInicio]?.toString() ?: "",
    fechaFin              = this[Actividades.fechaFin]?.toString(),
    superficieTratada     = this[Actividades.superficieTratada],
    problemaFitosanitario = this[Actividades.problemaFitosanitario],
    eficacia              = this[Actividades.eficacia],
    observaciones         = this[Actividades.observaciones]
)

private fun ResultRow.toSemillaResponse() = SemillaTratadaResponse(
    id                = this[SemillasTratadas.id].value,
    actividadId       = this[SemillasTratadas.actividadId],
    parcelaId         = this[SemillasTratadas.parcelaId],
    aplica            = this[SemillasTratadas.aplica],
    fechaSiembra      = this[SemillasTratadas.fechaSiembra]?.toString(),
    superficieHa      = this[SemillasTratadas.superficieHa],
    cantidadSemillaKg = this[SemillasTratadas.cantidadSemillaKg],
    productoId        = this[SemillasTratadas.productoId]
)