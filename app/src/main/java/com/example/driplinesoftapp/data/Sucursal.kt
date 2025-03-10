package com.example.driplinesoftapp.data

import com.google.gson.annotations.SerializedName

data class Sucursal(
    @SerializedName("id_sucursal") val idSucursal: Int,
    @SerializedName("nombre_sucursal") val nombreSucursal: String,
    @SerializedName("direccion") val direccion: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("horario_atencion") val horarioAtencion: String?,
    @SerializedName("activa") val activa: Boolean
)