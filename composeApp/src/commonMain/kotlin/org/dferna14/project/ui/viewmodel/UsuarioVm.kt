package org.dferna14.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    private val _mensajeRol = MutableStateFlow<String?>(null)
    val mensajeRol: StateFlow<String?> = _mensajeRol.asStateFlow()

    init {
        cargarUsuarios()
    }

    fun cargarUsuarios(rol: String? = null) {
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
                    cargarUsuarios()
                } else if (resultado is Result.Error) {
                    _mensajeError.value = resultado.message
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.value = "Error al crear aplicador: ${e.message}"
            }
        }
    }

    fun eliminarAplicador(id: Int) {
        viewModelScope.launch {
            try {
                val resultado = repository.eliminarUsuario(id)
                if (resultado is Result.Success) {
                    cargarUsuarios()
                } else if (resultado is Result.Error) {
                    _mensajeError.value = resultado.message
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.value = "Error al eliminar aplicador: ${e.message}"
            }
        }
    }

    /**
     * Promueve (TECNICO) o degrada (AGRICULTOR) a un usuario. Al terminar refresca
     * la lista y publica un mensaje de feedback en [mensajeRol].
     */
    fun cambiarRolUsuario(usuarioId: Int, nuevoRol: String) {
        viewModelScope.launch {
            when (val resultado = repository.cambiarRolUsuario(usuarioId, nuevoRol)) {
                is Result.Success -> {
                    _mensajeRol.value = if (nuevoRol == "TECNICO")
                        "Usuario promovido a técnico correctamente"
                    else
                        "Usuario degradado a agricultor correctamente"
                    cargarUsuarios()
                }
                is Result.Error -> _mensajeRol.value = "Error: ${resultado.message}"
                else -> Unit
            }
        }
    }

    fun limpiarMensajeError() {
        _mensajeError.value = null
    }

    fun limpiarMensajeRol() {
        _mensajeRol.value = null
    }
}
