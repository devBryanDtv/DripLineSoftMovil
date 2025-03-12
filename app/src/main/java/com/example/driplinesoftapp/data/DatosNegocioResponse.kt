package com.example.driplinesoftapp.data

data class DatosNegocioResponse(
    val success: Boolean,
    val datos_negocio: DatosNegocio
)


data class DatosNegocio(
    val nombre_menu: String?,
    val nombre_sucursal: String?,
    val nombre_comercial: String?,
    val logo_cliente: String?
)