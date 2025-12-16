package com.example.ukonnect2.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ukonnect2.R
import android.widget.Toast
import java.util.*

private const val CHANNEL_ID_GALERI = "galeri_reminder_channel"
private const val TAG = "GaleriReceiver"
private const val NOTIFICATION_ID = 302

class GaleriReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Toast.makeText(context, "Pengecekan Galeri Harian Dipicu!", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Notifikasi Diterima untuk cek Galeri.")

        // PENTING: Untuk testing, ini mengembalikan FALSE agar notif selalu muncul.
        val hasUploadedToday = checkIfPhotoWasUploadedToday(context)

        if (!hasUploadedToday) {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID_GALERI)
                .setSmallIcon(R.drawable.ic_calendar)
                .setContentTitle("📸 Ketinggalan Update Nih!")
                .setContentText("Anda belum ada update foto kegiatan terbaru hari ini. Ayo tambahkan sebelum 21:50!")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
            Log.i(TAG, "Notifikasi 'Belum Upload' dikirim.")
        } else {
            Log.i(TAG, "Foto sudah di-upload hari ini. Notifikasi dilewati.")
        }
    }

    /**
     * Fungsi placeholder untuk pemeriksaan data.
     */
    private fun checkIfPhotoWasUploadedToday(context: Context): Boolean {
        // GANTI LOGIKA INI SAAT ANDA PUNYA DATABASE!
        return false
    }
}