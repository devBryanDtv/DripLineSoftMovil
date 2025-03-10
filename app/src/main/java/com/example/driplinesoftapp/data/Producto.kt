package com.example.driplinesoftapp.data

import com.google.gson.annotations.SerializedName

data class Producto(
    @SerializedName("id_producto") val idProducto: Int,
    @SerializedName("id_menu") val idMenu: Int,
    @SerializedName("nombre_producto") val nombreProducto: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("precio") val precio: Double,
    @SerializedName("disponible") val disponible: Boolean,
    @SerializedName("imagenes_productos") val imagenes: List<ImagenProducto>?,

    var cantidad: Int = 1
)