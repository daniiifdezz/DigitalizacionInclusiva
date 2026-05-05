package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.Actividades
import org.dferna14.project.backend.db.ActividadProductos
import org.dferna14.project.backend.db.SemillasTratadas
import org.dferna14.project.backend.model.ActividadProductoRequest
import org.dferna14.project.backend.model.ActividadProductoResponse
import org.dferna14.project.backend.model.ActividadRequest
import org.dferna14.project.backend.model.ActividadResponse
import org.dferna14.project.backend.model.EstadoActividad
import org.dferna14.project.backend.model.SemillaTratadaRequest
import org.dferna14.project.backend.model.SemillaTratadaResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

fun Route.actividadRoutes() {

    route("/api/actividades") {

        // GET /api/actividades
        // Filtro opcional: ?estado=BORRADOR|PENDIENTE_VALIDAR|VALIDADA
        get {
            val estadoParam = call.request.queryParameters["estado"]
            val actividades = transaction {
                val query = if (estadoParam != null) {
                    Actividades.selectAll().where { Actividades.estado eq estadoParam }
                } else {
                    Actividades.selectAll()
                }
                query.map { it.toActividadResponse() }
            }
            call.respond(actividades)
        }

        // GET /api/actividades/pendientes
        // Devuelve solo actividades pendientes de validar (para desktop)
        get("pendientes") {
            val actividades = transaction {
                Actividades.selectAll()
                    .where { Actividades.estado eq EstadoActividad.PENDIENTE_VALIDAR.name }
                    .map { it.toActividadResponse() }
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
            val fechaInicioLocalDate = java.time.LocalDate.parse(request.fechaInicio)
            val fechaFinLocalDate = request.fechaFin?.let { java.time.LocalDate.parse(it) }

            val creada = transaction {
                val nuevaId = Actividades.insertAndGetId {
                    it[parcelaId] = request.parcelaId
                    it[equipoId] = request.equipoId
                    it[aplicadorId] = request.aplicadorId
                    it[Actividades.fechaInicio] = fechaInicioLocalDate
                    it[fechaFin] = fechaFinLocalDate
                    it[superficieTratada] = request.superficieTratada
                    it[problemaFitosanitario] = request.problemaFitosanitario
                    it[eficacia] = request.eficacia
                    it[observaciones] = request.observaciones
                    it[estado] = request.estado.name
                }.value

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
            val fechaInicioLocalDate = java.time.LocalDate.parse(request.fechaInicio)
            val fechaFinLocalDate = request.fechaFin?.let { java.time.LocalDate.parse(it) }

            val filasActualizadas = transaction {
                Actividades.update({ Actividades.id eq id }) {
                    it[parcelaId]= request.parcelaId
                    it[equipoId]= request.equipoId
                    it[aplicadorId]= request.aplicadorId
                    it[fechaInicio]= fechaInicioLocalDate
                    it[fechaFin] = fechaFinLocalDate
                    it[superficieTratada]= request.superficieTratada
                    it[problemaFitosanitario] = request.problemaFitosanitario
                    it[eficacia] = request.eficacia
                    it[observaciones] = request.observaciones
                    it[estado] = request.estado.name
                }
            }

            if (filasActualizadas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK)
        }

        // POST /api/actividades/{id}/enviar
        // Móvil: envía la actividad para validación (BORRADOR -> PENDIENTE_VALIDAR)
        post("{id}/enviar") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            val actividad = transaction {
                val filasActualizadas = Actividades.update({ Actividades.id eq id }) {
                    it[estado] = EstadoActividad.PENDIENTE_VALIDAR.name
                }
                if (filasActualizadas == 0) null
                else Actividades.selectAll()
                    .where { Actividades.id eq id }
                    .single()
                    .toActividadResponse()
            }

            if (actividad == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(actividad)
        }

        // POST /api/actividades/{id}/validar
        // Desktop: marca como validada (PENDIENTE_VALIDAR -> VALIDADA)
        post("{id}/validar") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            val actividad = transaction {
                val filasActualizadas = Actividades.update({ Actividades.id eq id }) {
                    it[estado] = EstadoActividad.VALIDADA.name
                }
                if (filasActualizadas == 0) null
                else Actividades.selectAll()
                    .where { Actividades.id eq id }
                    .single()
                    .toActividadResponse()
            }

            if (actividad == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(actividad)
        }

        // POST /api/actividades/{id}/devolver
        // Desktop: devuelve al agricultor (PENDIENTE_VALIDAR -> BORRADOR)
        post("{id}/devolver") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            val filasActualizadas = transaction {
                Actividades.update({ Actividades.id eq id }) {
                    it[estado] = EstadoActividad.BORRADOR.name
                }
            }

            if (filasActualizadas == 0) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.OK)
            }
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
                val fechaSiembraLocalDate = request.fechaSiembra?.let { java.time.LocalDate.parse(it) }

                val nuevoId = transaction {
                    SemillasTratadas.insertAndGetId {
                        it[SemillasTratadas.actividadId]       = actividadId
                        it[SemillasTratadas.parcelaId]         = request.parcelaId
                        it[SemillasTratadas.aplica]            = request.aplica
                        it[SemillasTratadas.fechaSiembra]      = fechaSiembraLocalDate
                        it[SemillasTratadas.superficieHa]      = request.superficieHa
                        it[SemillasTratadas.cantidadSemillaKg] = request.cantidadSemillaKg
                        it[SemillasTratadas.productoId]        = request.productoId
                        it[SemillasTratadas.variedadSemilla]   = request.variedadSemilla
                        it[SemillasTratadas.cultivoId]         = request.cultivoId
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
                        productoId        = request.productoId,
                        variedadSemilla   = request.variedadSemilla,
                        cultivoId         = request.cultivoId
                    )
                )
            }
        }
    }
}

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
    observaciones         = this[Actividades.observaciones],
    estado                = runCatching {
        EstadoActividad.valueOf(this[Actividades.estado] ?: "BORRADOR")
    }.getOrDefault(EstadoActividad.BORRADOR)
)

private fun ResultRow.toSemillaResponse() = SemillaTratadaResponse(
    id                = this[SemillasTratadas.id].value,
    actividadId       = this[SemillasTratadas.actividadId],
    parcelaId         = this[SemillasTratadas.parcelaId],
    aplica            = this[SemillasTratadas.aplica],
    fechaSiembra      = this[SemillasTratadas.fechaSiembra]?.toString(),
    superficieHa      = this[SemillasTratadas.superficieHa],
    cantidadSemillaKg = this[SemillasTratadas.cantidadSemillaKg],
    productoId        = this[SemillasTratadas.productoId],
    variedadSemilla   = this[SemillasTratadas.variedadSemilla],
    cultivoId         = this[SemillasTratadas.cultivoId]
)
