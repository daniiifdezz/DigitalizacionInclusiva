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
import org.dferna14.project.data.local.SessionStorage
import org.dferna14.project.data.repository.ExplotacionRepository
import org.dferna14.project.data.repository.TitularRepository
import org.dferna14.project.domain.model.Explotacion
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.Titular

/**
 * VM compartido por la pantalla de Configuración Inicial.
 * Gestiona Titular y Explotación bajo el supuesto de que la app es monoexplotación
 * (un solo registro de cada). Las funciones guardar* hacen UPSERT.
 */
class ConfiguracionVm(
    private val titularRepository: TitularRepository,
    private val explotacionRepository: ExplotacionRepository,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private val _titular = MutableStateFlow<Result<Titular?>>(Result.Loading)
    val titular: StateFlow<Result<Titular?>> = _titular.asStateFlow()

    private val _explotacion = MutableStateFlow<Result<Explotacion?>>(Result.Loading)
    val explotacion: StateFlow<Result<Explotacion?>> = _explotacion.asStateFlow()

    private val _guardadoExitoso = MutableSharedFlow<String>()
    val guardadoExitoso: SharedFlow<String> = _guardadoExitoso.asSharedFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _titular.value = Result.Loading
            try {
                _titular.value = titularRepository.getTitular()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _titular.value = Result.Error("Error al cargar titular: ${e.message}")
            }
        }
        viewModelScope.launch {
            _explotacion.value = Result.Loading
            try {
                _explotacion.value = explotacionRepository.getExplotacion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _explotacion.value = Result.Error("Error al cargar explotación: ${e.message}")
            }
        }
    }

    fun guardarTitular(titular: Titular) {
        viewModelScope.launch {
            try {
                val resultado = if (titular.id > 0) {
                    titularRepository.actualizarTitular(titular)
                } else {
                    titularRepository.crearTitular(titular)
                }
                when (resultado) {
                    is Result.Success -> {
                        _titular.value = Result.Success(resultado.data)
                        _guardadoExitoso.emit("Titular guardado correctamente")
                    }
                    is Result.Error -> {
                        _guardadoExitoso.emit(resultado.message)
                    }
                    else -> {}
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _guardadoExitoso.emit("Error al guardar titular: ${e.message}")
            }
        }
    }

    fun guardarExplotacion(explotacion: Explotacion) {
        viewModelScope.launch {
            try {
                val resultado = if (explotacion.id > 0) {
                    explotacionRepository.actualizarExplotacion(explotacion)
                } else {
                    explotacionRepository.crearExplotacion(explotacion)
                }
                when (resultado) {
                    is Result.Success -> {
                        sessionStorage.guardarNombreExplotacion(resultado.data.nombre)
                        _explotacion.value = Result.Success(resultado.data)
                        _guardadoExitoso.emit("Explotación guardada correctamente")
                    }
                    is Result.Error -> {
                        _guardadoExitoso.emit(resultado.message)
                    }
                    else -> {}
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _guardadoExitoso.emit("Error al guardar explotación: ${e.message}")
            }
        }
    }
}
