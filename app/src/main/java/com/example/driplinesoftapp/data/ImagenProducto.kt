package com.example.driplinesoftapp.data

import com.google.gson.annotations.SerializedName

data class ImagenProducto(
    @SerializedName("id_imagen") val idImagen: Int =0,
    @SerializedName("ruta_imagen") val rutaImagen: String
)