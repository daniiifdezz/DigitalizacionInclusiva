package org.dferna14.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dferna14.project.data.repository.ActividadRepository
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.Result

class ActividadListaVm(
    private val repository: ActividadRepository
) : ViewModel() {

    private val _actividades = MutableStateFlow<Result<List<Actividad>>>(Result.Loading)
    val actividades: StateFlow<Result<List<Actividad>>> = _actividades.asStateFlow()

    private val _actividadesPendientes = MutableStateFlow<Result<List<Actividad>>>(Result.Loading)
    val actividadesPendientes: StateFlow<Result<List<Actividad>>> = _actividadesPendientes.asStateFlow()

    private val _operacionExitosa = MutableStateFlow(false)
    val operacionExitosa: StateFlow<Boolean> = _operacionExitosa.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    init {
        cargarActividades()
        cargarActividadesPendientes()
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

    fun crearActividad(actividad: Actividad) {
        viewModelScope.launch {
            try {
                val resultado = repository.crearActividad(actividad)
                when (resultado) {
                    is Result.Success -> {
                        _operacionExitosa.value = true
                        cargarActividades()
                        cargarActividadesPendientes()
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
                _mensajeError.value = "Error al crear actividad: ${e.message}"
            }
        }
    }

    // Crea la actividad y asocia varios productos en cascada.
    // usuario selecciona N productos con dosis y todos quedan persistidos al pulsar "Guardar actividad".
    fun crearActividadConProductos(
        actividad: Actividad,
        productos: List<Pair<Int, Double>>
    ) {
        viewModelScope.launch {
            try {
                val resultado = repository.crearActividad(actividad)
                if (resultado is Result.Success) {
                    val actividadId = resultado.data.id
                    productos.forEach { (productoId, dosis) ->
                        val r = repository.crearActividadProducto(actividadId, productoId, dosis)
                        if (r is Result.Error) {
                            _mensajeError.value = r.message
                        }
                    }
                    _operacionExitosa.value = true
                    cargarActividades()
                    cargarActividadesPendientes()
                } else if (resultado is Result.Error) {
                    _operacionExitosa.value = false
                    _mensajeError.value = resultado.message
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.value = "Error al crear actividad: ${e.message}"
            }
        }
    }

    fun eliminarActividad(id: Int) {
        viewModelScope.launch {
            val resultado = repository.eliminarActividad(id)
            if (resultado is Result.Success) {
                cargarActividades()
                cargarActividadesPendientes()
            } else if (resultado is Result.Error) {
                _mensajeError.value = resultado.message
            }
        }
    }

    // Pre-relleno de superficie en formularios al elegir parcela.
    suspend fun getSuperficieParcela(parcelaId: Int): Double? =
        repository.getSuperficieParcela(parcelaId)

    fun resetOperacionExitosa() {
        _operacionExitosa.value = false
    }

    fun limpiarMensajeError() {
        _mensajeError.value = null
    }
}
