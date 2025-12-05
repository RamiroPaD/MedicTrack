package mx.edu.utng.rpd.meditrack.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import mx.edu.utng.rpd.meditrack.MainActivity

/**
 * BroadcastReceiver que se activa cuando llega la hora
 * de tomar un medicamento
 */
class MedicationAlarmReceiver : BroadcastReceiver() {

    /**
     * Este método se ejecuta automáticamente cuando
     * se dispara la alarma programada
     *
     * @param context: Contexto de la aplicación
     * @param intent: Intent que contiene los datos del medicamento
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Extraer información del medicamento desde el Intent
        val nombreMedicamento = intent.getStringExtra("nombre_medicamento") ?: "Medicamento"
        val dosis = intent.getStringExtra("dosis") ?: ""
        val hora = intent.getStringExtra("hora") ?: ""

        // Mostrar la notificación
        mostrarNotificacion(context, nombreMedicamento, dosis, hora)
    }

    /**
     * Crea y muestra la notificación local
     */
    private fun mostrarNotificacion(
        context: Context,
        nombreMedicamento: String,
        dosis: String,
        hora: String
    ) {
        // 1. CREAR CANAL DE NOTIFICACIÓN (obligatorio en Android 8+)
        val channelId = "meditrack_recordatorios"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // Solo necesario en Android Oreo (8.0) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Medicamentos",
                NotificationManager.IMPORTANCE_HIGH // Alta prioridad = suena y vibra
            ).apply {
                description = "Notificaciones para recordar tomar medicamentos"
                enableVibration(true) // Habilitar vibración
                enableLights(true) // Habilitar luz LED
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. CREAR INTENT para abrir la app cuando toquen la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("abrir_recordatorios", true) // Parámetro opcional
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 3. CONSTRUIR LA NOTIFICACIÓN
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Ícono (cambiar por uno personalizado)
            .setContentTitle("⏰ Hora de tomar $nombreMedicamento")
            .setContentText("$dosis a las $hora")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Es hora de tomar $nombreMedicamento ($dosis). Programado para las $hora.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridad alta
            .setCategory(NotificationCompat.CATEGORY_REMINDER) // Categoría: recordatorio
            .setAutoCancel(true) // Se elimina al tocarla
            .setContentIntent(pendingIntent) // Abre la app al tocar
            .setVibrate(longArrayOf(0, 500, 200, 500)) // Patrón de vibración
            .build()

        // 4. MOSTRAR LA NOTIFICACIÓN
        // Usar timestamp como ID único para permitir múltiples notificaciones
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
    }
}