package com.example.driplinesoftapp.data

import com.google.gson.annotations.SerializedName

data class PedidoRequest(
    @SerializedName("id_usuario_cliente") val idUsuarioCliente: Int,
    @SerializedName("metodo_pago") val metodoPago: String,
    @SerializedName("productos") val productos: List<ProductoCarrito>,
    @SerializedName("nota") val nota: String?,
    @SerializedName("descuento") val descuento: Double?
)
