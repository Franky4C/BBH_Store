package mx.tecnm.cdhidalgo.tiendaregalos

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
import mx.tecnm.cdhidalgo.tiendaregalos.dataclass.Producto
import mx.tecnm.cdhidalgo.tiendaregalos.dataclass.Usuario

class DetalleProducto : AppCompatActivity() {
    private lateinit var btnRegresar: ImageButton
    private lateinit var btnCarrito: ImageButton
    private lateinit var usuarioNombre: TextView
    private lateinit var imagenProducto: ImageView
    private lateinit var nombreProducto: TextView
    private lateinit var descripcionProducto: TextView
    private lateinit var precioProducto: TextView
    private lateinit var btnComprar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_producto)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnRegresar = findViewById(R.id.btn_regresar_detalle)
        btnCarrito = findViewById(R.id.btn_carrito_detalle)
        btnComprar = findViewById(R.id.btn_comprar_detalle)

        val usuario = intent.getParcelableExtra<Usuario>("usuario")
        val producto = intent.getParcelableExtra<Producto>("producto")

        usuarioNombre = findViewById(R.id.usuario_detalle)

        usuarioNombre.text = usuario?.nombre

        if(producto != null){
            imagenProducto = findViewById(R.id.imagen_detalle)
            nombreProducto = findViewById(R.id.nombre_detalle)
            descripcionProducto = findViewById(R.id.descripcion_detalle)
            precioProducto = findViewById(R.id.precio_detalle)

            imagenProducto.setImageResource(producto.imagen)
            nombreProducto.text = producto.nombre
            descripcionProducto.text = producto.descripcion
            precioProducto.text = producto.precio.toString()
        }

        btnRegresar.setOnClickListener {
            val intent = Intent(this, Tienda::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

    }
}