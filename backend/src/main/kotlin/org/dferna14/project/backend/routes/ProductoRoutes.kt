package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.ActividadProductos
import org.dferna14.project.backend.db.Productos
import org.dferna14.project.backend.db.SemillasTratadas
import org.dferna14.project.backend.model.ProductoRequest
import org.dferna14.project.backend.model.ProductoResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

fun Route.productoRoutes() {

    route("/api/productos") {

        // GET /api/productos
        get {
            val productos = transaction {
                Productos.selectAll().map {
                    ProductoResponse(
                        id              = it[Productos.id].value,
                        nombreComercial = it[Productos.nombreComercial],
                        materiaActiva    = it[Productos.materiaActiva],
                        numeroRegistro  = it[Productos.numeroRegistro]
                    )
                }
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
                    ?.let {
                        ProductoResponse(
                            id              = it[Productos.id].value,
                            nombreComercial = it[Productos.nombreComercial],
                            materiaActiva    = it[Productos.materiaActiva],
                            numeroRegistro  = it[Productos.numeroRegistro]
                        )
                    }
            }

            if (producto == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(producto)
            }
        }

        // POST /api/productos - Crear nuevo producto
        post {
            val request = call.receive<ProductoRequest>()

            val creado = transaction {
                val nuevoId = Productos.insertAndGetId {
                    it[nombreComercial] = request.nombreComercial
                    it[materiaActiva] = request.materiaActiva
                    it[numeroRegistro] = request.numeroRegistro
                }.value

                Productos.selectAll()
                    .where { Productos.id eq nuevoId }
                    .single()
                    .let {
                        ProductoResponse(
                            id = it[Productos.id].value,
                            nombreComercial = it[Productos.nombreComercial],
                            materiaActiva = it[Productos.materiaActiva],
                            numeroRegistro = it[Productos.numeroRegistro]
                        )
                    }
            }

            call.respond(HttpStatusCode.Created, creado)
        }

        // PUT /api/productos/{id} - Actualizar producto
        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val request = call.receive<ProductoRequest>()

            val filasActualizadas = transaction {
                Productos.update({ Productos.id eq id }) {
                    it[nombreComercial] = request.nombreComercial
                    it[materiaActiva] = request.materiaActiva
                    it[numeroRegistro] = request.numeroRegistro
                }
            }

            if (filasActualizadas == 0) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.OK)
            }
        }

        // DELETE /api/productos/{id} - Eliminar producto
        // Devolvemos 409 para que la UI pueda mostrar un mensaje claro al usuario.
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val (enActividades, enSemillas) = transaction {
                val enActs = !ActividadProductos.selectAll()
                    .where { ActividadProductos.productoId eq id }
                    .empty()
                val enSems = !SemillasTratadas.selectAll()
                    .where { SemillasTratadas.productoId eq id }
                    .empty()
                enActs to enSems
            }

            if (enActividades || enSemillas) {
                return@delete call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("message" to "No se puede eliminar el producto porque está siendo usado en actividades o semillas")
                )
            }

            val filasEliminadas = transaction {
                Productos.deleteWhere { Productos.id eq id }
            }

            if (filasEliminadas == 0) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
