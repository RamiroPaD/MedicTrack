// ============================================================================
// utils/NotificationHelper.kt - NUEVO ARCHIVO
// ============================================================================
package mx.edu.utng.rpd.meditrack.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import mx.edu.utng.rpd.meditrack.MainActivity
import mx.edu.utng.rpd.meditrack.models.Recordatorio
import mx.edu.utng.rpd.meditrack.services.MedicationAlarmReceiver
import java.util.*

object NotificationHelper {

    private const val CHANNEL_ID = "meditrack_recordatorios"
    private const val CHANNEL_NAME = "Recordatorios de Medicamentos"

    // Crear canal de notificaciones (Android 8.0+)
    fun crearCanalNotificaciones(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para recordatorios de medicamentos"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Programar alarma para un recordatorio
    fun programarAlarma(context: Context, recordatorio: Recordatorio) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Crear intent para el receiver
        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra("recordatorio_id", recordatorio.id)
            putExtra("medicamento_nombre", recordatorio.nombreMedicamento)
            putExtra("medicamento_dosis", recordatorio.dosis)
            putExtra("hora", recordatorio.hora)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            recordatorio.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calcular tiempo de la alarma
        val calendar = Calendar.getInstance().apply {
            timeInMillis = recordatorio.fecha.toDate().time
            val horaPartes = recordatorio.hora.split(":")
            set(Calendar.HOUR_OF_DAY, horaPartes[0].toInt())
            set(Calendar.MINUTE, horaPartes[1].toInt())
            set(Calendar.SECOND, 0)
        }

        // Solo programar si es en el futuro
        if (calendar.timeInMillis > System.currentTimeMillis()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}