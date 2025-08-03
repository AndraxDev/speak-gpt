# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
# -optimizationpasses 5
-dontusemixedcaseclassnames
-verbose

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}

-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keepclassmembers class com.aallam.openai.api.** {
    *** Companion;
}

-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-keep class com.teslasoft.assistant.** { *; }
-keep class org.teslasoft.core.** { *; }
-keep class org.teslasoft.core.auth.** { *; }
-keep class java.lang.** { *; }
-keep class java.lang.invoke.** { *; }
-keep class com.didalgo.gpt3.** { *; }
-keep class okhttp3.** { *; }
-keep class com.theokanning.openai.** { *; }
-keep class com.theokanning.openai.completion.** { *; }
-keep class com.theokanning.openai.completion.chat.** { *; }
-keep class com.google.android.material.** { *; }
-keep class com.openai.client.** { *; }
-keep class com.openai.models.** { *; }
-keep class com.openai.core.** { *; }
-keep class com.google.android.material.bottomnavigation.** { *; }
-keep class com.google.android.material.bottomnavigation.BottomNavigationView
-dontwarn com.theokanning.openai.completion.chat.ChatMessage
-keep public class * implements com.bumptech.glide.module.GlideModule

-keep !interface * implements androidx.lifecycle.LifecycleObserver
-keep public class androidx.versionedparcelable.ParcelImpl
-keep @interface androidx.annotation.Keep
-keep class * extends com.google.gson.reflect.TypeToken
-keep,allowoptimization @com.google.gson.annotations.JsonAdapter class *

-keep class com.google.android.material.bottomnavigation.BottomNavigationView
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep !interface * implements androidx.lifecycle.LifecycleObserver
-keep public class androidx.versionedparcelable.ParcelImpl
-keep @interface androidx.annotation.Keep
-keep class com.google.gson.reflect.TypeToken
-keep class org.scilab.forge.jlatexmath.** { *; }
-keep class org.commonmark.node.** { *; }
-keep class io.noties.markwon.ext.latex.** { *; }
-keep class java.lang.reflect.** { *; }
-keep class org.apache.hc.client5.** { *; }
-keep class org.apache.hc.core5.** { *; }
-keep class java.lang.reflect.AnnotatedParameterizedType { *; }
-keep class java.lang.reflect.AnnotatedType { *; }
-keep class org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder { *; }
-keep class org.apache.hc.core5.http.ContentType { *; }
-keep class org.apache.hc.core5.http.HttpEntity { *; }
-keep class org.apache.hc.** { *; }
-dontwarn org.apache.hc.**

# Keep types used via reflection
-keepattributes Signature, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, *Annotation*

# Keep JSON Schema Generator internals (if used directly or via reflection)
-keep class com.github.victools.jsonschema.generator.** { *; }
-dontwarn com.github.victools.jsonschema.generator.**

-dontwarn java.lang.invoke.StringConcatFactory

-keepattributes InnerClasses

-keep class **.R
-keep class **.R$* {
    <fields>;
}

-if @kotlinx.serialization.Serializable class
com.aallam.openai.api.**,
com.aallam.openai.client.internal.data.**,
com.bumptech.glide.**,
com.openai.client.**,
com.openai.models.**,
com.openai.core.**,
java.lang.reflect.**,
org.apache.hc.client5.**,
org.apache.hc.core5.**

{
    static **$* *;
}

-keepnames class <1>$$serializer {
    static <1>$$serializer INSTANCE;
}

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
   static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
   static **$* *;
}
-keepclassmembers class <2>$<3> {
   kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
   public static ** INSTANCE;
}
-keepclassmembers class <1> {
   public static <1> INSTANCE;
   kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
  public static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
  public static void checkFieldIsNotNull(java.lang.Object, java.lang.String);
  public static void checkFieldIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
  public static void checkNotNull(java.lang.Object);
  public static void checkNotNull(java.lang.Object, java.lang.String);
  public static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
  public static void checkNotNullParameter(java.lang.Object, java.lang.String);
  public static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
  public static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String);
  public static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
  public static void throwUninitializedPropertyAccessException(java.lang.String);
}

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.slf4j.impl.StaticLoggerBinder
