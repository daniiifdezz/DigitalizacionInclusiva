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
}
