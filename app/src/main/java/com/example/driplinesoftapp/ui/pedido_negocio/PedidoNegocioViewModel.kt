package com.example.driplinesoftapp.ui.pedido_negocio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data_negocio.PedidoNegocio
import com.example.driplinesoftapp.data_negocio.PedidoNegocioResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PedidoNegocioViewModel : ViewModel() {

    private val _pedidos = MutableLiveData<List<PedidoNegocio>>()
    val pedidos: LiveData<List<PedidoNegocio>> = _pedidos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun obtenerHistorialPedidos(idUsuario: Int) {
        _isLoading.postValue(true)

        RetrofitClient.instance.obtenerHistorialPedidosNegocio(idUsuario)
            .enqueue(object : Callback<PedidoNegocioResponse> {
                override fun onResponse(
                    call: Call<PedidoNegocioResponse>,
                    response: Response<PedidoNegocioResponse>
                ) {
                    _isLoading.postValue(false)
                    if (response.isSuccessful && response.body()?.exito == true) {
                        _pedidos.postValue(response.body()?.pedidos ?: emptyList())
                    } else {
                        _errorMessage.postValue("No se encontraron pedidos.")
                    }
                }

                override fun onFailure(call: Call<PedidoNegocioResponse>, t: Throwable) {
                    _isLoading.postValue(false)
                    _errorMessage.postValue("Error al obtener los pedidos: ${t.message}")
                }
            })
    }
}
