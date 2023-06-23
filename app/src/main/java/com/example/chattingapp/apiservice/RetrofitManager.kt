package com.example.chattingapp.apiservice

import com.example.chattingapp.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitManager {
    val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_ADDRESS)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RetrofitService::class.java)
}