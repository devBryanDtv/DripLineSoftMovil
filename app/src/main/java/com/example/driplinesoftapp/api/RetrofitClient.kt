package com.example.driplinesoftapp.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://58de-200-56-95-130.ngrok-free.app/api/mobile/"
    const val BASE_URL_IMAGENES = "https://58de-200-56-95-130.ngrok-free.app/storage/"

    val gson = GsonBuilder().setLenient().create()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
