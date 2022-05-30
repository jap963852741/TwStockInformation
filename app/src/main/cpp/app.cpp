/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <jni.h>
#include <sstream>
#include <string>
#include <iostream>

#include <curl/curl.h>
#include <android/log.h>
#include <filesystem>
#include <regex>
#include <random>
#include <bitset>
#include "http.h"
#include "java_interop.h"
#include "json/json.h"
#include "logging.h"

namespace curlssl {
    namespace {
//函數功能: 傳入一個字串s，以splitSep裡的字元當做分割字符，回傳vector<string>
        std::vector<std::string> splitStr2Vec(std::string s, std::string splitSep) {
            std::vector<std::string> result;
            int current = 0; //最初由 0 的位置開始找
            int next = 0;
            while (next != -1) {
                next = s.find(splitSep, current); //尋找從current開始，出現splitSep的第一個位置(找不到則回傳-1)
                if (next != current) {
                    std::string tmp = s.substr(current, next - current);
                    if (!tmp.empty())  //忽略空字串(若不寫的話，尾巴都是分割符會錯)
                    {
                        result.push_back(tmp);
                    }
                }
                current = next + 1; //下次由 next + 1 的位置開始找起。
            }
            return result;
        }

        // replace all occurance of t in s to w
        std::string replace_all(std::string &s, std::string const &t, std::string const &w) {
            std::string::size_type pos = s.find(t), t_size = t.size(), r_size = w.size();
            while (pos != std::string::npos) { // found
                s.replace(pos, t_size, w);
                pos = s.find(t, pos + r_size);
            }
            return s;
        }


        void correctUtfBytes(char *bytes) {
            char three = 0;
            while (*bytes != '\0') {
                unsigned char utf8 = *(bytes++);
                three = 0;
                // Switch on the high four bits.
                switch (utf8 >> 4) {
                    case 0x00:
                    case 0x01:
                    case 0x02:
                    case 0x03:
                    case 0x04:
                    case 0x05:
                    case 0x06:
                    case 0x07:
                        // Bit pattern 0xxx. No need for any extra bytes.
                        break;
                    case 0x08:
                    case 0x09:
                    case 0x0a:
                    case 0x0b:
                    case 0x0f:
                        /*
                         * Bit pattern 10xx or 1111, which are illegal start bytes.
                         * Note: 1111 is valid for normal UTF-8, but not the
                         * modified UTF-8 used here.
                         */
                        *(bytes - 1) = '?';
                        break;
                    case 0x0e:
                        // Bit pattern 1110, so there are two additional bytes.
                        utf8 = *(bytes++);
                        if ((utf8 & 0xc0) != 0x80) {
                            --bytes;
                            *(bytes - 1) = '?';
                            break;
                        }
                        three = 1;
                        // Fall through to take care of the final byte.
                    case 0x0c:
                    case 0x0d:
                        // Bit pattern 110x, so there is one additional byte.
                        utf8 = *(bytes++);
                        if ((utf8 & 0xc0) != 0x80) {
                            --bytes;
                            if (three)--bytes;
                            *(bytes - 1) = '?';
                        }
                        break;
                }
            }
        }

