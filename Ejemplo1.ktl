package mx.edu.utng.rpd.meditrack.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.rpd.meditrack.models.Medicamento
import mx.edu.utng.rpd.meditrack.repository.FirebaseRepository

/**
 * ViewModel encargado de manejar la lógica relacionada con la gestión
 * de medicamentos, incluyendo carga, creación y eliminación.
 *
 * Utiliza corrutinas y StateFlow para exponer estados inmutables a la UI.
 */
class MedicamentosViewModel(
    private val repo: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    // ============================
    // StateFlows observables
    // ============================

    /** Lista de medicamentos obtenida de Firebase */
    private val _medicamentos = MutableStateFlow<List<Medicamento>>(emptyList())
    val medicamentos = _medicamentos.asStateFlow()

    /** Estado de carga (loading) para mostrar u ocultar un indicador en la UI */
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    /** Mensajes de error o éxito para mostrar retroalimentación al usuario */
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()

    /**
     * Se ejecuta al inicializar el ViewModel.
     * Carga automáticamente la lista de medicamentos.
     */
    init {
        cargarMedicamentos()
    }

    // ============================
    // Métodos principales
    // ============================

    /**
     * Obtiene los medicamentos desde el repositorio y actualiza el estado.
     */
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

    /**
     * Agrega un medicamento a Firebase.
     *
     * @param medicamento objeto Medicamento a agregar.
     * @param onSuccess callback cuando la operación es exitosa.
     * @param onError callback con un mensaje de error si falla.
     */
    fun agregarMedicamento(
        medicamento: Medicamento,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true

            try {
                val result = repo.agregarMedicamento(medicamento)

                if (result.isSuccess) {
                    cargarMedicamentos() // Refresca la lista
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

    /**
     * Elimina un medicamento de Firebase mediante su ID.
     *
     * @param medId ID del medicamento a eliminar.
     */
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

    /**
     * Limpia el mensaje actual (sirve para evitar que la UI lo muestre repetidamente).
     */
    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
