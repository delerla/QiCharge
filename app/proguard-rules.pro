# ============================================================
# QiCharge: Wireless Optimizer - ProGuard / R8 Rules
# ============================================================
# R8 with full-mode obfuscation is enabled via build.gradle.kts
# (isMinifyEnabled=true, isShrinkResources=true)

# --- Keep Android essentials ---
-keepclassmembers class * {
    @androidx.annotation.Keep <fields>;
    @androidx.annotation.Keep <methods>;
}
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# --- Kotlin & Coroutines ---
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# --- Compose (preserve runtime annotations) ---
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# --- Jetpack / AndroidX ---
-keep class androidx.lifecycle.** { *; }
-keep class androidx.activity.** { *; }
-dontwarn androidx.lifecycle.**

# --- App-specific data models (used in reflection/JSON parsing) ---
-keep class com.truestratix.qipositionfinder.model.** { *; }
-keep class com.truestratix.qipositionfinder.data.NetworkTelemetry { *; }
-keep class com.truestratix.qipositionfinder.data.CleanerScanResult { *; }
-keep class com.truestratix.qipositionfinder.data.JunkCategoryItem { *; }

# --- Obfuscation hardening ---
# Rename all non-public classes aggressively
-repackageclasses 'q'
-allowaccessmodification
-overloadaggressively

# Remove debug metadata
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Remove logging calls in release builds (no debug traces)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Remove Kotlin intrinsic null-checks (minor perf + anti-tamper)
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkNotNull(...);
    static void checkExpressionValueIsNotNull(...);
    static void checkNotNullExpressionValue(...);
    static void checkParameterIsNotNull(...);
    static void checkNotNullParameter(...);
}

# --- Suppress warnings for known safe libraries ---
-dontwarn org.json.**
-dontwarn java.awt.**
-dontwarn javax.annotation.**
