package com.example.driplinesoftapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.driplinesoftapp.models.Usuario
import com.google.gson.Gson

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        private const val KEY_ID = "id_usuario"
        private const val KEY_NOMBRE = "nombre"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROL = "rol"
        private const val KEY_FECHA_CREACION = "fecha_creacion"
        private const val IS_LOGGED_IN = "is_logged_in"
    }

    // Guardar datos del usuario en SharedPreferences
    fun saveUser(user: Usuario) {
        editor.putInt(KEY_ID, user.idUsuario)
        editor.putString(KEY_NOMBRE, user.nombre)
        editor.putString(KEY_EMAIL, user.email)
        editor.putString(KEY_ROL, user.rol)
        editor.putString(KEY_FECHA_CREACION, user.fechaCreacion)
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.apply()
    }

    // Obtener los datos del usuario
    fun getUser(): Usuario? {
        if (!isLoggedIn()) return null
        return Usuario(
            idUsuario = prefs.getInt(KEY_ID, -1),
            nombre = prefs.getString(KEY_NOMBRE, "") ?: "",
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            contraseña = "", // No guardamos la contraseña por seguridad
            rol = prefs.getString(KEY_ROL, "") ?: "",
            fechaCreacion = prefs.getString(KEY_FECHA_CREACION, "") ?: "",
            pivot = null
        )
    }

    // Verificar si el usuario está logueado
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    // Cerrar sesión
    fun logout() {
        editor.clear().apply()
    }
}
