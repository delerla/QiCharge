package com.truestratix.qipositionfinder.ui.screens

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
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
import com.truestratix.qipositionfinder.data.JunkCategoryItem
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

    fun runScan() {
        coroutineScope.launch {
            isScanning = true
            isCleaned = false
            scanResult = cleanerManager.scanJunk()
            isScanning = false
        }
    }

    LaunchedEffect(Unit) {
        runScan()
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
            text = "Reclaim storage from download packages, residual logs & cache buffers",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Hero Card: Cleanable Junk
        val totalJunk = scanResult?.totalJunkBytes ?: 0L
        val totalFiles = scanResult?.totalFilesFound ?: 0
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(
                        if (totalJunk > 0) WarningAmber.copy(alpha = 0.6f) else OptimalGreen.copy(alpha = 0.6f),
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
                    imageVector = if (isCleaned) Icons.Default.CheckCircle else if (isScanning) Icons.Default.Sync else Icons.Default.CleaningServices,
                    contentDescription = null,
                    tint = if (isCleaned) OptimalGreen else NeonCyan,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (isScanning) {
                    Text(
                        text = "Scanning Storage...",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(0.7f),
                        color = NeonCyan,
                        trackColor = GaugeTrack
                    )
                } else {
                    Text(
                        text = if (isCleaned) "Cleanup Complete!" else formatBytes(totalJunk),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isCleaned) {
                            "Freed ${formatBytes(cleanedAmount)} of redundant files"
                        } else if (totalJunk > 0 || totalFiles > 0) {
                            "$totalFiles cleanable files identified across storage"
                        } else {
                            "Storage is tidy • No junk files detected"
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                        color = if (isCleaned) OptimalGreen else TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

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
                                val categories = scanResult?.categories ?: emptyList()
                                val freed = cleanerManager.cleanAll(categories)
                                cleanedAmount = freed
                                isCleaned = true
                                scanResult = cleanerManager.scanJunk()
                                isScanning = false
                            }
                        },
                        enabled = !isScanning && ((scanResult?.totalJunkBytes ?: 0L) > 0L || (scanResult?.totalFilesFound ?: 0) > 0),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = DarkNavy
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Clean All Junk", fontWeight = FontWeight.Bold)
                    }

                    // Rescan Button
                    OutlinedButton(
                        onClick = { runScan() },
                        enabled = !isScanning,
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

        // Breakdown Categories
        Text(
            text = "Storage Junk Breakdown",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        scanResult?.categories?.forEach { category ->
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = when (category.id) {
                                "apk" -> Icons.Default.Android
                                "temp" -> Icons.Default.Drafts
                                "cache" -> Icons.Default.Memory
                                else -> Icons.Default.FolderOpen
                            },
                            contentDescription = null,
                            tint = when (category.id) {
                                "apk" -> WarningAmber
                                "temp" -> ElectricBlue
                                else -> NeonCyan
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = category.name,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${category.fileCount} file(s) • ${category.description}",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 11.sp),
                                color = TextSecondary,
                                maxLines = 2
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (category.sizeBytes > 0) formatBytes(category.sizeBytes) else "${category.fileCount} items",
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            fontSize = 13.sp
                        )

                        if (category.targetFiles.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val freed = cleanerManager.cleanCategory(category)
                                        cleanedAmount = freed
                                        isCleaned = true
                                        scanResult = cleanerManager.scanJunk()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Clean category",
                                    tint = WarningAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Deep Storage Tools
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "System Storage & Downloads",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Manage system-wide files, browser downloads, and heavy third-party app caches directly via Android settings.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Open Downloads
                    OutlinedButton(
                        onClick = {
                            try {
                                context.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
                            } catch (_: Exception) {
                                context.startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Downloads", fontSize = 12.sp)
                    }

                    // Open System Storage Manager
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
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(imageVector = Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Storage Manager", fontSize = 12.sp)
                    }
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
