package org.dferna14.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dferna14.project.data.repository.ActividadRepository
import org.dferna14.project.domain.model.Fertilizacion
import org.dferna14.project.domain.model.Result

class FertilizacionVm(
    private val repository: ActividadRepository
) : ViewModel() {

    private val _fertilizacion = MutableStateFlow<Result<Fertilizacion?>>(Result.Loading)
    val fertilizacion: StateFlow<Result<Fertilizacion?>> = _fertilizacion.asStateFlow()

    private val _operacionExitosa = MutableStateFlow(false)
    val operacionExitosa: StateFlow<Boolean> = _operacionExitosa.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    fun cargarFertilizacion(actividadId: Int) {
        viewModelScope.launch {
            _fertilizacion.value = Result.Loading
            try {
                _fertilizacion.value = repository.getFertilizacionPorActividad(actividadId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _fertilizacion.value = Result.Error("Error al obtener fertilización: ${e.message}")
            }
        }
    }

    suspend fun guardarFertilizacion(
        actividadId: Int,
        fertilizacion: Fertilizacion
    ): Result<Fertilizacion> {
        return try {
            val resultado = repository.guardarFertilizacion(actividadId, fertilizacion)
            if (resultado is Result.Success) {
                _operacionExitosa.value = true
                _fertilizacion.value = Result.Success(resultado.data)
            } else if (resultado is Result.Error) {
                _mensajeError.value = resultado.message
            }
            resultado
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al guardar fertilización: ${e.message}")
        }
    }

    fun resetOperacionExitosa() {
        _operacionExitosa.value = false
    }

    fun limpiarMensajeError() {
        _mensajeError.value = null
    }
}
