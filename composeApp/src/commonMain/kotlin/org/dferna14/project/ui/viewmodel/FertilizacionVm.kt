package org.dferna14.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dferna14.project.data.repository.ActividadRepository
import org.dferna14.project.domain.model.Fertilizacion
import org.dferna14.project.domain.model.Producto
import org.dferna14.project.domain.model.Result

class FertilizacionVm(
    private val repository: ActividadRepository
) : ViewModel() {

    private val _fertilizacion = MutableStateFlow<Result<Fertilizacion?>>(Result.Loading)
    val fertilizacion: StateFlow<Result<Fertilizacion?>> = _fertilizacion.asStateFlow()

    // Catálogo de fertilizantes que se ofrece en el dropdown de la pantalla móvil.
    private val _fertilizantes = MutableStateFlow<Result<List<Producto>>>(Result.Loading)
    val fertilizantes: StateFlow<Result<List<Producto>>> = _fertilizantes.asStateFlow()

    private val _operacionExitosa = MutableStateFlow(false)
    val operacionExitosa: StateFlow<Boolean> = _operacionExitosa.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    private val _guardadoExitoso = MutableSharedFlow<String>()
    val guardadoExitoso: SharedFlow<String> = _guardadoExitoso.asSharedFlow()

    init {
        cargarFertilizantes()
    }

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

    fun cargarFertilizantes() {
        viewModelScope.launch {
            _fertilizantes.value = Result.Loading
            try {
                _fertilizantes.value = repository.getFertilizantes()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _fertilizantes.value = Result.Error("Error al cargar fertilizantes: ${e.message}")
            }
        }
    }


    fun guardarFertilizacion(
        actividadId: Int,
        aplica: Boolean,
        productoId: Int?,
        riquezaNpk: String?,
        numeroAlbaran: String?,
        dosis: Double?,
        observaciones: String?,
        fechaInicio: String?
    ) {
        viewModelScope.launch {
            try {
                val fertilizacion = Fertilizacion(
                    actividadId   = actividadId,
                    productoId    = productoId,
                    aplica        = aplica,
                    fechaInicio   = fechaInicio,
                    numeroAlbaran = numeroAlbaran,
                    riquezaNpk    = riquezaNpk,
                    dosis         = dosis,
                    observaciones = observaciones
                )
                val resultado = repository.guardarFertilizacion(actividadId, fertilizacion)
                if (resultado is Result.Success) {
                    _operacionExitosa.value = true
                    _fertilizacion.value = Result.Success(resultado.data)
                    _guardadoExitoso.emit("Fertilización guardada correctamente")
                } else if (resultado is Result.Error) {
                    _mensajeError.value = resultado.message
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.value = "Error al guardar fertilización: ${e.message}"
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
