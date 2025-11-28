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
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
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
    private lateinit var imgQr: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_compra_confirmada)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtIdCompra     = findViewById(R.id.txt_id_compra)
        txtTotal        = findViewById(R.id.txt_total_compra)
        txtCorreo       = findViewById(R.id.txt_correo_compra)
        txtFecha        = findViewById(R.id.txt_fecha_compra)
        txtResumen      = findViewById(R.id.txt_resumen_compra)
        btnVolverTienda = findViewById(R.id.btn_volver_tienda)
        imgQr           = findViewById(R.id.img_qr_compra)


        // Datos del Intent
        val idCompra    = intent.getStringExtra("orden_id") ?: "SIN-ID"
        val total       = intent.getDoubleExtra("orden_total", 0.0)
        val correo      = intent.getStringExtra("orden_correo") ?: "Desconocido"
        val fechaMillis = intent.getLongExtra("orden_fecha", 0L)
        val resumen     = intent.getStringExtra("orden_resumen") ?: "Sin detalle."

        val fechaFormateada = if (fechaMillis > 0L) {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date(fechaMillis))
        } else {
            "-"
        }

        // Mostrar datos en pantalla
        txtIdCompra.text   = idCompra
        txtTotal.text      = "$" + String.format("%.2f", total)
        txtCorreo.text     = "Comprador: $correo"
        txtFecha.text      = "Fecha: $fechaFormateada"
        txtResumen.text    = resumen


        // 🔥 Construir contenido completo para el QR
        val contenidoQR = buildString {
            append("🧾 ORDEN DE COMPRA\n")
            append("ID: $idCompra\n")
            append("Cliente: $correo\n")
            append("Fecha: $fechaFormateada\n")
            append("--------------------------\n")
            append("Artículos:\n")
            resumen.split("\n").forEach {
                append("- $it\n")
            }
            append("--------------------------\n")
            append("TOTAL: $${String.format("%.2f", total)}\n")
        }

        generarQrParaCompra(contenidoQR)


        btnVolverTienda.setOnClickListener {
            finish()
        }
    }


    // GENERAR QR CON TODOS LOS DETALLES
    private fun generarQrParaCompra(texto: String) {
        try {
            val size = 900

            // Para soportar UTF-8, emojis y texto extenso
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )

            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                texto,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    bitmap.setPixel(
                        x, y,
                        if (bitMatrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                    )
                }
            }

            imgQr.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
