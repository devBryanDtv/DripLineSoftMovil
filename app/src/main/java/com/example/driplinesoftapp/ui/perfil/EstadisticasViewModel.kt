package com.example.driplinesoftapp.ui.perfil

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data_negocio.CantidadPedidosResponse
import com.example.driplinesoftapp.data_negocio.EstadisticasClienteResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EstadisticasViewModel : ViewModel() {

    // LiveData para la cantidad de pedidos por usuario
    private val _cantidadPedidos = MutableLiveData<Int>()
    val cantidadPedidos: LiveData<Int> = _cantidadPedidos

    // LiveData para estadísticas del negocio
    private val _cantidadSucursales = MutableLiveData<Int>()
    val cantidadSucursales: LiveData<Int> = _cantidadSucursales

    private val _cantidadMenus = MutableLiveData<Int>()
    val cantidadMenus: LiveData<Int> = _cantidadMenus

    private val _cantidadProductos = MutableLiveData<Int>()
    val cantidadProductos: LiveData<Int> = _cantidadProductos

    // Manejo de errores
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Obtener cantidad de pedidos por usuario
     */
    fun obtenerCantidadPedidosUsuario(idUsuario: Int) {
        RetrofitClient.instance.obtenerCantidadPedidosUsuario(idUsuario)
            .enqueue(object : Callback<CantidadPedidosResponse> {
                override fun onResponse(
                    call: Call<CantidadPedidosResponse>,
                    response: Response<CantidadPedidosResponse>
                ) {
                    if (response.isSuccessful && response.body()?.exito == true) {
                        _cantidadPedidos.value = response.body()?.cantidadPedidos ?: 0
                    } else {
                        _error.value = "Error al obtener cantidad de pedidos"
                        Log.e("EstadisticasViewModel", "❌ Error en la respuesta de pedidos: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<CantidadPedidosResponse>, t: Throwable) {
                    _error.value = "Error de conexión"
                    Log.e("EstadisticasViewModel", "❌ Error de conexión al obtener pedidos: ${t.message}")
                }
            })
    }

    /**
     * Obtener estadísticas del negocio
     */
    fun obtenerEstadisticasCliente(idUsuario: Int) {
        RetrofitClient.instance.obtenerEstadisticasCliente(idUsuario)
            .enqueue(object : Callback<EstadisticasClienteResponse> {
                override fun onResponse(
                    call: Call<EstadisticasClienteResponse>,
                    response: Response<EstadisticasClienteResponse>
                ) {
                    if (response.isSuccessful && response.body()?.exito == true) {
                        _cantidadSucursales.value = response.body()?.cantidadSucursales ?: 0
                        _cantidadMenus.value = response.body()?.cantidadMenus ?: 0
                        _cantidadProductos.value = response.body()?.cantidadProductos ?: 0
                    } else {
                        _error.value = "Error al obtener estadísticas del negocio"
                        Log.e("EstadisticasViewModel", "❌ Error en la respuesta de estadísticas: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<EstadisticasClienteResponse>, t: Throwable) {
                    _error.value = "Error de conexión"
                    Log.e("EstadisticasViewModel", "❌ Error de conexión al obtener estadísticas: ${t.message}")
                }
            })
    }
}
