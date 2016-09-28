# Proguard config for Android app

# Don't warn on missing classes
-dontwarn twitter4j.**

# Avoid libs
-keep class android.support.v7.widget.SearchView { *; }
-keep class twitter4j.** { *; }

-keepattributes EnclosingMethod

# keep map for debug
#-printmapping mapping.txt