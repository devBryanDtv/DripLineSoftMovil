package com.example.driplinesoftapp.data

import com.google.gson.annotations.SerializedName

data class PedidoResponse2(
    @SerializedName("exito") val exito: Boolean,
    @SerializedName("pedidos") val pedidos: List<Pedido>
)

data class Pedido(
    @SerializedName("id_pedido") val idPedido: Int,
    @SerializedName("nombre_comercial") val nombreComercial: String,
    @SerializedName("nombre_sucursal") val nombreSucursal: String,
    @SerializedName("fecha_pedido") val fechaPedido: String,
    @SerializedName("fecha_entregado") val fechaEntregado: String?,
    @SerializedName("metodo_pago") val metodoPago: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("total") val total: Double,
    @SerializedName("descuento") val descuento: Double?,
    @SerializedName("nota") val nota: String?,
    @SerializedName("tiempo_entrega_estimado") val tiempoEntregaEstimado: Int?,
    @SerializedName("detalles") val detalles: List<DetallePedido>
)
