package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.Actividades
import org.dferna14.project.backend.db.DatosAgronomicos
import org.dferna14.project.backend.db.Parcelas
import org.dferna14.project.backend.db.ReferenciaSigpac
import org.dferna14.project.backend.model.DatosAgronomicosResponse
import org.dferna14.project.backend.model.ParcelaCompletaResponse
import org.dferna14.project.backend.model.ParcelaRequest
import org.dferna14.project.backend.model.ParcelaResponse
import org.dferna14.project.backend.model.ReferenciaSigpacResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

fun Route.parcelaRoutes() {

    route("/api/parcelas") {

        // GET /api/parcelas
        get {
            val parcelas = transaction {
                Parcelas.selectAll().map {
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
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val parcela = transaction {
                Parcelas.selectAll()
                    .where { Parcelas.id eq id }
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
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val parcelaCompleta = transaction {
                val parcela = Parcelas.selectAll()
                    .where { Parcelas.id eq id }
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
            val request = call.receive<ParcelaRequest>()

            val creada = transaction {
                val nuevaId = Parcelas.insertAndGetId {
                    it[explotacionId] = request.explotacionId
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
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<ParcelaRequest>()

            val filasActualizadas = transaction {
                Parcelas.update({ Parcelas.id eq id }) {
                    it[explotacionId] = request.explotacionId
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
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val tieneActividades = transaction {
                !Actividades.selectAll()
                    .where { Actividades.parcelaId eq id }
                    .empty()
            }

            if (tieneActividades) {
                return@delete call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("message" to "No se puede eliminar la parcela porque tiene actividades asociadas")
                )
            }

            val eliminadas = transaction { Parcelas.deleteWhere { Parcelas.id eq id } }
            if (eliminadas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.NoContent)
        }
    }
}