package mx.edu.utng.rpd.meditrack.models

import com.google.firebase.Timestamp

data class Usuario(
    val id: String = "",
    val email: String = "",
    val nombre: String = "",
    val fechaRegistro: Timestamp = Timestamp.now()
)
