// ============================================================================
// repository/FirebaseRepository.kt - OPTIMIZADO
// ============================================================================
package mx.edu.utng.rpd.meditrack.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.tasks.await
import mx.edu.utng.rpd.meditrack.models.*
import com.google.firebase.Timestamp
import java.util.*

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // ========== AUTENTICACIÓN ==========

    suspend fun registrarUsuario(email: String, password: String, nombre: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("Error al obtener ID")

            val usuario = Usuario(
                id = userId,
                email = email,
                nombre = nombre
            )

            db.collection("usuarios").document(userId).set(usuario).await()
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarSesion(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("Error al iniciar sesión")
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    fun obtenerUsuarioActual() = auth.currentUser?.uid

    // ========== MEDICAMENTOS ==========

    suspend fun agregarMedicamento(med: Medicamento): Result<String> {
        return try {
            val userId = obtenerUsuarioActual() ?: throw Exception("Usuario no autenticado")
            val docRef = db.collection("usuarios").document(userId)
                .collection("medicamentos").document()

            val medConId = med.copy(id = docRef.id)
            docRef.set(medConId).await()

            // Crear recordatorios en lote (MUCHO MÁS RÁPIDO)
            crearRecordatoriosParaMedicamentoOptimizado(medConId)

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerMedicamentos(): Result<List<Medicamento>> {
        return try {
            val userId = obtenerUsuarioActual() ?: throw Exception("Usuario no autenticado")
            val snapshot = db.collection("usuarios").document(userId)
                .collection("medicamentos")
                .whereEqualTo("activo", true)
                .get()
                .await()

            val meds = snapshot.documents.mapNotNull { it.toObject(Medicamento::class.java) }
            Result.success(meds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarMedicamento(medId: String): Result<Unit> {
        return try {
            val userId = obtenerUsuarioActual() ?: throw Exception("Usuario no autenticado")
            db.collection("usuarios").document(userId)
                .collection("medicamentos")
                .document(medId)
                .update("activo", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== RECORDATORIOS OPTIMIZADOS ==========

    // VERSIÓN OPTIMIZADA: Usa WriteBatch para crear todos los recordatorios de una vez
    private suspend fun crearRecordatoriosParaMedicamentoOptimizado(medicamento: Medicamento): Result<List<String>> {
        return try {
            val userId = obtenerUsuarioActual() ?: throw Exception("Usuario no autenticado")
            val recordatoriosIds = mutableListOf<String>()

            // Usar WriteBatch para operaciones en lote (límite: 500 operaciones)
            val batch: WriteBatch = db.batch()
            val calendar = Calendar.getInstance()

            // Crear recordatorios para los próximos 7 días
            for (dia in 0..6) {
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.DAY_OF_MONTH, dia)

                medicamento.horarios.forEach { hora ->
                    val horaPartes = hora.split(":")
                    calendar.set(Calendar.HOUR_OF_DAY, horaPartes[0].toInt())
                    calendar.set(Calendar.MINUTE, horaPartes[1].toInt())
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    // Solo crear recordatorios futuros
                    if (calendar.timeInMillis > System.currentTimeMillis()) {
                        val docRef = db.collection("usuarios").document(userId)
                            .collection("recordatorios").document()

                        val recordatorio = Recordatorio(
                            id = docRef.id,
                            medicamentoId = medicamento.id,
                            nombreMedicamento = medicamento.nombre,
                            dosis = medicamento.dosis,
                            fecha = Timestamp(Date(calendar.timeInMillis)),
                            hora = hora,
                            estado = "pendiente",
                            notificado = false
                        )

                        batch.set(docRef, recordatorio)
                        recordatoriosIds.add(docRef.id)
                    }
                }
            }

            // Ejecutar todas las operaciones de una vez
            batch.commit().await()

            Result.success(recordatoriosIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerRecordatoriosHoy(): Result<List<Recordatorio>> {
        return try {
            val userId = obtenerUsuarioActual() ?: throw Exception("Usuario no autenticado")

            // Obtener inicio y fin del día actual
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val inicioDia = Timestamp(calendar.time)

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val finDia = Timestamp(calendar.time)

            val snapshot = db.collection("usuarios").document(userId)
                .collection("recordatorios")
                .whereGreaterThanOrEqualTo("fecha", inicioDia)
                .whereLessThanOrEqualTo("fecha", finDia)
                .orderBy("fecha", Query.Direction.ASCENDING)
                .get()
                .await()

            val recs = snapshot.documents.mapNotNull { it.toObject(Recordatorio::class.java) }
                .sortedWith(compareBy<Recordatorio> { it.hora.split(":")[0].toInt() }
                    .thenBy { it.hora.split(":")[1].toInt() })

            Result.success(recs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarEstadoRecordatorio(recId: String, estado: String): Result<Unit> {
        return try {
            val userId = obtenerUsuarioActual() ?: throw Exception("Usuario no autenticado")
            db.collection("usuarios").document(userId)
                .collection("recordatorios")
                .document(recId)
                .update("estado", estado)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerRecordatoriosPendientes(): Result<List<Recordatorio>> {
        return try {
            val userId = obtenerUsuarioActual() ?: throw Exception("Usuario no autenticado")

            val ahora = System.currentTimeMillis()
            val en30Minutos = ahora + (30 * 60 * 1000)

            val snapshot = db.collection("usuarios").document(userId)
                .collection("recordatorios")
                .whereEqualTo("estado", "pendiente")
                .whereEqualTo("notificado", false)
                .get()
                .await()

            val recs = snapshot.documents.mapNotNull {
                it.toObject(Recordatorio::class.java)
            }.filter { rec ->
                val recTime = rec.fecha.toDate().time
                recTime in ahora..en30Minutos
            }

            Result.success(recs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun marcarRecordatorioComoNotificado(recId: String): Result<Unit> {
        return try {
            val userId = obtenerUsuarioActual() ?: throw Exception("Usuario no autenticado")
            db.collection("usuarios").document(userId)
                .collection("recordatorios")
                .document(recId)
                .update("notificado", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== HISTORIAL ==========

    suspend fun agregarHistorial(hist: Historial): Result<String> {
        return try {
            val userId = obtenerUsuarioActual() ?: throw Exception("Usuario no autenticado")
            val docRef = db.collection("usuarios").document(userId)
                .collection("historial").document()

            val histConId = hist.copy(id = docRef.id)
            docRef.set(histConId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerHistorial(): Result<List<Historial>> {
        return try {
            val userId = obtenerUsuarioActual() ?: throw Exception("Usuario no autenticado")
            val snapshot = db.collection("usuarios").document(userId)
                .collection("historial")
                .orderBy("fechaHora", Query.Direction.DESCENDING)
                .get()
                .await()

            val hist = snapshot.documents.mapNotNull { it.toObject(Historial::class.java) }
            Result.success(hist)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== ALERGIAS ==========

    suspend fun agregarAlergia(alergia: Alergia): Result<String> {
        return try {
            val userId = obtenerUsuarioActual() ?: throw Exception("Usuario no autenticado")
            val docRef = db.collection("usuarios").document(userId)
                .collection("alergias").document()

            val alergiaConId = alergia.copy(id = docRef.id)
            docRef.set(alergiaConId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerAlergias(): Result<List<Alergia>> {
        return try {
            val userId = obtenerUsuarioActual() ?: throw Exception("Usuario no autenticado")
            val snapshot = db.collection("usuarios").document(userId)
                .collection("alergias")
                .get()
                .await()

            val alergias = snapshot.documents.mapNotNull { it.toObject(Alergia::class.java) }
            Result.success(alergias)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarAlergia(alergiaId: String): Result<Unit> {
        return try {
            val userId = obtenerUsuarioActual() ?: throw Exception("Usuario no autenticado")
            db.collection("usuarios").document(userId)
                .collection("alergias")
                .document(alergiaId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verificarAlergia(nombreMedicamento: String): Result<Alergia?> {
        return try {
            val userId = obtenerUsuarioActual() ?: throw Exception("Usuario no autenticado")
            val snapshot = db.collection("usuarios").document(userId)
                .collection("alergias")
                .get()
                .await()

            val alergia = snapshot.documents
                .mapNotNull { it.toObject(Alergia::class.java) }
                .find { it.medicamento.equals(nombreMedicamento, ignoreCase = true) }

            Result.success(alergia)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== ESTADÍSTICAS ==========

    suspend fun obtenerEstadisticas(): Result<Map<String, Int>> {
        return try {
            val userId = obtenerUsuarioActual() ?: throw Exception("Usuario no autenticado")

            val histSnapshot = db.collection("usuarios").document(userId)
                .collection("historial")
                .get()
                .await()

            val tomados = histSnapshot.documents.count {
                it.getString("estado") == "tomado"
            }
            val omitidos = histSnapshot.documents.count {
                it.getString("estado") == "omitido"
            }

            val total = tomados + omitidos
            val adherencia = if (total > 0) ((tomados.toFloat() / total) * 100).toInt() else 0

            Result.success(mapOf(
                "adherencia" to adherencia,
                "tomados" to tomados,
                "omitidos" to omitidos
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}