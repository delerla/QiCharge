package com.truestratix.qipositionfinder.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.NetworkInterface
import java.net.URL

data class NetworkTelemetry(
    val connectionType: String = "Checking...",
    val isWifiConnected: Boolean = false,
    val wifiRssiDbm: Int = 0,
    val wifiSignalPercent: Int = 0,
    val wifiSignalQuality: String = "—",
    val wifiFrequency: String = "—",
    val wifiLinkSpeed: String = "—",
    val localIpv4: String = "—",
    val localIpv6: String = "—",
    val publicIp: String = "Detecting...",
    val geoCountry: String = "Detecting...",
    val geoCity: String = "—",
    val geoIsp: String = "—"
)

class NetworkTelemetryManager(private val context: Context) {

    fun getLocalNetworkTelemetry(): NetworkTelemetry {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

        var connectionType = "Disconnected"
        var isWifi = false

        val activeNetwork = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)

        if (caps != null) {
            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    connectionType = "Wi-Fi"
                    isWifi = true
                }
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    connectionType = "Cellular Data"
                }
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    connectionType = "Ethernet"
                }
                else -> connectionType = "Connected"
            }
        }

        var rssi = 0
        var signalPercent = 0
        var quality = "—"
        var freq = "—"
        var linkSpeed = "—"

        if (isWifi && wifiManager != null) {
            @Suppress("DEPRECATION")
            val wifiInfo = wifiManager.connectionInfo
            if (wifiInfo != null) {
                rssi = wifiInfo.rssi
                // Map RSSI (-100 to -50) to 0-100%
                signalPercent = ((rssi + 100) * 2).coerceIn(0, 100)
                quality = when {
                    signalPercent >= 80 -> "Excellent"
                    signalPercent >= 60 -> "Good"
                    signalPercent >= 40 -> "Fair"
                    signalPercent > 0 -> "Weak"
                    else -> "No Signal"
                }

                val frequencyMhz = wifiInfo.frequency
                freq = when {
                    frequencyMhz in 2400..2500 -> "2.4 GHz"
                    frequencyMhz in 4900..5900 -> "5.0 GHz"
                    frequencyMhz in 5925..7125 -> "6.0 GHz (Wi-Fi 6E)"
                    frequencyMhz > 0 -> "$frequencyMhz MHz"
                    else -> "—"
                }

                val speedMbps = wifiInfo.linkSpeed
                linkSpeed = if (speedMbps > 0) "$speedMbps Mbps" else "—"
            }
        }

        // Local IP Addresses
        var ipv4 = "—"
        var ipv6 = "—"
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress) {
                        if (address is Inet4Address && ipv4 == "—") {
                            ipv4 = address.hostAddress ?: "—"
                        } else if (address is Inet6Address && !address.isLinkLocalAddress && ipv6 == "—") {
                            ipv6 = address.hostAddress?.substringBefore("%") ?: "—"
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        return NetworkTelemetry(
            connectionType = connectionType,
            isWifiConnected = isWifi,
            wifiRssiDbm = rssi,
            wifiSignalPercent = signalPercent,
            wifiSignalQuality = quality,
            wifiFrequency = freq,
            wifiLinkSpeed = linkSpeed,
            localIpv4 = ipv4,
            localIpv6 = ipv6
        )
    }

    suspend fun fetchPublicGeoIp(): Map<String, String> = withContext(Dispatchers.IO) {
        val result = mutableMapOf<String, String>()
        try {
            val url = URL("https://ipwho.is/")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "QiCharge-Android")
            }

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.use { it.readText() }
                val json = JSONObject(response)

                if (json.optBoolean("success", true)) {
                    result["ip"] = json.optString("ip", "—")
                    result["country"] = json.optString("country", "—")
                    result["city"] = json.optString("city", "—")
                    result["region"] = json.optString("region", "—")
                    val connectionObj = json.optJSONObject("connection")
                    result["isp"] = connectionObj?.optString("isp", "—") ?: json.optString("isp", "—")
                }
            }
            connection.disconnect()
        } catch (_: Exception) {
            // Fallback to simple ipify if ipwho fails
            try {
                val url = URL("https://api.ipify.org?format=json")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 3000
                    readTimeout = 3000
                }
                if (connection.responseCode == 200) {
                    val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                    val json = JSONObject(response)
                    result["ip"] = json.optString("ip", "—")
                    result["country"] = "Global"
                }
                connection.disconnect()
            } catch (_: Exception) {}
        }
        result
    }
}
