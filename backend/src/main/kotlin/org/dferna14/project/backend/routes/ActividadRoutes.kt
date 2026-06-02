package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.Actividades
import org.dferna14.project.backend.db.ActividadProductos
import org.dferna14.project.backend.db.Fertilizaciones
import org.dferna14.project.backend.db.Parcelas
import org.dferna14.project.backend.db.SemillasTratadas
import org.dferna14.project.backend.mapper.toActividadProductoResponse
import org.dferna14.project.backend.mapper.toActividadResponse
import org.dferna14.project.backend.mapper.toSemillaTratadaResponse
import org.dferna14.project.backend.model.ActividadProductoRequest
import org.dferna14.project.backend.model.ActividadProductoResponse
import org.dferna14.project.backend.model.ActividadRequest
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
        // LEFT JOIN  con parcelas
        get {
            val estadoParam = call.request.queryParameters["estado"]
            val actividades = transaction {
                val base = (Actividades leftJoin Parcelas).selectAll()
                val query = if (estadoParam != null) {
                    base.where { Actividades.estado eq estadoParam }
                } else {
                    base
                }
                query.map { it.toActividadResponse() }
            }
            call.respond(actividades)
        }

        // GET /api/actividades/pendientes
        // Devuelve solo actividades pendientes de validar (para desktop)
        get("pendientes") {
            val actividades = transaction {
                (Actividades leftJoin Parcelas).selectAll()
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
                (Actividades leftJoin Parcelas).selectAll()
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

                (Actividades leftJoin Parcelas).selectAll()
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
                    it[fechaInicio]= fechaInicioLocalDate
                    it[fechaFin] = fechaFinLocalDate
                    it[superficieTratada]= request.superficieTratada
                    it[problemaFitosanitario] = request.problemaFitosanitario
                    it[eficacia] = request.eficacia
                    it[observaciones] = request.observaciones
                    it[estado] = request.estado.name
                    if (request.equipoId != null) it[equipoId] = request.equipoId
                    if (request.aplicadorId != null) it[aplicadorId] = request.aplicadorId
                }
            }

            if (filasActualizadas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK)
        }

        // POST /api/actividades/{id}/enviar
        // Móvil: envía la actividad para validación (BORRADOR -> PENDIENTE_VALIDAR).
        // Si la actividad no tiene fechaFin todavía, se rellena automáticamente con
        // la fecha de hoy — el técnico puede modificarla después desde Desktop.
        post("{id}/enviar") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            val (filas, actividad) = transaction {
                val fechaFinActual = Actividades.selectAll()
                    .where { Actividades.id eq id }
                    .firstOrNull()
                    ?.get(Actividades.fechaFin)

                val filasActualizadas = Actividades.update({
                    (Actividades.id eq id) and (Actividades.estado eq EstadoActividad.BORRADOR.name)
                }) {
                    it[estado] = EstadoActividad.PENDIENTE_VALIDAR.name
                    if (fechaFinActual == null) {
                        it[fechaFin] = java.time.LocalDate.now()
                    }
                }

                val act = if (filasActualizadas == 0) null
                else (Actividades leftJoin Parcelas).selectAll()
                    .where { Actividades.id eq id }
                    .single()
                    .toActividadResponse()

                filasActualizadas to act
            }

            when {
                filas == 0 -> call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("message" to "La actividad no existe o no está en estado BORRADOR")
                )
                actividad == null -> call.respond(HttpStatusCode.NotFound)
                else -> call.respond(actividad)
            }
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
                else (Actividades leftJoin Parcelas).selectAll()
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
        // Borra en cascada los hijos  en misma transaccion antes de borrar la actividad
        // evitamos violaciones de FK
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val filasEliminadas = transaction {
                SemillasTratadas.deleteWhere { SemillasTratadas.actividadId eq id }
                ActividadProductos.deleteWhere { ActividadProductos.actividadId eq id }
                Fertilizaciones.deleteWhere { Fertilizaciones.actividadId eq id }
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

            // DELETE /api/actividades/{id}/productos/{actividadProductoId}
            // Borra UNA fila de actividad_producto, comprobando que pertenezca a la actividad indicada
            delete("{actividadProductoId}") {
                val actividadId = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val apId = call.parameters["actividadProductoId"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)

                val filasEliminadas = transaction {
                    ActividadProductos.deleteWhere {
                        (ActividadProductos.id eq apId) and
                                (ActividadProductos.actividadId eq actividadId)
                    }
                }

                if (filasEliminadas == 0) call.respond(HttpStatusCode.NotFound)
                else call.respond(HttpStatusCode.NoContent)
            }
        }

        // Semilla tratada de una actividad
        // Relación 1:1 lógica (una semilla por actividad). Si existe actualiza, sino insertamos.
        // Así evitamos duplicados que antes daban bugs.
        route("{id}/semilla") {

            get {
                val actividadId = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

                val semilla = transaction {
                    SemillasTratadas.selectAll()
                        .where { SemillasTratadas.actividadId eq actividadId }
                        .orderBy(SemillasTratadas.id to SortOrder.DESC)
                        .firstOrNull()
                        ?.toSemillaTratadaResponse()
                }

                if (semilla == null) call.respond(HttpStatusCode.NotFound)
                else call.respond(semilla)
            }

            post {
                val actividadId = call.parameters["id"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

                val request = call.receive<SemillaTratadaRequest>()
                val fechaSiembraLocalDate = request.fechaSiembra?.let { java.time.LocalDate.parse(it) }

                val (semillaId, esNueva) = transaction {
                    val existente = SemillasTratadas.selectAll()
                        .where { SemillasTratadas.actividadId eq actividadId }
                        .orderBy(SemillasTratadas.id to SortOrder.DESC)
                        .firstOrNull()

                    if (existente != null) {
                        val idExistente = existente[SemillasTratadas.id].value
                        SemillasTratadas.update({ SemillasTratadas.id eq idExistente }) {
                            it[parcelaId]         = request.parcelaId
                            it[aplica]            = request.aplica
                            it[fechaSiembra]      = fechaSiembraLocalDate
                            it[superficieHa]      = request.superficieHa
                            it[cantidadSemillaKg] = request.cantidadSemillaKg
                            it[productoId]        = request.productoId
                            it[variedadSemilla]   = request.variedadSemilla
                            it[cultivoId]         = request.cultivoId
                        }
                        idExistente to false
                    } else {
                        val nuevoId = SemillasTratadas.insertAndGetId {
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
                        nuevoId to true
                    }
                }

                call.respond(
                    if (esNueva) HttpStatusCode.Created else HttpStatusCode.OK,
                    SemillaTratadaResponse(
                        id                = semillaId,
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

