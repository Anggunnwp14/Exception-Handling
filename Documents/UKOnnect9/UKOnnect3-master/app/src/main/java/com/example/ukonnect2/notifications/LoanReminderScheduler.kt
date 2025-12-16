package com.example.ukonnect2.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.example.ukonnect2.R
import com.example.ukonnect2.ui.screen.Loan
import java.util.concurrent.TimeUnit // Tambahkan import ini
// ... (import lainnya)

// Konstanta Waktu Pengingat: 10 menit sebelum jatuh tempo
private val REMINDER_OFFSET_MS = TimeUnit.MINUTES.toMillis(10) // Lebih rapi menggunakan TimeUnit
private const val CHANNEL_ID = "loan_reminder_channel"
private const val CHANNEL_NAME = "Pengingat Peminjaman"

class LoanReminderScheduler(private val context: Context) {

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager // Ubah menjadi val publik agar bisa diakses di Composable
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    // ... (Fungsi createNotificationChannel tetap sama) ...
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Saluran untuk notifikasi pengembalian alat."
            }
            notificationManager.createNotificationChannel(channel)
        }
    }


    /**
     * Menjadwalkan pengingat 10 menit sebelum waktu selesai, termasuk penanganan izin Exact Alarm.
     */
    fun scheduleReminder(loan: Loan) {
        val reminderTime = loan.tanggalSelesai - REMINDER_OFFSET_MS

        // Pastikan pengingat berada di masa depan
        if (reminderTime < System.currentTimeMillis()) {
            return
        }

        // KELUAR JIKA IZIN EXACT ALARM TIDAK ADA (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Notifikasi tidak bisa dijadwalkan dengan presisi tanpa izin.
                // Caller (Composable) harus meminta izin ini.
                return
            }
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("LOAN_ID", loan.id)
            putExtra("LOAN_TITLE", loan.namaEquipment)
            putExtra("LOAN_QTY", loan.jumlah)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            loan.id.hashCode(), // ID unik berdasarkan ID pinjaman
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Atur alarm menggunakan try-catch untuk menangani SecurityException pada API < 31
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // setExactAndAllowWhileIdle lebih disukai
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Ini menangani kasus di mana izin belum diberikan (API < 31)
            // atau ada masalah lain saat pemanggilan setExact/setExactAndAllowWhileIdle.
            Toast.makeText(context, "Gagal menjadwalkan alarm: Izin Alarm Tepat Waktu mungkin diperlukan.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * FUNGSI BARU: Mengarahkan pengguna ke halaman pengaturan izin Exact Alarm (API 31+).
     */
    fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                // Intent untuk membuka halaman permintaan izin 'Alarm dan Pengingat Tepat Waktu'
                val intent = Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + context.packageName)
                )
                // Flag ini diperlukan karena dipanggil dari Context non-Activity (dari Composable -> ViewModel)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Gagal membuka pengaturan izin alarm.", Toast.LENGTH_LONG).show()
            }
        }
    }


    fun cancelReminder(loanId: String) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            loanId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}