package com.example.driplinesoftapp.data_negocio

import com.google.gson.annotations.SerializedName

data class PedidoNegocioResponse(
    @SerializedName("exito") val exito: Boolean,
    @SerializedName("pedidos") val pedidos: List<PedidoNegocio>
)

data class PedidoNegocio(
    @SerializedName("id_pedido") val idPedido: Int,
    @SerializedName("nombre_comercial") val nombreComercial: String,
    @SerializedName("nombre_sucursal") val nombreSucursal: String,
    @SerializedName("fecha_pedido") val fechaPedido: String,
    @SerializedName("metodo_pago") val metodoPago: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("total") val total: Float,
    @SerializedName("descuento") val descuento: Float?,
    @SerializedName("nota") val nota: String?,
    @SerializedName("tiempo_entrega_estimado") val tiempoEntregaEstimado: Int?,
    @SerializedName("detalles") val detalles: List<DetallePedidoNegocio>
)

data class DetallePedidoNegocio(
    @SerializedName("id_detalle") val idDetalle: Int,
    @SerializedName("id_producto") val idProducto: Int,
    @SerializedName("nombre_producto") val nombreProducto: String,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("subtotal") val subtotal: Float
)
