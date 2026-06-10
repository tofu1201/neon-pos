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
-dontwarn java.lang.management.**
-dontwarn org.slf4j.impl.**
-dontwarn io.ktor.util.debug.**

# Fix for Missing class javax.lang.model.element.Modifier
-dontwarn javax.lang.model.**
-dontwarn com.google.errorprone.annotations.**