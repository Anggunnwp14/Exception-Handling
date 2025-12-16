package com.example.ukonnect2.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import java.util.Calendar
import java.util.concurrent.TimeUnit
import android.provider.Settings
import android.net.Uri
import android.content.ActivityNotFoundException
import java.util.Date

private val REMINDER_OFFSET_MS = TimeUnit.MINUTES.toMillis(5)

private const val CHANNEL_ID_ABSENSI = "absensi_reminder_channel"
private const val CHANNEL_NAME_ABSENSI = "Pengingat Absensi"
private const val TAG = "AbsensiScheduler"

private const val ACTION_ABSENSI_MASUK = "com.example.ukonnect2.ACTION_ABSENSI_MASUK"
private const val ACTION_ABSENSI_PULANG = "com.example.ukonnect2.ACTION_ABSENSI_PULANG"


class AbsensiScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_ABSENSI,
                CHANNEL_NAME_ABSENSI,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Saluran untuk notifikasi pengingat absensi harian."
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleDailyReminders(
        targetMasukHour: Int = 20, // TARGET BARU: 20:10
        targetMasukMinute: Int = 10, // TARGET BARU: 10
        targetPulangHour: Int = 17,
        targetPulangMinute: Int = 0
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "Gagal menjadwalkan: Izin Exact Alarm tidak diberikan.")
                Toast.makeText(context, "Izin Alarm Tepat Waktu diperlukan untuk Absensi.", Toast.LENGTH_LONG).show()
                return
            }
        }

        // 1. Jadwalkan Pengingat MASUK (Notif 20:05)
        scheduleSingleDailyAlarm(
            targetHour = targetMasukHour,
            targetMinute = targetMasukMinute,
            action = ACTION_ABSENSI_MASUK,
            requestCode = 101,
            isRescheduling = false
        )

        // 2. Jadwalkan Pengingat PULANG (Notif 16:55)
        scheduleSingleDailyAlarm(
            targetHour = targetPulangHour,
            targetMinute = targetPulangMinute,
            action = ACTION_ABSENSI_PULANG,
            requestCode = 102,
            isRescheduling = false
        )

        Log.i(TAG, "Pengingat harian Absensi (Masuk & Pulang) telah dijadwalkan.")
    }

    internal fun scheduleSingleDailyAlarm(
        targetHour: Int,
        targetMinute: Int,
        action: String,
        requestCode: Int,
        isRescheduling: Boolean = false
    ) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            timeInMillis = timeInMillis - REMINDER_OFFSET_MS

            if (isRescheduling || timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, AbsensiReceiver::class.java).apply {
            this.action = action
            putExtra("ABSENSI_TYPE", if (action == ACTION_ABSENSI_MASUK) "Masuk" else "Pulang")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.i(TAG, "Alarm $action dijadwalkan pada: ${Date(calendar.timeInMillis)}")

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException saat menjadwalkan alarm Absensi: ${e.message}")
        }
    }


    fun cancelAllReminders() {
        cancelSingleReminder(101, ACTION_ABSENSI_MASUK)
        cancelSingleReminder(102, ACTION_ABSENSI_PULANG)
        Log.i(TAG, "Semua pengingat Absensi dibatalkan.")
    }

    private fun cancelSingleReminder(requestCode: Int, action: String) {
        val intent = Intent(context, AbsensiReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + context.packageName)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Gagal membuka pengaturan izin alarm.", Toast.LENGTH_LONG).show()
            }
        }
    }
}