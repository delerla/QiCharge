package com.truestratix.qipositionfinder.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.truestratix.qipositionfinder.MainActivity
import com.truestratix.qipositionfinder.data.BatteryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChargingAlertService : Service {

    constructor() : super()

    private lateinit var repository: BatteryRepository
    private val scope = CoroutineScope(Dispatchers.Main)
    private var monitorJob: Job? = null
    private var alertTriggeredForTarget = false
    private var alertTriggeredForTemp = false

    companion object {
        const val CHANNEL_ID = "qi_charging_monitor_channel"
        const val ALERT_CHANNEL_ID = "qi_charging_alerts_channel"
        const val NOTIFICATION_ID = 1001

        const val EXTRA_TARGET_LEVEL = "extra_target_level"
        const val EXTRA_TEMP_LIMIT = "extra_temp_limit"

        fun start(context: Context, targetLevel: Int = 80, tempLimit: Float = 40.0f) {
            val intent = Intent(context, ChargingAlertService::class.java).apply {
                putExtra(EXTRA_TARGET_LEVEL, targetLevel)
                putExtra(EXTRA_TEMP_LIMIT, tempLimit)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, ChargingAlertService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = BatteryRepository(this)
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val targetLevel = intent?.getIntExtra(EXTRA_TARGET_LEVEL, 80) ?: 80
        val tempLimit = intent?.getFloatExtra(EXTRA_TEMP_LIMIT, 40.0f) ?: 40.0f

        startForeground(NOTIFICATION_ID, buildForegroundNotification("Monitoring battery status..."))
        repository.startMonitoring()

        monitorJob?.cancel()
        monitorJob = scope.launch {
            repository.telemetry.collect { telemetry ->
                if (!telemetry.isCharging) {
                    updateNotification("Unplugged • Battery at ${telemetry.level}%")
                    alertTriggeredForTarget = false
                    alertTriggeredForTemp = false
                    return@collect
                }

                val statusText = "${telemetry.plugType.name} Charging: ${telemetry.level}% • " +
                        String.format("%.1f W", telemetry.wattageWatts) + " • " +
                        String.format("%.1f°C", telemetry.temperatureCelsius)
                updateNotification(statusText)

                // 80% / 85% Health Limit Alarm
                if (telemetry.level >= targetLevel && !alertTriggeredForTarget) {
                    alertTriggeredForTarget = true
                    fireAlert(
                        title = "Target Charge Reached ($targetLevel%)!",
                        message = "Optimal battery health threshold reached. Unplug or remove from charger to preserve battery longevity."
                    )
                }

                // Overheating Safety Alert
                if (telemetry.temperatureCelsius >= tempLimit && !alertTriggeredForTemp) {
                    alertTriggeredForTemp = true
                    fireAlert(
                        title = "Battery Overheat Warning (${String.format("%.1f", telemetry.temperatureCelsius)}°C)!",
                        message = "Wireless coil is getting hot. Realign phone with pad center or allow it to cool."
                    )
                }
            }
        }

        return START_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val monitorChannel = NotificationChannel(
                CHANNEL_ID,
                "Charging HUD Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live battery charging telemetry while active"
            }

            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Battery Health & Heat Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when optimal charging target or thermal limit is reached"
                enableVibration(true)
            }

            manager.createNotificationChannel(monitorChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    private fun buildForegroundNotification(contentText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("QiCharge Active")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(contentText: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildForegroundNotification(contentText))
    }

    private fun fireAlert(title: String, message: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alertNotification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), alertNotification)

        // Haptic feedback & vibration
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 500), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        } catch (_: Exception) {}

        // Sound alert
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(applicationContext, notificationUri)
            ringtone.play()
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        monitorJob?.cancel()
        repository.stopMonitoring()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
