# Google Play Console Submission Guide: QiCharge

## 1. Release Files & Credentials

| Asset | Location / Value |
|---|---|
| **Production App Bundle (.aab)** | `C:\Dev\QiPositionFinder\app\build\outputs\bundle\release\app-release.aab` |
| **Debug APK (for quick phone testing)** | `C:\Dev\QiPositionFinder\app\build\outputs\apk\debug\app-debug.apk` |
| **Release Keystore (.jks)** | `C:\Dev\QiPositionFinder\keystore\release.jks` |
| **Keystore Password** | `QiCharge2026!` |
| **Key Alias** | `qicharge-release` |
| **Key Password** | `QiCharge2026!` |
| **Application ID** | `com.truestratix.qipositionfinder` |
| **Privacy Policy (Live URL needed)** | Upload `C:\Dev\QiPositionFinder\privacy_policy.html` to `https://truestratix.com/privacy-qicharge.html` or GitHub Pages |

---

## 2. Store Listing Copy

### App Title (max 30 chars)
`QiCharge: Wireless Optimizer`

### Short Description (max 80 chars)
`Real-time Qi wireless coil alignment, live charging HUD & battery optimizer.`

### Full Description (max 4000 chars)
```
Tired of waking up to a phone that didn't charge because it was slightly off-center on your wireless charger? 

QiCharge is the essential utility for wireless charging efficiency and battery longevity. It gives you real-time electrical telemetry and a visual sweet spot alignment gauge to ensure your phone's internal induction coil concentricity matches your charging pad.

KEY FEATURES:

⚡ QI SWEET SPOT ALIGNMENT GAUGE
• Real-time wattage and induction coupling telemetry.
• Instant visual feedback: Poor (Low Induction) ➔ Good ➔ Optimal Sweet Spot (Peak Wattage).
• Stop wasting power: Proper coil alignment eliminates eddy-current waste heat and speeds up charging.

📊 ADVANCED CHARGING TELEMETRY HUD
• Live Wattage (W = V × A) calculated in real time.
• Live Amperage (mA) flow rate.
• Millivolt (mV) voltage diagnostics.
• Precise battery core temperature in Celsius and Fahrenheit.
• Battery health status and cell chemistry metrics.

🛡️ SMART BATTERY HEALTH OPTIMIZER
• 80% / 85% Health Alarm: Extend lithium-ion lifespan from ~500 cycles to over 1,500 cycles by avoiding high-voltage degradation.
• Thermal Throttling Warning: Receive instant alerts if misalignment causes wireless induction coils to overheat (>40°C).
• Audio chime and haptic vibration alerts when limits are met.

📱 DEVICE & HARDWARE TELEMETRY
• Accurate system RAM utilization telemetry.
• Internal storage breakdown.
• Direct shortcut to Android native per-app battery optimization settings.

🔒 PRIVACY FIRST & NO ADS:
QiCharge does not collect, track, or transmit any personal data. All telemetry is calculated locally on your device.
```

---

## 3. Google Play Console Form Answers

### A. Data Safety Form
* **Does your app collect or share any user data?** ➔ **No**
* **Does your app transfer data over an encrypted connection?** ➔ **Yes**
* **Can users request their data to be deleted?** ➔ **Yes** (or mark N/A as no data is stored)

### B. App Content & Target Audience
* **Ads:** Select **"No, my app does not contain ads"**.
* **App Access:** Select **"All features are available without restrictions"**.
* **Target Audience:** Select **18 and older** (or 13+).
* **Financial Features / COVID-19 / News:** Select **"No"** to all.

---

## 4. Personal Developer Account: 20-Tester Closed Testing Track

> [!IMPORTANT]
> Google Play requires personal developer accounts to run a **Closed Test with at least 20 opted-in testers for 14 continuous days** before applying for production release.

1. In Play Console, go to **Testing > Closed testing**.
2. Click **Create track** (name it `Closed Beta`).
3. Click **Create new release** and drag & drop `app-release.aab`.
4. Go to the **Testers** tab:
   * Create an email list of 20+ testers (friends, colleagues, or beta testing communities).
   * Copy the "Join on the web" or "Join on Android" link and send it to them.
5. All 20 testers must click **Accept Invite** and install the app for 14 continuous days.
6. Once the 14-day period is complete, click **Apply for production** on your Play Console dashboard.