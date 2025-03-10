package com.example.driplinesoftapp.data

import com.google.gson.annotations.SerializedName

data class ProductoResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<Producto>
)