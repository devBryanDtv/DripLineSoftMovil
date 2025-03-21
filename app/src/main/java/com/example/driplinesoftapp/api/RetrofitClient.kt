package com.example.driplinesoftapp.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.90.145:8000/api/mobile/"
    const val BASE_URL_IMAGENES = "http://192.168.90.145:8000/storage/"

    val gson = GsonBuilder().setLenient().create()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
