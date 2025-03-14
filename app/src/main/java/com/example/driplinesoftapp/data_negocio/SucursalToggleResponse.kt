package com.example.driplinesoftapp.data_negocio

import com.google.gson.annotations.SerializedName

data class SucursalToggleResponse(
    @SerializedName("exito") val exito: Boolean,
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("estado") val estado: Boolean? // Puede ser nulo si hay un error
)
