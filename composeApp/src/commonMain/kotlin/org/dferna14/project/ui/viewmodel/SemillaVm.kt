package org.dferna14.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dferna14.project.data.repository.ActividadRepository
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.SemillaTratada

class SemillaVm(
    private val repository: ActividadRepository
) : ViewModel() {

    private val _semilla = MutableStateFlow<Result<SemillaTratada?>>(Result.Loading)
    val semilla: StateFlow<Result<SemillaTratada?>> = _semilla.asStateFlow()

    private val _operacionExitosa = MutableStateFlow(false)
    val operacionExitosa: StateFlow<Boolean> = _operacionExitosa.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    fun cargarSemilla(actividadId: Int) {
        viewModelScope.launch {
            _semilla.value = Result.Loading
            try {
                _semilla.value = repository.getSemillaTratada(actividadId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _semilla.value = Result.Error("Error al obtener semilla: ${e.message}")
            }
        }
    }

    suspend fun crearSemillaTratada(semilla: SemillaTratada): Result<SemillaTratada> {
        return try {
            val resultado = repository.crearSemillaTratada(semilla)
            if (resultado is Result.Success) {
                _operacionExitosa.value = true
                _semilla.value = Result.Success(resultado.data)
            } else if (resultado is Result.Error) {
                _mensajeError.value = resultado.message
            }
            resultado
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error("Error al crear semilla: ${e.message}")
        }
    }

    // Pre-relleno de superficie en el formulario al cargar la actividad.
    suspend fun getSuperficieParcela(parcelaId: Int): Double? =
        repository.getSuperficieParcela(parcelaId)

    fun resetOperacionExitosa() {
        _operacionExitosa.value = false
    }

    fun limpiarMensajeError() {
        _mensajeError.value = null
    }
}
