package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import mx.tecnm.cdhidalgo.bbhstore.adaptadores.AdaptadorCarrito
import mx.tecnm.cdhidalgo.bbhstore.dataclass.CarritoManager
import mx.tecnm.cdhidalgo.bbhstore.dataclass.ItemOrden
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Orden
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Usuario

class CarritoActivity : AppCompatActivity() {

    private lateinit var rvCarrito: RecyclerView
    private lateinit var txtTotalItems: TextView
    private lateinit var txtTotalImporte: TextView
    private lateinit var btnVaciar: Button
    private lateinit var btnProceder: Button
    private lateinit var btnRegresar: ImageButton
    private lateinit var adaptador: AdaptadorCarrito

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

        usuario = intent.getParcelableExtra("usuario")

        rvCarrito = findViewById(R.id.rv_carrito)
        txtTotalItems = findViewById(R.id.txt_total_items)
        txtTotalImporte = findViewById(R.id.txt_total_importe)
        btnVaciar = findViewById(R.id.btn_vaciar_carrito)
        btnProceder = findViewById(R.id.btn_proceder_compra)
        btnRegresar = findViewById(R.id.btn_regresar_carrito)

        rvCarrito.layoutManager = LinearLayoutManager(this)
        adaptador = AdaptadorCarrito()
        rvCarrito.adapter = adaptador

        adaptador.onCantidadCambiada = {
            actualizarResumen()
        }

        btnVaciar.setOnClickListener {
            CarritoManager.vaciarCarrito()
            adaptador.refrescarDatos()
            actualizarResumen()
        }

        btnProceder.setOnClickListener { procederCompra() }

        btnRegresar.setOnClickListener {
            val intent = Intent(this, Tienda::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
            finish()
        }

        actualizarResumen()
    }

    private fun procederCompra() {
        // 1) Validar carrito y selección
        if (CarritoManager.obtenerItems().isEmpty()) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            return
        }

        val seleccionados = adaptador.obtenerItemsSeleccionados()
        if (seleccionados.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un producto", Toast.LENGTH_SHORT).show()
            return
        }

        // 2) Verificar que todos los productos tengan idDocumento
        for (item in seleccionados) {
            val idDoc = item.producto.idDocumento
            if (idDoc.isNullOrEmpty()) {
                val nombre = item.producto.nombreCorto ?: item.producto.nombre ?: "Producto"
                Toast.makeText(
                    this,
                    "No se puede comprar: $nombre no tiene ID en Firestore",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        // 3) Preparar lista de ItemOrden (para la orden y el resumen)
        val listaItemsOrden = seleccionados.map { item ->
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
        val total = listaItemsOrden.sumOf { it.subtotal }

        val correoUsuario = usuario?.correo
            ?: FirebaseAuth.getInstance().currentUser?.email

        if (correoUsuario.isNullOrEmpty()) {
            Toast.makeText(this, "No se pudo determinar el usuario", Toast.LENGTH_LONG).show()
            return
        }

        val orden = Orden(
            idCompra = idCompra,
            fecha = System.currentTimeMillis(),
            usuarioCorreo = correoUsuario,
            items = listaItemsOrden,
            total = total
        )

        val resumen = listaItemsOrden.joinToString("\n") { item ->
            val nombre = item.nombreCorto ?: item.nombre ?: "Producto"
            "${item.cantidad} x $nombre  -  $${String.format("%.2f", item.subtotal)}"
        }

        // 4) Transacción: validar y RESTAR stock global para TODOS los seleccionados
        db.runTransaction { tx ->
            for (item in seleccionados) {
                val prod = item.producto
                val idDoc = prod.idDocumento!!

                val docRef = db.collection("bbh_productos").document(idDoc)
                val snap = tx.get(docRef)
                val stockActual = snap.getLong("stock") ?: 0L
                val cant = item.cantidad

                if (stockActual < cant) {
                    val nombreProd = prod.nombreCorto ?: prod.nombre ?: "Producto"
                    throw Exception("STOCK:$nombreProd")
                }

                tx.update(docRef, "stock", stockActual - cant)
            }
            null
        }.addOnSuccessListener {
            // 5) Si la transacción fue bien → guardar la orden
            db.collection("bbh_ordenes")
                .document(orden.idCompra)
                .set(orden)
                .addOnSuccessListener {
                    // Eliminar del carrito solo los productos seleccionados
                    CarritoManager.eliminarItems(seleccionados)
                    adaptador.refrescarDatos()
                    actualizarResumen()

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
        }.addOnFailureListener { e ->
            val msg = e.message ?: ""
            if (msg.startsWith("STOCK:")) {
                val nombreProd = msg.removePrefix("STOCK:")
                Toast.makeText(
                    this,
                    "No hay stock suficiente para $nombreProd, revisa tu carrito.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Error al actualizar stock: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun actualizarResumen() {
        val totalItems = CarritoManager.obtenerCantidadTotal()
        val totalImporte = CarritoManager.obtenerImporteTotal()
        txtTotalItems.text = "Artículos: $totalItems"
        txtTotalImporte.text = "Total: $${String.format("%.2f", totalImporte)}"
    }
}
