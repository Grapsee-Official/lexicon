// lexora_jni.cpp — JNI bridge between Kotlin (LexoraEngine.android.kt) and C++ engine
// Per spec: "KMM wrapper. Hard gate: Kotlin/Swift can call C++ and receive strings."
#include "lexora/c_api.h"
#include "lexora/engine.hpp"
#include <jni.h>
#include <string>
#include <vector>
#include <cstring>

// Store engine handles in a map (handle → LexoraEngineHandle*)
// The Kotlin side passes a jlong that's a pointer cast
#define CAST_HANDLE(ptr) reinterpret_cast<LexoraEngineHandle*>(ptr)
#define CAST_PTR(handle) reinterpret_cast<jlong>(handle)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_lexora_engine_LexoraEngine_nativeCreate(JNIEnv* env, jobject thiz) {
    auto* handle = lexora_engine_create();
    return CAST_PTR(handle);
}

JNIEXPORT jboolean JNICALL
Java_com_lexora_engine_LexoraEngine_nativeLoad(JNIEnv* env, jobject thiz, jlong handle, jstring db_dir) {
    const char* dir = env->GetStringUTFChars(db_dir, nullptr);
    bool result = lexora_engine_load(CAST_HANDLE(handle), dir) == 0;
    env->ReleaseStringUTFChars(db_dir, dir);
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jlong JNICALL
Java_com_lexora_engine_LexoraEngine_nativeLookup(JNIEnv* env, jobject thiz, jlong handle, jstring word) {
    const char* word_str = env->GetStringUTFChars(word, nullptr);
    auto* entry = lexora_engine_lookup(CAST_HANDLE(handle), word_str);
    env->ReleaseStringUTFChars(word, word_str);
    return reinterpret_cast<jlong>(entry);
}

JNIEXPORT jlong JNICALL
Java_com_lexora_engine_LexoraEngine_nativeFuzzySearch(JNIEnv* env, jobject thiz, jlong handle, jstring query, jint max_dist, jint limit) {
    const char* query_str = env->GetStringUTFChars(query, nullptr);
    auto* results = lexora_engine_fuzzy_search(CAST_HANDLE(handle), query_str, max_dist, limit);
    env->ReleaseStringUTFChars(query, query_str);
    return reinterpret_cast<jlong>(results);
}

JNIEXPORT void JNICALL
Java_com_lexora_engine_LexoraEngine_nativeFreeEntry(JNIEnv* env, jobject thiz, jlong entry_ptr) {
    auto* entry = reinterpret_cast<LexoraWordEntry*>(entry_ptr);
    lexora_engine_free_entry(entry);
}

JNIEXPORT void JNICALL
Java_com_lexora_engine_LexoraEngine_nativeFreeResults(JNIEnv* env, jobject thiz, jlong results_ptr) {
    auto* results = reinterpret_cast<LexoraSearchResults*>(results_ptr);
    lexora_engine_free_results(results);
}

JNIEXPORT void JNICALL
Java_com_lexora_engine_LexoraEngine_nativeEnableDawg(JNIEnv* env, jobject thiz, jlong handle, jboolean enabled) {
    lexora_engine_enable_dawg(CAST_HANDLE(handle), enabled ? 1 : 0);
}

JNIEXPORT jboolean JNICALL
Java_com_lexora_engine_LexoraEngine_nativeIsDawgEnabled(JNIEnv* env, jobject thiz, jlong handle) {
    return lexora_engine_is_dawg_enabled(CAST_HANDLE(handle)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jlongArray JNICALL
Java_com_lexora_engine_LexoraEngine_nativeGetMemoryUsage(JNIEnv* env, jobject thiz, jlong handle) {
    auto info = lexora_engine_get_memory_usage(CAST_HANDLE(handle));
    jlongArray result = env->NewLongArray(3);
    jlong arr[3] = {
        static_cast<jlong>(info.hash_table_bytes),
        static_cast<jlong>(info.dawg_bytes),
        static_cast<jlong>(info.total_bytes)
    };
    env->SetLongArrayRegion(result, 0, 3, arr);
    return result;
}

JNIEXPORT void JNICALL
Java_com_lexora_engine_LexoraEngine_nativeClose(JNIEnv* env, jobject thiz, jlong handle) {
    lexora_engine_close(CAST_HANDLE(handle));
}

// ─── Entry field accessors ──────────────────────────────────────────────────

JNIEXPORT jobjectArray JNICALL
Java_com_lexora_engine_LexoraEngine_nativeGetEntryFields(JNIEnv* env, jobject thiz, jlong entry_ptr) {
    auto* entry = reinterpret_cast<LexoraWordEntry*>(entry_ptr);
    jclass strClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(6, strClass, nullptr);

    env->SetObjectArrayElement(result, 0, env->NewStringUTF(entry->id ?: ""));
    env->SetObjectArrayElement(result, 1, env->NewStringUTF(entry->word ?: ""));
    env->SetObjectArrayElement(result, 2, env->NewStringUTF(entry->phonetic ?: ""));
    env->SetObjectArrayElement(result, 3, env->NewStringUTF(entry->part_of_speech ?: ""));
    env->SetObjectArrayElement(result, 4, env->NewStringUTF(entry->etymology ?: ""));
    env->SetObjectArrayElement(result, 5, env->NewStringUTF(entry->language ?: ""));

    return result;
}

JNIEXPORT jobjectArray JNICALL
Java_com_lexora_engine_LexoraEngine_nativeGetEntryDefinitions(JNIEnv* env, jobject thiz, jlong entry_ptr) {
    auto* entry = reinterpret_cast<LexoraWordEntry*>(entry_ptr);
    jclass strClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(entry->definition_count, strClass, nullptr);

    for (uint32_t i = 0; i < entry->definition_count; ++i) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(entry->definitions[i] ?: ""));
    }
    return result;
}

JNIEXPORT jobjectArray JNICALL
Java_com_lexora_engine_LexoraEngine_nativeGetEntrySynonyms(JNIEnv* env, jobject thiz, jlong entry_ptr) {
    auto* entry = reinterpret_cast<LexoraWordEntry*>(entry_ptr);
    jclass strClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(entry->synonym_count, strClass, nullptr);

    for (uint32_t i = 0; i < entry->synonym_count; ++i) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(entry->synonyms[i] ?: ""));
    }
    return result;
}

