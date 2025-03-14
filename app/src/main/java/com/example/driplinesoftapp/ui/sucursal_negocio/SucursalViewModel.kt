package com.example.driplinesoftapp.ui.sucursal_negocio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data.Sucursal
import com.example.driplinesoftapp.data.SucursalResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SucursalViewModel : ViewModel() {

    private val _sucursales = MutableLiveData<List<Sucursal>>()
    val sucursales: LiveData<List<Sucursal>> = _sucursales

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun cargarSucursales(idCliente: Int) {
        _isLoading.postValue(true)  // Mostrar el ProgressBar

        RetrofitClient.instance.obtenerSucursalesPorCliente(idCliente).enqueue(object : Callback<SucursalResponse> {
            override fun onResponse(call: Call<SucursalResponse>, response: Response<SucursalResponse>) {
                _isLoading.postValue(false)  // Ocultar el ProgressBar
                if (response.isSuccessful && response.body()?.success == true) {
                    _sucursales.postValue(response.body()?.data ?: emptyList())
                } else {
                    _errorMessage.postValue("No se encontraron sucursales")
                }
            }

            override fun onFailure(call: Call<SucursalResponse>, t: Throwable) {
                _isLoading.postValue(false)  // Ocultar el ProgressBar en caso de error
                _errorMessage.postValue("Error de conexión: ${t.message}")
            }
        })
    }
}
