package com.example.driplinesoftapp.ui.pedido

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.Pedido
import com.example.driplinesoftapp.data.PedidoResponse2
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PedidoViewModel : ViewModel() {

    private val _pedidos = MutableLiveData<List<Pedido>>()
    val pedidos: LiveData<List<Pedido>> = _pedidos

    fun cargarHistorialPedidos(idUsuario: Int) {
        Log.d("PedidoViewModel", "Cargando historial de pedidos para usuario: $idUsuario")

        RetrofitClient.instance.obtenerHistorialPedidos(idUsuario)
            .enqueue(object : Callback<PedidoResponse2> {
                override fun onResponse(call: Call<PedidoResponse2>, response: Response<PedidoResponse2>) {
                    if (response.isSuccessful && response.body()?.exito == true) {
                        _pedidos.value = response.body()?.pedidos ?: emptyList()
                        Log.d("PedidoViewModel", "Pedidos recibidos: ${_pedidos.value?.size}")
                    } else {
                        _pedidos.value = emptyList()
                        Log.e("PedidoViewModel", "Error en la API: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<PedidoResponse2>, t: Throwable) {
                    _pedidos.value = emptyList()
                    Log.e("PedidoViewModel", "Fallo en la conexión: ${t.message}")
                }
            })
    }
}
