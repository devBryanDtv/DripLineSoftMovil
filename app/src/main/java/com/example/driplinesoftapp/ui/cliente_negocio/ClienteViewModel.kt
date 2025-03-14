package com.example.driplinesoftapp.ui.cliente_negocio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.driplinesoftapp.api.RetrofitClient
import com.example.driplinesoftapp.data_negocio.UsuarioAsociadoResponse
import com.example.driplinesoftapp.models.Usuario
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClienteViewModel : ViewModel() {

    private val _usuarios = MutableLiveData<List<Usuario>>()
    val usuarios: LiveData<List<Usuario>> = _usuarios

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun obtenerUsuariosPorCliente(idCliente: Int) {
        _isLoading.postValue(true) // Mostrar ProgressBar

        RetrofitClient.instance.obtenerUsuariosPorCliente(idCliente)
            .enqueue(object : Callback<UsuarioAsociadoResponse> {
                override fun onResponse(call: Call<UsuarioAsociadoResponse>, response: Response<UsuarioAsociadoResponse>) {
                    _isLoading.postValue(false) // Ocultar ProgressBar

                    if (response.isSuccessful && response.body()?.success == true) {
                        _usuarios.postValue(response.body()?.data ?: emptyList())
                    } else {
                        _usuarios.postValue(emptyList())
                        _errorMessage.postValue("No se encontraron usuarios asociados.")
                    }
                }

                override fun onFailure(call: Call<UsuarioAsociadoResponse>, t: Throwable) {
                    _isLoading.postValue(false) // Ocultar ProgressBar en caso de error
                    _usuarios.postValue(emptyList())
                    _errorMessage.postValue("Error de conexión: ${t.message}")
                }
            })
    }
}
