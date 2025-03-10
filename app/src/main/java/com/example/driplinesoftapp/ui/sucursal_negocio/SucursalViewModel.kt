package com.example.driplinesoftapp.ui.sucursal_negocio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SucursalViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is sucursal negocio Fragment"
    }
    val text: LiveData<String> = _text
}