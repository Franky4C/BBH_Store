package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Orden

class AdminDashboardActivity : AppCompatActivity() {

    private val db = Firebase.firestore

    private lateinit var btnRegresar: ImageButton
    private lateinit var btnIrAdminProductos: Button
    private lateinit var btnIrAdminUsuarios: Button

    private lateinit var txtSinDatos: TextView

    private lateinit var txtTop1Nombre: TextView
    private lateinit var txtTop1Cantidad: TextView
    private lateinit var txtTop1Total: TextView

    private lateinit var txtTop2Nombre: TextView
    private lateinit var txtTop2Cantidad: TextView
    private lateinit var txtTop2Total: TextView

    private lateinit var cardTop1: MaterialCardView
    private lateinit var cardTop2: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnRegresar = findViewById(R.id.btn_regresar_admin)
        btnIrAdminProductos = findViewById(R.id.btn_ir_admin_productos)
        btnIrAdminUsuarios = findViewById(R.id.btn_ir_admin_usuarios)

        txtSinDatos = findViewById(R.id.txt_sin_datos_admin)

        txtTop1Nombre = findViewById(R.id.txt_top1_nombre)
        txtTop1Cantidad = findViewById(R.id.txt_top1_cantidad)
        txtTop1Total = findViewById(R.id.txt_top1_total)

        txtTop2Nombre = findViewById(R.id.txt_top2_nombre)
        txtTop2Cantidad = findViewById(R.id.txt_top2_cantidad)
        txtTop2Total = findViewById(R.id.txt_top2_total)

        cardTop1 = findViewById(R.id.card_top1)
        cardTop2 = findViewById(R.id.card_top2)

        cardTop1.visibility = View.GONE
        cardTop2.visibility = View.GONE

        btnRegresar.setOnClickListener { finish() }

        btnIrAdminProductos.setOnClickListener {
            startActivity(Intent(this, AdminProductosActivity::class.java))
        }

        btnIrAdminUsuarios.setOnClickListener {
            startActivity(Intent(this, AdminUsuariosActivity::class.java))
        }

        cargarTopProductos()
    }

    private fun cargarTopProductos() {
        db.collection("bbh_ordenes")
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.isEmpty) {
                    txtSinDatos.visibility = View.VISIBLE
                    cardTop1.visibility = View.GONE
                    cardTop2.visibility = View.GONE
                    return@addOnSuccessListener
                }

                val listaOrdenes = snapshot.toObjects(Orden::class.java)

                val mapaVentas = mutableMapOf<String, Pair<Int, Double>>()

                for (orden in listaOrdenes) {
                    for (item in orden.items) {
                        val nombre = item.nombreCorto ?: item.nombre ?: "Producto"
                        val cantidad = item.cantidad
                        val subtotal = item.subtotal

                        val actual = mapaVentas[nombre] ?: Pair(0, 0.0)
                        mapaVentas[nombre] = Pair(
                            actual.first + cantidad,
                            actual.second + subtotal
                        )
                    }
                }

                if (mapaVentas.isEmpty()) {
                    txtSinDatos.visibility = View.VISIBLE
                    cardTop1.visibility = View.GONE
                    cardTop2.visibility = View.GONE
                    return@addOnSuccessListener
                }

                txtSinDatos.visibility = View.GONE
                cardTop1.visibility = View.VISIBLE
                cardTop2.visibility = View.VISIBLE

                val topProductos = mapaVentas.entries
                    .sortedByDescending { it.value.first }
                    .take(2)

                val top1 = topProductos.getOrNull(0)
                if (top1 != null) {
                    val (cant, totalVendido) = top1.value
                    txtTop1Nombre.text = top1.key
                    txtTop1Cantidad.text = "Unidades vendidas: $cant"
                    txtTop1Total.text = "Total vendido: $" + String.format("%.2f", totalVendido)
                } else {
                    txtTop1Nombre.text = "Sin datos"
                    txtTop1Cantidad.text = ""
                    txtTop1Total.text = ""
                }

                val top2 = topProductos.getOrNull(1)
                if (top2 != null) {
                    val (cant2, totalVendido2) = top2.value
                    txtTop2Nombre.text = top2.key
                    txtTop2Cantidad.text = "Unidades vendidas: $cant2"
                    txtTop2Total.text = "Total vendido: $" + String.format("%.2f", totalVendido2)
                } else {
                    txtTop2Nombre.text = "Sin datos"
                    txtTop2Cantidad.text = ""
                    txtTop2Total.text = ""
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar ventas: ${e.message}", Toast.LENGTH_LONG).show()
                txtSinDatos.visibility = View.VISIBLE
                cardTop1.visibility = View.GONE
                cardTop2.visibility = View.GONE
            }
    }
}
