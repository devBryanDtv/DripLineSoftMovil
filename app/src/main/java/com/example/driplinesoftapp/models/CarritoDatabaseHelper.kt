package com.example.driplinesoftapp.models

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.driplinesoftapp.data.ProductoCarrito

class CarritoDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "carrito.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_CARRITO = "carrito"

        // Columnas
        const val COLUMN_ID = "id"
        const val COLUMN_ID_USUARIO = "id_usuario"
        const val COLUMN_ID_PRODUCTO = "id_producto"
        const val COLUMN_CANTIDAD = "cantidad"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_CARRITO (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ID_USUARIO INTEGER NOT NULL,
                $COLUMN_ID_PRODUCTO INTEGER NOT NULL,
                $COLUMN_CANTIDAD INTEGER NOT NULL,
                UNIQUE ($COLUMN_ID_USUARIO, $COLUMN_ID_PRODUCTO) ON CONFLICT REPLACE
            )
        """.trimIndent()

        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CARRITO")
        onCreate(db)
    }

    // ➤ Insertar o actualizar el producto en el carrito
    fun agregarProducto(idUsuario: Int, idProducto: Int, cantidad: Int) {
        val db = writableDatabase

        // Verificar si el producto ya existe en el carrito
        val cursor = db.query(
            TABLE_CARRITO,
            null,
            "$COLUMN_ID_USUARIO = ? AND $COLUMN_ID_PRODUCTO = ?",
            arrayOf(idUsuario.toString(), idProducto.toString()),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            // Si el producto ya existe, actualizamos la cantidad
            val nuevaCantidad = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CANTIDAD)) + cantidad
            actualizarCantidad(idUsuario, ProductoCarrito(idProducto, nuevaCantidad))
        } else {
            // Si el producto no existe, lo insertamos
            val values = ContentValues().apply {
                put(COLUMN_ID_USUARIO, idUsuario)
                put(COLUMN_ID_PRODUCTO, idProducto)
                put(COLUMN_CANTIDAD, cantidad)
            }
            db.insert(TABLE_CARRITO, null, values)
        }
        cursor.close()
        db.close()
    }

    // ➤ Obtener todos los productos del carrito de un usuario
    fun obtenerProductosPorUsuario(idUsuario: Int): List<ProductoCarrito> {
        val productos = mutableListOf<ProductoCarrito>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_CARRITO,
            null,
            "$COLUMN_ID_USUARIO = ?",
            arrayOf(idUsuario.toString()),
            null,
            null,
            null
        )

        while (cursor.moveToNext()) {
            productos.add(
                ProductoCarrito(
                    idProducto = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID_PRODUCTO)),
                    cantidad = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CANTIDAD))
                )
            )
        }

        cursor.close()
        db.close()
        return productos
    }

    // ➤ Actualizar la cantidad de un producto en el carrito
    fun actualizarCantidad(idUsuario: Int, productoCarrito: ProductoCarrito) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CANTIDAD, productoCarrito.cantidad)
        }
        db.update(TABLE_CARRITO, values, "$COLUMN_ID_USUARIO = ? AND $COLUMN_ID_PRODUCTO = ?", arrayOf(idUsuario.toString(), productoCarrito.idProducto.toString()))
        db.close()
    }

    // ➤ Eliminar producto del carrito
    fun eliminarProducto(idUsuario: Int, idProducto: Int) {
        val db = writableDatabase

        db.delete(
            TABLE_CARRITO,
            "$COLUMN_ID_USUARIO = ? AND $COLUMN_ID_PRODUCTO = ?",
            arrayOf(idUsuario.toString(), idProducto.toString())
        )

        db.close()
    }

    // ➤ Vaciar el carrito completo de un usuario
    fun vaciarCarrito(idUsuario: Int) {
        val db = writableDatabase
        db.delete(TABLE_CARRITO, "$COLUMN_ID_USUARIO = ?", arrayOf(idUsuario.toString()))
        db.close()
    }

    // Método para obtener un producto por ID de usuario y producto
    fun obtenerProductoPorId(idUsuario: Int, idProducto: Int): ProductoCarrito? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CARRITO,
            null,
            "$COLUMN_ID_USUARIO = ? AND $COLUMN_ID_PRODUCTO = ?",
            arrayOf(idUsuario.toString(), idProducto.toString()),
            null,
            null,
            null
        )

        var producto: ProductoCarrito? = null
        if (cursor.moveToFirst()) {
            producto = ProductoCarrito(
                idProducto = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID_PRODUCTO)),
                cantidad = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CANTIDAD))
            )
        }

        cursor.close()
        db.close()
        return producto
    }
}
