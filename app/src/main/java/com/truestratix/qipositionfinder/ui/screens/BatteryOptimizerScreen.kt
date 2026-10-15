package com.truestratix.qipositionfinder.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.WindowManager
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
        "Overnight Longevity",
        "Limits charge to 80% with gentle chime to avoid overnight trickle heat",
        80,
        39.0f,
        Icons.Default.Bedtime
    ),
    QI_ALIGNMENT(
        "Qi Sweet Spot Mode",
        "Keeps screen active while aligning phone on wireless pad",
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

    var selectedProfile by remember { mutableStateOf(OptimizerProfile.OVERNIGHT) }
    var isServiceRunning by remember { mutableStateOf(true) }
    var targetLimit by remember { mutableFloatStateOf(80f) }
    var thermalLimit by remember { mutableFloatStateOf(40f) }
    var keepScreenAwake by remember { mutableStateOf(false) }

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
            text = "Extend lithium-ion cycle lifespan and prevent thermal degradation",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Estimated Time & Health Stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Current Battery", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Text(
                        text = "${telemetry.level}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = OptimalGreen
                    )
                    Text(
                        text = if (telemetry.isCharging) "Charging at ${String.format("%.1f", telemetry.wattageWatts)} W" else "Unplugged",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                        color = if (telemetry.isCharging) NeonCyan else TextSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Est. to 80%", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    val remainingMins = calculateTimeToTarget(telemetry.level, 80, telemetry.wattageWatts)
                    Text(
                        text = if (!telemetry.isCharging) "—" else if (telemetry.level >= 80) "Reached!" else "${remainingMins}m",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Est. to 100%: ${if (!telemetry.isCharging) "—" else "${calculateTimeToTarget(telemetry.level, 100, telemetry.wattageWatts)}m"}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Smart Charging Profiles Header
        Text(
            text = "Smart Charging Profiles",
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
                        if (profile == OptimizerProfile.QI_ALIGNMENT) {
                            keepScreenAwake = true
                        } else {
                            keepScreenAwake = false
                        }
                        if (isServiceRunning) {
                            ChargingAlertService.start(context, profile.targetPercent, profile.thermalLimit)
                        }
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

        // Fine Tuning Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Custom Alarm Thresholds",
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
                    onValueChange = {
                        targetLimit = it
                        if (isServiceRunning) {
                            ChargingAlertService.start(context, it.toInt(), thermalLimit)
                        }
                    },
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
                    onValueChange = {
                        thermalLimit = it
                        if (isServiceRunning) {
                            ChargingAlertService.start(context, targetLimit.toInt(), it)
                        }
                    },
                    valueRange = 36f..45f,
                    steps = 8,
                    colors = SliderDefaults.colors(thumbColor = WarningAmber, activeTrackColor = WarningAmber)
                )

                // Keep Screen Awake toggle
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
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Native Android Battery Usage Shortcut
        Button(
            onClick = {
                try {
                    context.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
                } catch (_: Exception) {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Android System Battery Optimization")
        }
    }
}

fun calculateTimeToTarget(currentLevel: Int, targetLevel: Int, wattage: Float): Int {
    if (currentLevel >= targetLevel) return 0
    if (wattage <= 0.5f) return 60 // fallback estimate

    // Typical modern 4500mAh battery has ~17.5 Wh capacity
    val totalCapacityWh = 17.5f
    val neededPercent = (targetLevel - currentLevel) / 100f
    val neededWh = totalCapacityWh * neededPercent
    val hours = (neededWh / (wattage * 0.85f)) // 85% charging efficiency
    return (hours * 60).toInt().coerceIn(1, 300)
}
