package org.dferna14.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dferna14.project.data.repository.ActividadRepository
import org.dferna14.project.domain.model.EquipoAplicacion
import org.dferna14.project.domain.model.Result

class EquipoVm(
    private val repository: ActividadRepository
) : ViewModel() {

    private val _equipos = MutableStateFlow<Result<List<EquipoAplicacion>>>(Result.Loading)
    val equipos: StateFlow<Result<List<EquipoAplicacion>>> = _equipos.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    init {
        cargarEquipos()
    }

    fun cargarEquipos() {
        viewModelScope.launch {
            _equipos.value = Result.Loading
            try {
                _equipos.value = repository.getEquipos()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _equipos.value = Result.Error("Error al cargar equipos: ${e.message}")
            }
        }
    }

    fun crearEquipo(equipo: EquipoAplicacion) {
        viewModelScope.launch {
            try {
                val resultado = repository.crearEquipo(equipo)
                if (resultado is Result.Success) {
                    cargarEquipos()
                } else if (resultado is Result.Error) {
                    _mensajeError.value = resultado.message
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.value = "Error al crear equipo: ${e.message}"
            }
        }
    }

    fun eliminarEquipo(id: Int) {
        viewModelScope.launch {
            try {
                val resultado = repository.eliminarEquipo(id)
                if (resultado is Result.Success) {
                    cargarEquipos()
                } else if (resultado is Result.Error) {
                    _mensajeError.value = resultado.message
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.value = "Error al eliminar equipo: ${e.message}"
            }
        }
    }

    fun limpiarMensajeError() {
        _mensajeError.value = null
    }
}
