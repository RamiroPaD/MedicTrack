// ============================================================================
// viewmodels/MedicamentosViewModel.kt - CORREGIDO
// ============================================================================
package mx.edu.utng.rpd.meditrack.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.rpd.meditrack.models.Medicamento
import mx.edu.utng.rpd.meditrack.repository.FirebaseRepository

class MedicamentosViewModel(private val repo: FirebaseRepository = FirebaseRepository()) : ViewModel() {

    private val _medicamentos = MutableStateFlow<List<Medicamento>>(emptyList())
    val medicamentos = _medicamentos.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()

    init {
        cargarMedicamentos()
    }

    fun cargarMedicamentos() {
        viewModelScope.launch {
            _loading.value = true
            val result = repo.obtenerMedicamentos()
            if (result.isSuccess) {
                _medicamentos.value = result.getOrNull() ?: emptyList()
            } else {
                _mensaje.value = "Error al cargar medicamentos"
            }
            _loading.value = false
        }
    }

    fun agregarMedicamento(medicamento: Medicamento, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val result = repo.agregarMedicamento(medicamento)
                if (result.isSuccess) {
                    cargarMedicamentos()
                    _mensaje.value = "Medicamento agregado correctamente"
                    onSuccess()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    _mensaje.value = error
                    onError(error)
                }
            } catch (e: Exception) {
                val error = e.message ?: "Error al agregar medicamento"
                _mensaje.value = error
                onError(error)
            } finally {
                _loading.value = false
            }
        }
    }

    fun eliminarMedicamento(medId: String) {
        viewModelScope.launch {
            _loading.value = true
            val result = repo.eliminarMedicamento(medId)
            if (result.isSuccess) {
                cargarMedicamentos()
                _mensaje.value = "Medicamento eliminado"
            } else {
                _mensaje.value = "Error al eliminar"
            }
            _loading.value = false
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
