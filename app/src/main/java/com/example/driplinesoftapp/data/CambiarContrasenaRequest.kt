package com.example.driplinesoftapp.data

data class CambiarContrasenaRequest(
    val id_usuario: Int,
    val contrasena_actual: String,
    val nueva_contrasena: String
)