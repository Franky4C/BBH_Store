package mx.tecnm.cdhidalgo.bbhstore

import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Orden
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Usuario
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import mx.tecnm.cdhidalgo.bbhstore.adaptadores.AdaptadorCarrito
import mx.tecnm.cdhidalgo.bbhstore.dataclass.CarritoManager
import mx.tecnm.cdhidalgo.bbhstore.dataclass.ItemOrden

class CarritoActivity : AppCompatActivity() {

    private lateinit var rvCarrito: RecyclerView
    private lateinit var txtTotalItems: TextView
    private lateinit var txtTotalImporte: TextView
    private lateinit var btnVaciar: Button
    private lateinit var btnProceder: Button
    private lateinit var btnRegresar: ImageButton

    private val db = Firebase.firestore
    private var usuario: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_carrito)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Si en algún momento decides mandar el usuario por Intent, aquí lo recuperas
        usuario = intent.getParcelableExtra("usuario")

        rvCarrito = findViewById(R.id.rv_carrito)
        txtTotalItems = findViewById(R.id.txt_total_items)
        txtTotalImporte = findViewById(R.id.txt_total_importe)
        btnVaciar = findViewById(R.id.btn_vaciar_carrito)
        btnProceder = findViewById(R.id.btn_proceder_compra)
        btnRegresar = findViewById(R.id.btn_regresar_carrito)

        rvCarrito.layoutManager = LinearLayoutManager(this)
        val adaptador = AdaptadorCarrito()
        rvCarrito.adapter = adaptador

        adaptador.onCantidadCambiada = {
            actualizarResumen()
        }

        btnVaciar.setOnClickListener {
            CarritoManager.vaciarCarrito()
            rvCarrito.adapter?.notifyDataSetChanged()
            actualizarResumen()
        }

        btnProceder.setOnClickListener {
            val itemsCarrito = CarritoManager.obtenerItems()
            if (itemsCarrito.isEmpty()) {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Construir la lista de ItemOrden para guardar en Firestore
            val listaItemsOrden = itemsCarrito.map { item ->
                ItemOrden(
                    nombre = item.producto.nombre,
                    nombreCorto = item.producto.nombreCorto,
                    categoria = item.producto.categoria,
                    precioUnitario = item.producto.precio,
                    cantidad = item.cantidad,
                    subtotal = item.producto.precio * item.cantidad
                )
            }

            val idCompra = "ORD-" + System.currentTimeMillis().toString()
            val total = CarritoManager.obtenerImporteTotal()

            // Correo del usuario: si tienes Usuario úsalo, si no agarra el de FirebaseAuth
            val correoUsuario = usuario?.correo
                ?: FirebaseAuth.getInstance().currentUser?.email

            val orden = Orden(
                idCompra = idCompra,
                fecha = System.currentTimeMillis(),
                usuarioCorreo = correoUsuario,
                items = listaItemsOrden,
                total = total
            )

            // TEXTO RESUMEN PARA LA PANTALLA FINAL
            val resumen = listaItemsOrden.joinToString("\n") { item ->
                val nombre = item.nombreCorto ?: item.nombre ?: "Producto"
                "${item.cantidad} x $nombre  -  $${String.format("%.2f", item.subtotal)}"
            }

            // Guardar orden en Firestore
            db.collection("bbh_ordenes")
                .document(orden.idCompra)
                .set(orden)
                .addOnSuccessListener {
                    // Vaciar carrito solo si se guardó bien
                    CarritoManager.vaciarCarrito()
                    rvCarrito.adapter?.notifyDataSetChanged()
                    actualizarResumen()

                    // Ir a pantalla de confirmación CON MÁS DATOS
                    val intent = Intent(this, CompraConfirmadaActivity::class.java)
                    intent.putExtra("orden_id", orden.idCompra)
                    intent.putExtra("orden_total", orden.total)
                    intent.putExtra("orden_correo", orden.usuarioCorreo)
                    intent.putExtra("orden_fecha", orden.fecha)
                    intent.putExtra("orden_resumen", resumen)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error al guardar la orden: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        btnRegresar.setOnClickListener {
            val intent = Intent(this, Tienda::class.java)
            startActivity(intent)
            finish()
        }

        actualizarResumen()
    }

    private fun actualizarResumen() {
        val totalItems = CarritoManager.obtenerCantidadTotal()
        val totalImporte = CarritoManager.obtenerImporteTotal()
        txtTotalItems.text = "Artículos: $totalItems"
        txtTotalImporte.text = "Total: $${String.format("%.2f", totalImporte)}"
    }
}
