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
import org.dferna14.project.data.remote.DependenciasParcelaDto
import org.dferna14.project.data.repository.ActividadRepository
import org.dferna14.project.data.repository.ExplotacionRepository
import org.dferna14.project.domain.model.Cultivo
import org.dferna14.project.domain.model.DatosAgronomicos
import org.dferna14.project.domain.model.Explotacion
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.ParcelaCompleta
import org.dferna14.project.domain.model.ReferenciaSigpac
import org.dferna14.project.domain.model.Result

class ParcelaVm(
    private val repository: ActividadRepository,
    private val explotacionRepository: ExplotacionRepository
) : ViewModel() {

    private val _parcelas = MutableStateFlow<Result<List<Parcela>>>(Result.Loading)
    val parcelas: StateFlow<Result<List<Parcela>>> = _parcelas.asStateFlow()

    private val _parcelaCompleta = MutableStateFlow<Result<ParcelaCompleta?>>(Result.Loading)
    val parcelaCompleta: StateFlow<Result<ParcelaCompleta?>> = _parcelaCompleta.asStateFlow()

    private val _cultivos = MutableStateFlow<Result<List<Cultivo>>>(Result.Loading)
    val cultivos: StateFlow<Result<List<Cultivo>>> = _cultivos.asStateFlow()

    private val _explotaciones = MutableStateFlow<Result<List<Explotacion>>>(Result.Loading)
    val explotaciones: StateFlow<Result<List<Explotacion>>> = _explotaciones.asStateFlow()

    private val _operacionExitosa = MutableStateFlow(false)
    val operacionExitosa: StateFlow<Boolean> = _operacionExitosa.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    private val _guardadoExitoso = MutableSharedFlow<String>()
    val guardadoExitoso: SharedFlow<String> = _guardadoExitoso.asSharedFlow()

    init {
        cargarParcelas()
        cargarExplotaciones()
    }

    fun cargarExplotaciones() {
        viewModelScope.launch {
            _explotaciones.value = Result.Loading
            try {
                _explotaciones.value = explotacionRepository.getExplotaciones()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _explotaciones.value = Result.Error("Error al cargar explotaciones: ${e.message}")
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

    fun actualizarParcela(parcela: Parcela) {
        viewModelScope.launch {
            try {
                val resultado = repository.actualizarParcela(parcela)
                when (resultado) {
                    is Result.Success -> {
                        cargarParcelas()
                        cargarParcelaCompleta(parcela.id)
                        _guardadoExitoso.emit("Datos básicos guardados correctamente")
                    }
                    is Result.Error -> _guardadoExitoso.emit(resultado.message)
                    else -> {}
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _guardadoExitoso.emit("Error al actualizar parcela: ${e.message}")
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

    /**
     * Consulta las dependencias (datos hijos) de una parcela
     */
    suspend fun obtenerDependencias(parcelaId: Int): DependenciasParcelaDto? {
        val resultado = repository.getDependenciasParcela(parcelaId)
        return if (resultado is Result.Success) resultado.data else null
    }

    /**
     * Borrado en cascada de la parcela y todos sus datos hijos (solo Desktop/técnico).
     */
    fun eliminarParcelaEnCascada(parcelaId: Int) {
        viewModelScope.launch {
            try {
                val resultado = repository.eliminarParcelaEnCascada(parcelaId)
                if (resultado is Result.Success) {
                    cargarParcelas()
                } else if (resultado is Result.Error) {
                    _mensajeError.value = resultado.message
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.value = "No se pudo eliminar la parcela y sus datos: ${e.message}"
            }
        }
    }

    fun cargarParcelaCompleta(parcelaId: Int) {
        viewModelScope.launch {
            _parcelaCompleta.value = Result.Loading
            try {
                _parcelaCompleta.value = repository.getParcelaCompleta(parcelaId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _parcelaCompleta.value = Result.Error("Error al cargar parcela: ${e.message}")
            }
        }
    }

    fun cargarCultivos() {
        viewModelScope.launch {
            _cultivos.value = Result.Loading
            try {
                _cultivos.value = repository.getCultivos()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _cultivos.value = Result.Error("Error al cargar cultivos: ${e.message}")
            }
        }
    }

    /**
     * Upsert SIGPAC. El VM mira el estado actual de _parcelaCompleta para decidir
     * si la operación es POST (no existía aún) o PUT (ya existía).
     */
    fun guardarSigpac(parcelaId: Int, sigpac: ReferenciaSigpac) {
        viewModelScope.launch {
            try {
                val esActualizacion = (_parcelaCompleta.value as? Result.Success)
                    ?.data?.referenciaSigpac != null
                val resultado = repository.guardarSigpac(parcelaId, sigpac, esActualizacion)
                when (resultado) {
                    is Result.Success -> {
                        cargarParcelaCompleta(parcelaId)
                        _guardadoExitoso.emit("Datos SIGPAC guardados correctamente")
                    }
                    is Result.Error -> _guardadoExitoso.emit(resultado.message)
                    else -> {}
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _guardadoExitoso.emit("Error al guardar SIGPAC: ${e.message}")
            }
        }
    }

    fun guardarAgronomico(parcelaId: Int, agronomico: DatosAgronomicos) {
        viewModelScope.launch {
            try {
                val esActualizacion = (_parcelaCompleta.value as? Result.Success)
                    ?.data?.datosAgronomicos != null
                val resultado = repository.guardarAgronomico(parcelaId, agronomico, esActualizacion)
                when (resultado) {
                    is Result.Success -> {
                        cargarParcelaCompleta(parcelaId)
                        _guardadoExitoso.emit("Datos agronómicos guardados correctamente")
                    }
                    is Result.Error -> _guardadoExitoso.emit(resultado.message)
                    else -> {}
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _guardadoExitoso.emit("Error al guardar agronómicos: ${e.message}")
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
