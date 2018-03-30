#include <jni.h>
#include <string>

# ifdef Debug
#include <android/log.h>
#define LOG_TAG "Android-Zhang:"
#define LOGE(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
# endif

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_example_km_ndk_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    #ifdef Debug
        LOGE("这是一条测试数据");
    # endif
    return env->NewStringUTF(hello.c_str());
}
