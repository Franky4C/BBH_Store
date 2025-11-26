package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import mx.tecnm.cdhidalgo.bbhstore.adaptadores.AdaptadorArtesania
import mx.tecnm.cdhidalgo.bbhstore.dataclass.CarritoManager
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Producto
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Usuario

class Tienda : AppCompatActivity() {

    companion object {
        const val ADMIN_EMAIL = "jdra464@gmail.com"   // correo de admin
    }

    private lateinit var btnRegresar: ImageButton
    private lateinit var btnCarrito: ImageButton
    private lateinit var btnHistorial: ImageButton
    private lateinit var btnAdmin: ImageButton
    private lateinit var btn_favoritos: ImageButton

    private lateinit var texto: TextView
    private lateinit var contadorCarrito: TextView
    private lateinit var usuario: Usuario

    private lateinit var rvArtesanias: RecyclerView
    private lateinit var listaArtesanias: ArrayList<Producto>
    private lateinit var adaptadorArtesania: AdaptadorArtesania

    // Firestore
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tienda)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Usuario recibido desde Login
        if (intent.hasExtra("usuario")) {
            usuario = intent.getParcelableExtra("usuario")!!
        } else {
            finish()
            return
        }

        // Referencias UI
        texto = findViewById(R.id.usuario_tienda)
        btnRegresar = findViewById(R.id.btn_cerrar_sesion_tienda)
        btnCarrito = findViewById(R.id.btn_carrito_tienda)
        btn_favoritos = findViewById(R.id.btn_favoritos_tienda)
        contadorCarrito = findViewById(R.id.contador_carrito_tienda)
        rvArtesanias = findViewById(R.id.artesanias_tienda)

        btnHistorial = findViewById(R.id.btn_historial_tienda)
        btnAdmin = findViewById(R.id.btn_admin_tienda)

        // Nombre del usuario
        val nombreCompleto = "${usuario.nombre} ${usuario.apaterno} ${usuario.amaterno}"
        texto.text = nombreCompleto

        // Mostrar/ocultar botón Admin según correo
        val correoUsuario = usuario.correo
        if (correoUsuario == ADMIN_EMAIL) {
            btnAdmin.visibility = View.VISIBLE
        } else {
            btnAdmin.visibility = View.GONE
        }

        // Botón regresar (cierra la tienda)
        btnRegresar.setOnClickListener { finish() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        // Recycler y adaptador
        listaArtesanias = ArrayList()
        adaptadorArtesania = AdaptadorArtesania(listaArtesanias)

        rvArtesanias.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvArtesanias.setHasFixedSize(true)
        rvArtesanias.adapter = adaptadorArtesania

        // Click → Detalle
        adaptadorArtesania.onProductoClick = { producto ->
            val intent = Intent(this, DetalleProducto::class.java)
            intent.putExtra("usuario", usuario)
            intent.putExtra("producto", producto)
            startActivity(intent)
        }

        // Long click → agregar al carrito respetando stock
        adaptadorArtesania.onProductoLongClick = { producto ->
            val agregado = CarritoManager.agregarProducto(producto, 1)
            if (agregado) {
                Toast.makeText(this, "Agregado al carrito", Toast.LENGTH_SHORT).show()
                actualizarContadorCarrito()
            } else {
                Toast.makeText(this, "Sin stock suficiente", Toast.LENGTH_SHORT).show()
            }
        }

        // Abrir carrito
        btnCarrito.setOnClickListener {
            val intent = Intent(this, CarritoActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        // Abrir favoritos
        btn_favoritos.setOnClickListener {
            val intent = Intent(this, FavoritosActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        // Abrir historial (solo para este usuario)
        btnHistorial.setOnClickListener {
            val intent = Intent(this, HistorialActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        // Abrir dashboard admin (solo visible si es admin)
        btnAdmin.setOnClickListener {
            val intent = Intent(this, AdminDashboardActivity::class.java)
            startActivity(intent)
        }

        actualizarContadorCarrito()

        // Cargar productos desde Firestore
        cargarProductosDesdeFirestore()
    }

    override fun onResume() {
        super.onResume()
        actualizarContadorCarrito()
        cargarProductosDesdeFirestore()   // <- recarga la lista desde Firestore
    }

    private fun cargarProductosDesdeFirestore() {
        db.collection("bbh_productos")
            .get()
            .addOnSuccessListener { snapshot ->
                listaArtesanias.clear()

                for (doc in snapshot.documents) {
                    val nombreCorto = doc.getString("nombreCorto") ?: ""
                    val nombre = doc.getString("nombre") ?: nombreCorto
                    val descripcion = doc.getString("descripcion") ?: ""
                    val categoria = doc.getString("categoria") ?: "artesania"
                    val precio = doc.getDouble("precio") ?: 0.0
                    val stock = doc.getLong("stock")?.toInt() ?: 0

                    val imagenUrl = doc.getString("imagenUrl")   // URL subida a Storage

                    val producto = Producto(
                        imagen = R.drawable.artesania1,          // placeholder
                        nombreCorto = nombreCorto,
                        nombre = nombre,
                        precio = precio,
                        descripcion = descripcion,
                        categoria = categoria,
                        stock = stock,
                        imagenUrl = imagenUrl                    // IMPORTANTE
                    )
                    listaArtesanias.add(producto)
                }

                adaptadorArtesania.notifyDataSetChanged()

                if (listaArtesanias.isEmpty()) {
                    Toast.makeText(this, "No hay productos configurados", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al cargar productos: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }




    private fun actualizarContadorCarrito() {
        val total = CarritoManager.obtenerCantidadTotal()
        contadorCarrito.text = total.toString()
    }
}
