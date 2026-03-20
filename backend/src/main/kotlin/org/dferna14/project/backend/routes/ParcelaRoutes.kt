package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.Parcelas
import org.dferna14.project.backend.model.ParcelaResponse
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

/**
 * Endpoints REST para Parcela.
 * Solo lectura — la gestión completa se hace desde Desktop.
 *
 * GET /api/parcelas        → lista todas las parcelas
 * GET /api/parcelas/{id}   → detalle de una parcela
 */
fun Route.parcelaRoutes() {

    route("/api/parcelas") {

        // ── GET /api/parcelas ─────────────────────────────────────────────────
        get {
            val parcelas = transaction {
                Parcelas.selectAll().map {
                    ParcelaResponse(
                        id                   = it[Parcelas.id].value,
                        explotacionId        = it[Parcelas.explotacionId],
                        orden                = it[Parcelas.orden],
                        sistemaAsesoramiento = it[Parcelas.sistemaAsesoramiento],
                        zonaNitratos         = it[Parcelas.zonaNitratos]
                    )
                }
            }
            call.respond(parcelas)
        }

        // ── GET /api/parcelas/{id} ────────────────────────────────────────────
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
    }
}