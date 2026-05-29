package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.ActividadProductos
import org.dferna14.project.backend.db.Fertilizaciones
import org.dferna14.project.backend.db.Productos
import org.dferna14.project.backend.db.SemillasTratadas
import org.dferna14.project.backend.model.ProductoRequest
import org.dferna14.project.backend.model.ProductoResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.productoRoutes() {

    route("/api/productos") {

        // GET /api/productos[?tipo=FITOSANITARIO|FERTILIZANTE]
        // Catálogo unificado: el query param permite a las pantallas de fertilización
        // pedir solo fertilizantes y a las de tratamiento solo fitosanitarios.
        get {
            val tipoFiltro = call.request.queryParameters["tipo"]
            val productos = transaction {
                val query = if (tipoFiltro != null) {
                    Productos.selectAll().where { Productos.tipo eq tipoFiltro }
                } else {
                    Productos.selectAll()
                }
                query.map { it.toProductoResponse() }
            }
            call.respond(productos)
        }

        // GET /api/productos/{id}
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val producto = transaction {
                Productos.selectAll()
                    .where { Productos.id eq id }
                    .singleOrNull()
                    ?.toProductoResponse()
            }

            if (producto == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(producto)
        }

        // POST /api/productos
        post {
            val request = call.receive<ProductoRequest>()

            val creado = transaction {
                val nuevoId = Productos.insertAndGetId {
                    it[nombreComercial]  = request.nombreComercial
                    it[materiaActiva]    = request.materiaActiva
                    it[numeroRegistro]   = request.numeroRegistro
                    it[tipo]             = request.tipo
                    it[riquezaNpk]       = request.riquezaNpk
                    it[tipoFertilizante] = request.tipoFertilizante
                }.value

                Productos.selectAll()
                    .where { Productos.id eq nuevoId }
                    .single()
                    .toProductoResponse()
            }

            call.respond(HttpStatusCode.Created, creado)
        }

        // PUT /api/productos/{id}
        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<ProductoRequest>()

            val filasActualizadas = transaction {
                Productos.update({ Productos.id eq id }) {
                    it[nombreComercial]  = request.nombreComercial
                    it[materiaActiva]    = request.materiaActiva
                    it[numeroRegistro]   = request.numeroRegistro
                    it[tipo]             = request.tipo
                    it[riquezaNpk]       = request.riquezaNpk
                    it[tipoFertilizante] = request.tipoFertilizante
                }
            }

            if (filasActualizadas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK)
        }

        // DELETE /api/productos/{id}
        // Devolvemos 409 para que la UI muestre un mensaje claro cuando el producto
        // esté siendo usado en actividades o semillas.
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val refs = transaction {
                Triple(
                    !ActividadProductos.selectAll()
                        .where { ActividadProductos.productoId eq id }
                        .empty(),
                    !SemillasTratadas.selectAll()
                        .where { SemillasTratadas.productoId eq id }
                        .empty(),
                    !Fertilizaciones.selectAll()
                        .where { Fertilizaciones.productoId eq id }
                        .empty()
                )
            }
            val (enActividades, enSemillas, enFertilizaciones) = refs

            if (enActividades || enSemillas || enFertilizaciones) {
                return@delete call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("message" to "No se puede eliminar el producto porque está siendo usado en actividades, semillas o fertilizaciones")
                )
            }

            val filasEliminadas = transaction {
                Productos.deleteWhere { Productos.id eq id }
            }

            if (filasEliminadas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun ResultRow.toProductoResponse() = ProductoResponse(
    id               = this[Productos.id].value,
    nombreComercial  = this[Productos.nombreComercial],
    materiaActiva    = this[Productos.materiaActiva],
    numeroRegistro   = this[Productos.numeroRegistro],
    tipo             = this[Productos.tipo],
    riquezaNpk       = this[Productos.riquezaNpk],
    tipoFertilizante = this[Productos.tipoFertilizante]
)
