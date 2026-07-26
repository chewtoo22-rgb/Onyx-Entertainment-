#include <jni.h>

#include "onyx_dsp_engine.h"

namespace {
onyx::DspEngine* AsEngine(jlong handle) {
    return reinterpret_cast<onyx::DspEngine*>(handle);
}
}  // namespace

extern "C" JNIEXPORT jlong JNICALL
Java_com_onyx_avhub_core_dsp_AudioDspEngine_nativeCreate(
    JNIEnv* /* env */, jobject /* thiz */, jint sampleRateHz, jint channelCount, jint bandCount) {
    auto* engine = new onyx::DspEngine(sampleRateHz, channelCount, bandCount);
    return reinterpret_cast<jlong>(engine);
}

extern "C" JNIEXPORT void JNICALL
Java_com_onyx_avhub_core_dsp_AudioDspEngine_nativeSetBand(
    JNIEnv* /* env */, jobject /* thiz */, jlong handle, jint bandIndex, jdouble b0, jdouble b1,
    jdouble b2, jdouble a1, jdouble a2) {
    onyx::DspEngine* engine = AsEngine(handle);
    if (engine == nullptr) {
        return;
    }
    engine->SetBand(bandIndex, b0, b1, b2, a1, a2);
}

extern "C" JNIEXPORT void JNICALL
Java_com_onyx_avhub_core_dsp_AudioDspEngine_nativeProcess(
    JNIEnv* env, jobject /* thiz */, jlong handle, jfloatArray buffer, jint frameCount) {
    onyx::DspEngine* engine = AsEngine(handle);
    if (engine == nullptr) {
        return;
    }
    jfloat* elements = env->GetFloatArrayElements(buffer, nullptr);
    if (elements == nullptr) {
        return;
    }
    engine->Process(elements, frameCount);
    env->ReleaseFloatArrayElements(buffer, elements, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_onyx_avhub_core_dsp_AudioDspEngine_nativeDestroy(
    JNIEnv* /* env */, jobject /* thiz */, jlong handle) {
    delete AsEngine(handle);
}
