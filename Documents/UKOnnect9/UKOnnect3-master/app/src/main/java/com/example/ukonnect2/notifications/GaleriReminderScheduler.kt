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

private const val CHANNEL_ID_GALERI = "galeri_reminder_channel"
private const val CHANNEL_NAME_GALERI = "Pengingat Galeri"
private const val TAG = "GaleriScheduler"
private const val REQUEST_CODE_GALERI = 301

class GaleriReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_GALERI,
                CHANNEL_NAME_GALERI,
                NotificationManager.IMPORTANCE_LOW
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun scheduleDailyCheck(
        deadlineHour: Int = 22,
        deadlineMinute: Int = 10
    ) {

        val notifTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, deadlineHour)
            set(Calendar.MINUTE, deadlineMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Notifikasi 5 menit sebelum deadline
            timeInMillis -= TimeUnit.MINUTES.toMillis(5)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DATE, 1)
            }
        }

        val intent = Intent(context, GaleriReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_GALERI,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            notifTime.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        Log.i(TAG, "Galeri alarm dijadwalkan: ${Date(notifTime.timeInMillis)}")
    }
}
