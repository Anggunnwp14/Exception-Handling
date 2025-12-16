package com.example.ukonnect2.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

private val REMINDER_OFFSET_MS = TimeUnit.MINUTES.toMillis(10) // reminder aktivitas -10 menit

// DAILY CHECK: offset 5 menit
private const val DAILY_REMINDER_OFFSET_MINUTES = 5
private val DAILY_REMINDER_OFFSET_MS =
    TimeUnit.MINUTES.toMillis(DAILY_REMINDER_OFFSET_MINUTES.toLong())

private const val CHANNEL_ID_AKTIVITAS = "aktivitas_reminder_channel"
private const val CHANNEL_NAME_AKTIVITAS = "Pengingat Aktivitas"

private const val TAG = "AktivitasScheduler"

private const val ACTION_AKTIVITAS_REMINDER =
    "com.example.ukonnect2.ACTION_AKTIVITAS_REMINDER"

private const val ACTION_AKTIVITAS_DAILY_CHECK =
    "com.example.ukonnect2.ACTION_AKTIVITAS_DAILY_CHECK"

private const val REQUEST_CODE_DAILY_CHECK = 401


class AktivitasReminderScheduler(private val context: Context) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_AKTIVITAS,
                CHANNEL_NAME_AKTIVITAS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Saluran untuk notifikasi pengingat aktivitas."
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    // ============================================================
    //  DAILY CHECK 23:00 → Notifikasi 22:55
    // ============================================================
    fun scheduleDailyAktivitasCheck(
        deadlineHour: Int = 23,   // 🔥 Diubah: 23 (11 malam)
        deadlineMinute: Int = 0    // 🔥 Diubah: 0 (menit)
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            Log.e(TAG, "Izin Exact Alarm belum diberikan.")
            return
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, deadlineHour)  // 23
            set(Calendar.MINUTE, deadlineMinute)     // 0
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Notifikasi 5 menit sebelum 23:00 → 22:55
            timeInMillis = timeInMillis - DAILY_REMINDER_OFFSET_MS

            // Jika waktu sudah lewat → jadwalkan besok
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, AktivitasReceiver::class.java).apply {
            action = ACTION_AKTIVITAS_DAILY_CHECK
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_DAILY_CHECK,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        Log.i(
            TAG,
            "Daily Check dijadwalkan: ${Date(calendar.timeInMillis)} (Notifikasi 22:55)" // 🔥 Diperbarui
        )
    }

    // ============================================================
    //   REMINDER PER AKTIVITAS (10 menit sebelum)
    // ============================================================
    fun scheduleReminder(aktivitasId: Int, waktuMillis: Long, title: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            Log.e(TAG, "Izin Exact Alarm belum diberikan.")
            return
        }

        // Waktu paling awal = waktu aktivitas - 10 menit
        val reminderTime = waktuMillis - REMINDER_OFFSET_MS

        val intent = Intent(context, AktivitasReceiver::class.java).apply {
            action = ACTION_AKTIVITAS_REMINDER
            putExtra("ACTIVITAS_ID", aktivitasId)
            putExtra("ACTIVITAS_TITLE", title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            aktivitasId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            reminderTime,
            pendingIntent
        )

        Log.i(
            TAG,
            "Reminder aktivitas dijadwalkan: ID=$aktivitasId, Title=$title, At ${Date(reminderTime)}"
        )
    }

    // ============================================================
    //    CANCEL REMINDER (cuma butuh aktivitasId)
    // ============================================================
    fun cancelReminder(aktivitasId: Int) {
        val intent = Intent(context, AktivitasReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            aktivitasId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Log.i(TAG, "Reminder dibatalkan untuk ID=$aktivitasId")
        }
    }
}