package mx.edu.utng.rpd.meditrack.models

import com.google.firebase.Timestamp

data class Medicamento(
    val id: String = "",
    val nombre: String = "",
    val dosis: String = "",
    val cantidad: String = "",
    val frecuencia: String = "",
    val horarios: List<String> = emptyList(),
    val fechaInicio: Timestamp = Timestamp.now(),
    val activo: Boolean = true
)