package com.example.driplinesoftapp.data

import com.google.gson.annotations.SerializedName


data class ClienteResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<Cliente>
)