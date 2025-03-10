package com.example.driplinesoftapp.data

import com.google.gson.annotations.SerializedName

data class ProductoCarrito(
    @SerializedName("id_producto") val idProducto: Int,
    @SerializedName("cantidad") val cantidad: Int
)
