package fr.funetdelire.simplealert.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.funetdelire.simplealert.AlertPreferences
import fr.funetdelire.simplealert.R
import fr.funetdelire.simplealert.client.AlertClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

class SimpleAlertService : Service() {
    companion object {
        var started = false;
    }
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val notificationManager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelMonitoring = NotificationChannel(applicationContext.getString(R.string.channel_monitoring_id), applicationContext.getString(
            R.string.channel_monitoring_name), NotificationManager.IMPORTANCE_LOW).apply {
            description = "SimpleAlert Monitoring"
        }
        val channelAlert = NotificationChannel(applicationContext.getString(R.string.channel_alert_id), applicationContext.getString(
            R.string.channel_alert_name), NotificationManager.IMPORTANCE_HIGH).apply {
            description = "SimpleAlert alert channel"
        }
        notificationManager.createNotificationChannel(channelMonitoring)
        notificationManager.createNotificationChannel(channelAlert)
    }

    private fun createMonitorNotification(): Notification {
        return NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.channel_monitoring_id)
        )
            .setContentTitle("Monitoring alerts")
            .setOngoing(true)
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createAlertNotification(message: String?) : Notification {
        return NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.channel_alert_id)
        )
            .setContentTitle("Alert")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun getAlert(client: AlertClient) {
        var notif : String? = null;
        try {
            val message = client.getAlert().execute()
            if (message.isSuccessful) {
                notif = message.body()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            notif = "Unreachable server"
        }

        if (notif != null) {
            with(NotificationManagerCompat.from(applicationContext)) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                }
                notify(1, createAlertNotification(notif))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        started = true

        startForeground(2, createMonitorNotification(), FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        val preferences = AlertPreferences.getInstance(applicationContext)
        createChannel()

        val wakeLock = createWakeLock()
        wakeLock.acquire()
        coroutineScope.launch {
            while (true) {
                Log.d("test", preferences.getServer())
                val client = AlertClient.getClient(preferences.getServer())
                getAlert(client)
                val time = preferences.getTime()
                delay(time.seconds)
            }
        }
        wakeLock.release()
        return START_STICKY
    }

    private fun createWakeLock(): PowerManager.WakeLock {
        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SimpleAlertService:")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}