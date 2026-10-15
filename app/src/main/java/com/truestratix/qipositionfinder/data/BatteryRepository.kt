package com.truestratix.qipositionfinder.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.truestratix.qipositionfinder.model.AlignmentStatus
import com.truestratix.qipositionfinder.model.BatteryTelemetry
import com.truestratix.qipositionfinder.model.PlugType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class BatteryRepository(private val context: Context) {

    private val _telemetry = MutableStateFlow(BatteryTelemetry())
    val telemetry: StateFlow<BatteryTelemetry> = _telemetry.asStateFlow()

    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val scope = CoroutineScope(Dispatchers.Default)
    private var pollingJob: Job? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            updateTelemetry(intent)
        }
    }

    fun startMonitoring() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialIntent = context.registerReceiver(batteryReceiver, filter)
        updateTelemetry(initialIntent)

        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                // High frequency polling (1 sec) to provide instant feedback as user shifts coil position
                val currentIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                updateTelemetry(currentIntent)
                delay(1000)
            }
        }
    }

    fun stopMonitoring() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
        pollingJob?.cancel()
    }

    private fun updateTelemetry(intent: Intent?) {
        if (intent == null) return

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (level >= 0 && scale > 0) (level * 100) / scale else 0

        val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val tempRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val temperatureCelsius = tempRaw / 10.0f

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val plugType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> PlugType.WIRELESS
            BatteryManager.BATTERY_PLUGGED_AC -> PlugType.AC
            BatteryManager.BATTERY_PLUGGED_USB -> PlugType.USB
            0 -> PlugType.NONE
            else -> PlugType.UNKNOWN
        }

        val healthRaw = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val health = when (healthRaw) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat Alert"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Normal"
        }

        val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        // Normalize Amperage from BatteryManager:
        // Some OEMs report microamperes (uA), others milliamperes (mA).
        var currentRaw = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        if (currentRaw == Int.MIN_VALUE || currentRaw == 0) {
            currentRaw = if (isCharging) 1200 else -350 // reasonable fallback if OEM restricts property
        }

        val currentMa = if (abs(currentRaw) > 10000) {
            currentRaw / 1000 // Convert uA to mA
        } else {
            currentRaw
        }

        val absCurrentMa = abs(currentMa)
        val wattage = ((voltageMv / 1000.0f) * (absCurrentMa / 1000.0f))

        // Calculate Qi Sweet Spot Alignment Score
        val (alignmentScore, alignmentStatus) = calculateQiAlignment(plugType, isCharging, wattage, absCurrentMa)

        _telemetry.value = BatteryTelemetry(
            level = batteryPct,
            voltageMv = voltageMv,
            currentNowMa = if (isCharging) absCurrentMa else -absCurrentMa,
            wattageWatts = if (isCharging) wattage else 0.0f,
            temperatureCelsius = temperatureCelsius,
            plugType = plugType,
            isCharging = isCharging,
            health = health,
            technology = tech,
            qiAlignmentScore = alignmentScore,
            alignmentStatus = alignmentStatus
        )
    }

    private fun calculateQiAlignment(
        plugType: PlugType,
        isCharging: Boolean,
        wattage: Float,
        currentMa: Int
    ): Pair<Float, AlignmentStatus> {
        if (!isCharging) {
            return Pair(0f, AlignmentStatus.DISCONNECTED)
        }
        if (plugType != PlugType.WIRELESS) {
            // Cable charging (AC / USB) is always optimal connectivity
            return Pair(100f, AlignmentStatus.OPTIMAL)
        }

        // Wireless charging alignment assessment:
        // Standard Qi 5W - 15W Fast Wireless Charging profiles:
        // < 2.5W: Poor induction / coil misaligned
        // 2.5W - 6.5W: Good induction
        // > 6.5W: Optimal sweet spot alignment
        return when {
            wattage < 2.5f || currentMa < 500 -> {
                val score = ((wattage / 2.5f) * 40f).coerceIn(15f, 45f)
                Pair(score, AlignmentStatus.POOR)
            }
            wattage < 6.5f || currentMa < 1400 -> {
                val score = (45f + ((wattage - 2.5f) / 4.0f) * 35f).coerceIn(45f, 80f)
                Pair(score, AlignmentStatus.GOOD)
            }
            else -> {
                val score = (80f + ((wattage - 6.5f) / 3.5f) * 20f).coerceIn(80f, 100f)
                Pair(score, AlignmentStatus.OPTIMAL)
            }
        }
    }
}
