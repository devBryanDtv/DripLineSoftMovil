package com.example.driplinesoftapp.models

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("contraseña") val contraseña: String,
    @SerializedName("rol") val rol: String,
    @SerializedName("fecha_creacion") val fechaCreacion: String,
    @SerializedName("pivot") val pivot: Pivot? // Agregamos el objeto Pivot
)

// Definimos el modelo del objeto "pivot"
data class Pivot(
    @SerializedName("id_cliente") val idCliente: Int,
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
