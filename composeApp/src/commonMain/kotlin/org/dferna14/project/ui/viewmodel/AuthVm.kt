package org.dferna14.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dferna14.project.data.remote.LoginRequest
import org.dferna14.project.data.remote.RegisterRequest
import org.dferna14.project.data.remote.UsuarioDto
import org.dferna14.project.data.repository.ActividadRepository
import org.dferna14.project.domain.model.Result

/**
 * ViewModel del flujo de autenticación (login y registro).
 * Mantiene el último Result emitido por el repositorio y expone
 * un StateFlow de UsuarioDto autenticado para que la navegación
 * pueda reaccionar a Result.Success.
 */
class AuthVm(
    private val repository: ActividadRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<Result<UsuarioDto>?>(null)
    val estado: StateFlow<Result<UsuarioDto>?> = _estado.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            repository.login(LoginRequest(email = email, password = password))
                .collect { _estado.value = it }
        }
    }

    fun register(email: String, password: String, nombre: String) {
        viewModelScope.launch {
            repository.register(
                RegisterRequest(email = email, password = password, nombre = nombre)
            ).collect { _estado.value = it }
        }
    }

    fun resetEstado() {
        _estado.value = null
    }
}
