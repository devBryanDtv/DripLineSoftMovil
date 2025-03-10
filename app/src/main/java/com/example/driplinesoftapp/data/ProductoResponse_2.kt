package com.example.driplinesoftapp.data

data class ProductoResponse_2(
    val success: Boolean,
    val message: String?,
    val data: List<Producto> // El array de productos está dentro de 'data'
)
