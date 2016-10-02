# Proguard config for Android app

# Don't warn on missing classes
-dontwarn twitter4j.**

# Avoid libs
-keep class android.support.v7.widget.SearchView { *; }
-keep class twitter4j.** { *; }

-keepattributes EnclosingMethod

# keep map for debug
#-printmapping mapping.txt

# Glide proguard rules
# see https://github.com/bumptech/glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keepresourcexmlelements manifest/application/meta-data@value=GlideModule