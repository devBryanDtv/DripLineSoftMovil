package com.example.driplinesoftapp.data

import com.google.gson.annotations.SerializedName

data class Menu(
    @SerializedName("id_menu") val idMenu: Int,
    @SerializedName("id_sucursal") val idSucursal: Int,
    @SerializedName("nombre_menu") val nombreMenu: String,
    @SerializedName("categoria") val categoria: String
)