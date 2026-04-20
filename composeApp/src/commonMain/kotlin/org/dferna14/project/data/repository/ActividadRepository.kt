package org.dferna14.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.dferna14.project.data.remote.ActividadApi
import org.dferna14.project.data.remote.ActividadCreateDto
import org.dferna14.project.data.remote.ActividadDto
import org.dferna14.project.data.remote.EstadoActividadDto
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.EstadoActividad
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Producto
import org.dferna14.project.domain.model.Result

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
        } catch (e: Exception) {
            emit(Result.Error("Error al cargar actividades: ${e.message}"))
        }
    }

    fun getActividadesPendientes(): Flow<Result<List<Actividad>>> = flow {
        emit(Result.Loading)
        try {
            val actividades = api.getActividadesPendientes().map { it.toDomain() }
            emit(Result.Success(actividades))
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
        } catch (e: Exception) {
            Result.Error("Error al crear actividad: ${e.message}")
        }
    }

    suspend fun enviarActividad(id: Int): Result<Actividad> {
        return try {
            val dto = api.enviarActividad(id)
            Result.Success(dto.toDomain())
        } catch (e: Exception) {
            Result.Error("Error al enviar actividad: ${e.message}")
        }
    }

    suspend fun validarActividad(id: Int): Result<Actividad> {
        return try {
            val dto = api.validarActividad(id)
            Result.Success(dto.toDomain())
        } catch (e: Exception) {
            Result.Error("Error al validar actividad: ${e.message}")
        }
    }

    suspend fun devolverActividad(id: Int): Result<Unit> {
        return try {
            val exito = api.devolverActividad(id)
            if (exito) Result.Success(Unit)
            else Result.Error("No se pudo devolver la actividad")
        } catch (e: Exception) {
            Result.Error("Error al devolver actividad: ${e.message}")
        }
    }

    suspend fun eliminarActividad(id: Int): Result<Unit> {
        return try {
            val eliminado = api.eliminarActividad(id)
            if (eliminado) Result.Success(Unit)
            else Result.Error("No se pudo eliminar la actividad")
        } catch (e: Exception) {
            Result.Error("Error al eliminar actividad: ${e.message}")
        }
    }

    suspend fun getActividad(id: Int): Result<Actividad> {
        return try {
            val dto = api.getActividad(id)
            Result.Success(dto.toDomain())
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
                    sistemaAsesoramiento = dto.sistemaAsesoramiento,
                    zonaNitratos         = dto.zonaNitratos
                )
            }
            emit(Result.Success(parcelas))
        } catch (e: Exception) {
            emit(Result.Error("Error al cargar parcelas: ${e.message}"))
        }
    }

    // Productos

    fun getProductos(): Flow<Result<List<Producto>>> = flow {
        emit(Result.Loading)
        try {
            val productos = api.getProductos().map { dto ->
                Producto(
                    id = dto.id,
                    nombreComercial = dto.nombreComercial,
                    materiaActiva = dto.materiaActiva,
                    numeroRegistro = dto.numeroRegistro
                )
            }
            emit(Result.Success(productos))
        } catch (e: Exception) {
            emit(Result.Error("Error al cargar productos: ${e.message}"))
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
}