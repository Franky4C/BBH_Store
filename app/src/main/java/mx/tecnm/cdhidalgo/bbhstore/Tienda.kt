package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mx.tecnm.cdhidalgo.bbhstore.adaptadores.AdaptadorArtesania
import mx.tecnm.cdhidalgo.bbhstore.dataclass.CarritoManager
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Producto
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Usuario

class Tienda : AppCompatActivity() {

    private lateinit var btnRegresar: ImageButton
    private lateinit var btnCarrito: ImageButton
    private lateinit var btn_favoritos: ImageButton
    private lateinit var btnHistorial: ImageButton
    private lateinit var texto: TextView
    private lateinit var contadorCarrito: TextView
    private lateinit var usuario: Usuario

    private lateinit var rvArtesanias: RecyclerView
    private lateinit var listaArtesanias: ArrayList<Producto>
    private lateinit var adaptadorArtesania: AdaptadorArtesania

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tienda)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recibir usuario
        if (intent.hasExtra("usuario")) {
            usuario = intent.getParcelableExtra("usuario")!!
        } else {
            finish()
            return
        }

        texto = findViewById(R.id.usuario_tienda)
        btnRegresar = findViewById(R.id.btn_cerrar_sesion_tienda)
        btnCarrito = findViewById(R.id.btn_carrito_tienda)
        contadorCarrito = findViewById(R.id.contador_carrito_tienda)
        rvArtesanias = findViewById(R.id.artesanias_tienda)
        btn_favoritos = findViewById(R.id.btn_favoritos_tienda)
        btnHistorial = findViewById(R.id.btn_historial_tienda)


        val nombreCompleto = "${usuario.nombre} ${usuario.apaterno} ${usuario.amaterno}"
        texto.text = nombreCompleto

        btnRegresar.setOnClickListener { finish() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        cargarDatosDeEjemplo()

        rvArtesanias.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvArtesanias.setHasFixedSize(true)
        adaptadorArtesania = AdaptadorArtesania(listaArtesanias)
        rvArtesanias.adapter = adaptadorArtesania

        // Click → Detalle
        adaptadorArtesania.onProductoClick = { producto ->
            val intent = Intent(this, DetalleProducto::class.java)
            intent.putExtra("usuario", usuario)
            intent.putExtra("producto", producto)
            startActivity(intent)
        }

        // Long click → agregar al carrito respetando stock
        adaptadorArtesania.onProductoLongClick = { producto ->
            val agregado = CarritoManager.agregarProducto(producto, 1)
            if (agregado) {
                Toast.makeText(this, "Agregado al carrito", Toast.LENGTH_SHORT).show()
                actualizarContadorCarrito()
            } else {
                Toast.makeText(this, "Sin stock suficiente", Toast.LENGTH_SHORT).show()
            }
        }

        // Abrir pantalla de carrito (la crearemos enseguida)
        btnCarrito.setOnClickListener {
            val intent = Intent(this, CarritoActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        actualizarContadorCarrito()

        btn_favoritos.setOnClickListener {
            val intent = Intent(this, FavoritosActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        actualizarContadorCarrito()

        btnHistorial.setOnClickListener {
            val intent = Intent(this, HistorialActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

    }



    private fun actualizarContadorCarrito() {
        val total = CarritoManager.obtenerCantidadTotal()
        contadorCarrito.text = total.toString()
    }


    private fun cargarDatosDeEjemplo() {
        listaArtesanias = ArrayList()
        listaArtesanias.add(
            Producto(
                R.drawable.artesania1,
                "Xico Hoja",
                "Xico-Talavera Hoja",
                3309.00,
                "XICO es un personaje que busca generar un cambio positivo a través del arte...",
                "artesania",
                stock = 10  // NUEVO
            )
        )
        listaArtesanias.add(
            Producto(
                R.drawable.artesania2,
                "Xico Rojo",
                "Xico-Piel de Alebrije Rojo",
                2959.00,
                "XICO es un personaje que busca generar un cambio positivo a través del arte...",
                "artesania",
                stock = 10   // NUEVO
            )
        )
    }

    override fun onResume() {
        super.onResume()
        // Cada vez que regreses a Tienda (desde Carrito o CompraConfirmada)
        // se recalcula el número del carrito
        actualizarContadorCarrito()
    }

}
