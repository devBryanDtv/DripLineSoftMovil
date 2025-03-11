package com.example.driplinesoftapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.driplinesoftapp.databinding.ActivityMainBinding
import com.example.driplinesoftapp.models.CarritoDatabaseHelper
import com.example.driplinesoftapp.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var tvCartCount: TextView? = null // Referencia al contador en la ActionBar
    private lateinit var carritoDb: CarritoDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar base de datos
        carritoDb = CarritoDatabaseHelper(this)

        // Configurar navegación entre fragmentos
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_restaurante,
                R.id.navigation_pedido,
                R.id.navigation_perfil
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // Configurar el ícono del carrito
        val menuItem = menu?.findItem(R.id.action_cart)
        menuItem?.let {
            val actionView = it.actionView
            tvCartCount = actionView?.findViewById(R.id.tvCartCount)
            val ivCartIcon: ImageView? = actionView?.findViewById(R.id.ivCartIcon)

            // Redirigir a CarritoActivity al hacer clic
            actionView?.setOnClickListener {
                onOptionsItemSelected(menuItem)
            }

            ivCartIcon?.setOnClickListener {
                onOptionsItemSelected(menuItem)
            }
        }

        actualizarContadorCarrito()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                val intent = Intent(this, CarritoActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Método para actualizar la cantidad de productos en el carrito
     */
    private fun actualizarContadorCarrito() {
        if (tvCartCount == null) return

        // Obtener el ID del usuario autenticado desde SessionManager
        val sessionManager = SessionManager(this)
        val usuario = sessionManager.getUser()
        val idUsuario = usuario?.idUsuario ?: return

        // Obtener la cantidad total de productos del carrito del usuario autenticado
        val cantidadProductos = carritoDb.obtenerCantidadTotalProductos(idUsuario)

        if (cantidadProductos == 0) {
            tvCartCount?.visibility = View.GONE
        } else {
            tvCartCount?.visibility = View.VISIBLE
            tvCartCount?.text = cantidadProductos.toString()
        }
    }


    // Agregamos este método para que se actualice el contador cada vez que regresemos del Carrito
    override fun onResume() {
        super.onResume()
        actualizarContadorCarrito()
    }
}
