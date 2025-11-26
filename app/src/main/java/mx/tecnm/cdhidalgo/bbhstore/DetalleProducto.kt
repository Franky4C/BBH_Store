package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.bumptech.glide.Glide
import mx.tecnm.cdhidalgo.bbhstore.dataclass.CarritoManager
import mx.tecnm.cdhidalgo.bbhstore.dataclass.ItemOrden
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Orden
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Producto
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Usuario

class DetalleProducto : AppCompatActivity() {

    private val db = Firebase.firestore

    private lateinit var btnRegresar: ImageButton
    private lateinit var btnCarrito: ImageButton
    private lateinit var contadorCarrito: TextView
    private lateinit var usuarioNombre: TextView

    private lateinit var tituloDetalle: TextView
    private lateinit var imagenProducto: ImageView
    private lateinit var nombreProducto: TextView
    private lateinit var descripcionProducto: TextView
    private lateinit var precioProducto: TextView
    private lateinit var btnComprar: Button
    private lateinit var btnAgregarCarrito: Button

    private var usuario: Usuario? = null
    private lateinit var producto: Producto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_producto)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias UI (IDs según tu XML)
        btnRegresar = findViewById(R.id.btn_regresar_detalle)
        btnCarrito = findViewById(R.id.btn_carrito_detalle)
        contadorCarrito = findViewById(R.id.contador_carrito_detalle)
        usuarioNombre = findViewById(R.id.usuario_detalle)

        tituloDetalle = findViewById(R.id.txt_titulo_detalle_producto)
        imagenProducto = findViewById(R.id.imagen_detalle)
        nombreProducto = findViewById(R.id.nombre_detalle)
        descripcionProducto = findViewById(R.id.descripcion_detalle)
        precioProducto = findViewById(R.id.precio_detalle)

        btnComprar = findViewById(R.id.btn_comprar_detalle)
        btnAgregarCarrito = findViewById(R.id.btn_agregar_carrito_detalle)

        // Recibir datos
        usuario = intent.getParcelableExtra("usuario")
        producto = intent.getParcelableExtra("producto")
            ?: run {
                Toast.makeText(this, "No se pudo abrir el producto", Toast.LENGTH_LONG).show()
                finish()
                return
            }

        // Título de la pantalla
        tituloDetalle.text = "Detalles del producto"

        // Nombre del usuario
        usuario?.let { u ->
            val nombreCompleto = "${u.nombre} ${u.apaterno} ${u.amaterno}"
            usuarioNombre.text = nombreCompleto
        }

        // Datos del producto
        nombreProducto.text = producto.nombreCorto ?: producto.nombre ?: "Producto"
        descripcionProducto.text = producto.descripcion ?: ""
        precioProducto.text = "$${String.format("%.2f", producto.precio)}"

        // Cargar imagen:
        // - Si hay imagenUrl (de Firebase Storage) → Glide.
        // - Si no, usamos el drawable local (producto.imagen).
        if (!producto.imagenUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(producto.imagenUrl)
                .placeholder(producto.imagen)   // tu logo u otra imagen local
                .centerCrop()
                .into(imagenProducto)
        } else {
            imagenProducto.setImageResource(producto.imagen)
        }

        // Botón agregar al carrito
        btnAgregarCarrito.setOnClickListener {
            val agregado = CarritoManager.agregarProducto(producto, 1)
            if (agregado) {
                Toast.makeText(this, "Agregado al carrito", Toast.LENGTH_SHORT).show()
                actualizarContadorCarrito()
            } else {
                Toast.makeText(this, "Sin stock suficiente", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón comprar (compra directa de 1 pieza)
        btnComprar.setOnClickListener {
            val prod = producto ?: return@setOnClickListener
            val idDoc = prod.idDocumento

            if (idDoc.isNullOrEmpty()) {
                Toast.makeText(this, "No se puede comprar: producto sin ID", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val cantidad = 1
            val correoUsuario = usuario?.correo ?: FirebaseAuth.getInstance().currentUser?.email

            if (correoUsuario.isNullOrEmpty()) {
                Toast.makeText(this, "No se pudo determinar el usuario", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val docProducto = db.collection("bbh_productos").document(idDoc)

            // 1) Transacción: validar y restar stock GLOBAL
            db.runTransaction { tx ->
                val snap = tx.get(docProducto)
                val stockActual = snap.getLong("stock") ?: 0L

                if (stockActual < cantidad) {
                    throw Exception("Sin stock suficiente")
                }

                tx.update(docProducto, "stock", stockActual - cantidad)
            }.addOnSuccessListener {
                // 2) Si la transacción fue bien, ahora sí crear la orden
                val idCompra = "ORD-" + System.currentTimeMillis().toString()
                val total = prod.precio * cantidad

                val itemOrden = ItemOrden(
                    nombre = prod.nombre,
                    nombreCorto = prod.nombreCorto,
                    categoria = prod.categoria,
                    precioUnitario = prod.precio,
                    cantidad = cantidad,
                    subtotal = total
                    // si luego quieres, aquí puedes agregar idProducto
                )

                val orden = Orden(
                    idCompra = idCompra,
                    fecha = System.currentTimeMillis(),
                    usuarioCorreo = correoUsuario,
                    items = listOf(itemOrden),
                    total = total
                )

                db.collection("bbh_ordenes")
                    .document(orden.idCompra)
                    .set(orden)
                    .addOnSuccessListener {
                        val intent = Intent(this, CompraConfirmadaActivity::class.java)
                        intent.putExtra("orden_id", orden.idCompra)
                        intent.putExtra("orden_total", orden.total)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al guardar la orden: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }.addOnFailureListener { e ->
                if (e.message?.contains("Sin stock suficiente") == true) {
                    Toast.makeText(this, "Sin stock suficiente para este producto", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error al actualizar stock: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }


        // Icono de carrito
        btnCarrito.setOnClickListener {
            val intent = Intent(this, CarritoActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        // Flecha regresar
        btnRegresar.setOnClickListener {
            finish()
        }

        actualizarContadorCarrito()
    }

    override fun onResume() {
        super.onResume()
        actualizarContadorCarrito()
    }

    private fun actualizarContadorCarrito() {
        val total = CarritoManager.obtenerCantidadTotal()
        contadorCarrito.text = total.toString()
    }
}
