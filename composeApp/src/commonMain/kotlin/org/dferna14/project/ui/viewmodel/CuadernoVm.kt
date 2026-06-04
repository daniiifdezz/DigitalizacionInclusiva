package org.dferna14.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dferna14.project.data.repository.CuadernoRepository
import org.dferna14.project.domain.model.Result

/**
 * ViewModel de la pantalla de generación del PDF del Cuaderno de Campo (Desktop).
 *
 * Expone el estado de la descarga como StateFlow. Al obtener los bytes invoca el
 * callback `onBytesObtenidos` (que en Desktop abre el diálogo nativo para guardar
 * en disco) y pasa a estado Exito. El guardado en disco se delega a la UI porque
 * depende de la plataforma (expect/actual).
 */
class CuadernoVm(private val repository: CuadernoRepository) : ViewModel() {

    private val _estadoDescarga = MutableStateFlow<EstadoDescargaPdf>(EstadoDescargaPdf.Idle)
    val estadoDescarga: StateFlow<EstadoDescargaPdf> = _estadoDescarga.asStateFlow()

    fun descargarPdf(desde: String, hasta: String, onBytesObtenidos: (ByteArray) -> Unit) {
        _estadoDescarga.value = EstadoDescargaPdf.Descargando
        viewModelScope.launch {
            when (val resultado = repository.descargarPdf(desde, hasta)) {
                is Result.Success -> {
                    onBytesObtenidos(resultado.data)
                    _estadoDescarga.value = EstadoDescargaPdf.Exito
                }
                is Result.Error -> {
                    _estadoDescarga.value = EstadoDescargaPdf.Error(resultado.message)
                }
                else -> Unit
            }
        }
    }

    fun resetearEstado() {
        _estadoDescarga.value = EstadoDescargaPdf.Idle
    }
}

sealed interface EstadoDescargaPdf {
    object Idle : EstadoDescargaPdf
    object Descargando : EstadoDescargaPdf
    object Exito : EstadoDescargaPdf
    data class Error(val mensaje: String) : EstadoDescargaPdf
}
