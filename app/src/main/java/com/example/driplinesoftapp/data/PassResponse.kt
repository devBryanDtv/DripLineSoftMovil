package com.example.driplinesoftapp.data

data class PassResponse(
    val success: Boolean,
    val message: String,
    val errors: Map<String, List<String>>? = null // Manejo de errores detallados si los hay
)
