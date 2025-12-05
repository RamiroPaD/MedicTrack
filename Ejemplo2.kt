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

/**
 * ViewModel encargado de manejar la lógica relacionada con:
 * - Recordatorios del día
 * - Marcado de recordatorios como tomados u omitidos
 * - Historial de medicamentos
 * - Estadísticas de cumplimiento
 *
 * Utiliza corrutinas y StateFlow para actualizar y exponer datos reactivos a la UI.
 */
class RecordatoriosViewModel(
    private val repo: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    // ============================
    // StateFlows observables
    // ============================

    /** Lista de recordatorios del día actual */
    private val _recordatorios = MutableStateFlow<List<Recordatorio>>(emptyList())
    val recordatorios = _recordatorios.asStateFlow()

    /** Historial completo de eventos (tomas y omisiones) */
    private val _historial = MutableStateFlow<List<Historial>>(emptyList())
    val historial = _historial.asStateFlow()

    /** Estadísticas de cumplimiento (ej. tomados vs omitidos) */
    private val _estadisticas = MutableStateFlow<Map<String, Int>>(emptyMap())
    val estadisticas = _estadisticas.asStateFlow()

    /** Estado de carga para mostrar u ocultar indicadores */
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    // ============================
    // Métodos principales
    // ============================

    /**
     * Carga los recordatorios correspondientes al día de hoy.
     */
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

    /**
     * Marca un recordatorio como tomado.
     *
     * - Actualiza el estado del recordatorio en Firebase.
     * - Registra el evento en el historial.
     * - Recarga la lista de recordatorios.
     *
     * @param recordatorio Recordatorio a marcar como tomado.
     */
    fun marcarComoTomado(recordatorio: Recordatorio) {
        viewModelScope.launch {

            // 1. Actualizar el estado del recordatorio
            repo.actualizarEstadoRecordatorio(recordatorio.id, "completado")

            // 2. Registrar evento en historial
            val hist = Historial(
                medicamentoId = recordatorio.medicamentoId,
                nombreMedicamento = recordatorio.nombreMedicamento,
                dosis = recordatorio.dosis,
                fechaHora = Timestamp.now(),
                estado = "tomado"
            )
            repo.agregarHistorial(hist)

            // 3. Recargar recordatorios
            cargarRecorditorios()
        }
    }

    /**
     * Marca un recordatorio como omitido.
     *
     * - Actualiza su estado en Firebase.
     * - Agrega una entrada al historial.
     * - Recarga la lista de recordatorios.
     *
     * @param recordatorio Recordatorio a marcar como omitido.
     */
    fun marcarComoOmitido(recordatorio: Recordatorio) {
        viewModelScope.launch {

            repo.actualizarEstadoRecordatorio(recordatorio.id, "omitido")

            val hist = Historial(
                medicamentoId = recordatorio.medicamentoId,
                nombreMedicamento = recordatorio.nombreMedicamento,
                dosis = recordatorio.dosis,
                fechaHora = Timestamp.now(),
                estado = "omitido"
            )
            repo.agregarHistorial(hist)

            cargarRecordatorios()
        }
    }

    /**
     * Carga el historial de eventos (tomas, omisiones, fechas, etc.).
     */
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

    /**
     * Carga las estadísticas generales del usuario.
     * Ejemplo de datos esperados:
     * - "tomados" -> 15
     * - "omitidos" -> 4
     */
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
