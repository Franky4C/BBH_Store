package mx.tecnm.cdhidalgo.tiendaregalos

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CompraConfirmadaActivity : AppCompatActivity() {

    private lateinit var txtIdCompra: TextView
    private lateinit var txtTotal: TextView
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

        txtIdCompra = findViewById(R.id.txt_id_compra)
        txtTotal = findViewById(R.id.txt_total_compra)
        btnVolverTienda = findViewById(R.id.btn_volver_tienda)

        val idCompra = intent.getStringExtra("orden_id") ?: "SIN-ID"
        val total = intent.getDoubleExtra("orden_total", 0.0)

        txtIdCompra.text = idCompra
        txtTotal.text = "$${String.format("%.2f", total)}"

        btnVolverTienda.setOnClickListener {
            finish() // por ahora solo cerramos (regresa a donde vengas)
        }
    }
}
