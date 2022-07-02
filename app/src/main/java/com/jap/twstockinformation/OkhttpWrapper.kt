package com.jap.twstockinformation

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response


object OkhttpWrapper {


    fun get(request: Request):Response? {
        val okHttpClient = OkHttpClient()
//        val request: Request = Request.Builder().url(url).method("GET", requestBody).build()
        val call: Call = okHttpClient.newCall(request)
        return try {
            call.execute()
        } catch (e :Exception) {
            null
        }
    }
}