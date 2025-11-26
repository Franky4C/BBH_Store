package mx.tecnm.cdhidalgo.bbhstore.dataclass

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

object CarritoManager {

    data class ItemCarrito(
        val producto: Producto,
        var cantidad: Int
    )

    private val db = Firebase.firestore

    // Usuario asociado al carrito en esta sesión
    private var usuarioId: String? = null

    // Carrito en memoria
    private val items = mutableListOf<ItemCarrito>()

    // =============== CONFIGURACIÓN DE SESIÓN ===================

    /**
     * Configura el carrito para un usuario (por su correo)
     * y carga lo que tenga en Firestore:
     *
     *  Ruta: bbh_carritos/{usuarioId}/items
     */
    fun configurarUsuario(correo: String?, onFinished: (Boolean, String?) -> Unit) {
        usuarioId = correo
        items.clear()

        if (correo.isNullOrEmpty()) {
            onFinished(false, "Correo de usuario vacío")
            return
        }

        val colRef = db.collection("bbh_carritos")
            .document(correo)
            .collection("items")

        colRef.get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    val idProducto = doc.getString("idProducto")
                    val nombreCorto = doc.getString("nombreCorto")
                    val nombre = doc.getString("nombre")
                    val categoria = doc.getString("categoria")
                    val precio = doc.getDouble("precio") ?: 0.0
                    val imagenUrl = doc.getString("imagenUrl")
                    val stock = (doc.getLong("stock") ?: 0L).toInt()
                    val cantidad = (doc.getLong("cantidad") ?: 1L).toInt()

                    val producto = Producto(
                        idDocumento = idProducto,
                        imagen = 0,               // en carrito mostrarás la imagenUrl con Glide
                        nombreCorto = nombreCorto,
                        nombre = nombre,
                        precio = precio,
                        descripcion = null,
                        categoria = categoria,
                        stock = stock,
                        imagenUrl = imagenUrl
                    )

                    items.add(ItemCarrito(producto, cantidad))
                }

                onFinished(true, null)
            }
            .addOnFailureListener { e ->
                onFinished(false, e.message)
            }
    }

    /**
     * Limpia solo la sesión local (se usa al cerrar sesión).
     * NO borra el carrito de Firestore, se conserva.
     */
    fun limpiarSesion() {
        usuarioId = null
        items.clear()
    }

    // =============== GETTERS BÁSICOS ===================

    fun obtenerItems(): List<ItemCarrito> = items

    fun obtenerCantidadTotal(): Int = items.sumOf { it.cantidad }

    fun obtenerImporteTotal(): Double =
        items.sumOf { it.cantidad * it.producto.precio }

    // =============== SINCRONIZACIÓN CON FIRESTORE ===================

    /**
     * Guarda el carrito completo en Firestore sobrescribiendo
     * la subcolección bbh_carritos/{usuarioId}/items.
     */
    private fun guardarCarritoEnFirestore(onFinished: (Boolean, String?) -> Unit) {
        val uid = usuarioId
        if (uid.isNullOrEmpty()) {
            onFinished(false, "Sin usuario asociado al carrito")
            return
        }

        val colRef = db.collection("bbh_carritos")
            .document(uid)
            .collection("items")

        // Estrategia simple: borrar todo e insertar estado actual
        colRef.get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()

                // Borrar lo que hubiera antes
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }

                // Insertar los items actuales
                for (item in items) {
                    val newDoc = colRef.document()
                    val data = hashMapOf(
                        "idProducto"  to item.producto.idDocumento,
                        "nombreCorto" to item.producto.nombreCorto,
                        "nombre"      to item.producto.nombre,
                        "categoria"   to item.producto.categoria,
                        "precio"      to item.producto.precio,
                        "stock"       to item.producto.stock,
                        "imagenUrl"   to item.producto.imagenUrl,
                        "cantidad"    to item.cantidad
                    )
                    batch.set(newDoc, data)
                }

                batch.commit()
                    .addOnSuccessListener { onFinished(true, null) }
                    .addOnFailureListener { e -> onFinished(false, e.message) }
            }
            .addOnFailureListener { e ->
                onFinished(false, e.message)
            }
    }

    // Versión “fire and forget” para usar dentro de agregar/actualizar/vaciar
    private fun sync() {
        guardarCarritoEnFirestore { _, _ -> /* ignoramos el resultado aquí */ }
    }

    // =============== OPERACIONES DEL CARRITO ===================

    fun vaciarCarrito() {
        items.clear()
        sync()
    }

    /**
     * Elimina del carrito solo los items que vengan en [itemsAEliminar].
     * Se usa el nombre del producto para identificarlos (como ya lo hacías).
     */
    fun eliminarItems(itemsAEliminar: List<ItemCarrito>) {
        items.removeAll { item ->
            itemsAEliminar.any { sel ->
                sel.producto.nombre == item.producto.nombre
            }
        }
        sync()
    }

    /**
     * Agrega [cantidad] unidades de un producto al carrito.
     * Respeta el stock y no permite cantidades negativas.
     * Devuelve true si se pudo agregar, false si no había stock suficiente.
     */
    fun agregarProducto(producto: Producto, cantidad: Int = 1): Boolean {
        if (cantidad <= 0) return false

        val existente = items.find { it.producto.nombre == producto.nombre }

        val cantidadActual = existente?.cantidad ?: 0
        val nuevaCantidad = cantidadActual + cantidad

        if (nuevaCantidad > producto.stock) {
            return false
        }

        if (existente == null) {
            items.add(ItemCarrito(producto, cantidad))
        } else {
            existente.cantidad = nuevaCantidad
        }

        sync()
        return true
    }

    /**
     * Cambia la cantidad de un producto en el carrito.
     * - Si nuevaCantidad <= 0 → elimina el producto.
     * - Si nuevaCantidad > stock → no hace nada y devuelve false.
     */
    fun actualizarCantidad(producto: Producto, nuevaCantidad: Int): Boolean {
        val existente = items.find { it.producto.nombre == producto.nombre }
            ?: return false

        if (nuevaCantidad <= 0) {
            items.remove(existente)
            sync()
            return true
        }

        if (nuevaCantidad > producto.stock) {
            return false
        }

        existente.cantidad = nuevaCantidad
        sync()
        return true
    }
}
