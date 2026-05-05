package org.dferna14.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dferna14.project.data.repository.ActividadRepository
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Result

class ParcelaVm(
    private val repository: ActividadRepository
) : ViewModel() {

    private val _parcelas = MutableStateFlow<Result<List<Parcela>>>(Result.Loading)
    val parcelas: StateFlow<Result<List<Parcela>>> = _parcelas.asStateFlow()

    private val _operacionExitosa = MutableStateFlow(false)
    val operacionExitosa: StateFlow<Boolean> = _operacionExitosa.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    init {
        cargarParcelas()
    }

    fun cargarParcelas() {
        viewModelScope.launch {
            repository.getParcelas().collect { resultado ->
                _parcelas.value = resultado
            }
        }
    }

    fun crearParcela(parcela: Parcela) {
        viewModelScope.launch {
            try {
                val resultado = repository.crearParcela(parcela)
                when (resultado) {
                    is Result.Success -> {
                        _operacionExitosa.value = true
                        cargarParcelas()
                    }
                    is Result.Error -> {
                        _operacionExitosa.value = false
                        _mensajeError.value = resultado.message
                    }
                    else -> {}
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.value = "Error al crear parcela: ${e.message}"
            }
        }
    }

    fun eliminarParcela(id: Int) {
        viewModelScope.launch {
            try {
                val resultado = repository.eliminarParcela(id)
                if (resultado is Result.Success) {
                    cargarParcelas()
                } else if (resultado is Result.Error) {
                    _mensajeError.value = resultado.message
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.value = "Error al eliminar parcela: ${e.message}"
            }
        }
    }

    fun resetOperacionExitosa() {
        _operacionExitosa.value = false
    }

    fun limpiarMensajeError() {
        _mensajeError.value = null
    }
}
