// ============================================================================
// viewmodels/RecordatoriosViewModel.kt
// ============================================================================
package mx.edu.utng.rpd.meditrack.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.rpd.meditrack.models.Recordatorio
import mx.edu.utng.rpd.meditrack.models.Historial
import mx.edu.utng.rpd.meditrack.repository.FirebaseRepository
import com.google.firebase.Timestamp

class RecordatoriosViewModel(private val repo: FirebaseRepository = FirebaseRepository()) : ViewModel() {

    private val _recordatorios = MutableStateFlow<List<Recordatorio>>(emptyList())
    val recordatorios = _recordatorios.asStateFlow()

    private val _historial = MutableStateFlow<List<Historial>>(emptyList())
    val historial = _historial.asStateFlow()

    private val _estadisticas = MutableStateFlow<Map<String, Int>>(emptyMap())
    val estadisticas = _estadisticas.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    fun cargarRecordatorios() {
        viewModelScope.launch {
            _loading.value = true
            val result = repo.obtenerRecordatoriosHoy()
            if (result.isSuccess) {
                _recordatorios.value = result.getOrNull() ?: emptyList()
            }
            _loading.value = false
        }
    }

    fun marcarComoTomado(recordatorio: Recordatorio) {
        viewModelScope.launch {
            // Actualizar estado del recordatorio
            repo.actualizarEstadoRecordatorio(recordatorio.id, "completado")

            // Agregar al historial
            val hist = Historial(
                medicamentoId = recordatorio.medicamentoId,
                nombreMedicamento = recordatorio.nombreMedicamento,
                dosis = recordatorio.dosis,
                fechaHora = Timestamp.now(),
                estado = "tomado"
            )
            repo.agregarHistorial(hist)

            // Recargar datos
            cargarRecordatorios()
        }
    }

    fun marcarComoOmitido(recordatorio: Recordatorio) {
        viewModelScope.launch {
            // Actualizar estado del recordatorio
            repo.actualizarEstadoRecordatorio(recordatorio.id, "omitido")

            // Agregar al historial
            val hist = Historial(
                medicamentoId = recordatorio.medicamentoId,
                nombreMedicamento = recordatorio.nombreMedicamento,
                dosis = recordatorio.dosis,
                fechaHora = Timestamp.now(),
                estado = "omitido"
            )
            repo.agregarHistorial(hist)

            // Recargar datos
            cargarRecordatorios()
        }
    }

    fun cargarHistorial() {
        viewModelScope.launch {
            _loading.value = true
            val result = repo.obtenerHistorial()
            if (result.isSuccess) {
                _historial.value = result.getOrNull() ?: emptyList()
            }
            _loading.value = false
        }
    }

    fun cargarEstadisticas() {
        viewModelScope.launch {
            _loading.value = true
            val result = repo.obtenerEstadisticas()
            if (result.isSuccess) {
                _estadisticas.value = result.getOrNull() ?: emptyMap()
            }
            _loading.value = false
        }
    }
}