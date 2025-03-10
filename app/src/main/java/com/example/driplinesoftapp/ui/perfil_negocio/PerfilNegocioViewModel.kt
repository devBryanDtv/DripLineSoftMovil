package com.example.driplinesoftapp.ui.perfil_negocio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PerfilNegocioViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Pedido Fragment"
    }
    val text: LiveData<String> = _text
}