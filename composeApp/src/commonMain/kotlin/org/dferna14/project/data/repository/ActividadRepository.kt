package org.dferna14.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.dferna14.project.data.remote.ActividadApi
import org.dferna14.project.data.remote.ActividadCreateDto
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.Parcela
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
            val actividades = api.getActividades().map { dto ->
                Actividad(
                    id                    = dto.id,
                    parcelaId             = dto.parcelaId,
                    equipoId              = dto.equipoId,
                    aplicadorId           = dto.aplicadorId,
                    fechaInicio           = dto.fechaInicio,
                    fechaFin              = dto.fechaFin,
                    superficieTratada     = dto.superficieTratada,
                    problemaFitosanitario = dto.problemaFitosanitario,
                    eficacia              = dto.eficacia,
                    observaciones         = dto.observaciones,
                    sincronizado          = true
                )
            }
            emit(Result.Success(actividades))
        } catch (e: Exception) {
            emit(Result.Error("Error al cargar actividades: ${e.message}"))
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
                    observaciones         = actividad.observaciones
                )
            )
            Result.Success(actividad.copy(id = dto.id, sincronizado = true))
        } catch (e: Exception) {
            println("EXCEPCION COMPLETA: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            Result.Error("Error al crear actividad: ${e.message}")
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

    //Parcelas

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
}