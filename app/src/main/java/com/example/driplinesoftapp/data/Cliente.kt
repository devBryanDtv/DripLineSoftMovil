package com.example.driplinesoftapp.data

import com.google.gson.annotations.SerializedName

data class Cliente(
    @SerializedName("id_cliente") val idCliente: Int,
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("nombre_comercial") val nombreComercial: String,
    @SerializedName("logo") val logo: String,
    @SerializedName("sector") val sector: String,
    @SerializedName("estado_suscripcion") val estadoSuscripcion: String
)