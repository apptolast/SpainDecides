# ProGuard rules for SpainDecides KMP Android App
# Generated for release builds with minification enabled

# ===========================
# Kotlin & Coroutines
# ===========================
-dontwarn kotlinx.**
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.** { *; }

# Keep Kotlin metadata for reflection
-keep class kotlin.Metadata { *; }

# ===========================
# Kotlinx Serialization
# ===========================
# Keep @Serializable classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serializers for all data classes
-keep,includedescriptorclasses class com.apptolast.spaindecides.**$$serializer { *; }
-keepclassmembers class com.apptolast.spaindecides.** {
    *** Companion;
}
-keepclasseswithmembers class com.apptolast.spaindecides.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ===========================
# Jetpack Compose
# ===========================
# Keep Compose runtime
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }

# Keep @Composable functions
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ===========================
# Koin Dependency Injection
# ===========================
-keep class org.koin.** { *; }
-keep class org.koin.core.** { *; }
-keep class org.koin.dsl.** { *; }

# Keep all Koin modules
-keep class * extends org.koin.core.module.Module { *; }

# Keep ViewModels injected by Koin
-keep class * extends androidx.lifecycle.ViewModel { *; }

# ===========================
# Ktor Client
# ===========================
-keep class io.ktor.** { *; }
-keep class io.ktor.client.** { *; }
-keep class io.ktor.client.engine.** { *; }
-keep class io.ktor.client.plugins.** { *; }
-keepclassmembers class io.ktor.** { *; }

# Keep Ktor serialization
-keep class io.ktor.serialization.** { *; }
-keep class io.ktor.utils.** { *; }

# OkHttp (used by Ktor on Android)
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ===========================
# Supabase
# ===========================
-keep class io.github.jan.supabase.** { *; }
-keepclassmembers class io.github.jan.supabase.** { *; }

# Keep Supabase auth classes
-keep class io.github.jan.supabase.gotrue.** { *; }
-keep class io.github.jan.supabase.postgrest.** { *; }
-keep class io.github.jan.supabase.realtime.** { *; }

# ===========================
# AndroidX & Lifecycle
# ===========================
-keep class androidx.lifecycle.** { *; }
-keep class androidx.navigation.** { *; }
-keep class androidx.activity.** { *; }

# ===========================
# Data Classes & Models
# ===========================
# Keep all data classes in the project
-keep class com.apptolast.spaindecides.data.model.** { *; }
-keep class com.apptolast.spaindecides.domain.** { *; }

# Keep all ViewModels
-keep class com.apptolast.spaindecides.presentation.viewmodel.** { *; }

# ===========================
# General Android Rules
# ===========================
-keepattributes Signature
-keepattributes Exceptions
-keepattributes SourceFile,LineNumberTable

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ===========================
# KMPNotifier & Firebase
# ===========================
# Keep KMPNotifier classes (push notifications)
-keep class com.mmk.kmpnotifier.** { *; }
-keepclassmembers class com.mmk.kmpnotifier.** { *; }

# Keep Firebase Messaging classes
-keep class com.google.firebase.** { *; }
-keep class com.google.firebase.messaging.** { *; }
-keepclassmembers class com.google.firebase.messaging.** { *; }

# Keep the FirebaseCloudMessagingService specifically
-keep class com.mmk.kmpnotifier.firebase.FirebaseCloudMessagingService { *; }

# ===========================
# Security & Encryption
# ===========================
# Keep KVault (secure storage)
-keep class com.liftric.kvault.** { *; }

# ===========================
# Remove Logging (Optional)
# ===========================
# Uncomment to remove all Log.d, Log.v calls in release
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
# }

# ===========================
# Optimization Settings
# ===========================
# Allow optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# ===========================
# Warnings to Ignore
# ===========================
-dontwarn org.slf4j.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# This is generated automatically by the Android Gradle plugin.
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean