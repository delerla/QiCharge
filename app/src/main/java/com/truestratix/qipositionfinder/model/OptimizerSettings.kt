package com.truestratix.qipositionfinder.model

data class OptimizerSettings(
    val chargeLimitAlarmEnabled: Boolean = true,
    val targetChargeLimit: Int = 80,
    val thermalWarningEnabled: Boolean = true,
    val thermalLimitCelsius: Float = 40.0f,
    val soundAlertEnabled: Boolean = true,
    val vibrationAlertEnabled: Boolean = true
)
