package com.example.driplinesoftapp.data

import com.google.gson.annotations.SerializedName

data class SucursalResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Sucursal>
)