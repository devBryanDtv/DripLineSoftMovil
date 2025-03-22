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
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var tvCartCount: TextView? = null
    private lateinit var carritoDb: CarritoDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carritoDb = CarritoDatabaseHelper(this)

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

        verificarMensajeExito()  // ✅ Mostrar mensaje de éxito si se recibe
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val menuItem = menu?.findItem(R.id.action_cart)
        menuItem?.let {
            val actionView = it.actionView
            tvCartCount = actionView?.findViewById(R.id.tvCartCount)
            val ivCartIcon: ImageView? = actionView?.findViewById(R.id.ivCartIcon)

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
                val sessionManager = SessionManager(this)
                val usuario = sessionManager.getUser()
                val idUsuario = usuario?.idUsuario ?: return true  // Si no hay usuario, se detiene la acción

                val cantidadProductos = carritoDb.obtenerCantidadTotalProductos(idUsuario)

                if (cantidadProductos == 0) {
                    // 🚨 Carrito vacío, mostrar Snackbar
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Tu carrito está vacío.",
                        Snackbar.LENGTH_LONG
                    )
                        .setBackgroundTint(getColor(android.R.color.holo_orange_light))
                        .setTextColor(getColor(android.R.color.black))
                        .setAction("OK") { }  // Acción vacía para cerrar el Snackbar
                        .show()
                } else {
                    // ✅ Carrito con productos, permitir navegación
                    val intent = Intent(this, CarritoActivity::class.java)
                    startActivity(intent)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun actualizarContadorCarrito() {
        if (tvCartCount == null) return

        val sessionManager = SessionManager(this)
        val usuario = sessionManager.getUser()
        val idUsuario = usuario?.idUsuario ?: return

        val cantidadProductos = carritoDb.obtenerCantidadTotalProductos(idUsuario)

        if (cantidadProductos == 0) {
            tvCartCount?.visibility = View.GONE
        } else {
            tvCartCount?.visibility = View.VISIBLE
            tvCartCount?.text = cantidadProductos.toString()
        }
    }

    // ✅ Mostrar mensaje de éxito y redirigir al fragmento de pedidos
    private fun verificarMensajeExito() {
        val mensajeExito = intent.getStringExtra("mensaje_exito")

        if (!mensajeExito.isNullOrEmpty()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                mensajeExito,
                Snackbar.LENGTH_INDEFINITE
            )
                .setTextColor(getColor(android.R.color.white))
                .setAction("Ver Pedido") {
                    val navView: BottomNavigationView = binding.navView
                    navView.selectedItemId = R.id.navigation_pedido  // 🔹 Cambiar a la pestaña de "Pedidos"
                }

                .show()

            // Limpiar el mensaje recibido después de mostrarlo
            intent.removeExtra("mensaje_exito")
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarContadorCarrito()
    }
}
