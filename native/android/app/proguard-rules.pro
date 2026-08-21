# =============================================================================
#  Lexora — ProGuard / R8 rules
#  -----------------------------------------------------------------------------
#  These rules are appended to the SDK-default `proguard-android-optimize.txt`
#  (see app/build.gradle.kts → buildTypes.release.proguardFiles). They are
#  effective only when `isMinifyEnabled = true`. The current release build
#  leaves minify off so the rules are inert in CI — they are written now so
#  that flipping `isMinifyEnabled = true` for a production release cannot
#  silently break the JNI bridge to the native C++ engine.
#
#  Three concerns drive every rule below:
#    1. JNI — the C++ engine calls into Java/Kotlin by *mangled* method name.
#       If R8 renames `com.lexora.engine.LexoraEngine` or any of its
#       `external fun native*` declarations, the JNI registration step throws
#       `java.lang.UnsatisfiedLinkError` at `System.loadLibrary` time and the
#       entire engine is bricked.
#    2. SQLite — the bundled SQLite shim and any future AndroidX SQLite helper
#       must keep their native entry points and reflective method names.
#    3. C-API symbol names — the .so file exports `Java_com_lexora_engine_*`
#       (JNI) and `lexora_engine_*` (C ABI). The .so is *not* touched by R8,
#       but the Java-side `external fun` declarations that the JNI resolver
#       matches against those symbols *are* in R8's reach. Keeping both the
#       classes and the native-method declarations intact is therefore
#       mandatory.
# =============================================================================


# -----------------------------------------------------------------------------
# 1. JNI bridge — the Kotlin class `com.lexora.engine.LexoraEngine` declares
#    `external fun native*` methods whose JVM-mangled names must match the
#    `Java_com_lexora_engine_LexoraEngine_native*` symbols exported by
#    `liblexora-engine.so` (see native/android/app/src/main/cpp/lexora_jni.cpp).
#
#    Keep the entire engine package and every member. The package is tiny
#    (LexoraEngine + DeltaSyncClient + LexoraColdStartOptimizer) so keeping
#    it whole incurs no measurable size cost relative to the native .so it
#    guards.
# -----------------------------------------------------------------------------
-keep class com.lexora.engine.** { *; }
-keep class com.lexora.engine.* { *; }

# Any class that declares a `native` method must be kept with its members,
# regardless of package. This is the standard JNI rule from the Android docs
# and is broader than just `com.lexora.engine.*` — it covers any future
# JNI surface (e.g. an NDK-backed image OCR module).
-keepclasseswithmembernames class * {
    native <methods>;
}

# The TextClassifier service is invoked reflectively by the framework from
# its manifest-declared name; without this rule R8 would rename the class
# and Android's TextClassifierManager would fail to bind.
-keep class com.lexora.textclassifier.** { *; }


# -----------------------------------------------------------------------------
# 2. SQLite — keep the symbols the C++ engine binds against.
#
#    The engine ships with either the system `libsqlite3.so` (found via
#    CMake `find_package(SQLite3)`) or the in-tree stub at
#    `app/src/main/cpp/sqlite3_stub/sqlite3_stub.c`. Both export the same
#    C symbol surface (`sqlite3_open`, `sqlite3_prepare_v2`, …) which the
#    NDK linker resolves against the .so file at load time — those symbols
#    are *outside* R8's reach.
#
#    What R8 *can* break is the Java/Kotlin side: if a future revision uses
#    `androidx.sqlite` or `Requery/sqlite-android`, the framework's
#    reflection must survive. Keep them defensively.
# -----------------------------------------------------------------------------
-keep class androidx.sqlite.** { *; }
-keep class androidx.sqlite.db.** { *; }
-keep class io.requery.android.database.** { *; }
-keep class org.sqlite.** { *; }

# SQLite open helpers are often instantiated reflectively by name.
-keep class * extends android.database.sqlite.SQLiteOpenHelper {
    <init>(...);
}
-keep class * extends androidx.sqlite.db.SupportSQLiteOpenHelper {
    <init>(...);
}


# -----------------------------------------------------------------------------
# 3. C-API function names — DO NOT obfuscate.
#
#    The C ABI in `native/cpp/include/lexora/c_api.h` exports the
#    `lexora_engine_*` family (`lexora_engine_create`, `lexora_engine_load`,
#    `lexora_engine_lookup`, `lexora_engine_fuzzy_search`,
#    `lexora_engine_free_entry`, `lexora_engine_free_results`,
#    `lexora_engine_enable_dawg`, `lexora_engine_is_dawg_enabled`,
#    `lexora_engine_get_memory_usage`, `lexora_engine_close`).
#
#    R8 cannot rename symbols inside the .so file, but it CAN rename the
#    Kotlin `external fun` declarations on `com.lexora.engine.LexoraEngine`
#    that the JNI resolver matches against `Java_com_lexora_engine_*`.
#    Without the keep rule above, the JVM would mangle `nativeLookup` into
#    `a` and the resolver would then look for
#    `Java_com_lexora_engine_LexoraEngine_a` — which does not exist, so
#    loadLibrary would throw UnsatisfiedLinkError.
#
#    Defensive belt-and-braces: also print a mapping note for any class
#    whose native method declarations are stripped, so a future build break
#    surfaces with a clear diagnostic instead of a runtime crash.
# -----------------------------------------------------------------------------
-keep,includedescriptorclasses class com.lexora.engine.LexoraEngine {
    native <methods>;
}

# Keep the data classes the JNI bridge marshals by field name. The C side
# sets fields by their exact JVM name through `env->GetFieldID`, so any
# rename would throw `NoSuchFieldError` at first lookup. Keeping the whole
# `com.lexora.engine` package above already covers this; the explicit rule
# documents intent for future maintainers.
-keep class com.lexora.engine.LexoraWordEntry { *; }
-keep class com.lexora.engine.LexoraSearchResult { *; }
-keep class com.lexora.engine.LexoraSearchResults { *; }
-keep class com.lexora.engine.LexoraMemoryInfo { *; }


# -----------------------------------------------------------------------------
# 4. Compose runtime — R8's default rules already keep the Compose runtime,
#    but the @Composable lambdas in our screens reference the engine types
#    by name (e.g. `LexoraViewModel::class`). Keep the ViewModel and its
#    StateFlow surface so recomposition doesn't trip over a missing symbol.
# -----------------------------------------------------------------------------
-keep class com.lexora.data.LexoraViewModel { *; }
-keep class com.lexora.data.Models$* { *; }
-keep class com.lexora.data.Models { *; }


# -----------------------------------------------------------------------------
# 5. Serialization — DeltaSyncClient uses kotlinx.serialization. R8 strips
#    the @Serializable metadata unless told otherwise; this is the canonical
#    keep rule from kotlinx-serialization's docs.
# -----------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep every @Serializable data class in the engine package — the server
# round-trips WordChange, SyncResponse, LanguagePackInfo by field name.
-keep,includedescriptorclasses @kotlinx.serialization.Serializable class **


# -----------------------------------------------------------------------------
# 6. Generic safety — never strip source file names or line numbers from
#    stack traces; crash logs are useless without them. The Data Safety
#    form (see PrivacyPolicy.md) declares that crash logs include stack
#    traces, so this rule is part of the privacy contract.
# -----------------------------------------------------------------------------
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
