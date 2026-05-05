package org.dferna14.project.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.dferna14.project.data.remote.ActividadApi
import org.dferna14.project.data.remote.ActividadCreateDto
import org.dferna14.project.data.remote.ActividadDto
import org.dferna14.project.data.remote.EstadoActividadDto
import org.dferna14.project.data.remote.ParcelaCreateDto
import org.dferna14.project.data.remote.ProductoCreateDto
import org.dferna14.project.data.remote.SemillaTratadaCreateDto
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.EstadoActividad
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Producto
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.SemillaTratada
import org.dferna14.project.data.remote.FertilizacionCreateDto
import org.dferna14.project.domain.model.Fertilizacion

/**
 * Repositorio offline-first para Actividades y Parcelas.
 *
 * Estrategia:
 * 1. Devuelve datos locales inmediatamente (sin esperar red)
 * 2. Intenta sincronizar con el backend en segundo plano
 * 3. Si no hay red, los datos se guardan localmente y se sincronizan después
 *
 * Por ahora trabaja directamente con la API hasta que implementemos SQLDelight.
 * La interfaz no cambiará cuando añadamos la caché local.
 */
class ActividadRepository(
    private val api: ActividadApi
) {

    // Actividades

    fun getActividades(): Flow<Result<List<Actividad>>> = flow {
        emit(Result.Loading)
        try {
            val actividades = api.getActividades().map { it.toDomain() }
            emit(Result.Success(actividades))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Result.Error("Error al cargar actividades: ${e.message}"))
        }
    }

    fun getActividadesPendientes(): Flow<Result<List<Actividad>>> = flow {
        emit(Result.Loading)
        try {
            val actividades = api.getActividadesPendientes().map { it.toDomain() }
            emit(Result.Success(actividades))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Result.Error("Error al cargar actividades pendientes: ${e.message}"))
        }
    }

    suspend fun crearActividad(actividad: Actividad): Result<Actividad> {
        return try {
            val dto = api.crearActividad(
                ActividadCreateDto(
                    parcelaId             = actividad.parcelaId,
                    equipoId              = actividad.equipoId,
                    aplicadorId           = actividad.aplicadorId,
                    fechaInicio           = actividad.fechaInicio,
                    fechaFin              = actividad.fechaFin,
                    superficieTratada     = actividad.superficieTratada,
                    problemaFitosanitario = actividad.problemaFitosanitario,
                    eficacia              = actividad.eficacia,
                    observaciones         = actividad.observaciones,
                    estado                = EstadoActividadDto.BORRADOR
                )
            )
            Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al crear actividad: ${e.message}")
        }
    }

    suspend fun enviarActividad(id: Int): Result<Actividad> {
        return try {
            val dto = api.enviarActividad(id)
            Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al enviar actividad: ${e.message}")
        }
    }

    suspend fun validarActividad(id: Int): Result<Actividad> {
        return try {
            val dto = api.validarActividad(id)
            Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al validar actividad: ${e.message}")
        }
    }

    suspend fun devolverActividad(id: Int): Result<Unit> {
        return try {
            val exito = api.devolverActividad(id)
            if (exito) Result.Success(Unit)
            else Result.Error("No se pudo devolver la actividad")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al devolver actividad: ${e.message}")
        }
    }

    suspend fun eliminarActividad(id: Int): Result<Unit> {
        return try {
            val eliminado = api.eliminarActividad(id)
            if (eliminado) Result.Success(Unit)
            else Result.Error("No se pudo eliminar la actividad")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al eliminar actividad: ${e.message}")
        }
    }

    suspend fun getActividad(id: Int): Result<Actividad> {
        return try {
            val dto = api.getActividad(id)
            Result.Success(dto.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al obtener actividad: ${e.message}")
        }
    }

    suspend fun actualizarActividad(actividad: Actividad): Result<Actividad> {
        return try {
            val dto = ActividadCreateDto(
                parcelaId             = actividad.parcelaId,
                equipoId              = actividad.equipoId,
                aplicadorId           = actividad.aplicadorId,
                fechaInicio           = actividad.fechaInicio,
                fechaFin              = actividad.fechaFin,
                superficieTratada     = actividad.superficieTratada,
                problemaFitosanitario = actividad.problemaFitosanitario,
                eficacia              = actividad.eficacia,
                observaciones         = actividad.observaciones,
                estado                = when (actividad.estado) {
                    EstadoActividad.BORRADOR -> EstadoActividadDto.BORRADOR
                    EstadoActividad.PENDIENTE_VALIDAR -> EstadoActividadDto.PENDIENTE_VALIDAR
                    EstadoActividad.VALIDADA -> EstadoActividadDto.VALIDADA
                }
            )
            val exito = api.actualizarActividad(actividad.id, dto)
            if (exito) Result.Success(actividad)
            else Result.Error("No se pudo actualizar la actividad")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al actualizar actividad: ${e.message}")
        }
    }

    // Parcelas

    fun getParcelas(): Flow<Result<List<Parcela>>> = flow {
        emit(Result.Loading)
        try {
            val parcelas = api.getParcelas().map { dto ->
                Parcela(
                    id                   = dto.id,
                    explotacionId        = dto.explotacionId,
                    orden                = dto.orden,
                    alias                = dto.alias,
                    sistemaAsesoramiento = dto.sistemaAsesoramiento,
                    zonaNitratos         = dto.zonaNitratos
                )
            }
            emit(Result.Success(parcelas))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Result.Error("Error al cargar parcelas: ${e.message}"))
        }
    }

    suspend fun crearParcela(parcela: Parcela): Result<Parcela> {
        return try {
            val dto = api.crearParcela(
                ParcelaCreateDto(
                    explotacionId        = parcela.explotacionId,
                    orden                = parcela.orden,
                    alias                = parcela.alias,
                    sistemaAsesoramiento = parcela.sistemaAsesoramiento,
                    zonaNitratos         = parcela.zonaNitratos
                )
            )
            Result.Success(
                Parcela(
                    id                   = dto.id,
                    explotacionId        = dto.explotacionId,
                    orden                = dto.orden,
                    alias                = dto.alias,
                    sistemaAsesoramiento = dto.sistemaAsesoramiento,
                    zonaNitratos         = dto.zonaNitratos
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al crear parcela: ${e.message}")
        }
    }

    suspend fun eliminarParcela(id: Int): Result<Unit> {
        return try {
            val exito = api.eliminarParcela(id)
            if (exito) Result.Success(Unit)
            else Result.Error("No se pudo eliminar la parcela")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al eliminar parcela: ${e.message}")
        }
    }

    // Productos

    fun getProductos(): Flow<Result<List<Producto>>> = flow {
        emit(Result.Loading)
        try {
            val productos = api.getProductos().map { dto ->
                Producto(
                    id = dto.id,
                    nombreComercial = dto.nombreComercial ?: "Sin nombre",
                    materiaActiva = dto.materiaActiva,
                    numeroRegistro = dto.numeroRegistro
                )
            }
            emit(Result.Success(productos))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Result.Error("Error al cargar productos: ${e.message}"))
        }
    }

    suspend fun crearProducto(producto: Producto): Result<Producto> {
        return try {
            val dto = api.crearProducto(
                ProductoCreateDto(
                    nombreComercial = producto.nombreComercial,
                    materiaActiva = producto.materiaActiva,
                    numeroRegistro = producto.numeroRegistro
                )
            )
            Result.Success(
                Producto(
                    id = dto.id,
                    nombreComercial = dto.nombreComercial ?: "Sin nombre",
                    materiaActiva = dto.materiaActiva,
                    numeroRegistro = dto.numeroRegistro
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al crear producto: ${e.message}")
        }
    }

    suspend fun eliminarProducto(id: Int): Result<Unit> {
        return try {
            val exito = api.eliminarProducto(id)
            if (exito) Result.Success(Unit)
            else Result.Error("No se pudo eliminar el producto")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al eliminar producto: ${e.message}")
        }
    }

    private fun ActividadDto.toDomain() = Actividad(
        id                    = id,
        parcelaId             = parcelaId,
        equipoId              = equipoId,
        aplicadorId           = aplicadorId,
        fechaInicio           = fechaInicio,
        fechaFin              = fechaFin,
        superficieTratada     = superficieTratada,
        problemaFitosanitario = problemaFitosanitario,
        eficacia              = eficacia,
        observaciones         = observaciones,
        estado                = when (estado) {
            EstadoActividadDto.BORRADOR -> EstadoActividad.BORRADOR
            EstadoActividadDto.PENDIENTE_VALIDAR -> EstadoActividad.PENDIENTE_VALIDAR
            EstadoActividadDto.VALIDADA -> EstadoActividad.VALIDADA
        },
        sincronizado          = true
    )

    // Semillas tratadas

    suspend fun getSemillaTratada(actividadId: Int): Result<SemillaTratada?> {
        return try {
            val dto = api.getSemillaTratada(actividadId)
            if (dto == null) {
                // No hay registro: Devolver Success(null) para que la UI muestre el formulario vacío
                println("INFO: No hay semilla tratada para actividad $actividadId")
                Result.Success(null)
            } else {
                Result.Success(
                    SemillaTratada(
                        id                = dto.id,
                        actividadId       = dto.actividadId,
                        parcelaId         = dto.parcelaId,
                        aplica            = dto.aplica,
                        fechaSiembra      = dto.fechaSiembra,
                        superficieHa      = dto.superficieHa,
                        cantidadSemillaKg = dto.cantidadSemillaKg,
                        productoId        = dto.productoId,
                        variedadSemilla   = dto.variedadSemilla,
                        cultivoId         = dto.cultivoId
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al obtener semilla tratada: ${e.message ?: "Error desconocido"}")
        }
    }

    suspend fun crearSemillaTratada(semilla: SemillaTratada): Result<SemillaTratada> {
        return try {
            println("DEBUG REPO: Enviando SemillaTratada al servidor: $semilla")
            val dto = api.crearSemillaTratada(
                SemillaTratadaCreateDto(
                    actividadId       = semilla.actividadId,
                    parcelaId         = semilla.parcelaId,
                    aplica            = semilla.aplica,
                    fechaSiembra      = semilla.fechaSiembra,
                    superficieHa      = semilla.superficieHa,
                    cantidadSemillaKg = semilla.cantidadSemillaKg,
                    productoId        = semilla.productoId,
                    variedadSemilla   = semilla.variedadSemilla,
                    cultivoId         = semilla.cultivoId
                )
            )
            println("DEBUG REPO: Respuesta SemillaTratada recibida: $dto")
            Result.Success(
                SemillaTratada(
                    id                = dto.id,
                    actividadId       = dto.actividadId,
                    parcelaId         = dto.parcelaId,
                    aplica            = dto.aplica,
                    fechaSiembra      = dto.fechaSiembra,
                    superficieHa      = dto.superficieHa,
                    cantidadSemillaKg = dto.cantidadSemillaKg,
                    productoId        = dto.productoId,
                    variedadSemilla   = dto.variedadSemilla,
                    cultivoId         = dto.cultivoId
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al crear semilla tratada: ${e.message ?: "Error desconocido"}")
        }
    }



    // Fertilizacion functions
    suspend fun getFertilizacion(cultivoId: Int): Result<Fertilizacion?> {
        return try {
            val dto = api.getFertilizacionByCultivo(cultivoId)
            if (dto == null) {
                Result.Success(null)
            } else {
                Result.Success(
                    Fertilizacion(
                        id                = dto.id,
                        cultivoId         = dto.cultivoId,
                        aplica            = dto.aplica,
                        fechaInicio       = dto.fechaInicio,
                        fechaFin          = dto.fechaFin,
                        tipoProducto      = dto.tipoProducto,
                        numeroAlbaran     = dto.numeroAlbaran,
                        riquezaNPK        = dto.riquezaNPK,
                        dosis             = dto.dosis,
                        tipoFertilizacion = dto.tipoFertilizacion,
                        observaciones     = dto.observaciones
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al obtener fertilización: ${e.message}")
        }
    }

    suspend fun crearFertilizacion(fertilizacion: Fertilizacion): Result<Fertilizacion> {
        return try {
            println("DEBUG REPO: Enviando Fertilizacion al servidor: $fertilizacion")
            val dto = api.crearFertilizacion(
                FertilizacionCreateDto(
                    cultivoId         = fertilizacion.cultivoId,
                    aplica            = fertilizacion.aplica,
                    fechaInicio       = fertilizacion.fechaInicio,
                    fechaFin          = fertilizacion.fechaFin,
                    tipoProducto      = fertilizacion.tipoProducto,
                    numeroAlbaran     = fertilizacion.numeroAlbaran,
                    riquezaNPK        = fertilizacion.riquezaNPK,
                    dosis             = fertilizacion.dosis,
                    tipoFertilizacion = fertilizacion.tipoFertilizacion,
                    observaciones     = fertilizacion.observaciones
                )
            )
            println("DEBUG REPO: Respuesta Fertilizacion recibida: $dto")
            Result.Success(
                Fertilizacion(
                    id                = dto.id,
                    cultivoId         = dto.cultivoId,
                    aplica            = dto.aplica,
                    fechaInicio       = dto.fechaInicio,
                    fechaFin          = dto.fechaFin,
                    tipoProducto      = dto.tipoProducto,
                    numeroAlbaran     = dto.numeroAlbaran,
                    riquezaNPK        = dto.riquezaNPK,
                    dosis             = dto.dosis,
                    tipoFertilizacion = dto.tipoFertilizacion,
                    observaciones     = dto.observaciones
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al crear fertilización: ${e.message ?: "Error desconocido"}")
        }
    }
}