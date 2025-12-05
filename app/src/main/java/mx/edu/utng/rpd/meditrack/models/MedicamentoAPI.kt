
package mx.edu.utng.rpd.meditrack.models

// ============================================================================
// models/MedicamentoAPI.kt - NUEVO ARCHIVO
// ============================================================================

data class MedicamentoAPI(
    val nombre: String,
    val concentraciones: List<String>,
    val presentaciones: List<String>,
    val usos: String
)