package mx.tecnm.cdhidalgo.bbhstore

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import mx.tecnm.cdhidalgo.bbhstore.adaptadores.AdminProductosAdapter
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Producto

class AdminProductosActivity : AppCompatActivity() {

    private val db = Firebase.firestore

    private lateinit var btnRegresar: ImageButton
    private lateinit var btnAgregar: Button
    private lateinit var txtSinProductos: TextView
    private lateinit var rvProductos: RecyclerView

    private lateinit var adaptador: AdminProductosAdapter

    // lista local de (idDocumento, producto)
    private val listaAdmin = mutableListOf<Pair<String, Producto>>()

    // para poder rellenar el campo imagen al editar
    private val mapaImagenNamePorId = mutableMapOf<String, String?>()

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

        btnAgregar.setOnClickListener {
            mostrarDialogoProducto()   // alta (sin idDocumento)
        }

        adaptador.onEditarClick = { idDoc, producto ->
            mostrarDialogoProducto(idDoc, producto)
        }

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
                    txtSinProductos.visibility = View.VISIBLE
                    adaptador.actualizar(emptyList())
                    return@addOnSuccessListener
                }

                txtSinProductos.visibility = View.GONE

                mapaImagenNamePorId.clear()
                val nuevaLista = mutableListOf<Pair<String, Producto>>()

                for (doc in snapshot.documents) {
                    val imagenName = doc.getString("imagenName")
                    mapaImagenNamePorId[doc.id] = imagenName

                    val producto = mapDocToProducto(doc, imagenName)
                    nuevaLista.add(doc.id to producto)
                }

                adaptador.actualizar(nuevaLista)
            }
            .addOnFailureListener { e ->
                txtSinProductos.visibility = View.VISIBLE
                Toast.makeText(this, "Error al cargar productos: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun mapDocToProducto(doc: DocumentSnapshot, imagenName: String?): Producto {
        val nombreCorto = doc.getString("nombreCorto")
        val nombre = doc.getString("nombre")
        val descripcion = doc.getString("descripcion")
        val categoria = doc.getString("categoria")
        val precio = doc.getDouble("precio") ?: 0.0
        val stock = (doc.getLong("stock") ?: 0L).toInt()

        val imagenResId = obtenerResDrawable(imagenName)

        return Producto(
            imagen = imagenResId,
            nombreCorto = nombreCorto,
            nombre = nombre,
            precio = precio,
            descripcion = descripcion,
            categoria = categoria,
            stock = stock
        )
    }

    private fun obtenerResDrawable(imagenName: String?): Int {
        if (imagenName.isNullOrBlank()) {
            return R.drawable.logo // pon aquí un drawable por defecto tuyo
        }
        val resId = resources.getIdentifier(imagenName, "drawable", packageName)
        return if (resId != 0) resId else R.drawable.logo
    }

    // ----------------- Alta / edición de producto -----------------

    private fun mostrarDialogoProducto(
        idDocumento: String? = null,
        producto: Producto? = null
    ) {
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

        // Si es edición, rellenar campos
        if (producto != null && idDocumento != null) {
            edtNombreCorto.setText(producto.nombreCorto ?: "")
            edtNombreLargo.setText(producto.nombre ?: "")
            edtDescripcion.setText(producto.descripcion ?: "")
            edtCategoria.setText(producto.categoria ?: "")
            edtPrecio.setText(producto.precio.toString())
            edtStock.setText(producto.stock.toString())

            val imagenName = mapaImagenNamePorId[idDocumento] ?: ""
            edtImagenName.setText(imagenName)
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
            val imagenName = edtImagenName.text.toString().trim()

            val precio = precioStr.toDoubleOrNull() ?: 0.0
            val stock = stockStr.toIntOrNull() ?: 0

            if (nombreCorto.isEmpty() || nombreLargo.isEmpty() || categoria.isEmpty()) {
                Toast.makeText(this, "Llena al menos nombre corto, nombre y categoría", Toast.LENGTH_LONG).show()
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

            val data = hashMapOf(
                "nombreCorto" to nombreCorto,
                "nombre" to nombreLargo,
                "descripcion" to descripcion,
                "categoria" to categoria,
                "precio" to precio,
                "stock" to stock,
                "imagenName" to imagenName
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
                        Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
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
                        Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
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
                        Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