        std::map<std::string, std::string> get_stock_information(const std::string &cacert_path) {
            std::string error;
            std::map<std::string, std::string> final_result;
            auto tmp_result = http::Client(cacert_path);
            tmp_result.set_header("referer:https://histock.tw/stock/3008");
            auto result = tmp_result.get(
                    "https://histock.tw/stock/module/stockdata.aspx?m=stocks&mid=NaN", &error);
            if (!result) {
                return final_result;
            }
            __android_log_print(ANDROID_LOG_INFO, "lclclc", "1"); //log i类型

            // Strip XSSI defense prefix:
            // https://gerrit-review.googlesource.com/Documentation/rest-api.html#output
            std::string payload = result.value();
            payload = replace_all(payload, "]", "");
            payload = replace_all(payload, "[", "");
            payload = replace_all(payload, "{", "");
            payload = replace_all(payload, " ", "");
            payload = replace_all(payload, "\n", "");
            payload = replace_all(payload, "\"", "");
            std::vector<std::string> split_string = splitStr2Vec(payload, "},");

//    __android_log_print(ANDROID_LOG_INFO, "lclclc", "%s",split_string); //log i类型

            for (std::string t: split_string) {
                std::string temp_no = "";
                std::string temp_name = "";
//         __android_log_print(ANDROID_LOG_INFO, "lclclc", "t %s",t.c_str()); //log i类型
                for (std::string temp_t: splitStr2Vec(t, ",")) {
//             __android_log_print(ANDROID_LOG_INFO, "lclclc", "temp_t %s",temp_t.c_str()); //log i类型
//             __android_log_print(ANDROID_LOG_INFO, "lclclc", "temp_t.find(\"No:\") %s",std::to_string(temp_t.find("No:")).c_str()); //log i类型

                    if (temp_t.find("No:") == 2) {
                        temp_no = replace_all(temp_t, "No:", "");
//                 __android_log_print(ANDROID_LOG_INFO, "lclclc", "No %s",temp_no.c_str()); //log i类型
                    } else if (temp_t.find("Name:") == 1) {
                        temp_name = replace_all(temp_t, "Name:", "");
//                 __android_log_print(ANDROID_LOG_INFO, "lclclc", "Name %s",temp_name.c_str()); //log i类型
                    }
                }
                final_result[temp_no] = temp_name;
//         std::string bb = std::to_string(a);
//         __android_log_print(ANDROID_LOG_INFO, "lclclc", "%s",bb.c_str()); //log i类型
//         __android_log_print(ANDROID_LOG_INFO, "lclclc", "%s",t.c_str()); //log i类型

            }

//     for (auto& [key, value]: final_result) {
//         __android_log_print(ANDROID_LOG_INFO, "key", "%s", key.c_str()); //log i类型
//         __android_log_print(ANDROID_LOG_INFO, "value", "%s", value.c_str()); //log i类型
//     }


//    Json::Value root;
//    std::istringstream(payload) >> root;
//    std::vector<std::string> titles;
//    for (const auto & change : root) {
////        vec.push_back() - 新增元素至vector 的尾端
//        __android_log_print(ANDROID_LOG_INFO, "size", "%u",change.size()); //log i类型
//
//        __android_log_print(ANDROID_LOG_INFO, "lclclc", "%s",change["No"].asCString()); //log i类型
//
//        __android_log_print(ANDROID_LOG_INFO, "lclclc", "%s",change.asString().c_str()); //log i类型
//      titles.push_back(change["No"].asString());
//    }
            return final_result;
        }

        std::string get_stock_price(const std::string &cacert_path) {
            std::string error;
            std::string final_result;
            auto tmp_result = http::Client(cacert_path);
            auto result = tmp_result.get("https://histock.tw/stock/rank.aspx?p=all", &error);
            if (!result) {
                return final_result;
            }
            final_result = result.value();
//        __android_log_print(ANDROID_LOG_INFO, "lclclc", "%s",result.value().c_str()); //log i类型
            return final_result;
        }

        std::string get_stock_fundamental(const std::string &cacert_path) {
            std::string error;
            std::string final_result;
            auto tmp_result = http::Client(cacert_path);
            std::string url = "https://www.twse.com.tw/exchangeReport/BWIBBU_d?response=json&date=&selectType=&_=";
            std::string unix_time = std::to_string(std::chrono::duration_cast<std::chrono::seconds>(
                    std::chrono::system_clock::now().time_since_epoch()).count());
            url.append(unix_time);
//        __android_log_print(ANDROID_LOG_INFO, "lclclc", "%s",url.c_str()); //log i类型
            auto result = tmp_result.get(url, &error);
            if (!result) {
                return final_result;
            }
            final_result = result.value();
//        __android_log_print(ANDROID_LOG_INFO, "lclclc", "%s",result.value().c_str()); //log i类型
            return final_result;
        }

