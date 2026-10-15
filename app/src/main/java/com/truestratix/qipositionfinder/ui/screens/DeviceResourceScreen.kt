package com.truestratix.qipositionfinder.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truestratix.qipositionfinder.ui.theme.*

@Composable
fun DeviceResourceScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // RAM stats
    val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo().apply {
        actManager.getMemoryInfo(this)
    }
    val totalRamGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
    val availRamGb = memInfo.availMem / (1024.0 * 1024.0 * 1024.0)
    val usedRamGb = totalRamGb - availRamGb
    val ramUsedPercent = ((usedRamGb / totalRamGb) * 100).toInt()

    // Storage stats
    val stat = StatFs(Environment.getDataDirectory().path)
    val blockSize = stat.blockSizeLong
    val totalBlocks = stat.blockCountLong
    val availBlocks = stat.availableBlocksLong
    val totalStorageGb = (totalBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)
    val availStorageGb = (availBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)
    val usedStorageGb = totalStorageGb - availStorageGb
    val storageUsedPercent = if (totalStorageGb > 0) ((usedStorageGb / totalStorageGb) * 100).toInt() else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        Text(
            text = "Device & Hardware Telemetry",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Accurate system memory and storage utilization",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // RAM Usage Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Memory, contentDescription = null, tint = NeonCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "System RAM", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Text(text = "$ramUsedPercent% Used", fontWeight = FontWeight.SemiBold, color = NeonCyan)
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (usedRamGb / totalRamGb).toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = NeonCyan,
                    trackColor = GaugeTrack
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Used: ${String.format("%.1f", usedRamGb)} GB", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp), color = TextSecondary)
                    Text(text = "Available: ${String.format("%.1f", availRamGb)} GB", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp), color = TextSecondary)
                    Text(text = "Total: ${String.format("%.1f", totalRamGb)} GB", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp), color = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Storage Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = ElectricBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Internal Storage", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Text(text = "$storageUsedPercent% Used", fontWeight = FontWeight.SemiBold, color = ElectricBlue)
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (usedStorageGb / totalStorageGb).toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = ElectricBlue,
                    trackColor = GaugeTrack
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Used: ${String.format("%.1f", usedStorageGb)} GB", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp), color = TextSecondary)
                    Text(text = "Free: ${String.format("%.1f", availStorageGb)} GB", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp), color = TextSecondary)
                    Text(text = "Total: ${String.format("%.1f", totalStorageGb)} GB", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp), color = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Specs Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "System Architecture",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SpecRow(label = "Device Model", value = "${Build.MANUFACTURER} ${Build.MODEL}")
                HorizontalDivider(color = CardBorderNavy, modifier = Modifier.padding(vertical = 6.dp))
                SpecRow(label = "Android Version", value = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                HorizontalDivider(color = CardBorderNavy, modifier = Modifier.padding(vertical = 6.dp))
                SpecRow(label = "CPU Supported ABIs", value = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a")
            }
        }
    }
}
