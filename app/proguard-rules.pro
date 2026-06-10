# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.yourcompany.pos.data.remote.**$$serializer { *; }
-keepclassmembers class com.yourcompany.pos.data.remote.** {
    *** Companion;
}
-keepclasseswithmembers class com.yourcompany.pos.data.remote.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.yourcompany.pos.data.remote.PosWebServer$** { *; }

# Room
-keep class com.yourcompany.pos.data.local.entity.** { *; }
-keep class com.yourcompany.pos.domain.model.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Ktor
-keep class io.ktor.** { *; }