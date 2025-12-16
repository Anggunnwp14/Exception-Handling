package com.example.ukonnect2.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ukonnect2.R
import android.widget.Toast

private const val CHANNEL_ID_ABSENSI = "absensi_reminder_channel"
private const val TAG = "AbsensiReceiver"
private const val ACTION_ABSENSI_MASUK = "com.example.ukonnect2.ACTION_ABSENSI_MASUK"
private const val ACTION_ABSENSI_PULANG = "com.example.ukonnect2.ACTION_ABSENSI_PULANG"

class AbsensiReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("ABSENSI_TYPE") ?: return
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Log.d(TAG, "Notifikasi Absensi ${type} Diterima & Diproses.")

        val (title, content, notificationId) = when (type) {
            "Masuk" -> Triple(
                "⏰ Saatnya Absensi Masuk!",
                "Waktu check-in sebentar lagi. Jangan lupa absen!",
                1001
            )
            "Pulang" -> Triple(
                "🚪 Waktunya Absensi Pulang",
                "Jangan lupa Check-Out sebelum meninggalkan lokasi.",
                1002
            )
            else -> return
        }

        // PASTIKAN ic_calendar ADA (Sesuai screenshot kamu, ini aman)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ABSENSI)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)

        // Scheduler ulang untuk besok bisa ditambahkan di sini jika perlu logic complex
        // Tapi logic reschedule harian sudah ada di Scheduler
    }
}