package com.example.ukonnect2.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.ukonnect2.R
import java.text.SimpleDateFormat
import java.util.*

class NotificationReceiver : BroadcastReceiver() {

    private val CHANNEL_ID = "loan_reminder_channel"

    override fun onReceive(context: Context, intent: Intent) {
        val loanId = intent.getStringExtra("LOAN_ID") ?: return
        val title = intent.getStringExtra("LOAN_TITLE") ?: "Alat Peminjaman"
        val qty = intent.getIntExtra("LOAN_QTY", 1)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Ganti dengan ikon notifikasi Anda
            .setContentTitle("🔔 Pengingat Pengembalian Alat")
            .setContentText("${qty}x ${title} harus segera dikembalikan!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // ID notifikasi harus unik (misalnya dari hash loanId)
        notificationManager.notify(loanId.hashCode(), notification)
    }
}