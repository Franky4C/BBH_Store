package mx.tecnm.cdhidalgo.bbhstore.dataclass

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

object FavoritosManager {

    private val db = Firebase.firestore

    // Usuario actual (correo) asociado a esta sesión de favoritos
    private var usuarioId: String? = null

    // Lista de productos favoritos en memoria
    private val favoritos = mutableListOf<Producto>()

    // ================== CONFIGURACIÓN DE SESIÓN ==================

    /**
     * Cargar favoritos del usuario desde Firestore:
     *   bbh_favoritos/{correo}/items
     */
    fun configurarUsuario(correo: String?, onFinished: (Boolean, String?) -> Unit) {
        usuarioId = correo
        favoritos.clear()

        if (correo.isNullOrEmpty()) {
            onFinished(false, "Correo de usuario vacío")
            return
        }

        val colRef = db.collection("bbh_favoritos")
            .document(correo)
            .collection("items")

        colRef.get()
            .addOnSuccessListener { snapshot ->
                favoritos.clear()
                for (doc in snapshot.documents) {
                    val idProducto  = doc.getString("idProducto") ?: doc.id
                    val nombreCorto = doc.getString("nombreCorto")
                    val nombre      = doc.getString("nombre")
                    val categoria   = doc.getString("categoria")
                    val precio      = doc.getDouble("precio") ?: 0.0
                    val imagenUrl   = doc.getString("imagenUrl")
                    val stock       = (doc.getLong("stock") ?: 0L).toInt()

                    val producto = Producto(
                        idDocumento = idProducto,
                        imagen      = 0,           // usaremos imagenUrl con Glide
                        nombreCorto = nombreCorto,
                        nombre      = nombre,
                        precio      = precio,
                        descripcion = null,
                        categoria   = categoria,
                        stock       = stock,
                        imagenUrl   = imagenUrl
                    )
                    favoritos.add(producto)
                }
                onFinished(true, null)
            }
            .addOnFailureListener { e ->
                onFinished(false, e.message)
            }
    }

    fun limpiarSesion() {
        usuarioId = null
        favoritos.clear()
    }

    // ================== GETTERS ==================

    fun obtenerFavoritos(): List<Producto> = favoritos

    fun esFavorito(producto: Producto): Boolean {
        val idProd = producto.idDocumento
        return if (!idProd.isNullOrEmpty()) {
            favoritos.any { it.idDocumento == idProd }
        } else {
            favoritos.any { it.nombre == producto.nombre }
        }
    }

    // ================== SINCRONIZACIÓN FIRESTORE ==================

    private fun sync(onFinished: (Boolean, String?) -> Unit = { _, _ -> }) {
        val uid = usuarioId
        if (uid.isNullOrEmpty()) {
            onFinished(false, "Sin usuario en FavoritosManager")
            return
        }

        val colRef = db.collection("bbh_favoritos")
            .document(uid)
            .collection("items")

        // Borramos y reinsertamos (simple y seguro)
        colRef.get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()

                // borrar actuales
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }

                // insertar lista actual
                for (p in favoritos) {
                    val docId = p.idDocumento ?: (p.nombreCorto ?: p.nombre ?: "producto")
                    val refDoc = colRef.document(docId)

                    val data = hashMapOf(
                        "idProducto"  to p.idDocumento,
                        "nombreCorto" to p.nombreCorto,
                        "nombre"      to p.nombre,
                        "categoria"   to p.categoria,
                        "precio"      to p.precio,
                        "stock"       to p.stock,
                        "imagenUrl"   to p.imagenUrl
                    )
                    batch.set(refDoc, data)
                }

                batch.commit()
                    .addOnSuccessListener { onFinished(true, null) }
                    .addOnFailureListener { e -> onFinished(false, e.message) }
            }
            .addOnFailureListener { e ->
                onFinished(false, e.message)
            }
    }

    // ================== OPERACIONES FAVORITOS ==================

    /**
     * Agrega un producto a favoritos.
     */
    fun agregarFavorito(producto: Producto, onFinished: (Boolean, String?) -> Unit = { _, _ -> }) {
        if (esFavorito(producto)) {
            onFinished(true, null)
            return
        }
        favoritos.add(producto)
        sync(onFinished)
    }

    /**
     * Elimina un producto de favoritos.
     */
    fun quitarFavorito(producto: Producto, onFinished: (Boolean, String?) -> Unit = { _, _ -> }) {
        val idProd = producto.idDocumento
        val idx = if (!idProd.isNullOrEmpty()) {
            favoritos.indexOfFirst { it.idDocumento == idProd }
        } else {
            favoritos.indexOfFirst { it.nombre == producto.nombre }
        }
        if (idx >= 0) {
            favoritos.removeAt(idx)
            sync(onFinished)
        } else {
            onFinished(true, null)
        }
    }

    /**
     * Alterna estado de favorito.
     */
    fun alternarFavorito(producto: Producto, onFinished: (Boolean, String?) -> Unit = { _, _ -> }) {
        if (esFavorito(producto)) {
            quitarFavorito(producto, onFinished)
        } else {
            agregarFavorito(producto, onFinished)
        }
    }
}
