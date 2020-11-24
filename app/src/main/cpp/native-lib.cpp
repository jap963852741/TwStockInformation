#include <jni.h>
#include <string>
#include <stdio.h>
#include <string>
#include <curl/curl.h>  //this searches the include path

extern "C" JNIEXPORT jstring JNICALL
Java_com_jap_twstockinformation_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_jap_twstockinformation_ExampleInstrumentedTest_GetAllStockNumber(
        JNIEnv* env,
        jobject /* this */,
) {

}
