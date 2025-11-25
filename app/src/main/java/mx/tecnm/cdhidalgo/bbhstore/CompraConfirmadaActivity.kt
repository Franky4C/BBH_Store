package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
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
    private lateinit var imgQr: ImageView      // NUEVO


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
        imgQr         = findViewById(R.id.img_qr_compra)   // NUEVO


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

        generarQrParaCompra(idCompra)

        // Volver a la tienda
        btnVolverTienda.setOnClickListener {
            finish()   // regresar a la Tienda que ya estaba abierta
        }

    }

    // NUEVO: función para generar el QR
    private fun generarQrParaCompra(texto: String) {
        try {
            val size = 512  // tamaño del QR en píxeles
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                texto,
                BarcodeFormat.QR_CODE,
                size,
                size,
                null
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) {
                        0xFF000000.toInt() // negro
                    } else {
                        0xFFFFFFFF.toInt() // blanco
                    }
                }
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)

            imgQr.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            // si hay error, simplemente dejamos el ImageView vacío
        }
    }
}