JNIEXPORT jobjectArray JNICALL
Java_com_lexora_engine_LexoraEngine_nativeGetEntryAntonyms(JNIEnv* env, jobject thiz, jlong entry_ptr) {
    auto* entry = reinterpret_cast<LexoraWordEntry*>(entry_ptr);
    jclass strClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(entry->antonym_count, strClass, nullptr);

    for (uint32_t i = 0; i < entry->antonym_count; ++i) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(entry->antonyms[i] ?: ""));
    }
    return result;
}

JNIEXPORT jobjectArray JNICALL
Java_com_lexora_engine_LexoraEngine_nativeGetEntryExamples(JNIEnv* env, jobject thiz, jlong entry_ptr) {
    auto* entry = reinterpret_cast<LexoraWordEntry*>(entry_ptr);
    jclass strClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(entry->example_count, strClass, nullptr);

    for (uint32_t i = 0; i < entry->example_count; ++i) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(entry->examples[i] ?: ""));
    }
    return result;
}

// ─── Result array accessors ─────────────────────────────────────────────────

JNIEXPORT jint JNICALL
Java_com_lexora_engine_LexoraEngine_nativeGetResultCount(JNIEnv* env, jobject thiz, jlong results_ptr) {
    auto* results = reinterpret_cast<LexoraSearchResults*>(results_ptr);
    return static_cast<jint>(results->count);
}

JNIEXPORT jlong JNICALL
Java_com_lexora_engine_LexoraEngine_nativeGetResultEntry(JNIEnv* env, jobject thiz, jlong results_ptr, jint index) {
    auto* results = reinterpret_cast<LexoraSearchResults*>(results_ptr);
    return reinterpret_cast<jlong>(&results->results[index].entry);
}

JNIEXPORT jint JNICALL
Java_com_lexora_engine_LexoraEngine_nativeGetResultDistance(JNIEnv* env, jobject thiz, jlong results_ptr, jint index) {
    auto* results = reinterpret_cast<LexoraSearchResults*>(results_ptr);
    return results->results[index].edit_distance;
}

} // extern "C"
