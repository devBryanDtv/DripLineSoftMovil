package com.example.driplinesoftapp.data_negocio

import com.example.driplinesoftapp.models.Usuario

data class UsuarioAsociadoResponse(
    val success: Boolean,
    val data: List<Usuario> // La lista de usuarios viene dentro de "data"
)