        std::string get_stock_income(const std::string &cacert_path) {
            std::string error;
            std::string final_result;
            auto tmp_result = http::Client(cacert_path);
            tmp_result.set_header(
                    "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (K HTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
            auto result = tmp_result.get("https://stock.wespai.com/income", &error);
            if (!result) {
                return final_result;
            }
            final_result = result.value();
//        __android_log_print(ANDROID_LOG_INFO, "lclclc", "%s",result.value().c_str()); //log i类型
            return final_result;
        }

        std::string get_Institutional_Investors_Ratio(const std::string &cacert_path) {
            std::string error;
            std::string final_result;
            auto tmp_result = http::Client(cacert_path);
            tmp_result.set_header(
                    "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (K HTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
            auto result = tmp_result.get("https://stock.wespai.com/p/60546", &error);
            if (!result) {
                return final_result;
            }
            final_result = result.value();
//        __android_log_print(ANDROID_LOG_INFO, "lclclc", "%s",result.value().c_str()); //log i类型
            return final_result;
        }


    }  // namespace



    extern "C" JNIEXPORT jobject JNICALL
    Java_com_jap_twstockinformation_MainActivity_getresult(JNIEnv *env, jobject /* this */,
                                                           jstring cacert_java) {

        const std::string cacert = curlssl::jni::Convert<std::string>::from(env, cacert_java);
        std::map<std::string, std::string> mMap = get_stock_information(cacert);

        jclass java_cls_HashMap = env->FindClass("java/util/HashMap");
        jmethodID java_mid_HashMap = env->GetMethodID(java_cls_HashMap, "<init>", "()V");
        jobject java_obj_HashMap = env->NewObject(java_cls_HashMap, java_mid_HashMap);
        jmethodID java_mid_HashMap_put = env->GetMethodID(java_cls_HashMap, "put",
                                                          "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        for (auto&[key, value]: mMap) {
            jstring c_key = env->NewStringUTF(key.c_str());
            jstring c_value = env->NewStringUTF(value.c_str());

            env->CallObjectMethod(java_obj_HashMap, java_mid_HashMap_put, c_key, c_value);

            env->DeleteLocalRef(c_key);
            env->DeleteLocalRef(c_value);

        }
        return java_obj_HashMap;


    }

    extern "C" JNIEXPORT jobject JNICALL
    Java_com_jap_twstockinformation_StockUtil_getMapNumName(JNIEnv *env, jobject /* this */,
                                                            jstring cacert_java) {

        const std::string cacert = curlssl::jni::Convert<std::string>::from(env, cacert_java);
        std::map<std::string, std::string> mMap = get_stock_information(cacert);

        jclass java_cls_HashMap = env->FindClass("java/util/HashMap");
        jmethodID java_mid_HashMap = env->GetMethodID(java_cls_HashMap, "<init>", "()V");
        jobject java_obj_HashMap = env->NewObject(java_cls_HashMap, java_mid_HashMap);
        jmethodID java_mid_HashMap_put = env->GetMethodID(java_cls_HashMap, "put",
                                                          "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        for (auto&[key, value]: mMap) {
            jstring c_key = env->NewStringUTF(key.c_str());
            jstring c_value = env->NewStringUTF(value.c_str());

            env->CallObjectMethod(java_obj_HashMap, java_mid_HashMap_put, c_key, c_value);

            env->DeleteLocalRef(c_key);
            env->DeleteLocalRef(c_value);

        }
        return java_obj_HashMap;


    }

    extern "C" JNIEXPORT jstring JNICALL
    Java_com_jap_twstockinformation_StockUtil_getHtmlStringPrice(JNIEnv *env, jobject /* this */,
                                                                 jstring cacert_java) {
        const std::string cacert = curlssl::jni::Convert<std::string>::from(env, cacert_java);
        jstring html_string;
        std::string a = get_stock_price(cacert).c_str();
        html_string = env->NewStringUTF(a.c_str()); // C style string to Java String
        return html_string;
    }

    extern "C" JNIEXPORT jstring JNICALL
    Java_com_jap_twstockinformation_StockUtil_getJsonStringFundamental(JNIEnv *env,
                                                                       jobject /* this */,
                                                                       jstring cacert_java) {
        const std::string cacert = curlssl::jni::Convert<std::string>::from(env, cacert_java);
        jstring html_string;
        std::string a = get_stock_fundamental(cacert).c_str();
        html_string = env->NewStringUTF(a.c_str()); // C style string to Java String
        return html_string;
    }

    extern "C" JNIEXPORT jstring JNICALL
    Java_com_jap_twstockinformation_StockUtil_getHtmlStringIncome(JNIEnv *env, jobject /* this */,
                                                                  jstring cacert_java) {
        const std::string cacert = curlssl::jni::Convert<std::string>::from(env, cacert_java);
        jstring html_string;
        std::string str = get_stock_income(cacert);
        char *cstr = new char[str.length() + 1];
        strcpy(cstr, str.c_str());
        correctUtfBytes(cstr);//newStringUTF出现input is not valid Modified UTF-8错误解决办法
        html_string = env->NewStringUTF(cstr); // C style string to Java String
        return html_string;
    }

    extern "C" JNIEXPORT jstring JNICALL
    Java_com_jap_twstockinformation_StockUtil_getHtmlStringInstitutionalInvestorsRatio(JNIEnv *env,
                                                                                       jobject /* this */,
                                                                                       jstring cacert_java) {
        const std::string cacert = curlssl::jni::Convert<std::string>::from(env, cacert_java);
        jstring html_string;
        std::string str = get_Institutional_Investors_Ratio(cacert);
        char *cstr = new char[str.length() + 1];
        strcpy(cstr, str.c_str());
        correctUtfBytes(cstr);//newStringUTF出现input is not valid Modified UTF-8错误解决办法
        html_string = env->NewStringUTF(cstr); // C style string to Java String
        return html_string;
    }


}  // namespace curlssl
