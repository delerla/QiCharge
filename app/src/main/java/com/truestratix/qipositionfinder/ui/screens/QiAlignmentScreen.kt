package com.truestratix.qipositionfinder.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truestratix.qipositionfinder.model.AlignmentStatus
import com.truestratix.qipositionfinder.model.BatteryTelemetry
import com.truestratix.qipositionfinder.model.PlugType
import com.truestratix.qipositionfinder.ui.theme.*

@Composable
fun QiAlignmentScreen(
    telemetry: BatteryTelemetry,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(telemetry.alignmentStatus) {
        if (telemetry.alignmentStatus == AlignmentStatus.OPTIMAL) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val statusColor by animateColorAsState(
        targetValue = when (telemetry.alignmentStatus) {
            AlignmentStatus.OPTIMAL -> OptimalGreen
            AlignmentStatus.GOOD -> NeonCyan
            AlignmentStatus.POOR -> WarningAmber
            AlignmentStatus.SEARCHING -> ElectricBlue
            AlignmentStatus.DISCONNECTED -> TextSecondary
        },
        label = "statusColor"
    )

    val animatedScore by animateFloatAsState(
        targetValue = telemetry.qiAlignmentScore,
        animationSpec = tween(durationMillis = 600),
        label = "animatedScore"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Qi Wireless Coil Alignment",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Align your phone's coil with the charging pad for peak wattage & minimal heat",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Circular Induction Gauge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(260.dp)
                .padding(12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 18.dp.toPx()
                // Track
                drawArc(
                    color = GaugeTrack,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                // Active Sweep
                val sweep = (animatedScore / 100f) * 270f
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(WarningAmber, NeonCyan, OptimalGreen)
                    ),
                    startAngle = 135f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Inner HUD Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Power",
                    tint = statusColor,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = if (telemetry.isCharging) String.format("%.1f W", telemetry.wattageWatts) else "0.0 W",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = if (telemetry.isCharging) "${telemetry.currentNowMa} mA" else "Idle",
                    style = MaterialTheme.typography.bodyLarge,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${telemetry.level}% Battery",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Live Alignment Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(statusColor.copy(alpha = 0.5f), CardBorderNavy)))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = telemetry.alignmentStatus.label,
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = when (telemetry.alignmentStatus) {
                            AlignmentStatus.OPTIMAL -> "Coils perfectly matched. Minimal heat generation, highest speed."
                            AlignmentStatus.GOOD -> "Good induction transfer. Shift slightly to reach peak rate."
                            AlignmentStatus.POOR -> "Weak coupling. Move phone around pad center to avoid thermal loss."
                            AlignmentStatus.SEARCHING -> "Detecting power transfer..."
                            AlignmentStatus.DISCONNECTED -> "Phone is not resting on a Qi wireless pad."
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Thermal Monitoring Notice
        if (telemetry.temperatureCelsius >= 39.0f) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(AlertRed, CardBorderNavy)))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Thermal Alert",
                        tint = AlertRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Thermal Alert: Battery at ${String.format("%.1f", telemetry.temperatureCelsius)}°C. Wireless charging coil misalignment generates eddy heat. Realign phone to cool down.",
                        color = AlertRed,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp)
                    )
                }
            }
        }

        // Science & How to Align Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Tip",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Qi Alignment Science",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Induction coils require concentric concentricity. A 5mm misalignment can reduce wattage by up to 50% and turn the remaining energy into waste heat.\n• Watch the live wattage: adjust your phone on the pad until the wattage numbers reach their highest peak.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                    color = TextSecondary
                )
            }
        }
    }
}
