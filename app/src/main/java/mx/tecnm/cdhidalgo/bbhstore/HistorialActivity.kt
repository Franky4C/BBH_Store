package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import mx.tecnm.cdhidalgo.bbhstore.adaptadores.HistorialAdapter
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Orden
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Usuario

class HistorialActivity : AppCompatActivity() {

    private val db = Firebase.firestore

    private lateinit var btnRegresar: ImageButton
    private lateinit var rvHistorial: RecyclerView
    private lateinit var txtVacio: TextView

    private lateinit var adaptador: HistorialAdapter
    private var usuario: Usuario? = null
    private var correoUsuario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historial)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnRegresar = findViewById(R.id.btn_regresar_historial)
        rvHistorial = findViewById(R.id.rv_historial)
        txtVacio = findViewById(R.id.txt_historial_vacio)

        // Intentamos recuperar el usuario (si lo mandaste desde Tienda)
        usuario = intent.getParcelableExtra("usuario")

        // Correo: preferimos Usuario, si no, FirebaseAuth
        correoUsuario = usuario?.correo ?: FirebaseAuth.getInstance().currentUser?.email

        if (correoUsuario == null) {
            Toast.makeText(
                this,
                "No se pudo obtener el correo del usuario",
                Toast.LENGTH_LONG
            ).show()
        }

        rvHistorial.layoutManager = LinearLayoutManager(this)
        adaptador = HistorialAdapter(emptyList())
        rvHistorial.adapter = adaptador

        // Click en una orden → reutilizamos la pantalla de compra confirmada
        adaptador.onOrdenClick = { orden ->
            val resumen = orden.items.joinToString("\n") { item ->
                val nombre = item.nombreCorto ?: item.nombre ?: "Producto"
                "${item.cantidad} x $nombre  -  $${String.format("%.2f", item.subtotal)}"
            }

            val intent = Intent(this, CompraConfirmadaActivity::class.java)
            intent.putExtra("orden_id", orden.idCompra)
            intent.putExtra("orden_total", orden.total)
            intent.putExtra("orden_correo", orden.usuarioCorreo)
            intent.putExtra("orden_fecha", orden.fecha)
            intent.putExtra("orden_resumen", resumen)
            startActivity(intent)
        }

        btnRegresar.setOnClickListener { finish() }

        cargarHistorial()
    }

    private fun cargarHistorial() {
        val correo = correoUsuario
        if (correo == null) {
            txtVacio.visibility = View.VISIBLE
            txtVacio.text = "No se pudo determinar el usuario para mostrar historial."
            return
        }

        db.collection("bbh_ordenes")
            .whereEqualTo("usuarioCorreo", correo)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    txtVacio.visibility = View.VISIBLE
                    txtVacio.text = "Aún no tienes compras registradas."
                    adaptador.actualizarDatos(emptyList())
                    return@addOnSuccessListener
                }

                val lista = snapshot.toObjects(Orden::class.java)

                txtVacio.visibility = View.GONE
                adaptador.actualizarDatos(lista)
            }
            .addOnFailureListener { e ->
                txtVacio.visibility = View.VISIBLE
                txtVacio.text = "Error al cargar historial."
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
