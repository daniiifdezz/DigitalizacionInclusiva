package org.dferna14.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dferna14.project.data.repository.ActividadRepository
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Result

class ActividadViewModel(
    private val repository: ActividadRepository
) : ViewModel() {

    private val _actividades = MutableStateFlow<Result<List<Actividad>>>(Result.Loading)
    val actividades: StateFlow<Result<List<Actividad>>> = _actividades.asStateFlow()

    private val _actividadesPendientes = MutableStateFlow<Result<List<Actividad>>>(Result.Loading)
    val actividadesPendientes: StateFlow<Result<List<Actividad>>> = _actividadesPendientes.asStateFlow()

    private val _parcelas = MutableStateFlow<Result<List<Parcela>>>(Result.Loading)
    val parcelas: StateFlow<Result<List<Parcela>>> = _parcelas.asStateFlow()

    private val _actividadActual = MutableStateFlow<Result<Actividad>>(Result.Loading)
    val actividadActual: StateFlow<Result<Actividad>> = _actividadActual.asStateFlow()

    private val _operacionExitosa = MutableStateFlow(false)
    val operacionExitosa: StateFlow<Boolean> = _operacionExitosa.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    init {
        cargarActividades()
        cargarParcelas()
    }

    fun cargarActividades() {
        viewModelScope.launch {
            repository.getActividades().collect { resultado ->
                _actividades.value = resultado
            }
        }
    }

    fun cargarActividadesPendientes() {
        viewModelScope.launch {
            repository.getActividadesPendientes().collect { resultado ->
                _actividadesPendientes.value = resultado
            }
        }
    }

    fun cargarParcelas() {
        viewModelScope.launch {
            repository.getParcelas().collect { resultado ->
                _parcelas.value = resultado
            }
        }
    }

    fun cargarActividad(id: Int) {
        viewModelScope.launch {
            _actividadActual.value = Result.Loading
            val resultado = repository.getActividad(id)
            _actividadActual.value = resultado
        }
    }

    fun crearActividad(actividad: Actividad) {
        viewModelScope.launch {
            val resultado = repository.crearActividad(actividad)
            when (resultado) {
                is Result.Success -> {
                    _operacionExitosa.value = true
                    cargarActividades()
                }
                is Result.Error -> {
                    _operacionExitosa.value = false
                    _mensajeError.value = resultado.message
                }
                else -> {}
            }
        }
    }

    fun actualizarActividad(actividad: Actividad) {
        viewModelScope.launch {
            val resultado = repository.actualizarActividad(actividad)
            when (resultado) {
                is Result.Success -> {
                    _operacionExitosa.value = true
                    _actividadActual.value = resultado
                    cargarActividades()
                }
                is Result.Error -> {
                    _operacionExitosa.value = false
                    _mensajeError.value = resultado.message
                }
                else -> {}
            }
        }
    }

    fun enviarActividad(id: Int) {
        viewModelScope.launch {
            val resultado = repository.enviarActividad(id)
            when (resultado) {
                is Result.Success -> {
                    _operacionExitosa.value = true
                    _actividadActual.value = resultado
                    cargarActividades()
                }
                is Result.Error -> {
                    _mensajeError.value = resultado.message
                }
                else -> {}
            }
        }
    }

    fun validarActividad(id: Int) {
        viewModelScope.launch {
            val resultado = repository.validarActividad(id)
            when (resultado) {
                is Result.Success -> {
                    _operacionExitosa.value = true
                    _actividadActual.value = resultado
                    cargarActividades()
                }
                is Result.Error -> {
                    _mensajeError.value = resultado.message
                }
                else -> {}
            }
        }
    }

    fun devolverActividad(id: Int) {
        viewModelScope.launch {
            val resultado = repository.devolverActividad(id)
            when (resultado) {
                is Result.Success -> {
                    _operacionExitosa.value = true
                    cargarActividades()
                }
                is Result.Error -> {
                    _mensajeError.value = resultado.message
                }
                else -> {}
            }
        }
    }

    fun eliminarActividad(id: Int) {
        viewModelScope.launch {
            val resultado = repository.eliminarActividad(id)
            if (resultado is Result.Success) {
                cargarActividades()
            }
        }
    }

    fun resetOperacionExitosa() {
        _operacionExitosa.value = false
    }

    fun limpiarMensajeError() {
        _mensajeError.value = null
    }

    fun limpiarActividadActual() {
        _actividadActual.value = Result.Loading
    }
}