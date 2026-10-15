package com.truestratix.qipositionfinder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truestratix.qipositionfinder.model.BatteryTelemetry
import com.truestratix.qipositionfinder.model.PlugType
import com.truestratix.qipositionfinder.ui.theme.*

@Composable
fun ChargingDashboardScreen(
    telemetry: BatteryTelemetry,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        Text(
            text = "Advanced Charging HUD",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Real-time battery electrical parameters and thermal diagnostics",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Main Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (telemetry.plugType) {
                        PlugType.WIRELESS -> Icons.Default.Sensors
                        PlugType.AC -> Icons.Default.Bolt
                        PlugType.USB -> Icons.Default.Cable
                        else -> Icons.Default.BatteryStd
                    },
                    contentDescription = "Source",
                    tint = if (telemetry.isCharging) OptimalGreen else TextSecondary,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (telemetry.isCharging) {
                            when (telemetry.plugType) {
                                PlugType.WIRELESS -> "Qi Wireless Charging"
                                PlugType.AC -> "AC Fast Charging"
                                PlugType.USB -> "USB Cable Charging"
                                else -> "Charging"
                            }
                        } else "Discharging (Battery Power)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Current Battery: ${telemetry.level}% • ${telemetry.health}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                        color = if (telemetry.isCharging) OptimalGreen else TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2x2 Telemetry Grid
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                title = "Live Wattage",
                value = String.format("%.2f W", telemetry.wattageWatts),
                subtitle = "P = V × I",
                icon = Icons.Default.ElectricBolt,
                accentColor = NeonCyan,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            MetricCard(
                title = "Amperage Current",
                value = "${telemetry.currentNowMa} mA",
                subtitle = if (telemetry.currentNowMa >= 0) "Incoming" else "Discharging",
                icon = Icons.Default.Speed,
                accentColor = ElectricBlue,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            val tempF = (telemetry.temperatureCelsius * 9f / 5f) + 32f
            val tempColor = when {
                telemetry.temperatureCelsius >= 40.0f -> AlertRed
                telemetry.temperatureCelsius >= 36.0f -> WarningAmber
                else -> OptimalGreen
            }
            MetricCard(
                title = "Temperature",
                value = String.format("%.1f °C", telemetry.temperatureCelsius),
                subtitle = String.format("%.1f °F", tempF),
                icon = Icons.Default.Thermostat,
                accentColor = tempColor,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            MetricCard(
                title = "Voltage",
                value = String.format("%.2f V", telemetry.voltageMv / 1000.0f),
                subtitle = "${telemetry.voltageMv} mV",
                icon = Icons.AutoMirrored.Filled.ShowChart,
                accentColor = WarningAmber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary specs
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Battery Chemical Specifications",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SpecRow(label = "Cell Technology", value = telemetry.technology)
                HorizontalDivider(color = CardBorderNavy, modifier = Modifier.padding(vertical = 6.dp))
                SpecRow(label = "Reported Health", value = telemetry.health)
                HorizontalDivider(color = CardBorderNavy, modifier = Modifier.padding(vertical = 6.dp))
                SpecRow(label = "Power Delivery Source", value = telemetry.plugType.name)
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardNavy),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp), color = accentColor)
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp), color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp), fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
