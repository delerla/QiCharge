package com.truestratix.qipositionfinder.ui.screens

import android.content.Intent
import android.os.Build
import android.os.storage.StorageManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truestratix.qipositionfinder.data.CleanerScanResult
import com.truestratix.qipositionfinder.data.StorageCleanerManager
import com.truestratix.qipositionfinder.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CleanerScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val cleanerManager = remember { StorageCleanerManager(context) }
    var scanResult by remember { mutableStateOf<CleanerScanResult?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    var isCleaned by remember { mutableStateOf(false) }
    var cleanedAmount by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        isScanning = true
        scanResult = cleanerManager.scanJunk()
        isScanning = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Storage & Junk Cleaner",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Safely clear temporary app caches & optimize storage buffers",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Hero Card: Cleanable Junk
        val junkMb = ((scanResult?.totalJunkBytes ?: 0L) / (1024.0 * 1024.0))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(
                        if (junkMb > 0) WarningAmber.copy(alpha = 0.6f) else OptimalGreen.copy(alpha = 0.6f),
                        CardBorderNavy
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isCleaned) Icons.Default.CheckCircle else Icons.Default.CleaningServices,
                    contentDescription = null,
                    tint = if (isCleaned) OptimalGreen else NeonCyan,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isCleaned) "Cache Cleared!" else String.format("%.1f MB", junkMb),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = if (isCleaned) {
                        "Freed ${formatBytes(cleanedAmount)} of temporary data"
                    } else if (junkMb > 0) {
                        "Cleanable App Caches & Traces"
                    } else {
                        "Your cache storage is in optimal condition"
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                    color = if (isCleaned) OptimalGreen else TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1-Tap Clean Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isScanning = true
                                val freed = cleanerManager.cleanAppJunk()
                                cleanedAmount = freed
                                isCleaned = true
                                scanResult = cleanerManager.scanJunk()
                                isScanning = false
                            }
                        },
                        enabled = !isScanning && (scanResult?.totalJunkBytes ?: 0L) > 0L,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = DarkNavy
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Clean Cache", fontWeight = FontWeight.Bold)
                    }

                    // Rescan Button
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                isScanning = true
                                isCleaned = false
                                scanResult = cleanerManager.scanJunk()
                                isScanning = false
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isScanning) "Scanning..." else "Scan Again")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Breakdown List
        scanResult?.items?.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = item.name, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                        }
                    }
                    Text(
                        text = formatBytes(item.sizeBytes),
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Deep System Storage Manager Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "System Deep Storage Cleanup",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Under modern Android privacy rules, clearing heavy third-party app caches requires opening the system storage dashboard.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
                Button(
                    onClick = {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startActivity(Intent(StorageManager.ACTION_MANAGE_STORAGE))
                            } else {
                                context.startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
                            }
                        } catch (_: Exception) {
                            context.startActivity(Intent(Settings.ACTION_SETTINGS))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Open System Storage Manager")
                }
            }
        }
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.1f KB", kb)
        else -> "$bytes B"
    }
}
