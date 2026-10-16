package com.truestratix.qipositionfinder.ui.screens

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truestratix.qipositionfinder.model.BatteryTelemetry
import com.truestratix.qipositionfinder.service.ChargingAlertService
import com.truestratix.qipositionfinder.ui.theme.*

enum class OptimizerProfile(
    val title: String,
    val description: String,
    val targetPercent: Int,
    val thermalLimit: Float,
    val icon: ImageVector
) {
    OVERNIGHT(
        "Overnight Longevity (80% Cap)",
        "Stops cycle wear at 80% with sound/vibration alerts to avoid overnight trickle heat",
        80,
        39.0f,
        Icons.Default.Bedtime
    ),
    QI_ALIGNMENT(
        "Qi Sweet Spot Mode",
        "Keeps screen active while aligning phone on wireless induction pad",
        85,
        40.0f,
        Icons.Default.Sensors
    ),
    MAX_CAPACITY(
        "Full 100% Travel Charge",
        "Full charge alert with strict 42°C overheating safety shield",
        100,
        42.0f,
        Icons.Default.BatteryChargingFull
    )
}

@Composable
fun BatteryOptimizerScreen(
    telemetry: BatteryTelemetry = BatteryTelemetry(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val prefs = remember { context.getSharedPreferences("qicharge_optimizer", Context.MODE_PRIVATE) }

    var selectedProfile by remember { mutableStateOf(OptimizerProfile.OVERNIGHT) }
    var targetLimit by remember { mutableFloatStateOf(prefs.getInt("target_limit", 80).toFloat()) }
    var thermalLimit by remember { mutableFloatStateOf(prefs.getFloat("thermal_limit", 40f)) }
    var keepScreenAwake by remember { mutableStateOf(prefs.getBoolean("screen_awake", false)) }
    var isProtectionActive by remember { mutableStateOf(prefs.getBoolean("protection_active", true)) }

    // Screen awake handler
    DisposableEffect(keepScreenAwake) {
        val window = (context as? Activity)?.window
        if (keepScreenAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun applyOptimization(target: Int, thermal: Float) {
        prefs.edit()
            .putInt("target_limit", target)
            .putFloat("thermal_limit", thermal)
            .putBoolean("screen_awake", keepScreenAwake)
            .putBoolean("protection_active", true)
            .apply()

        isProtectionActive = true
        ChargingAlertService.start(context, target, thermal)

        // Haptic feedback & confirmation chime
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                v?.vibrate(100)
            }
        } catch (_: Exception) {}

        Toast.makeText(context, "Optimization Applied: Guarding at $target% & ${thermal.toInt()}°C", Toast.LENGTH_SHORT).show()
    }

    fun stopOptimization() {
        prefs.edit().putBoolean("protection_active", false).apply()
        isProtectionActive = false
        ChargingAlertService.stop(context)
        Toast.makeText(context, "Battery Guard Paused", Toast.LENGTH_SHORT).show()
    }

    fun testAlert() {
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, notificationUri)
            ringtone.play()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 300), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                v?.vibrate(300)
            }
        } catch (_: Exception) {}
        Toast.makeText(context, "Audio & Haptic Alert Triggered", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        Text(
            text = "Battery Health Optimizer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Extend lithium-ion cycle lifespan and enforce thermal safety limits",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Protection Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    if (isProtectionActive) listOf(OptimalGreen, NeonCyan) else listOf(CardBorderNavy, CardBorderNavy)
                )
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isProtectionActive) Icons.Default.Shield else Icons.Default.ShieldMoon,
                            contentDescription = null,
                            tint = if (isProtectionActive) OptimalGreen else TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isProtectionActive) "Health Guard Active" else "Guard Paused",
                                fontWeight = FontWeight.Bold,
                                color = if (isProtectionActive) OptimalGreen else TextSecondary,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (isProtectionActive) "Target: ${targetLimit.toInt()}% • Heat Limit: ${thermalLimit.toInt()}°C" else "Tap below to activate guard",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isProtectionActive,
                        onCheckedChange = { active ->
                            if (active) {
                                applyOptimization(targetLimit.toInt(), thermalLimit)
                            } else {
                                stopOptimization()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OptimalGreen,
                            checkedTrackColor = OptimalGreen.copy(alpha = 0.5f)
                        )
                    )
                }

                HorizontalDivider(color = CardBorderNavy, modifier = Modifier.padding(vertical = 14.dp))

                // Telemetry summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Battery Level", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                        Text(
                            text = "${telemetry.level}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = OptimalGreen
                        )
                    }
                    Column {
                        Text(text = "Temperature", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                        Text(
                            text = String.format("%.1f°C", telemetry.temperatureCelsius),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (telemetry.temperatureCelsius >= thermalLimit) WarningAmber else TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Est. to Target", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                        val remainingMins = calculateTimeToTarget(telemetry.level, targetLimit.toInt(), telemetry.wattageWatts)
                        Text(
                            text = if (!telemetry.isCharging) "—" else if (telemetry.level >= targetLimit) "Ready!" else "${remainingMins}m",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profiles Header
        Text(
            text = "Select Optimization Profile",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OptimizerProfile.values().forEach { profile ->
            val isSelected = selectedProfile == profile
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        selectedProfile = profile
                        targetLimit = profile.targetPercent.toFloat()
                        thermalLimit = profile.thermalLimit
                        keepScreenAwake = (profile == OptimizerProfile.QI_ALIGNMENT)
                        applyOptimization(profile.targetPercent, profile.thermalLimit)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) CardNavy else DarkNavy
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        if (isSelected) listOf(NeonCyan, ElectricBlue) else listOf(CardBorderNavy, CardBorderNavy)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = profile.icon,
                        contentDescription = null,
                        tint = if (isSelected) NeonCyan else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.title,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NeonCyan else TextPrimary
                        )
                        Text(
                            text = profile.description,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                            color = TextSecondary
                        )
                    }
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Tuning & 1-Tap Apply
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Custom Guard Thresholds",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Target slider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Target Charge Limit", color = TextSecondary, fontSize = 14.sp)
                    Text(text = "${targetLimit.toInt()}%", fontWeight = FontWeight.Bold, color = OptimalGreen)
                }
                Slider(
                    value = targetLimit,
                    onValueChange = { targetLimit = it },
                    valueRange = 70f..100f,
                    steps = 5,
                    colors = SliderDefaults.colors(thumbColor = OptimalGreen, activeTrackColor = OptimalGreen)
                )

                // Thermal slider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Thermal Safety Limit", color = TextSecondary, fontSize = 14.sp)
                    Text(text = "${thermalLimit.toInt()}°C", fontWeight = FontWeight.Bold, color = WarningAmber)
                }
                Slider(
                    value = thermalLimit,
                    onValueChange = { thermalLimit = it },
                    valueRange = 36f..45f,
                    steps = 8,
                    colors = SliderDefaults.colors(thumbColor = WarningAmber, activeTrackColor = WarningAmber)
                )

                // Keep screen awake toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Keep Screen On While Aligning", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(text = "Prevents display timeout while adjusting phone on pad", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = keepScreenAwake,
                        onCheckedChange = { keepScreenAwake = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = ElectricBlue)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Apply button
                    Button(
                        onClick = { applyOptimization(targetLimit.toInt(), thermalLimit) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Apply Optimization", fontWeight = FontWeight.Bold)
                    }

                    // Test Alarm button
                    OutlinedButton(
                        onClick = { testAlert() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Test Alarm", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Hardware Battery Protection Bridge
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Device Hardware 80% Cap Settings",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Most modern tablets and phones (Samsung, Google Pixel, OnePlus) include a hardware 'Protect Battery' or '80% Limit' switch in system settings. Tap below to toggle it on directly.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Button(
                    onClick = {
                        openSystemBatteryProtection(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Open System Battery Protection")
                }
            }
        }
    }
}

fun openSystemBatteryProtection(context: Context) {
    // 1. Try Samsung's Protect Battery activity
    val samsungIntent = Intent().apply {
        component = ComponentName("com.samsung.android.sm", "com.samsung.android.sm.battery.ui.BatteryActivity")
    }
    try {
        context.startActivity(samsungIntent)
        return
    } catch (_: Exception) {}

    // 2. Try Battery Saver settings
    try {
        context.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
        return
    } catch (_: Exception) {}

    // 3. Fallback to general settings
    try {
        context.startActivity(Intent(Settings.ACTION_SETTINGS))
    } catch (_: Exception) {}
}

fun calculateTimeToTarget(currentLevel: Int, targetLevel: Int, wattage: Float): Int {
    if (currentLevel >= targetLevel) return 0
    if (wattage <= 0.5f) return 45

    val totalCapacityWh = 17.5f
    val neededPercent = (targetLevel - currentLevel) / 100f
    val neededWh = totalCapacityWh * neededPercent
    val hours = (neededWh / (wattage * 0.85f))
    return (hours * 60).toInt().coerceIn(1, 300)
}
