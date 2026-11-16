# Google Play Billing Library keep rules for release builds (R8/ProGuard)
-keep class com.android.billingclient.api.** { *; }
-dontwarn com.android.billingclient.**

# Keep Room database entities and generated DAOs
-keep class com.example.data.** { *; }

# Keep BuildConfig fields
-keep class com.example.BuildConfig { *; }
