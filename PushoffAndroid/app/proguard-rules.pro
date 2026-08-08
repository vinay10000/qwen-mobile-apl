# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep MediaPipe classes
-keep class com.google.mediapipe.** { *; }

# Keep LibGDX classes
-keep class com.badlogic.gdx.** { *; }
-dontwarn com.badlogic.gdx.**

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Supabase
-keep class io.github.jan_gmbh.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
