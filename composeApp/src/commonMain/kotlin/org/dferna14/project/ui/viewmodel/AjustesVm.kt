package org.dferna14.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dferna14.project.data.local.SessionStorage
import org.dferna14.project.data.remote.BASE_URL_POR_DEFECTO

class AjustesVm(private val sessionStorage: SessionStorage) : ViewModel() {

    private val _urlActual = MutableStateFlow(obtenerUrlInicial())
    val urlActual: StateFlow<String> = _urlActual.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private fun obtenerUrlInicial(): String =
        sessionStorage.obtenerUrlBackend()?.takeIf { it.isNotBlank() } ?: BASE_URL_POR_DEFECTO

    fun guardarUrl(nuevaUrl: String) {
        viewModelScope.launch {
            val limpia = nuevaUrl.trim().trimEnd('/')

            if (limpia.isBlank()) {
                _mensaje.value = "La URL no puede estar vacía"
                return@launch
            }

            if (!limpia.startsWith("http://") && !limpia.startsWith("https://")) {
                _mensaje.value = "La URL debe empezar por http:// o https://"
                return@launch
            }

            sessionStorage.guardarUrlBackend(limpia)
            _urlActual.value = limpia
            _mensaje.value = "URL guardada. Estará activa en la siguiente petición"
        }
    }

    fun restaurarUrlPorDefecto() {
        viewModelScope.launch {
            sessionStorage.guardarUrlBackend("")
            _urlActual.value = BASE_URL_POR_DEFECTO
            _mensaje.value = "URL restaurada al valor por defecto"
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
