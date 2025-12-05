package mx.edu.utng.rpd.meditrack.models

import com.google.firebase.Timestamp

data class Alergia(
    val id: String = "",
    val medicamento: String = "",
    val reaccion: String = "",
    val gravedad: String = "media", // alta, media, baja
    val fechaRegistro: Timestamp = Timestamp.now()
)