package com.ni.helper

import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiInterface {
    @FormUrlEncoded
    @POST("sendchat")
    fun sendChat(@FieldMap map: Map<String?, String?>?): Call<HashMap<String?, String?>?>?
}