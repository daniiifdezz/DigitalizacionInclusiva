package org.dferna14.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dferna14.project.data.repository.ActividadRepository
import org.dferna14.project.domain.model.Actividad
import org.dferna14.project.domain.model.Parcela
import org.dferna14.project.domain.model.Result

/**
 * ViewModel de Actividades.
 * Gestiona el estado de la pantalla y lanza las operaciones de datos.
 * La UI observa los StateFlow y se actualiza automáticamente.
 */
class ActividadViewModel(
    private val repository: ActividadRepository
) : ViewModel() {

    //Estado de la pantalla

    private val _actividades = MutableStateFlow<Result<List<Actividad>>>(Result.Loading)
    val actividades: StateFlow<Result<List<Actividad>>> = _actividades.asStateFlow()

    private val _parcelas = MutableStateFlow<Result<List<Parcela>>>(Result.Loading)
    val parcelas: StateFlow<Result<List<Parcela>>> = _parcelas.asStateFlow()

    private val _operacionExitosa = MutableStateFlow(false)
    val operacionExitosa: StateFlow<Boolean> = _operacionExitosa.asStateFlow()

    //Inicializacion

    init {
        cargarActividades()
        cargarParcelas()
    }

    //Acciones

    fun cargarActividades() {
        viewModelScope.launch {
            repository.getActividades().collect { resultado ->
                _actividades.value = resultado
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

    fun crearActividad(actividad: Actividad) {
        viewModelScope.launch {
            val resultado = repository.crearActividad(actividad)
            if (resultado is Result.Success) {
                _operacionExitosa.value = true
                cargarActividades() // refresca la lista
            }
        }
    }

    fun eliminarActividad(id: Int) {
        viewModelScope.launch {
            val resultado = repository.eliminarActividad(id)
            if (resultado is Result.Success) {
                cargarActividades() // refresh
            }
        }
    }

    fun resetOperacionExitosa() {
        _operacionExitosa.value = false
    }
}