package com.example.driplinesoftapp.ui.restaurante

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.Cliente
import com.example.driplinesoftapp.data.ClienteResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestauranteViewModel : ViewModel() {

    private val _clientes = MutableLiveData<List<Cliente>>()
    val clientes: LiveData<List<Cliente>> = _clientes

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun cargarClientes() {
        _isLoading.postValue(true)  // Mostrar el ProgressBar

        RetrofitClient.instance.obtenerClientesActivos().enqueue(object : Callback<ClienteResponse> {
            override fun onResponse(call: Call<ClienteResponse>, response: Response<ClienteResponse>) {
                _isLoading.postValue(false)  // Ocultar el ProgressBar
                if (response.isSuccessful && response.body()?.success == true) {
                    _clientes.postValue(response.body()?.data ?: emptyList())
                } else {
                    _errorMessage.postValue("No se encontraron clientes activos")
                }
            }

            override fun onFailure(call: Call<ClienteResponse>, t: Throwable) {
                _isLoading.postValue(false)  // Ocultar el ProgressBar en caso de error
                _errorMessage.postValue("Error de conexión: ${t.message}")
            }
        })
    }
}
