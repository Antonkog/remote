# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\development\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-repackageclasses ''

-dontwarn org.fourthline.cling.**
-dontwarn org.seamless.**
-dontwarn org.eclipse.jetty.**
-dontwarn okio.**

-keep class javax.** { *; }
-keep class org.** { *; }
-keep class org.fourthline.cling.** { *;}
-keep class org.seamless.** { *;}
-keep class org.eclipse.jetty.** { *;}
-keep class org.slf4j.** { *;}
-keep class javax.servlet.** { *;}

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.wezom.kiviremote.net.model.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep Glide
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl

##---------------End: proguard configuration for Gson  ----------

# Crashlytics
-keepattributes SourceFile,LineNumberTable