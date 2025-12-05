package mx.edu.utng.rpd.meditrack.models

import com.google.firebase.Timestamp

data class Historial(
    val id: String = "",
    val medicamentoId: String = "",
    val nombreMedicamento: String = "",
    val dosis: String = "",
    val fechaHora: Timestamp = Timestamp.now(),
    val estado: String = "tomado" // tomado, omitido
)