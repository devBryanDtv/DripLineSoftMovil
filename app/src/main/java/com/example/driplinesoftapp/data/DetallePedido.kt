package com.example.driplinesoftapp.data

import com.google.gson.annotations.SerializedName

data class DetallePedido(
    @SerializedName("id_detalle") val idDetalle: Int,
    @SerializedName("id_producto") val idProducto: Int,
    @SerializedName("nombre_producto") val nombreProducto: String,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("subtotal") val subtotal: Double
)
