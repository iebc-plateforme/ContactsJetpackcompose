# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep line numbers for debugging crashes
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep generic signature (for reflection)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Kotlin - Optimized
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
    public static void throw*(...);
}

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.contacts.android.contacts.**$$serializer { *; }
-keepclassmembers class com.contacts.android.contacts.** {
    *** Companion;
}
-keepclasseswithmembers class com.contacts.android.contacts.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ApplicationComponentManager { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Jetpack Compose - Only keep what's necessary
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.platform.** { *; }

# Google ML Kit - Only keep API classes
-keep class com.google.mlkit.vision.** { *; }
-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**

# Google AdMob - Keep only public API
-keep public class com.google.android.gms.ads.** { public *; }
-keep public class com.google.ads.** { public *; }
-dontwarn com.google.android.gms.ads.**

# CameraX - Keep only public API
-keep public class androidx.camera.** { public *; }
-dontwarn androidx.camera.**

# ZXing (QR Code) - Keep only needed classes
-keep class com.google.zxing.qrcode.** { *; }
-keep class com.journeyapps.barcodescanner.BarcodeCallback { *; }
-keep class com.journeyapps.barcodescanner.BarcodeResult { *; }
-dontwarn com.google.zxing.**

# Coil - Keep only essential
-dontwarn coil.**
-keep public class coil.** { public *; }

# Accompanist - Keep only essential
-dontwarn com.google.accompanist.**

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep parcelables
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep enums
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Google Play Billing
-keep class com.android.billingclient.api.** { *; }