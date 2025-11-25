package mx.tecnm.cdhidalgo.bbhstore

import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.FirebaseAuth
import mx.tecnm.cdhidalgo.bbhstore.dataclass.ItemOrden
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Orden
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import mx.tecnm.cdhidalgo.bbhstore.dataclass.CarritoManager
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Producto
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Usuario

class DetalleProducto : AppCompatActivity() {

    private lateinit var btnRegresar: ImageButton
    private lateinit var btnCarrito: ImageButton
    private lateinit var contadorCarrito: TextView
    private lateinit var usuarioNombre: TextView
    private lateinit var imagenProducto: ImageView
    private lateinit var nombreProducto: TextView
    private lateinit var descripcionProducto: TextView
    private lateinit var precioProducto: TextView
    private lateinit var btnComprar: Button
    private lateinit var btnAgregarCarrito: Button
    private val db = Firebase.firestore
    private var usuario: Usuario? = null
    private var producto: Producto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_producto)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias de UI (coinciden con tu XML)
        btnRegresar = findViewById(R.id.btn_regresar_detalle)
        btnCarrito = findViewById(R.id.btn_carrito_detalle)
        contadorCarrito = findViewById(R.id.contador_carrito_detalle)
        usuarioNombre = findViewById(R.id.usuario_detalle)
        imagenProducto = findViewById(R.id.imagen_detalle)
        nombreProducto = findViewById(R.id.nombre_detalle)
        descripcionProducto = findViewById(R.id.descripcion_detalle)
        precioProducto = findViewById(R.id.precio_detalle)
        btnComprar = findViewById(R.id.btn_comprar_detalle)
        btnAgregarCarrito = findViewById(R.id.btn_agregar_carrito_detalle)

        // Recibir datos
        usuario = intent.getParcelableExtra("usuario")
        producto = intent.getParcelableExtra("producto")

        // Mostrar nombre de usuario (completo si quieres)
        usuario?.let { u ->
            val nombreCompleto = "${u.nombre} ${u.apaterno} ${u.amaterno}"
            usuarioNombre.text = nombreCompleto
        }

        // Mostrar datos del producto
        producto?.let { p ->
            imagenProducto.setImageResource(p.imagen)
            nombreProducto.text = p.nombre
            descripcionProducto.text = p.descripcion
            precioProducto.text = "$${p.precio}"
        }

        // Nuevo botón: AGREGAR A CARRITO
        btnAgregarCarrito.setOnClickListener {
            val prod = producto ?: return@setOnClickListener
            val agregado = CarritoManager.agregarProducto(prod, 1)
            if (agregado) {
                Toast.makeText(this, "Agregado al carrito", Toast.LENGTH_SHORT).show()
                actualizarContadorCarrito()
            } else {
                Toast.makeText(this, "Sin stock suficiente", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón COMPRAR
        btnComprar.setOnClickListener {
            val prod = producto ?: return@setOnClickListener

            // Validar stock si lo manejas en Producto
            // if (prod.stock <= 0) { ... }  // solo si tienes ese campo

            val idCompra = "ORD-" + System.currentTimeMillis().toString()
            val cantidad = 1
            val total = prod.precio * cantidad
            val correoUsuario = usuario?.correo ?: FirebaseAuth.getInstance().currentUser?.email

            val itemOrden = ItemOrden(
                nombre = prod.nombre,
                nombreCorto = prod.nombreCorto,
                categoria = prod.categoria,
                precioUnitario = prod.precio,
                cantidad = cantidad,
                subtotal = total
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
                    Toast.makeText(this, "Error al guardar la orden: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }


        // Icono de carrito: abrir pantalla de carrito
        btnCarrito.setOnClickListener {
            val intent = Intent(this, CarritoActivity::class.java)
            startActivity(intent)
        }

        // Flecha regresar: volver a la actividad anterior (Tienda)
        btnRegresar.setOnClickListener {
            finish()
        }

        // Contador inicial
        actualizarContadorCarrito()
    }

    override fun onResume() {
        super.onResume()
        // Por si se va al carrito y regresa a detalle
        actualizarContadorCarrito()
    }

    private fun actualizarContadorCarrito() {
        val total = CarritoManager.obtenerCantidadTotal()
        contadorCarrito.text = total.toString()
    }
}
