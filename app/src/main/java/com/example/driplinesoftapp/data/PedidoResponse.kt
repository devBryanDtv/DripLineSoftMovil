package com.example.driplinesoftapp.data

data class PedidoResponse(
    val success: Boolean,
    val message: String,
    val data: PedidoData?
)