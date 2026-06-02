package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.DatosAgronomicos
import org.dferna14.project.backend.db.Parcelas
import org.dferna14.project.backend.db.ReferenciaSigpac
import org.dferna14.project.backend.mapper.toDatosAgronomicosResponse
import org.dferna14.project.backend.mapper.toReferenciaSigpacResponse
import org.dferna14.project.backend.model.DatosAgronomicosRequest
import org.dferna14.project.backend.model.ReferenciaSigpacRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Rutas anidadas bajo /api/parcelas/{id}/sigpac y /agronomico.
 * POST = crear si no existe, error si ya existe.
 * PUT  = actualizar si existe, error 404 si no existe.
 */
fun Route.sigpacRoutes() {

    route("/api/parcelas/{id}") {

        // POST /api/parcelas/{id}/sigpac
        post("sigpac") {
            val parcelaId = call.parameters["id"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            if (!parcelaExiste(parcelaId)) {
                return@post call.respond(HttpStatusCode.NotFound, mapOf("message" to "Parcela no encontrada"))
            }

            val request = call.receive<ReferenciaSigpacRequest>()

            val creada = transaction {
                val yaExiste = !ReferenciaSigpac.selectAll()
                    .where { ReferenciaSigpac.parcelaId eq parcelaId }
                    .empty()
                if (yaExiste) return@transaction null

                val nuevoId = ReferenciaSigpac.insertAndGetId {
                    it[ReferenciaSigpac.parcelaId] = parcelaId
                    it[provincia]        = request.provincia
                    it[terminoMunicipal] = request.terminoMunicipal
                    it[codigoAgregado]   = request.codigoAgregado
                    it[zona]             = request.zona
                    it[numeroPoligono]   = request.numeroPoligono
                    it[numeroParcela]    = request.numeroParcela
                    it[numeroRecinto]    = request.numeroRecinto
                    it[usoSigpac]        = request.usoSigpac
                    it[superficieHa]     = request.superficieHa
                }.value

                ReferenciaSigpac.selectAll()
                    .where { ReferenciaSigpac.id eq nuevoId }
                    .single()
                    .toReferenciaSigpacResponse()
            }

            if (creada == null) {
                call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("message" to "Ya existe una referencia SIGPAC para esta parcela. Usa PUT para actualizar.")
                )
            } else {
                call.respond(HttpStatusCode.Created, creada)
            }
        }

        // PUT /api/parcelas/{id}/sigpac
        put("sigpac") {
            val parcelaId = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<ReferenciaSigpacRequest>()

            val filas = transaction {
                ReferenciaSigpac.update({ ReferenciaSigpac.parcelaId eq parcelaId }) {
                    it[provincia]        = request.provincia
                    it[terminoMunicipal] = request.terminoMunicipal
                    it[codigoAgregado]   = request.codigoAgregado
                    it[zona]             = request.zona
                    it[numeroPoligono]   = request.numeroPoligono
                    it[numeroParcela]    = request.numeroParcela
                    it[numeroRecinto]    = request.numeroRecinto
                    it[usoSigpac]        = request.usoSigpac
                    it[superficieHa]     = request.superficieHa
                }
            }

            if (filas == 0) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("message" to "No existe una referencia SIGPAC para esta parcela. Usa POST para crearla.")
                )
            } else {
                call.respond(HttpStatusCode.OK)
            }
        }

        // POST /api/parcelas/{id}/agronomico
        post("agronomico") {
            val parcelaId = call.parameters["id"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            if (!parcelaExiste(parcelaId)) {
                return@post call.respond(HttpStatusCode.NotFound, mapOf("message" to "Parcela no encontrada"))
            }

            val request = call.receive<DatosAgronomicosRequest>()
            val fechaInicio = request.fechaInicio?.let { java.time.LocalDate.parse(it) }
            val fechaFin    = request.fechaFin?.let { java.time.LocalDate.parse(it) }

            val creado = transaction {
                val yaExiste = !DatosAgronomicos.selectAll()
                    .where { DatosAgronomicos.parcelaId eq parcelaId }
                    .empty()
                if (yaExiste) return@transaction null

                val nuevoId = DatosAgronomicos.insertAndGetId {
                    it[DatosAgronomicos.parcelaId] = parcelaId
                    it[especieVariedad]    = request.especieVariedad
                    it[ecoregimenPractica] = request.ecoregimenPractica
                    it[secanoRegadio]      = request.secanoRegadio
                    it[cultivoId]          = request.cultivoId
                    it[DatosAgronomicos.fechaInicio] = fechaInicio
                    it[DatosAgronomicos.fechaFin]    = fechaFin
                    it[aireLibreProtegido] = request.aireLibreProtegido
                }.value

                DatosAgronomicos.selectAll()
                    .where { DatosAgronomicos.id eq nuevoId }
                    .single()
                    .toDatosAgronomicosResponse()
            }

            if (creado == null) {
                call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("message" to "Ya existen datos agronómicos para esta parcela. Usa PUT para actualizarlos.")
                )
            } else {
                call.respond(HttpStatusCode.Created, creado)
            }
        }

        // PUT /api/parcelas/{id}/agronomico
        put("agronomico") {
            val parcelaId = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<DatosAgronomicosRequest>()
            val fechaInicio = request.fechaInicio?.let { java.time.LocalDate.parse(it) }
            val fechaFin    = request.fechaFin?.let { java.time.LocalDate.parse(it) }

            val filas = transaction {
                DatosAgronomicos.update({ DatosAgronomicos.parcelaId eq parcelaId }) {
                    it[especieVariedad]    = request.especieVariedad
                    it[ecoregimenPractica] = request.ecoregimenPractica
                    it[secanoRegadio]      = request.secanoRegadio
                    it[cultivoId]          = request.cultivoId
                    it[DatosAgronomicos.fechaInicio] = fechaInicio
                    it[DatosAgronomicos.fechaFin]    = fechaFin
                    it[aireLibreProtegido] = request.aireLibreProtegido
                }
            }

            if (filas == 0) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("message" to "No existen datos agronómicos para esta parcela. Usa POST para crearlos.")
                )
            } else {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

private fun parcelaExiste(parcelaId: Int): Boolean = transaction {
    !Parcelas.selectAll().where { Parcelas.id eq parcelaId }.empty()
}

