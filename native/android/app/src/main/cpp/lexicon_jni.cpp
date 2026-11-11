// lexicon_jni.cpp — JNI bridge between Kotlin (LexiconEngine.android.kt) and C++ engine
// Per spec: "KMM wrapper. Hard gate: Kotlin/Swift can call C++ and receive strings."
#include "lexicon/c_api.h"
#include "lexicon/engine.hpp"
#include <jni.h>
#include <string>
#include <vector>
#include <cstring>

// Store engine handles in a map (handle → LexiconEngineHandle*)
// The Kotlin side passes a jlong that's a pointer cast
#define CAST_HANDLE(ptr) reinterpret_cast<LexiconEngineHandle*>(ptr)
#define CAST_PTR(handle) reinterpret_cast<jlong>(handle)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeCreate(JNIEnv* env, jobject thiz) {
    auto* handle = lexicon_engine_create();
    return CAST_PTR(handle);
}

JNIEXPORT jboolean JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeLoad(JNIEnv* env, jobject thiz, jlong handle, jstring db_dir) {
    if (!handle || !db_dir) return JNI_FALSE;
    const char* dir = env->GetStringUTFChars(db_dir, nullptr);
    if (!dir) return JNI_FALSE;
    bool result = lexicon_engine_load(CAST_HANDLE(handle), dir) == 0;
    env->ReleaseStringUTFChars(db_dir, dir);
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jlong JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeLookup(JNIEnv* env, jobject thiz, jlong handle, jstring word) {
    if (!handle || !word) return 0;
    const char* word_str = env->GetStringUTFChars(word, nullptr);
    if (!word_str) return 0;
    auto* entry = lexicon_engine_lookup(CAST_HANDLE(handle), word_str);
    env->ReleaseStringUTFChars(word, word_str);
    return reinterpret_cast<jlong>(entry);
}

JNIEXPORT jlong JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeFuzzySearch(JNIEnv* env, jobject thiz, jlong handle, jstring query, jint max_dist, jint limit) {
    if (!handle || !query) return 0;
    const char* query_str = env->GetStringUTFChars(query, nullptr);
    if (!query_str) return 0;
    auto* results = lexicon_engine_fuzzy_search(CAST_HANDLE(handle), query_str, max_dist, static_cast<uint32_t>(limit > 0 ? limit : 20));
    env->ReleaseStringUTFChars(query, query_str);
    return reinterpret_cast<jlong>(results);
}

JNIEXPORT void JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeFreeEntry(JNIEnv* env, jobject thiz, jlong entry_ptr) {
    auto* entry = reinterpret_cast<LexiconWordEntry*>(entry_ptr);
    lexicon_engine_free_entry(entry);
}

JNIEXPORT void JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeFreeResults(JNIEnv* env, jobject thiz, jlong results_ptr) {
    auto* results = reinterpret_cast<LexiconSearchResults*>(results_ptr);
    lexicon_engine_free_results(results);
}

JNIEXPORT void JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeEnableDawg(JNIEnv* env, jobject thiz, jlong handle, jboolean enabled) {
    lexicon_engine_enable_dawg(CAST_HANDLE(handle), enabled ? 1 : 0);
}

JNIEXPORT jboolean JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeIsDawgEnabled(JNIEnv* env, jobject thiz, jlong handle) {
    return lexicon_engine_is_dawg_enabled(CAST_HANDLE(handle)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jlongArray JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeGetMemoryUsage(JNIEnv* env, jobject thiz, jlong handle) {
    auto info = lexicon_engine_get_memory_usage(CAST_HANDLE(handle));
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
Java_com_lexicon_engine_LexiconEngine_nativeClose(JNIEnv* env, jobject thiz, jlong handle) {
    lexicon_engine_close(CAST_HANDLE(handle));
}

// ─── Entry field accessors ──────────────────────────────────────────────────

JNIEXPORT jobjectArray JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeGetEntryFields(JNIEnv* env, jobject thiz, jlong entry_ptr) {
    auto* entry = reinterpret_cast<LexiconWordEntry*>(entry_ptr);
    jclass strClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(6, strClass, nullptr);
    if (!entry) return result;

    env->SetObjectArrayElement(result, 0, env->NewStringUTF(entry->id ? entry->id : ""));
    env->SetObjectArrayElement(result, 1, env->NewStringUTF(entry->word ? entry->word : ""));
    env->SetObjectArrayElement(result, 2, env->NewStringUTF(entry->phonetic ? entry->phonetic : ""));
    env->SetObjectArrayElement(result, 3, env->NewStringUTF(entry->part_of_speech ? entry->part_of_speech : ""));
    env->SetObjectArrayElement(result, 4, env->NewStringUTF(entry->etymology ? entry->etymology : ""));
    env->SetObjectArrayElement(result, 5, env->NewStringUTF(entry->language ? entry->language : ""));

    return result;
}

JNIEXPORT jobjectArray JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeGetEntryDefinitions(JNIEnv* env, jobject thiz, jlong entry_ptr) {
    auto* entry = reinterpret_cast<LexiconWordEntry*>(entry_ptr);
    jclass strClass = env->FindClass("java/lang/String");
    uint32_t n = entry ? entry->definition_count : 0;
    jobjectArray result = env->NewObjectArray(n, strClass, nullptr);

    for (uint32_t i = 0; i < n; ++i) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(entry->definitions[i] ? entry->definitions[i] : ""));
    }
    return result;
}

JNIEXPORT jobjectArray JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeGetEntrySynonyms(JNIEnv* env, jobject thiz, jlong entry_ptr) {
    auto* entry = reinterpret_cast<LexiconWordEntry*>(entry_ptr);
    jclass strClass = env->FindClass("java/lang/String");
    uint32_t n = entry ? entry->synonym_count : 0;
    jobjectArray result = env->NewObjectArray(n, strClass, nullptr);

    for (uint32_t i = 0; i < n; ++i) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(entry->synonyms[i] ? entry->synonyms[i] : ""));
    }
    return result;
}

