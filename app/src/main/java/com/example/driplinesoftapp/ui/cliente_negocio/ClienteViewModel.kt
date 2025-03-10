package com.example.driplinesoftapp.ui.cliente_negocio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ClienteViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Pedido Fragment"
    }
    val text: LiveData<String> = _text
}