package com.example.driplinesoftapp.models

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("contraseña") val contraseña: String,
    @SerializedName("rol") val rol: String,
    @SerializedName("fecha_creacion") val fechaCreacion: String
)