JNIEXPORT jobjectArray JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeGetEntryAntonyms(JNIEnv* env, jobject thiz, jlong entry_ptr) {
    auto* entry = reinterpret_cast<LexiconWordEntry*>(entry_ptr);
    jclass strClass = env->FindClass("java/lang/String");
    uint32_t n = entry ? entry->antonym_count : 0;
    jobjectArray result = env->NewObjectArray(n, strClass, nullptr);

    for (uint32_t i = 0; i < n; ++i) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(entry->antonyms[i] ? entry->antonyms[i] : ""));
    }
    return result;
}

JNIEXPORT jobjectArray JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeGetEntryExamples(JNIEnv* env, jobject thiz, jlong entry_ptr) {
    auto* entry = reinterpret_cast<LexiconWordEntry*>(entry_ptr);
    jclass strClass = env->FindClass("java/lang/String");
    uint32_t n = entry ? entry->example_count : 0;
    jobjectArray result = env->NewObjectArray(n, strClass, nullptr);

    for (uint32_t i = 0; i < n; ++i) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(entry->examples[i] ? entry->examples[i] : ""));
    }
    return result;
}

// ─── Result array accessors ─────────────────────────────────────────────────

JNIEXPORT jint JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeGetResultCount(JNIEnv* env, jobject thiz, jlong results_ptr) {
    auto* results = reinterpret_cast<LexiconSearchResults*>(results_ptr);
    return static_cast<jint>(results->count);
}

JNIEXPORT jlong JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeGetResultEntry(JNIEnv* env, jobject thiz, jlong results_ptr, jint index) {
    auto* results = reinterpret_cast<LexiconSearchResults*>(results_ptr);
    return reinterpret_cast<jlong>(&results->results[index].entry);
}

JNIEXPORT jint JNICALL
Java_com_lexicon_engine_LexiconEngine_nativeGetResultDistance(JNIEnv* env, jobject thiz, jlong results_ptr, jint index) {
    auto* results = reinterpret_cast<LexiconSearchResults*>(results_ptr);
    return results->results[index].edit_distance;
}

} // extern "C"
