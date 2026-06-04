package org.dferna14.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dferna14.project.data.local.SessionStorage
import org.dferna14.project.data.remote.LoginRequest
import org.dferna14.project.data.remote.RegisterRequest
import org.dferna14.project.data.remote.UsuarioDto
import org.dferna14.project.data.repository.ActividadRepository
import org.dferna14.project.domain.model.Result

/**
 * ViewModel de autenticación con persistencia de sesión (JWT).
 *
 * - `estado`: último Result de login/registro, lo consumen LoginScreen/RegisterScreen.
 * - `estadoSesion`: estado global que usa el composable raíz (App) para decidir qué
 *   mostrar: comprobando / no autenticado / autenticado.
 * - `usuarioActual`: usuario en sesión (null si no hay).
 *
 * Una única instancia se comparte en toda la app (cache del ViewModelStore vía
 * koinViewModel), de modo que login/logout en cualquier pantalla propaga el estado.
 */
class AuthVm(
    private val repository: ActividadRepository,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private val _estado = MutableStateFlow<Result<UsuarioDto>?>(null)
    val estado: StateFlow<Result<UsuarioDto>?> = _estado.asStateFlow()

    private val _estadoSesion = MutableStateFlow<EstadoSesion>(EstadoSesion.Comprobando)
    val estadoSesion: StateFlow<EstadoSesion> = _estadoSesion.asStateFlow()

    private val _usuarioActual = MutableStateFlow<UsuarioDto?>(null)
    val usuarioActual: StateFlow<UsuarioDto?> = _usuarioActual.asStateFlow()

    /**
     * Al arrancar la app: si hay token guardado, lo valida contra GET /me.
     * 200 → restaura sesión; error/401 → limpia y manda a login.
     */
    fun intentarRestaurarSesion() {
        viewModelScope.launch {
            if (!sessionStorage.haySesion()) {
                _estadoSesion.value = EstadoSesion.NoAutenticado
                return@launch
            }
            when (val resultado = repository.getMe()) {
                is Result.Success -> {
                    _usuarioActual.value = resultado.data
                    _estadoSesion.value = EstadoSesion.Autenticado(resultado.data)
                }
                is Result.Error -> {
                    sessionStorage.limpiarSesion()
                    _usuarioActual.value = null
                    _estadoSesion.value = EstadoSesion.NoAutenticado
                }
                else -> Unit
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            repository.login(LoginRequest(email = email, password = password))
                .collect { res ->
                    _estado.value = res
                    if (res is Result.Success) marcarAutenticado(res.data)
                }
        }
    }

    fun register(email: String, password: String, nombre: String) {
        viewModelScope.launch {
            repository.register(
                RegisterRequest(email = email, password = password, nombre = nombre)
            ).collect { res ->
                _estado.value = res
                if (res is Result.Success) marcarAutenticado(res.data)
            }
        }
    }

    private fun marcarAutenticado(usuario: UsuarioDto) {
        _usuarioActual.value = usuario
        _estadoSesion.value = EstadoSesion.Autenticado(usuario)
    }

    /** Limpia la sesión persistida y vuelve a login. */
    fun cerrarSesion() {
        sessionStorage.limpiarSesion()
        _usuarioActual.value = null
        _estado.value = null
        _estadoSesion.value = EstadoSesion.NoAutenticado
    }

    fun resetEstado() {
        _estado.value = null
    }
}

/**
 * Estado global de sesión observado por el composable raíz.
 */
sealed interface EstadoSesion {
    object Comprobando : EstadoSesion
    object NoAutenticado : EstadoSesion
    data class Autenticado(val usuario: UsuarioDto) : EstadoSesion
}
