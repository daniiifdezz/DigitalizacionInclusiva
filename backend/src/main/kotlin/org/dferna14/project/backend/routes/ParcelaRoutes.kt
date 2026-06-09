package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.plugins.tenantId
import org.dferna14.project.backend.db.ActividadProductos
import org.dferna14.project.backend.db.Actividades
import org.dferna14.project.backend.db.DatosAgronomicos
import org.dferna14.project.backend.db.DatosMedioambientales
import org.dferna14.project.backend.db.FertilizacionParcelas
import org.dferna14.project.backend.db.Fertilizaciones
import org.dferna14.project.backend.db.Parcelas
import org.dferna14.project.backend.db.ReferenciaSigpac
import org.dferna14.project.backend.db.SemillasTratadas
import org.dferna14.project.backend.model.DatosAgronomicosResponse
import org.dferna14.project.backend.model.DependenciasParcelaDto
import org.dferna14.project.backend.model.ParcelaCompletaResponse
import org.dferna14.project.backend.model.ParcelaRequest
import org.dferna14.project.backend.model.ParcelaResponse
import org.dferna14.project.backend.model.ReferenciaSigpacResponse
import org.dferna14.project.backend.plugins.currentUserId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

fun Route.parcelaRoutes() {

    route("/api/parcelas") {

        // GET /api/parcelas
        get {
            val tenantId = call.tenantId()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val userId = call.currentUserId()
            val rol = call.principal<JWTPrincipal>()?.payload?.getClaim("rol")?.asString()
            val parcelas = transaction {
                val condicion = if (rol == "TECNICO") {
                    Parcelas.explotacionId eq tenantId
                } else {
                    (Parcelas.explotacionId eq tenantId) and (Parcelas.creadorId eq userId)
                }
                Parcelas.selectAll().where(condicion).map {
                    ParcelaResponse(
                        id                   = it[Parcelas.id].value,
                        explotacionId        = it[Parcelas.explotacionId],
                        orden                = it[Parcelas.orden],
                        alias                = it[Parcelas.alias],
                        sistemaAsesoramiento = it[Parcelas.sistemaAsesoramiento],
                        zonaNitratos         = it[Parcelas.zonaNitratos]
                    )
                }
            }
            call.respond(parcelas)
        }

        // GET /api/parcelas/{id}
        get("{id}") {
            val tenantId = call.tenantId()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val userId = call.currentUserId()
            val rol = call.principal<JWTPrincipal>()?.payload?.getClaim("rol")?.asString()
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val parcela = transaction {
                val condicion = if (rol == "TECNICO") {
                    (Parcelas.id eq id) and (Parcelas.explotacionId eq tenantId)
                } else {
                    (Parcelas.id eq id) and (Parcelas.explotacionId eq tenantId) and (Parcelas.creadorId eq userId)
                }
                Parcelas.selectAll()
                    .where(condicion)
                    .singleOrNull()
                    ?.let {
                        ParcelaResponse(
                            id                   = it[Parcelas.id].value,
                            explotacionId        = it[Parcelas.explotacionId],
                            orden                = it[Parcelas.orden],
                            alias                = it[Parcelas.alias],
                            sistemaAsesoramiento = it[Parcelas.sistemaAsesoramiento],
                            zonaNitratos         = it[Parcelas.zonaNitratos]
                        )
                    }
            }

            if (parcela == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(parcela)
            }
        }

        // GET /api/parcelas/{id}/completa
        // Devuelve parcela + sub-objetos satélite (referenciasigpac, datosagronomicos).
        // Si SIGPAC o agronómicos no existen para esta parcela, su sub-objeto es null.
        get("{id}/completa") {
            val tenantId = call.tenantId()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val userId = call.currentUserId()
            val rol = call.principal<JWTPrincipal>()?.payload?.getClaim("rol")?.asString()
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val parcelaCompleta = transaction {
                val condicion = if (rol == "TECNICO") {
                    (Parcelas.id eq id) and (Parcelas.explotacionId eq tenantId)
                } else {
                    (Parcelas.id eq id) and (Parcelas.explotacionId eq tenantId) and (Parcelas.creadorId eq userId)
                }
                val parcela = Parcelas.selectAll()
                    .where(condicion)
                    .singleOrNull()
                    ?: return@transaction null

                val refSigpac = ReferenciaSigpac.selectAll()
                    .where { ReferenciaSigpac.parcelaId eq id }
                    .singleOrNull()

                val datosAgro = DatosAgronomicos.selectAll()
                    .where { DatosAgronomicos.parcelaId eq id }
                    .singleOrNull()

                ParcelaCompletaResponse(
                    parcela = ParcelaResponse(
                        id                   = parcela[Parcelas.id].value,
                        explotacionId        = parcela[Parcelas.explotacionId],
                        orden                = parcela[Parcelas.orden],
                        alias                = parcela[Parcelas.alias],
                        sistemaAsesoramiento = parcela[Parcelas.sistemaAsesoramiento],
                        zonaNitratos         = parcela[Parcelas.zonaNitratos]
                    ),
                    referenciaSigpac = refSigpac?.let {
                        ReferenciaSigpacResponse(
                            id               = it[ReferenciaSigpac.id].value,
                            parcelaId        = it[ReferenciaSigpac.parcelaId],
                            provincia        = it[ReferenciaSigpac.provincia],
                            terminoMunicipal = it[ReferenciaSigpac.terminoMunicipal],
                            codigoAgregado   = it[ReferenciaSigpac.codigoAgregado],
                            zona             = it[ReferenciaSigpac.zona],
                            numeroPoligono   = it[ReferenciaSigpac.numeroPoligono],
                            numeroParcela    = it[ReferenciaSigpac.numeroParcela],
                            numeroRecinto    = it[ReferenciaSigpac.numeroRecinto],
                            usoSigpac        = it[ReferenciaSigpac.usoSigpac],
                            superficieHa     = it[ReferenciaSigpac.superficieHa]
                        )
                    },
                    datosAgronomicos = datosAgro?.let {
                        DatosAgronomicosResponse(
                            id                 = it[DatosAgronomicos.id].value,
                            parcelaId          = it[DatosAgronomicos.parcelaId],
                            especieVariedad    = it[DatosAgronomicos.especieVariedad],
                            ecoregimenPractica = it[DatosAgronomicos.ecoregimenPractica],
                            secanoRegadio      = it[DatosAgronomicos.secanoRegadio],
                            cultivoId          = it[DatosAgronomicos.cultivoId],
                            fechaInicio        = it[DatosAgronomicos.fechaInicio]?.toString(),
                            fechaFin           = it[DatosAgronomicos.fechaFin]?.toString(),
                            aireLibreProtegido = it[DatosAgronomicos.aireLibreProtegido]
                        )
                    }
                )
            }

            if (parcelaCompleta == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(parcelaCompleta)
            }
        }

        // POST /api/parcelas - Crear nueva parcela
        post {
            val tenantId = call.tenantId()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val userId = call.currentUserId()
            val request = call.receive<ParcelaRequest>()

            val creada = transaction {
                val nuevaId = Parcelas.insertAndGetId {
                    it[explotacionId] = tenantId
                    it[creadorId]     = userId
                    it[orden] = request.orden
                    it[alias] = request.alias
                    it[sistemaAsesoramiento] = request.sistemaAsesoramiento
                    it[zonaNitratos] = request.zonaNitratos
                }.value

                Parcelas.selectAll()
                    .where { Parcelas.id eq nuevaId }
                    .single()
                    .let {
                        ParcelaResponse(
                            id = it[Parcelas.id].value,
                            explotacionId = it[Parcelas.explotacionId],
                            orden = it[Parcelas.orden],
                            alias = it[Parcelas.alias],
                            sistemaAsesoramiento = it[Parcelas.sistemaAsesoramiento],
                            zonaNitratos = it[Parcelas.zonaNitratos]
                        )
                    }
            }

            call.respond(HttpStatusCode.Created, creada)
        }

        // PUT /api/parcelas/{id} - Actualizar parcela
        put("{id}") {
            val tenantId = call.tenantId()
                ?: return@put call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<ParcelaRequest>()

            val filasActualizadas = transaction {
                Parcelas.update({ (Parcelas.id eq id) and (Parcelas.explotacionId eq tenantId) }) {
                    it[explotacionId] = tenantId
                    it[orden] = request.orden
                    it[alias] = request.alias
                    it[sistemaAsesoramiento] = request.sistemaAsesoramiento
                    it[zonaNitratos] = request.zonaNitratos
                }
            }

            if (filasActualizadas == 0) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.OK)
            }
        }

        // DELETE /api/parcelas/{id} - Eliminar parcela
        // Bloqueo con 409 en parcela referenciada en cualquier tabla hija
        delete("{id}") {
            val tenantId = call.tenantId()
                ?: return@delete call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token sin explotación"))
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val esDelTenant = transaction {
                Parcelas.selectAll().where { (Parcelas.id eq id) and (Parcelas.explotacionId eq tenantId) }.any()
            }
            if (!esDelTenant) return@delete call.respond(HttpStatusCode.NotFound)

            data class Refs(
                val actividades: Boolean,
                val semillas: Boolean,
                val sigpac: Boolean,
                val agronomicos: Boolean
            )

            val refs = transaction {
                Refs(
                    actividades = !Actividades.selectAll()
                        .where { Actividades.parcelaId eq id }
                        .empty(),
                    semillas = !SemillasTratadas.selectAll()
                        .where { SemillasTratadas.parcelaId eq id }
                        .empty(),
                    sigpac = !ReferenciaSigpac.selectAll()
                        .where { ReferenciaSigpac.parcelaId eq id }
                        .empty(),
                    agronomicos = !DatosAgronomicos.selectAll()
                        .where { DatosAgronomicos.parcelaId eq id }
                        .empty()
                )
            }

            if (refs.actividades || refs.semillas || refs.sigpac || refs.agronomicos) {
                val detalle = buildList {
                    if (refs.actividades) add("actividades")
                    if (refs.semillas)    add("semillas tratadas")
                    if (refs.sigpac)      add("referencia SIGPAC")
                    if (refs.agronomicos) add("datos agronómicos")
                }.joinToString(", ")
                return@delete call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("message" to "No se puede eliminar la parcela porque tiene datos asociados: $detalle")
                )
            }

            val eliminadas = transaction { Parcelas.deleteWhere { Parcelas.id eq id } }
            if (eliminadas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.NoContent)
        }

        // GET /api/parcelas/{id}/dependencias - Conteo de registros hijos
        // Lo usa el Desktop para mostrar un diálogo detallado antes del borrado en cascada.
        // NOTA: Fertilizaciones NO tiene parcela_id; se relaciona con la parcela a través
        // de las actividades de la parcela (Fertilizaciones.actividad_id). Por eso el conteo
        // de fertilizaciones se calcula sobre las actividades de la parcela.
        get("{id}/dependencias") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val dependencias = transaction {
                val actividadIds = Actividades.selectAll()
                    .where { Actividades.parcelaId eq id }
                    .map { it[Actividades.id].value }

                val fertilizaciones = if (actividadIds.isEmpty()) 0
                    else Fertilizaciones.selectAll()
                        .where { Fertilizaciones.actividadId inList actividadIds }
                        .count().toInt()

                DependenciasParcelaDto(
                    actividades = actividadIds.size,
                    semillas = SemillasTratadas.selectAll()
                        .where { SemillasTratadas.parcelaId eq id }.count().toInt(),
                    fertilizaciones = fertilizaciones,
                    referenciaSigpac = if (ReferenciaSigpac.selectAll()
                        .where { ReferenciaSigpac.parcelaId eq id }.empty()) 0 else 1,
                    datosAgronomicos = if (DatosAgronomicos.selectAll()
                        .where { DatosAgronomicos.parcelaId eq id }.empty()) 0 else 1
                )
            }
            call.respond(dependencias)
        }

        // DELETE /api/parcelas/{id}/cascada - Borrado en cascada (solo Desktop/técnico)
        // Elimina la parcela y TODOS sus datos hijos en una transacción atómica.
        // Orden: primero los nietos (hijos de actividad y pivote fertilización-parcela),
        // luego actividades, luego datos satélite de la parcela y finalmente la parcela.
        delete("{id}/cascada") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            transaction {
                // Actividades de la parcela y fertilizaciones colgando de esas actividades.
                val actividadIds = Actividades.selectAll()
                    .where { Actividades.parcelaId eq id }
                    .map { it[Actividades.id].value }

                val fertilizacionIds = if (actividadIds.isEmpty()) emptyList()
                    else Fertilizaciones.selectAll()
                        .where { Fertilizaciones.actividadId inList actividadIds }
                        .map { it[Fertilizaciones.id].value }

                // Hijos de las actividades.
                if (actividadIds.isNotEmpty()) {
                    ActividadProductos.deleteWhere { ActividadProductos.actividadId inList actividadIds }
                    SemillasTratadas.deleteWhere { SemillasTratadas.actividadId inList actividadIds }
                }

                // Pivote fertilización-parcela: tanto por las fertilizaciones a borrar
                // como por la propia parcela (relación N:M).
                if (fertilizacionIds.isNotEmpty())
                    FertilizacionParcelas.deleteWhere { FertilizacionParcelas.fertilizacionId inList fertilizacionIds }
                FertilizacionParcelas.deleteWhere { FertilizacionParcelas.parcelaId eq id }

                // Fertilizaciones de las actividades de la parcela.
                if (fertilizacionIds.isNotEmpty())
                    Fertilizaciones.deleteWhere { Fertilizaciones.id inList fertilizacionIds }

                // Ahora las actividades.
                Actividades.deleteWhere { Actividades.parcelaId eq id }

                // Semillas asociadas directamente a la parcela (por si quedara alguna).
                SemillasTratadas.deleteWhere { SemillasTratadas.parcelaId eq id }

                // Datos satélite de la parcela.
                ReferenciaSigpac.deleteWhere { ReferenciaSigpac.parcelaId eq id }
                DatosAgronomicos.deleteWhere { DatosAgronomicos.parcelaId eq id }
                DatosMedioambientales.deleteWhere { DatosMedioambientales.parcelaId eq id }

                // Finalmente la parcela.
                Parcelas.deleteWhere { Parcelas.id eq id }
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}