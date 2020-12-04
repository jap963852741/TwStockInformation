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
    std::vector<std::string> splitStr2Vec(std::string s, std::string splitSep)
    {
        std::vector<std::string> result;
        int current = 0; //最初由 0 的位置開始找
        int next = 0;
        while (next != -1)
        {
            next = s.find(splitSep, current); //尋找從current開始，出現splitSep的第一個位置(找不到則回傳-1)
            if (next != current)
            {
                std::string tmp = s.substr(current, next - current);
                if(!tmp.empty())  //忽略空字串(若不寫的話，尾巴都是分割符會錯)
                {
                    result.push_back(tmp);
                }
            }
            current = next + 1; //下次由 next + 1 的位置開始找起。
        }
        return result;
    }
    // replace all occurance of t in s to w
    std::string replace_all(std::string & s, std::string const & t, std::string const & w){
        std::string::size_type pos = s.find(t), t_size = t.size(), r_size = w.size();
        while(pos != std::string::npos){ // found
            s.replace(pos, t_size, w);
            pos = s.find(t, pos + r_size );
        }
        return s;
    }


    std::vector<std::string> get_change_titles(const std::string & cacert_path) {
    std::string error;
    auto result = http::Client(cacert_path)
                      .get(
                          "http://android-review.googlesource.com/changes/?q=status:open&n=10",
                          &error);
    if (!result) {
      return {error.c_str()};
    }

    // Strip XSSI defense prefix:
    // https://gerrit-review.googlesource.com/Documentation/rest-api.html#output
    const std::string payload = result.value().substr(5);

    Json::Value root;
    std::istringstream(payload) >> root;
    std::vector<std::string> titles;
    for (const auto& change : root) {
      titles.push_back(change["subject"].asString());
    }
    return titles;
  }

  std::vector<std::string> get_stock_information(const std::string & cacert_path) {
    std::string error;
    auto tmp_result = http::Client(cacert_path);
    tmp_result.set_header("referer:https://histock.tw/stock/3008");
    auto result = tmp_result.get("https://histock.tw/stock/module/stockdata.aspx?m=stocks&mid=NaN",&error);
    if (!result) {
        return {error.c_str()};
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
    std::vector<std::string> split_string = splitStr2Vec(payload,"},");

//    __android_log_print(ANDROID_LOG_INFO, "lclclc", "%s",split_string); //log i类型

     std::vector<std::string> titles;
     std::map<std::string,std::string> final_result;
     for (std::string t : split_string){
         std::string temp_no = "";
         std::string temp_name = "";
//         __android_log_print(ANDROID_LOG_INFO, "lclclc", "t %s",t.c_str()); //log i类型
         for(std::string temp_t : splitStr2Vec(t,",")){
//             __android_log_print(ANDROID_LOG_INFO, "lclclc", "temp_t %s",temp_t.c_str()); //log i类型
//             __android_log_print(ANDROID_LOG_INFO, "lclclc", "temp_t.find(\"No:\") %s",std::to_string(temp_t.find("No:")).c_str()); //log i类型

             if(temp_t.find("No:") == 2){
                 temp_no = replace_all(temp_t,"No:","");
//                 __android_log_print(ANDROID_LOG_INFO, "lclclc", "No %s",temp_no.c_str()); //log i类型
             } else if(temp_t.find("Name:") == 1){
                 temp_name = replace_all(temp_t,"Name:","");
//                 __android_log_print(ANDROID_LOG_INFO, "lclclc", "Name %s",temp_name.c_str()); //log i类型
             }
         }
         final_result[temp_no] = temp_name;
//         std::string bb = std::to_string(a);
//         __android_log_print(ANDROID_LOG_INFO, "lclclc", "%s",bb.c_str()); //log i类型
//         __android_log_print(ANDROID_LOG_INFO, "lclclc", "%s",t.c_str()); //log i类型

     }

     for (auto& [key, value]: final_result) {
         __android_log_print(ANDROID_LOG_INFO, "key", "%s", key.c_str()); //log i类型
         __android_log_print(ANDROID_LOG_INFO, "value", "%s", value.c_str()); //log i类型
     }


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
    return titles;
  }



}  // namespace

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_jap_twstockinformation_MainActivity_getGerritChanges(JNIEnv* env,
                                                       jobject /* this */,
                                                       jstring cacert_java) {
    if (cacert_java == nullptr) {
        logging::FatalError(env, "cacert argument cannot be null");
    }

    const std::string cacert =
            curlssl::jni::Convert<std::string>::from(env, cacert_java);
    return jni::Convert<jobjectArray, jstring>::from(env,
                                                     get_change_titles(cacert));

}

extern "C" JNIEXPORT jstring JNICALL
Java_com_jap_twstockinformation_MainActivity_getresult(JNIEnv* env
        ,jobject /* this */
        ,jstring cacert_java) {
  const char* test = "something";
  const std::string cacert = curlssl::jni::Convert<std::string>::from(env, cacert_java);
  get_stock_information(cacert);
  return (jstring) test;

}


}  // namespace curlssl
