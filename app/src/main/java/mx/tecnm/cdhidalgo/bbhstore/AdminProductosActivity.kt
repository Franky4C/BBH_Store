package mx.tecnm.cdhidalgo.bbhstore

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import mx.tecnm.cdhidalgo.bbhstore.adaptadores.AdminProductosAdapter
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Producto

class AdminProductosActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private val storage = FirebaseStorage.getInstance()

    private lateinit var btnRegresar: ImageButton
    private lateinit var btnAgregar: Button
    private lateinit var txtSinProductos: TextView
    private lateinit var rvProductos: RecyclerView

    private lateinit var adaptador: AdminProductosAdapter

    // lista local de (idDocumento, producto)
    private val listaAdmin = mutableListOf<Pair<String, Producto>>()

    // mapa para recordar la URL de imagen por id documento (para edición)
    private val mapaImagenUrlPorId = mutableMapOf<String, String?>()

    // estado del diálogo actual
    private var imagenSeleccionadaUri: Uri? = null
    private var imagenUrlExistente: String? = null

    // launcher para elegir imagen de la galería
    private var onImagePicked: ((Uri) -> Unit)? = null
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { onImagePicked?.invoke(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_productos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnRegresar = findViewById(R.id.btn_regresar_admin_prod)
        btnAgregar = findViewById(R.id.btn_agregar_producto)
        txtSinProductos = findViewById(R.id.txt_sin_productos)
        rvProductos = findViewById(R.id.rv_admin_productos)

        rvProductos.layoutManager = LinearLayoutManager(this)
        adaptador = AdminProductosAdapter(listaAdmin)
        rvProductos.adapter = adaptador

        btnRegresar.setOnClickListener { finish() }

        // Alta de producto
        btnAgregar.setOnClickListener {
            mostrarDialogoProducto() // sin id → alta
        }

        // Editar producto
        adaptador.onEditarClick = { idDoc, producto ->
            mostrarDialogoProducto(idDoc, producto)
        }

        // Eliminar producto
        adaptador.onEliminarClick = { idDoc, _ ->
            confirmarEliminar(idDoc)
        }

        cargarProductos()
    }

    // ----------------- Cargar productos de Firestore -----------------

    private fun cargarProductos() {
        db.collection("bbh_productos")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    txtSinProductos.visibility = TextView.VISIBLE
                    adaptador.actualizar(emptyList())
                    return@addOnSuccessListener
                }

                txtSinProductos.visibility = TextView.GONE

                mapaImagenUrlPorId.clear()
                val nuevaLista = mutableListOf<Pair<String, Producto>>()

                for (doc in snapshot.documents) {
                    val imagenUrl = doc.getString("imagenUrl")
                    mapaImagenUrlPorId[doc.id] = imagenUrl

                    val producto = mapDocToProducto(doc)
                    nuevaLista.add(doc.id to producto)
                }

                adaptador.actualizar(nuevaLista)
            }
            .addOnFailureListener { e ->
                txtSinProductos.visibility = TextView.VISIBLE
                Toast.makeText(
                    this,
                    "Error al cargar productos: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun mapDocToProducto(doc: DocumentSnapshot): Producto {
        val nombreCorto = doc.getString("nombreCorto")
        val nombre = doc.getString("nombre")
        val descripcion = doc.getString("descripcion")
        val categoria = doc.getString("categoria")
        val precio = doc.getDouble("precio") ?: 0.0
        val stock = (doc.getLong("stock") ?: 0L).toInt()
        val imagenUrl = doc.getString("imagenUrl")   // NUEVO

        // En admin usamos un drawable genérico como placeholder
        val imagenResId = R.drawable.logo

        return Producto(
            imagen = imagenResId,
            nombreCorto = nombreCorto,
            nombre = nombre,
            precio = precio,
            descripcion = descripcion,
            categoria = categoria,
            stock = stock,
            imagenUrl = imagenUrl
        )
    }


    // ----------------- Alta / edición de producto -----------------

    private fun mostrarDialogoProducto(
        idDocumento: String? = null,
        producto: Producto? = null
    ) {
        imagenSeleccionadaUri = null
        imagenUrlExistente = null

        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (idDocumento == null) "Agregar producto" else "Editar producto")

        val inflater: LayoutInflater = layoutInflater
        val vista = inflater.inflate(R.layout.dialog_admin_producto, null)

        val edtNombreCorto = vista.findViewById<EditText>(R.id.edt_nombre_corto)
        val edtNombreLargo = vista.findViewById<EditText>(R.id.edt_nombre_largo)
        val edtDescripcion = vista.findViewById<EditText>(R.id.edt_descripcion)
        val edtCategoria = vista.findViewById<EditText>(R.id.edt_categoria)
        val edtPrecio = vista.findViewById<EditText>(R.id.edt_precio)
        val edtStock = vista.findViewById<EditText>(R.id.edt_stock)
        val edtImagenName = vista.findViewById<EditText>(R.id.edt_imagen_name)


        // Convertimos este EditText en "selector de imagen"
        edtImagenName.isFocusable = false
        edtImagenName.isClickable = true
        edtImagenName.hint = "Toca aquí para elegir imagen"

        // Si es edición, rellenar campos de texto
        if (producto != null && idDocumento != null) {
            edtNombreCorto.setText(producto.nombreCorto ?: "")
            edtNombreLargo.setText(producto.nombre ?: "")
            edtDescripcion.setText(producto.descripcion ?: "")
            edtCategoria.setText(producto.categoria ?: "")
            edtPrecio.setText(producto.precio.toString())
            edtStock.setText(producto.stock.toString())

            imagenUrlExistente = mapaImagenUrlPorId[idDocumento]
            if (!imagenUrlExistente.isNullOrEmpty()) {
                edtImagenName.setText("Imagen actual guardada")
            }
        }

        // Click para elegir imagen desde la galería
        edtImagenName.setOnClickListener {
            onImagePicked = { uri ->
                imagenSeleccionadaUri = uri
                val nombreArchivo =
                    uri.lastPathSegment?.substringAfterLast("/") ?: "Imagen seleccionada"
                edtImagenName.setText(nombreArchivo)
            }
            pickImageLauncher.launch("image/*")
        }

        builder.setView(vista)

        builder.setPositiveButton(
            if (idDocumento == null) "Guardar" else "Actualizar"
        ) { dialog, _ ->
            val nombreCorto = edtNombreCorto.text.toString().trim()
            val nombreLargo = edtNombreLargo.text.toString().trim()
            val descripcion = edtDescripcion.text.toString().trim()
            val categoria = edtCategoria.text.toString().trim()
            val precioStr = edtPrecio.text.toString().trim()
            val stockStr = edtStock.text.toString().trim()

            val precio = precioStr.toDoubleOrNull() ?: 0.0
            val stock = stockStr.toIntOrNull() ?: 0

            if (nombreCorto.isEmpty() || nombreLargo.isEmpty() || categoria.isEmpty()) {
                Toast.makeText(
                    this,
                    "Llena al menos nombre corto, nombre y categoría",
                    Toast.LENGTH_LONG
                ).show()
                return@setPositiveButton
            }

            if (precio <= 0.0) {
                Toast.makeText(this, "El precio debe ser mayor a 0", Toast.LENGTH_LONG).show()
                return@setPositiveButton
            }

            if (stock < 0) {
                Toast.makeText(this, "El stock no puede ser negativo", Toast.LENGTH_LONG).show()
                return@setPositiveButton
            }

            // Si el usuario seleccionó una nueva imagen → subir y luego guardar
            if (imagenSeleccionadaUri != null) {
                subirImagenYGuardar(
                    uri = imagenSeleccionadaUri!!,
                    idDocumento = idDocumento,
                    nombreCorto = nombreCorto,
                    nombreLargo = nombreLargo,
                    descripcion = descripcion,
                    categoria = categoria,
                    precio = precio,
                    stock = stock
                )
            } else {
                // No se seleccionó nueva imagen:
                // - Alta: se guarda sin imagenUrl (o podrías impedirlo si quieres).
                // - Edición: se reutiliza imagenUrlExistente.
                guardarProductoEnFirestore(
                    idDocumento = idDocumento,
                    nombreCorto = nombreCorto,
                    nombreLargo = nombreLargo,
                    descripcion = descripcion,
                    categoria = categoria,
                    precio = precio,
                    stock = stock,
                    imagenUrl = imagenUrlExistente
                )
            }

            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    // ----------------- Subir imagen a Storage y luego guardar -----------------

    private fun subirImagenYGuardar(
        uri: Uri,
        idDocumento: String?,
        nombreCorto: String,
        nombreLargo: String,
        descripcion: String,
        categoria: String,
        precio: Double,
        stock: Int
    ) {
        val nombreArchivo = "${System.currentTimeMillis()}_${nombreCorto}.jpg"
        val ref = storage.reference.child("productos/$nombreArchivo")

        Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_SHORT).show()

        ref.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Error al subir imagen")
                }
                ref.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                val url = downloadUri.toString()
                guardarProductoEnFirestore(
                    idDocumento = idDocumento,
                    nombreCorto = nombreCorto,
                    nombreLargo = nombreLargo,
                    descripcion = descripcion,
                    categoria = categoria,
                    precio = precio,
                    stock = stock,
                    imagenUrl = url
                )
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al subir imagen: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // ----------------- Guardar / actualizar en Firestore -----------------

    private fun guardarProductoEnFirestore(
        idDocumento: String?,
        nombreCorto: String,
        nombreLargo: String,
        descripcion: String,
        categoria: String,
        precio: Double,
        stock: Int,
        imagenUrl: String?
    ) {
        val data = hashMapOf(
            "nombreCorto" to nombreCorto,
            "nombre" to nombreLargo,
            "descripcion" to descripcion,
            "categoria" to categoria,
            "precio" to precio,
            "stock" to stock,
            "imagenUrl" to imagenUrl
        )

        if (idDocumento == null) {
            // Alta
            db.collection("bbh_productos")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show()
                    cargarProductos()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error al guardar: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        } else {
            // Edición
            db.collection("bbh_productos")
                .document(idDocumento)
                .set(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
                    cargarProductos()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error al actualizar: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    // ----------------- Eliminar producto -----------------

    private fun confirmarEliminar(idDocumento: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar producto")
            .setMessage("¿Deseas eliminar este producto del catálogo?")
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection("bbh_productos")
                    .document(idDocumento)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
                        cargarProductos()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al eliminar: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
