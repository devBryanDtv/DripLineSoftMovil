package com.example.driplinesoftapp.data

import com.example.driplinesoftapp.models.Usuario

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: Usuario?
)