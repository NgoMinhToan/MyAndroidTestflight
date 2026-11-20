# R8 / resource shrinking rules for the release build.
#
# Most libraries ship their own consumer rules (androidx, Compose, Hilt, Retrofit, OkHttp,
# kotlinx.serialization, Coil), so the default configuration shrinks this app without extra work.
# The entries below are defensive: they document the reflection boundaries the app relies on and
# keep the build working if a dependency stops shipping its rules.

# ---------------------------------------------------------------------------------------------
# Retrofit
# ---------------------------------------------------------------------------------------------
# Retrofit resolves endpoint annotations and generic return/parameter types reflectively.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep,allowobfuscation,allowshrinking interface com.mycompany.testflight.data.remote.TestflightApi
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
# R8 full mode strips generic signatures from interfaces that are only referenced as types.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# ---------------------------------------------------------------------------------------------
# kotlinx.serialization
# ---------------------------------------------------------------------------------------------
# Generated serializers are looked up through the companion object and keep JSON keys in
# @SerialName, so the DTO members must survive obfuscation of their annotations.
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault
-keepclassmembers class com.mycompany.testflight.data.remote.dto.** {
    *** Companion;
}
-keepclasseswithmembers class com.mycompany.testflight.data.remote.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---------------------------------------------------------------------------------------------
# Diagnostics
# ---------------------------------------------------------------------------------------------
# Keep line numbers so release crash reports still point at source lines.
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
