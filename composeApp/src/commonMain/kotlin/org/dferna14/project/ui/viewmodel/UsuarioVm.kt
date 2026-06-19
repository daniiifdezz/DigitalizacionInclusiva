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
import org.dferna14.project.domain.model.Result
import org.dferna14.project.domain.model.Usuario

class UsuarioVm(
    private val repository: ActividadRepository
) : ViewModel() {

    private val _usuarios = MutableStateFlow<Result<List<Usuario>>>(Result.Loading)
    val usuarios: StateFlow<Result<List<Usuario>>> = _usuarios.asStateFlow()

    private val _tecnicos = MutableStateFlow<Result<List<Usuario>>>(Result.Loading)
    val tecnicos: StateFlow<Result<List<Usuario>>> = _tecnicos.asStateFlow()

    private val _mensajeError = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
    val mensajeError: SharedFlow<String> = _mensajeError.asSharedFlow()

    private val _mensajeRol = MutableStateFlow<String?>(null)
    val mensajeRol: StateFlow<String?> = _mensajeRol.asStateFlow()

    private var ultimoFiltroRol: String? = null

    fun cargarTecnicos() {
        viewModelScope.launch {
            _tecnicos.value = Result.Loading
            try {
                _tecnicos.value = repository.getUsuarios("TECNICO")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _tecnicos.value = Result.Error("Error al cargar técnicos: ${e.message}")
            }
        }
    }

    fun cargarUsuarios(rol: String? = null) {
        ultimoFiltroRol = rol
        viewModelScope.launch {
            _usuarios.value = Result.Loading
            try {
                _usuarios.value = repository.getUsuarios(rol)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _usuarios.value = Result.Error("Error al cargar usuarios: ${e.message}")
            }
        }
    }

    fun crearAplicador(usuario: Usuario, contrasena: String? = null) {
        viewModelScope.launch {
            try {
                val resultado = repository.crearUsuario(usuario, contrasena)
                if (resultado is Result.Success) {
                    cargarUsuarios(ultimoFiltroRol)
                } else if (resultado is Result.Error) {
                    _mensajeError.tryEmit(resultado.message)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.tryEmit("Error al crear aplicador: ${e.message}")
            }
        }
    }

    fun eliminarAplicador(id: Int) {
        viewModelScope.launch {
            try {
                val resultado = repository.eliminarUsuario(id)
                if (resultado is Result.Success) {
                    cargarUsuarios(ultimoFiltroRol)
                } else if (resultado is Result.Error) {
                    _mensajeError.tryEmit(resultado.message)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.tryEmit("Error al eliminar aplicador: ${e.message}")
            }
        }
    }


    fun cambiarRolUsuario(usuarioId: Int, nuevoRol: String) {
        viewModelScope.launch {
            when (val resultado = repository.cambiarRolUsuario(usuarioId, nuevoRol)) {
                is Result.Success -> {
                    _mensajeRol.value = if (nuevoRol == "TECNICO")
                        "Usuario promovido a técnico correctamente"
                    else
                        "Usuario degradado a agricultor correctamente"
                    cargarUsuarios(rol = "AGRICULTOR")
                    cargarTecnicos()
                }
                is Result.Error -> _mensajeRol.value = "Error: ${resultado.message}"
                else -> Unit
            }
        }
    }

    fun limpiarMensajeRol() {
        _mensajeRol.value = null
    }
}
