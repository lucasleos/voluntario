package unpsjb.ing.tntpm2024.voluntario.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import unpsjb.ing.tntpm2024.voluntario.MainActivity
import unpsjb.ing.tntpm2024.voluntario.R
import unpsjb.ing.tntpm2024.voluntario.data.local.PreferenceHelper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Si el usuario ya tiene un turno registrado, actualizamos su token en Firebase
        val turnoId = PreferenceHelper.getTurnoId(applicationContext)
        if (turnoId != null) {
            FirebaseDatabase.getInstance().getReference("turnos")
                .child(turnoId)
                .child("fcmToken")
                .setValue(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val titulo = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Turno Asignado"
        val cuerpo = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Se ha definido la fecha y lugar de tu encuesta."

        mostrarNotificacion(titulo, cuerpo)
    }

    private fun mostrarNotificacion(titulo: String, cuerpo: String) {
        val channelId = "canal_turnos_voluntario"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                channelId,
                "Notificaciones de Turnos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos sobre asignación de turnos presenciales"
            }
            notificationManager.createNotificationChannel(canal)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // O un icono propio
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}