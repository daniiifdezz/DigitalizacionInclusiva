package org.dferna14.project.backend.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.dferna14.project.backend.db.Productos
import org.dferna14.project.backend.model.ProductoResponse
import org.jetbrains.exposed.sql.selectAll
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
    }
}
