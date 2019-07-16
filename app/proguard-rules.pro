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

# Uncomment this to preserve the line intCondition information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line intCondition information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-repackageclasses ''

-dontwarn org.fourthline.cling.**
-dontwarn org.seamless.**
-dontwarn org.eclipse.jetty.**
-dontwarn okio.**
#cling
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
# Application classes that will be serialized/deserialized over Gson
-keep class com.wezom.kiviremote.net.model.** { *; }
# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
##---------------End: proguard configuration for Gson  ----------


# Parceler rules
# Source: https://github.com/johncarl81/parceler#configuring-proguard

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keep class org.parceler.Parceler$$Parcels
# coroutines
-keep class kotlinx.coroutines**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
#RxJava

# RX
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
-dontnote rx.internal.util.PlatformDependent

# Retrofit 2.X
## https://square.github.io/retrofit/ ##
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
# Glide specific rules #
# https://github.com/bumptech/glide
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
# Crashlytics
-keepattributes SourceFile,LineNumberTable

# Room
## https://square.github.io/retrofit/ ##
-dontwarn android.arch.persistence.room.**
-keep class room.** { *; }
-keepattributes InnerClasses
-keepattributes Exceptions
-keep class * implements java.sql.Driver
