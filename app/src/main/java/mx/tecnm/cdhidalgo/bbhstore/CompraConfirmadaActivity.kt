package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CompraConfirmadaActivity : AppCompatActivity() {

    private lateinit var txtIdCompra: TextView
    private lateinit var txtTotal: TextView
    private lateinit var txtCorreo: TextView
    private lateinit var txtFecha: TextView
    private lateinit var txtResumen: TextView
    private lateinit var btnVolverTienda: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_compra_confirmada)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias UI
        txtIdCompra   = findViewById(R.id.txt_id_compra)
        txtTotal      = findViewById(R.id.txt_total_compra)
        txtCorreo     = findViewById(R.id.txt_correo_compra)
        txtFecha      = findViewById(R.id.txt_fecha_compra)
        txtResumen    = findViewById(R.id.txt_resumen_compra)
        btnVolverTienda = findViewById(R.id.btn_volver_tienda)

        // Datos que vienen del Intent
        val idCompra    = intent.getStringExtra("orden_id") ?: "SIN-ID"
        val total       = intent.getDoubleExtra("orden_total", 0.0)
        val correo      = intent.getStringExtra("orden_correo") ?: "Desconocido"
        val fechaMillis = intent.getLongExtra("orden_fecha", 0L)
        val resumen     = intent.getStringExtra("orden_resumen") ?: "Sin detalle de productos."

        // Mostrar datos
        txtIdCompra.text = idCompra
        txtTotal.text = "$" + String.format("%.2f", total)
        txtCorreo.text = "Comprador: $correo"

        if (fechaMillis > 0L) {
            val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            txtFecha.text = "Fecha: ${formato.format(Date(fechaMillis))}"
        } else {
            txtFecha.text = "Fecha: -"
        }

        txtResumen.text = resumen

        // Volver a la tienda
        btnVolverTienda.setOnClickListener {
            finish()   // regresar a la Tienda que ya estaba abierta
        }

    }
}
