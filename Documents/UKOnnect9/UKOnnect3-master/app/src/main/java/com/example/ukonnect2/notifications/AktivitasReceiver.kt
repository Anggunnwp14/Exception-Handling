package com.example.ukonnect2.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ukonnect2.R
import android.widget.Toast

private const val CHANNEL_ID_AKTIVITAS = "aktivitas_reminder_channel"
private const val TAG = "AktivitasReceiver"

class AktivitasReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val activityId = intent.getIntExtra("ACTIVITAS_ID", 0)
        val title = intent.getStringExtra("ACTIVITAS_TITLE") ?: "Aktivitas Terjadwal"

        if (activityId == 0) return

        Toast.makeText(context, "Pengingat Aktivitas dipicu!", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Notifikasi Diterima untuk ID: $activityId, Title: $title")

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_AKTIVITAS)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentTitle("🔔 10 Menit Lagi: $title")
            .setContentText("Aktivitas Anda akan dimulai dalam 10 menit. Jangan sampai terlewat!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(activityId, notification)
    }
}
