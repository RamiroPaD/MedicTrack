package mx.edu.utng.rpd.meditrack.models

import com.google.firebase.Timestamp

data class Recordatorio(
    val id: String = "",
    val medicamentoId: String = "",
    val nombreMedicamento: String = "",
    val dosis: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val hora: String = "",
    val estado: String = "pendiente", // pendiente, completado, omitido
    val notificado: Boolean = false
)