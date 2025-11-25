package mx.tecnm.cdhidalgo.tiendaregalos

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
import mx.tecnm.cdhidalgo.tiendaregalos.adaptadores.AdaptadorCarrito
import mx.tecnm.cdhidalgo.tiendaregalos.dataclass.CarritoManager
import mx.tecnm.cdhidalgo.tiendaregalos.dataclass.Orden

class CarritoActivity : AppCompatActivity() {

    private lateinit var rvCarrito: RecyclerView
    private lateinit var txtTotalItems: TextView
    private lateinit var txtTotalImporte: TextView
    private lateinit var btnVaciar: Button
    private lateinit var btnProceder: Button
    private lateinit var btnRegresar: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_carrito)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
            if (CarritoManager.obtenerCantidadTotal() == 0) {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            } else {
                // 1. Generar ID de compra (simple por ahora)
                val idCompra = "ORD-" + System.currentTimeMillis().toString()

                // 2. Crear la orden
                val orden = Orden(
                    idCompra = idCompra,
                    fecha = System.currentTimeMillis(),
                    usuarioCorreo = null, // si tienes el usuario puedes pasar correo
                    items = CarritoManager.obtenerItems().toList(),
                    total = CarritoManager.obtenerImporteTotal()
                )

                // 3. Limpiar carrito
                CarritoManager.vaciarCarrito()

                // 4. Ir a pantalla de confirmación
                val intent = Intent(this, CompraConfirmadaActivity::class.java)
                intent.putExtra("orden_id", orden.idCompra)
                intent.putExtra("orden_total", orden.total)
                startActivity(intent)
                finish()
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
