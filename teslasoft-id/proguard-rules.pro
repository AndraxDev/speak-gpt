# Keep the rules as narrow as possible so the library can be shrunk before it is bundled into the app.
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*

-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-dontnote kotlinx.serialization.AnnotationsKt
-keepnames class <1>$$serializer {
    static <1>$$serializer INSTANCE;
}

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

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

-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn java.lang.invoke.StringConcatFactory

# Keep the public API that the app consumes directly; everything else can still be shrunk.
-keep class org.teslasoft.core.auth.AccountSyncListener { *; }
-keep class org.teslasoft.core.auth.SystemInfo { *; }
-keep class org.teslasoft.core.auth.SystemInfo$Companion { *; }
-keep class org.teslasoft.core.auth.client.SettingsListener { *; }
-keep class org.teslasoft.core.auth.client.SyncListener { *; }
-keep class org.teslasoft.core.auth.client.TeslasoftIDClient { *; }
-keep class org.teslasoft.core.auth.internal.ApplicationSignature { *; }
-keep class org.teslasoft.core.auth.widget.TeslasoftIDCircledButton { *; }
