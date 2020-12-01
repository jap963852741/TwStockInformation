#include <jni.h>
#include <string>
#include <stdio.h>
#include "curl/include/curl/curl.h"  //this searches the include path
#include <android/log.h>
extern "C" JNIEXPORT jstring JNICALL
Java_com_jap_twstockinformation_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

//extern "C" JNIEXPORT jstring JNICALL
//Java_com_jap_twstockinformation_ExampleInstrumentedTest_GetAllStockNumber(
//        JNIEnv* env,
//        jobject /* this */
//) {
//    CURL *curl;
//    CURLcode res;
//
//    curl = curl_easy_init();
//    if(curl) {
//        curl_easy_setopt(curl, CURLOPT_URL, "https://example.com");
//
//        /* Forcing HTTP/3 will make the connection fail if the server isn't
//           accessible over QUIC + HTTP/3 on the given host and port.
//           Consider using CURLOPT_ALTSVC instead! */
//        curl_easy_setopt(curl, CURLOPT_HTTP_VERSION, (long)CURL_HTTP_VERSION_3);
//
//        /* Perform the request, res will get the return code */
//        res = curl_easy_perform(curl);
//        /* Check for errors */
//        if(res != CURLE_OK)
//            fprintf(stderr, "curl_easy_perform() failed: %s\n",
//                    curl_easy_strerror(res));
//
//        /* always cleanup */
//        curl_easy_cleanup(curl);
//    }
//    return 0;
//}
