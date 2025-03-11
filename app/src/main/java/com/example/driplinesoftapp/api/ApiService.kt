package com.example.driplinesoftapp.api

import com.example.driplinesoftapp.data.ClienteResponse
import com.example.driplinesoftapp.data.LoginRequest
import com.example.driplinesoftapp.data.LoginResponse
import com.example.driplinesoftapp.data.MenuResponse
import com.example.driplinesoftapp.data.PedidoRequest
import com.example.driplinesoftapp.data.PedidoResponse
import com.example.driplinesoftapp.data.Producto
import com.example.driplinesoftapp.data.ProductoCarrito
import com.example.driplinesoftapp.data.ProductoRequest
import com.example.driplinesoftapp.data.ProductoResponse
import com.example.driplinesoftapp.data.ProductoResponse_2
import com.example.driplinesoftapp.data.RegisterRequest
import com.example.driplinesoftapp.data.RegisterResponse
import com.example.driplinesoftapp.data.SucursalResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("clientes-activos")
    fun obtenerClientesActivos(): Call<ClienteResponse>

    @GET("clientes/{id}/sucursales")
    fun obtenerSucursalesPorCliente(
        @Path("id") idCliente: Int
    ): Call<SucursalResponse>

    @GET("sucursales/{id}/menus")
    fun obtenerMenusPorSucursal(
        @Path("id") idSucursal: Int
    ): Call<MenuResponse>

    @GET("menus/{id}/productos")
    fun obtenerProductosPorMenu(
        @Path("id") idMenu: Int
    ): Call<ProductoResponse>

    @POST("productos/carrito/detalles")
    fun obtenerDetallesProductosCarrito(@Body productos: ProductoRequest): Call<ProductoResponse_2>

    @POST("crear-pedido")
    fun crearPedido(@Body pedidoRequest: PedidoRequest): Call<PedidoResponse>

    @POST("registro") // Endpoint en Laravel
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

}