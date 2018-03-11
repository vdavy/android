# Proguard config for Android app

# Don't warn on missing classes
-dontwarn twitter4j.**

# Avoid libs
-keep class android.support.v7.widget.SearchView { *; }
-keep class twitter4j.** { *; }

-keepattributes EnclosingMethod
-optimizationpasses 5

# Avoid spring lib : http://stackoverflow.com/questions/11640571/problems-obfuscating-android-application-that-uses-spring-for-android
-keep class com.fasterxml.jackson.** { *; }
-keep class org.springframework.** { *; }
-keep class com.stationmillenium.android.replay.dto.** { public *; }

# Don't warn on missing classes
-dontwarn org.springframework.http.client.**
-dontwarn org.springframework.http.converter.feed.**
-dontwarn org.springframework.http.converter.json.**
-dontwarn org.springframework.http.converter.xml.**
-dontwarn com.fasterxml.jackson.databind.ext.DOMSerializer
-dontwarn java.nio.file.Path*,java.beans.Transient,java.beans.ConstructorProperties
-dontwarn com.google.android.gms.internal.**

# keep map for debug
#-printmapping mapping.txt

# Glide proguard rules
# see https://github.com/bumptech/glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Crashlytics rules
# see https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?authuser=0
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Google cast
-keep class com.stationmillenium.android.cast.CastOptionsProvider { public *; }
-keep class android.support.v7.app.MediaRouteActionProvider { *; }