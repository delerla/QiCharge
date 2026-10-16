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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truestratix.qipositionfinder.data.NetworkTelemetry
import com.truestratix.qipositionfinder.data.NetworkTelemetryManager
import com.truestratix.qipositionfinder.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DeviceResourceScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
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

    // Network Telemetry
    val networkManager = remember { NetworkTelemetryManager(context) }
    var networkData by remember { mutableStateOf(networkManager.getLocalNetworkTelemetry()) }
    var publicGeoData by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isRefreshingNetwork by remember { mutableStateOf(false) }

    fun refreshNetwork() {
        coroutineScope.launch {
            isRefreshingNetwork = true
            networkData = networkManager.getLocalNetworkTelemetry()
            publicGeoData = networkManager.fetchPublicGeoIp()
            isRefreshingNetwork = false
        }
    }

    LaunchedEffect(Unit) {
        refreshNetwork()
    }

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
            text = "Live system RAM, internal storage, Wi-Fi radio & network geolocation",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Network & Wi-Fi Card
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
                        Icon(
                            imageVector = if (networkData.isWifiConnected) Icons.Default.Wifi else Icons.Default.SignalCellularAlt,
                            contentDescription = null,
                            tint = NeonCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Network & Connectivity", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    IconButton(
                        onClick = { refreshNetwork() },
                        enabled = !isRefreshingNetwork,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Network",
                            tint = if (isRefreshingNetwork) TextSecondary else NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Wi-Fi signal gauge if connected to Wi-Fi
                if (networkData.isWifiConnected) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Wi-Fi Signal Strength",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                            color = TextSecondary
                        )
                        Text(
                            text = "${networkData.wifiRssiDbm} dBm • ${networkData.wifiSignalPercent}% (${networkData.wifiSignalQuality})",
                            fontWeight = FontWeight.Bold,
                            color = if (networkData.wifiSignalPercent >= 60) OptimalGreen else WarningAmber,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { networkData.wifiSignalPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (networkData.wifiSignalPercent >= 60) OptimalGreen else WarningAmber,
                        trackColor = GaugeTrack
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    SpecRow(label = "Wi-Fi Frequency", value = networkData.wifiFrequency)
                    HorizontalDivider(color = CardBorderNavy, modifier = Modifier.padding(vertical = 6.dp))
                    SpecRow(label = "Link Speed", value = networkData.wifiLinkSpeed)
                    HorizontalDivider(color = CardBorderNavy, modifier = Modifier.padding(vertical = 6.dp))
                } else {
                    SpecRow(label = "Connection Type", value = networkData.connectionType)
                    HorizontalDivider(color = CardBorderNavy, modifier = Modifier.padding(vertical = 6.dp))
                }

                // IP Details
                SpecRow(label = "Local IPv4", value = networkData.localIpv4)
                if (networkData.localIpv6 != "—") {
                    HorizontalDivider(color = CardBorderNavy, modifier = Modifier.padding(vertical = 6.dp))
                    SpecRow(label = "Local IPv6", value = networkData.localIpv6)
                }

                HorizontalDivider(color = CardBorderNavy, modifier = Modifier.padding(vertical = 6.dp))
                val publicIp = publicGeoData["ip"] ?: if (isRefreshingNetwork) "Detecting..." else "Unavailable"
                SpecRow(label = "Public IP", value = publicIp)

                val country = publicGeoData["country"] ?: ""
                val city = publicGeoData["city"] ?: ""
                val locationText = if (country.isNotEmpty() && city.isNotEmpty()) "$city, $country" else if (country.isNotEmpty()) country else if (isRefreshingNetwork) "Locating..." else "—"
                HorizontalDivider(color = CardBorderNavy, modifier = Modifier.padding(vertical = 6.dp))
                SpecRow(label = "IP Geolocation", value = locationText)

                val isp = publicGeoData["isp"] ?: ""
                if (isp.isNotEmpty()) {
                    HorizontalDivider(color = CardBorderNavy, modifier = Modifier.padding(vertical = 6.dp))
                    SpecRow(label = "Service Provider (ISP)", value = isp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
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

        Spacer(modifier = Modifier.height(16.dp))

        // Developer & Organization Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Developer & Open Source",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SpecRow(label = "Application", value = "QiCharge: Wireless Optimizer v1.0.1")
                HorizontalDivider(color = CardBorderNavy, modifier = Modifier.padding(vertical = 6.dp))
                SpecRow(label = "Developer / Organization", value = "TrueStratix")
                HorizontalDivider(color = CardBorderNavy, modifier = Modifier.padding(vertical = 6.dp))
                SpecRow(label = "Lead Author", value = "Dr. Delér Langenberg")
                HorizontalDivider(color = CardBorderNavy, modifier = Modifier.padding(vertical = 6.dp))
                SpecRow(label = "Contact", value = "delerla@yahoo.de")

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://delerla.github.io/QiCharge/"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Privacy Policy", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://truestratix.com"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "truestratix.com", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
