package com.truestratix.qipositionfinder.model

enum class PlugType {
    NONE,
    WIRELESS,
    AC,
    USB,
    UNKNOWN
}

enum class AlignmentStatus(val label: String) {
    DISCONNECTED("Not on Charger"),
    SEARCHING("Detecting Alignment..."),
    POOR("Poor Alignment (Low Induction)"),
    GOOD("Good Alignment"),
    OPTIMAL("Optimal Sweet Spot! (Peak Induction)")
}

data class BatteryTelemetry(
    val level: Int = 0,
    val voltageMv: Int = 0,
    val currentNowMa: Int = 0,
    val wattageWatts: Float = 0.0f,
    val temperatureCelsius: Float = 0.0f,
    val plugType: PlugType = PlugType.NONE,
    val isCharging: Boolean = false,
    val health: String = "Good",
    val technology: String = "Li-ion",
    val qiAlignmentScore: Float = 0.0f,
    val alignmentStatus: AlignmentStatus = AlignmentStatus.DISCONNECTED
)
