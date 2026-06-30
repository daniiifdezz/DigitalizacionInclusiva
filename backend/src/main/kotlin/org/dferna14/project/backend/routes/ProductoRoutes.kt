package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.ActividadProductos
import org.dferna14.project.backend.db.Fertilizaciones
import org.dferna14.project.backend.db.Productos
import org.dferna14.project.backend.db.SemillasTratadas
import org.dferna14.project.backend.model.DependenciasProductoDto
import org.dferna14.project.backend.model.ProductoRequest
import org.dferna14.project.backend.model.ProductoResponse
import org.dferna14.project.backend.plugins.tenantId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.productoRoutes() {

    route("/api/productos") {

        // GET /api/productos[?tipo=FITOSANITARIO|FERTILIZANTE]
        get {
            val tenantId = call.tenantId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val tipoFiltro = call.request.queryParameters["tipo"]
            val productos = transaction {
                val query = if (tipoFiltro != null) {
                    Productos.selectAll().where {
                        (Productos.explotacionId eq tenantId) and (Productos.tipo eq tipoFiltro)
                    }
                } else {
                    Productos.selectAll().where { Productos.explotacionId eq tenantId }
                }
                query.map { it.toProductoResponse() }
            }
            call.respond(productos)
        }

        // GET /api/productos/{id}
        get("{id}") {
            val tenantId = call.tenantId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val producto = transaction {
                Productos.selectAll()
                    .where { (Productos.id eq id) and (Productos.explotacionId eq tenantId) }
                    .singleOrNull()
                    ?.toProductoResponse()
            }

            if (producto == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(producto)
        }

        // POST /api/productos
        post {
            val tenantId = call.tenantId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request = call.receive<ProductoRequest>()

            val creado = transaction {
                val nuevoId = Productos.insertAndGetId {
                    it[explotacionId]    = tenantId
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
            val tenantId = call.tenantId() ?: return@put call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<ProductoRequest>()

            val filasActualizadas = transaction {
                Productos.update({ (Productos.id eq id) and (Productos.explotacionId eq tenantId) }) {
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
        // Primero verifica ownership (404 si no es del tenant), luego comprueba
        // referencias (409 si está en uso) y finalmente elimina.
        delete("{id}") {
            val tenantId = call.tenantId() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val existe = transaction {
                !Productos.selectAll()
                    .where { (Productos.id eq id) and (Productos.explotacionId eq tenantId) }
                    .empty()
            }
            if (!existe) return@delete call.respond(HttpStatusCode.NotFound)

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
                Productos.deleteWhere { (Productos.id eq id) and (Productos.explotacionId eq tenantId) }
            }

            if (filasEliminadas == 0) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.NoContent)
        }

        // GET /api/productos/{id}/dependencias
        get("{id}/dependencias") {
            val tenantId = call.tenantId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val existe = transaction {
                !Productos.selectAll()
                    .where { (Productos.id eq id) and (Productos.explotacionId eq tenantId) }
                    .empty()
            }
            if (!existe) return@get call.respond(HttpStatusCode.NotFound)

            val dependencias = transaction {
                DependenciasProductoDto(
                    actividadProductos = ActividadProductos.selectAll()
                        .where { ActividadProductos.productoId eq id }.count().toInt(),
                    semillas = SemillasTratadas.selectAll()
                        .where { SemillasTratadas.productoId eq id }.count().toInt(),
                    fertilizaciones = Fertilizaciones.selectAll()
                        .where { Fertilizaciones.productoId eq id }.count().toInt()
                )
            }
            call.respond(dependencias)
        }

        // DELETE /api/productos/{id}/cascada
        // Estrategia mixta: borra actividad_producto, pone null en semillatratada
        // y fertilizacion, luego elimina el producto. Solo opera sobre productos
        // del tenant autenticado.
        delete("{id}/cascada") {
            val tenantId = call.tenantId() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val existe = transaction {
                !Productos.selectAll()
                    .where { (Productos.id eq id) and (Productos.explotacionId eq tenantId) }
                    .empty()
            }
            if (!existe) return@delete call.respond(HttpStatusCode.NotFound)

            transaction {
                ActividadProductos.deleteWhere { ActividadProductos.productoId eq id }
                SemillasTratadas.update({ SemillasTratadas.productoId eq id }) {
                    it[SemillasTratadas.productoId] = null
                }
                Fertilizaciones.update({ Fertilizaciones.productoId eq id }) {
                    it[Fertilizaciones.productoId] = null
                }
                Productos.deleteWhere { (Productos.id eq id) and (Productos.explotacionId eq tenantId) }
            }

            call.respond(HttpStatusCode.NoContent)
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
