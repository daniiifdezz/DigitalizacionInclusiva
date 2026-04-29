package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.DatosAgronomicos
import org.dferna14.project.backend.db.Parcelas
import org.dferna14.project.backend.db.ReferenciaSigpac
import org.dferna14.project.backend.model.ParcelaCompletaResponse
import org.dferna14.project.backend.model.ParcelaRequest
import org.dferna14.project.backend.model.ParcelaResponse
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
        // Devuelve parcela con todos los datos de tablas satélite
        get("{id}/completa") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val parcelaCompleta = transaction {
                // Datos de parcela
                val parcela = Parcelas.selectAll()
                    .where { Parcelas.id eq id }
                    .singleOrNull()
                    ?: return@transaction null

                // LEFT JOIN referenciasigpac
                val refSigpac = ReferenciaSigpac.selectAll()
                    .where { ReferenciaSigpac.parcelaId eq id }
                    .singleOrNull()

                // LEFT JOIN datosagronomicos
                val datosAgro = DatosAgronomicos.selectAll()
                    .where { DatosAgronomicos.parcelaId eq id }
                    .singleOrNull()

                // Construir respuesta combinada
                ParcelaCompletaResponse(
                    id                   = parcela[Parcelas.id].value,
                    explotacionId        = parcela[Parcelas.explotacionId],
                    orden                = parcela[Parcelas.orden],
                    alias                = parcela[Parcelas.alias],
                    zonaNitratos         = parcela[Parcelas.zonaNitratos],
                    sistemaAsesoramiento = parcela[Parcelas.sistemaAsesoramiento],

                    // SIGPAC
                    provincia           = refSigpac?.get(ReferenciaSigpac.provincia),
                    terminoMunicipal    = refSigpac?.get(ReferenciaSigpac.terminoMunicipal),
                    codigoAgregado     = refSigpac?.get(ReferenciaSigpac.codigoAgregado),
                    zona              = refSigpac?.get(ReferenciaSigpac.zona),
                    numeroPoligono     = refSigpac?.get(ReferenciaSigpac.numeroPoligono),
                    numeroParcela     = refSigpac?.get(ReferenciaSigpac.numeroParcela),
                    numeroRecinto     = refSigpac?.get(ReferenciaSigpac.numeroRecinto),
                    usoSigpac          = refSigpac?.get(ReferenciaSigpac.usoSigpac),
                    superficieHa        = refSigpac?.get(ReferenciaSigpac.superficieHa),

                    // Agronómicos
                    especieVariedad    = datosAgro?.get(DatosAgronomicos.especieVariedad),
                    ecoregimenPractica = datosAgro?.get(DatosAgronomicos.ecoregimenPractica),
                    secanoRegadio    = datosAgro?.get(DatosAgronomicos.secanoRegadio),
                    cultivo          = datosAgro?.get(DatosAgronomicos.cultivo),
                    fechaInicio      = datosAgro?.get(DatosAgronomicos.fechaInicio)?.toString(),
                    fechaFin         = datosAgro?.get(DatosAgronomicos.fechaFin)?.toString(),
                    aireLibreProtegido = datosAgro?.get(DatosAgronomicos.aireLibreProtegido)
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

            val nuevaId = transaction {
                Parcelas.insertAndGetId {
                    it[explotacionId] = request.explotacionId
                    it[orden] = request.orden
                    it[alias] = request.alias
                    it[sistemaAsesoramiento] = request.sistemaAsesoramiento
                    it[zonaNitratos] = request.zonaNitratos
                }.value
            }

            val creada = transaction {
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

            val filasEliminadas = transaction {
                Parcelas.deleteWhere { Parcelas.id eq id }
            }

            if (filasEliminadas == 0) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}