package org.dferna14.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dferna14.project.data.repository.ActividadRepository
import org.dferna14.project.domain.model.Producto
import org.dferna14.project.domain.model.Result

class ProductoVm(
    private val repository: ActividadRepository
) : ViewModel() {

    private val _productos = MutableStateFlow<Result<List<Producto>>>(Result.Loading)
    val productos: StateFlow<Result<List<Producto>>> = _productos.asStateFlow()

    private val _operacionExitosa = MutableStateFlow(false)
    val operacionExitosa: StateFlow<Boolean> = _operacionExitosa.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            repository.getProductos().collect { resultado ->
                _productos.value = resultado
            }
        }
    }

    fun crearProducto(producto: Producto) {
        viewModelScope.launch {
            try {
                val resultado = repository.crearProducto(producto)
                when (resultado) {
                    is Result.Success -> {
                        _operacionExitosa.value = true
                        cargarProductos()
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
                _mensajeError.value = "Error al crear producto: ${e.message}"
            }
        }
    }

    fun eliminarProducto(id: Int) {
        viewModelScope.launch {
            try {
                val resultado = repository.eliminarProducto(id)
                if (resultado is Result.Success) {
                    cargarProductos()
                } else if (resultado is Result.Error) {
                    _mensajeError.value = resultado.message
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
}
