package com.example.driplinesoftapp.api

import com.example.driplinesoftapp.data.CambiarContrasenaRequest
import com.example.driplinesoftapp.data.ClienteResponse
import com.example.driplinesoftapp.data.DatosNegocioResponse
import com.example.driplinesoftapp.data.DatosPedidoRequest
import com.example.driplinesoftapp.data.LoginRequest
import com.example.driplinesoftapp.data.LoginResponse
import com.example.driplinesoftapp.data.MenuResponse
import com.example.driplinesoftapp.data.PassResponse
import com.example.driplinesoftapp.data.Pedido
import com.example.driplinesoftapp.data.PedidoRequest
import com.example.driplinesoftapp.data.PedidoResponse
import com.example.driplinesoftapp.data.PedidoResponse2
import com.example.driplinesoftapp.data.Producto
import com.example.driplinesoftapp.data.ProductoCarrito
import com.example.driplinesoftapp.data.ProductoRequest
import com.example.driplinesoftapp.data.ProductoResponse
import com.example.driplinesoftapp.data.ProductoResponse_2
import com.example.driplinesoftapp.data.RegisterRequest
import com.example.driplinesoftapp.data.RegisterResponse
import com.example.driplinesoftapp.data.SucursalResponse
import com.example.driplinesoftapp.data_negocio.CantidadPedidosResponse
import com.example.driplinesoftapp.data_negocio.EstadisticasClienteResponse
import com.example.driplinesoftapp.data_negocio.PedidoNegocioResponse
import com.example.driplinesoftapp.data_negocio.SucursalToggleResponse
import com.example.driplinesoftapp.data_negocio.UsuarioAsociadoResponse
import com.example.driplinesoftapp.models.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("pedidos/historial/{id_usuario}")
    fun obtenerHistorialPedidos(@Path("id_usuario") idUsuario: Int): Call<PedidoResponse2>

    @GET("negocio/{id_usuario}/historial-pedidos")
    fun obtenerHistorialPedidosNegocio(@Path("id_usuario") idUsuario: Int): Call<PedidoNegocioResponse>

    @POST("obtener-datos-pedido")
    fun obtenerDatosPedido(@Body requestBody: DatosPedidoRequest): Call<DatosNegocioResponse>


    @POST("cambiar-contrasena")
    fun cambiarContrasena(@Body request: CambiarContrasenaRequest): Call<PassResponse>

    @GET("clientes/{id_cliente}/usuarios")
    fun obtenerUsuariosPorCliente(
        @Path("id_cliente") idCliente: Int  // 🔹 Corrección: ahora usa @Path
    ): Call<UsuarioAsociadoResponse>  // Ahora devuelve un objeto que contiene 'success' y 'data'

    @POST("sucursales/{id_sucursal}/toggle")
    fun toggleSucursal(
        @Path("id_sucursal") idSucursal: Int
    ): Call<SucursalToggleResponse>

    @GET("estadisticas/cliente/{id_usuario}")
    fun obtenerEstadisticasCliente(
        @Path("id_usuario") idUsuario: Int
    ): Call<EstadisticasClienteResponse>

    @GET("estadisticas/pedidos/{id_usuario}")
    fun obtenerCantidadPedidosUsuario(
        @Path("id_usuario") idUsuario: Int
    ): Call<CantidadPedidosResponse>

    @PUT("pedido/cancelar/{id}")
    fun cancelarPedido(@Path("id") idPedido: Int): Call<Map<String, Any>>


}