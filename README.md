# QiCharge: Wireless Optimizer ⚡📱

[![Android](https://img.shields.io/badge/Platform-Android_8.0+-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Target SDK](https://img.shields.io/badge/Target_SDK-API_35_(Android_15)-00E5FF.svg)](https://developer.android.com/about/versions/15)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-7F52FF.svg?logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

**QiCharge: Wireless Optimizer** is a telemetry, coil alignment, and battery longevity utility for Android. It eliminates the guessing game of wireless charging by visualizing electromagnetic induction coupling in real-time, preventing eddy-current waste heat, and preserving lithium-ion battery lifespan.

---

## 🌟 Key Features

### ⚡ 1. Qi Wireless Sweet Spot Alignment HUD
* **Real-time Induction Coupling Meter:** Computes live wireless induction power ($W = V \times I$).
* **Visual concentricity feedback:** Transitions dynamically from *Poor Coupling (Low Wattage)* ➔ *Good* ➔ *Optimal Sweet Spot (Peak Efficiency)*.
* **Haptic Guidance:** Delivers subtle tactile confirmation the exact moment your phone hits the pad's induction apex.
* **Screen Stay-Awake Mode:** Optional toggle to keep the display active while positioning your phone on charging docks.

### 📊 2. High-Precision Charging Telemetry HUD
* **Live electrical telemetry:** Millivolts ($mV$), instantaneous flow rate ($mA$), and wattage ($W$).
* **Thermal Diagnostics:** Dual Celsius (°C) and Fahrenheit (°F) battery temperature readouts with thermal throttling warnings.
* **Battery Chemistry:** Health condition, voltage stability, and power delivery source identification (Qi Wireless, AC Fast Charger, USB).

### 🛡️ 3. Battery Health & Longevity Optimizer
* **80% / 85% Health Alarm:** Stops prolonged high-voltage saturation, extending lithium battery lifespan from ~500 to over 1,500 recharge cycles.
* **Thermal Overheating Shield:** Alarms if coil misalignment causes induction temperatures to spike ($>40^\circ\text{C}$).
* **Smart Charging Profiles:**
  * *Overnight Longevity* (soft chime at 80% to avoid trickle-cook stress)
  * *Qi Sweet Spot Mode* (screen awake for dock placement)
  * *Full Travel Charge* (100% capacity alert with thermal shield)

### 🧹 4. Storage & Junk Cleaner
* Scans and safely clears transient app cache buffers and residual files.
* Direct deep-link integration into Android OS native storage manager.
* 100% compliant with Google Play's *Misleading Claims Policy* (zero fake RAM-killer gimmicks).

### 🖥️ 5. Device & Hardware Monitor
* Live system RAM utilization breakdown (Used, Free, Total).
* Internal flash storage status.
* Device architecture and CPU ABI diagnostics.

---

## 🔒 Privacy & Safety Guarantee

QiCharge is built with a **zero-data-collection** architecture:
* **No Account Required:** All telemetry is computed in-memory on your local device.
* **Zero Telemetry Uplink:** No analytics SDKs or advertising trackers.
* **Hosted Privacy Policy:** Fully transparent documentation hosted via GitHub Pages.

---

## 🛠️ Tech Stack & Architecture

* **Architecture:** MVVM (Model-View-ViewModel) + Coroutines & Reactive `StateFlow`
* **UI Toolkit:** Jetpack Compose (Material 3 Dark Theme)
* **Minimum SDK:** Android 8.0 (API 26) — Supports >98% of active Android devices
* **Target SDK:** Android 15 (API 35) — 100% compliant with Google Play requirements
* **Build System:** Gradle 8.9 with Version Catalog (`libs.versions.toml`) & Java 21

---

## 📦 Building from Source

```bash
# Clone the repository
git clone https://github.com/delerla/QiCharge.git
cd QiCharge

# Build Debug APK
./gradlew assembleDebug

# Build Signed Production Release Bundle (.aab)
./gradlew bundleRelease
```

---

## 📄 License

```
Copyright 2026 TrueStratix (Dr. Delér Langenberg)

Licensed under the Apache License, Version 2.0.
```
