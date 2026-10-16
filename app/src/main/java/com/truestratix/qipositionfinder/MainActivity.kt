package com.truestratix.qipositionfinder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.truestratix.qipositionfinder.data.BatteryRepository
import com.truestratix.qipositionfinder.model.BatteryTelemetry
import com.truestratix.qipositionfinder.ui.components.AboutAppDialog
import com.truestratix.qipositionfinder.ui.screens.BatteryOptimizerScreen
import com.truestratix.qipositionfinder.ui.screens.ChargingDashboardScreen
import com.truestratix.qipositionfinder.ui.screens.CleanerScreen
import com.truestratix.qipositionfinder.ui.screens.DeviceResourceScreen
import com.truestratix.qipositionfinder.ui.screens.QiAlignmentScreen
import com.truestratix.qipositionfinder.ui.theme.CardBorderNavy
import com.truestratix.qipositionfinder.ui.theme.CardNavy
import com.truestratix.qipositionfinder.ui.theme.DarkNavy
import com.truestratix.qipositionfinder.ui.theme.NeonCyan
import com.truestratix.qipositionfinder.ui.theme.QiPositionFinderTheme
import com.truestratix.qipositionfinder.ui.theme.TextPrimary
import com.truestratix.qipositionfinder.ui.theme.TextSecondary

enum class AppTab(val title: String, val icon: ImageVector) {
    SWEET_SPOT("Sweet Spot", Icons.Default.Sensors),
    HUD("Charging HUD", Icons.Default.Bolt),
    OPTIMIZER("Optimizer", Icons.Default.HealthAndSafety),
    CLEANER("Cleaner", Icons.Default.CleaningServices),
    HARDWARE("Hardware", Icons.Default.Memory)
}

class MainActivity : ComponentActivity() {

    private lateinit var batteryRepository: BatteryRepository

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Notification permission handled
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        batteryRepository = BatteryRepository(this)
        batteryRepository.startMonitoring()

        checkPermissions()

        setContent {
            QiPositionFinderTheme {
                val telemetry by batteryRepository.telemetry.collectAsState(initial = BatteryTelemetry())
                var selectedTab by remember { mutableStateOf(AppTab.SWEET_SPOT) }
                var showAboutDialog by remember { mutableStateOf(false) }

                if (showAboutDialog) {
                    com.truestratix.qipositionfinder.ui.components.AboutAppDialog(
                        onDismissRequest = { showAboutDialog = false }
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DarkNavy,
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = {
                                androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "QiCharge",
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = com.truestratix.qipositionfinder.ui.theme.TextPrimary,
                                        fontSize = 20.sp
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { showAboutDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "About Developer & App",
                                        tint = NeonCyan
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = CardNavy)
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = CardNavy,
                            tonalElevation = NavigationBarDefaults.Elevation
                        ) {
                            AppTab.values().forEach { tab ->
                                NavigationBarItem(
                                    selected = selectedTab == tab,
                                    onClick = { selectedTab = tab },
                                    icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                                    label = { Text(text = tab.title) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = DarkNavy,
                                        selectedTextColor = NeonCyan,
                                        indicatorColor = NeonCyan,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    when (selectedTab) {
                        AppTab.SWEET_SPOT -> QiAlignmentScreen(
                            telemetry = telemetry,
                            modifier = Modifier.padding(innerPadding)
                        )
                        AppTab.HUD -> ChargingDashboardScreen(
                            telemetry = telemetry,
                            modifier = Modifier.padding(innerPadding)
                        )
                        AppTab.OPTIMIZER -> BatteryOptimizerScreen(
                            telemetry = telemetry,
                            modifier = Modifier.padding(innerPadding)
                        )
                        AppTab.CLEANER -> CleanerScreen(
                            modifier = Modifier.padding(innerPadding)
                        )
                        AppTab.HARDWARE -> DeviceResourceScreen(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryRepository.stopMonitoring()
    }
}
