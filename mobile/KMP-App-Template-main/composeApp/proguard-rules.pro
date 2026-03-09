# ProGuard/R8 rules for SignalWhisper

# --- Kotlin Serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.signalwhisper.**$$serializer { *; }
-keepclassmembers class com.signalwhisper.** { *** Companion; }
-keepclasseswithmembers class com.signalwhisper.** { kotlinx.serialization.KSerializer serializer(...); }
-keep class com.signalwhisper.domain.models.** { *; }
-keep class com.signalwhisper.data.remote.** { *; }

# Keep all @Serializable classes
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> { static <1>$Companion Companion; }
-if @kotlinx.serialization.Serializable class ** { static **$* *; }
-keepclassmembers class <2>$<3> { kotlinx.serialization.KSerializer serializer(...); }
-if @kotlinx.serialization.Serializable class ** { public static ** INSTANCE; }
-keepclassmembers class <1> { public static ** INSTANCE; }

# --- Ktor ---
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# --- Koin ---
-keep class org.koin.** { *; }
-dontwarn org.koin.**
-keepclassmembers class * { public <init>(...); }

# --- Compose ---
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# --- Firebase ---
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class dev.gitlive.firebase.** { *; }
-dontwarn dev.gitlive.firebase.**

# --- RevenueCat ---
-keep class com.revenuecat.** { *; }
-dontwarn com.revenuecat.**

# --- General ---
-keepattributes Signature
-keepattributes Exceptions
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
