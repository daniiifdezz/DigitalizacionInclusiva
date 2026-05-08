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
import org.dferna14.project.domain.model.ActividadProducto
import org.dferna14.project.domain.model.Result

class ActividadDetalleVm(
    private val repository: ActividadRepository
) : ViewModel() {

    private val _actividadActual = MutableStateFlow<Result<Actividad>>(Result.Loading)
    val actividadActual: StateFlow<Result<Actividad>> = _actividadActual.asStateFlow()

    private val _operacionExitosa = MutableStateFlow(false)
    val operacionExitosa: StateFlow<Boolean> = _operacionExitosa.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    private val _productosActividad = MutableStateFlow<Result<List<ActividadProducto>>>(Result.Loading)
    val productosActividad: StateFlow<Result<List<ActividadProducto>>> = _productosActividad.asStateFlow()

    fun cargarActividad(id: Int) {
        viewModelScope.launch {
            _actividadActual.value = Result.Loading
            try {
                _actividadActual.value = repository.getActividad(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actividadActual.value = Result.Error("Error al cargar actividad: ${e.message}")
            }
        }
    }

    fun actualizarActividad(actividad: Actividad) {
        viewModelScope.launch {
            try {
                val resultado = repository.actualizarActividad(actividad)
                when (resultado) {
                    is Result.Success -> {
                        _operacionExitosa.value = true
                        _actividadActual.value = resultado
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
                _mensajeError.value = "Error al actualizar actividad: ${e.message}"
            }
        }
    }

    fun enviarActividad(id: Int) {
        viewModelScope.launch {
            try {
                val resultado = repository.enviarActividad(id)
                when (resultado) {
                    is Result.Success -> {
                        _operacionExitosa.value = true
                        _actividadActual.value = resultado
                    }
                    is Result.Error -> {
                        _mensajeError.value = resultado.message
                    }
                    else -> {}
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.value = "Error al enviar actividad: ${e.message}"
            }
        }
    }

    fun validarActividad(id: Int) {
        viewModelScope.launch {
            try {
                val resultado = repository.validarActividad(id)
                when (resultado) {
                    is Result.Success -> {
                        _operacionExitosa.value = true
                        _actividadActual.value = resultado
                    }
                    is Result.Error -> {
                        _mensajeError.value = resultado.message
                    }
                    else -> {}
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.value = "Error al validar actividad: ${e.message}"
            }
        }
    }

    fun devolverActividad(id: Int) {
        viewModelScope.launch {
            try {
                val resultado = repository.devolverActividad(id)
                when (resultado) {
                    is Result.Success -> {
                        _operacionExitosa.value = true
                    }
                    is Result.Error -> {
                        _mensajeError.value = resultado.message
                    }
                    else -> {}
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.value = "Error al devolver actividad: ${e.message}"
            }
        }
    }

    fun eliminarActividad(id: Int) {
        viewModelScope.launch {
            try {
                val resultado = repository.eliminarActividad(id)
                if (resultado is Result.Success) {
                    _operacionExitosa.value = true
                } else if (resultado is Result.Error) {
                    _mensajeError.value = resultado.message
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.value = "Error al eliminar actividad: ${e.message}"
            }
        }
    }

    // Productos aplicados a la actividad

    fun cargarProductosActividad(actividadId: Int) {
        viewModelScope.launch {
            _productosActividad.value = Result.Loading
            try {
                _productosActividad.value = repository.getActividadProductos(actividadId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _productosActividad.value = Result.Error("Error al cargar productos: ${e.message}")
            }
        }
    }

    fun añadirProducto(actividadId: Int, productoId: Int, dosis: Double) {
        viewModelScope.launch {
            try {
                val resultado = repository.crearActividadProducto(actividadId, productoId, dosis)
                when (resultado) {
                    is Result.Success -> cargarProductosActividad(actividadId)
                    is Result.Error -> _mensajeError.value = resultado.message
                    else -> {}
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.value = "Error al añadir producto: ${e.message}"
            }
        }
    }

    fun eliminarProducto(actividadProductoId: Int, actividadId: Int) {
        viewModelScope.launch {
            try {
                val resultado = repository.eliminarActividadProducto(actividadId, actividadProductoId)
                when (resultado) {
                    is Result.Success -> cargarProductosActividad(actividadId)
                    is Result.Error -> _mensajeError.value = resultado.message
                    else -> {}
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.value = "Error al eliminar producto: ${e.message}"
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
