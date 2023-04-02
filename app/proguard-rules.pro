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

#-renamesourcefileattribute SourceFile
#-keepattributes SourceFile,LineNumberTable
#-optimizationpasses 5
#-dontusemixedcaseclassnames
#-verbose

#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

#-keepclassmembers,allowobfuscation class * {
#  @com.google.gson.annotations.SerializedName <fields>;
#}

#-assumenosideeffects class android.util.Log {
#    public static *** d(...);
#    public static *** v(...);
#}

#-keep class com.teslasoft.assistant.** { *; }
#-keep class org.teslasoft.core.** { *; }
#
#-keepattributes InnerClasses
#
#-keep class **.R
#-keep class **.R$* {
#    <fields>;
#}

#-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
#  public static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
#  public static void checkFieldIsNotNull(java.lang.Object, java.lang.String);
#  public static void checkFieldIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
#  public static void checkNotNull(java.lang.Object);
#  public static void checkNotNull(java.lang.Object, java.lang.String);
#  public static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
#  public static void checkNotNullParameter(java.lang.Object, java.lang.String);
#  public static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
#  public static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String);
#  public static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
#  public static void throwUninitializedPropertyAccessException(java.lang.String);
#}

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.slf4j.impl.StaticLoggerBinder