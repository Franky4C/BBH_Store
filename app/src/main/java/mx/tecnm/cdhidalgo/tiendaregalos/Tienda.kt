package mx.tecnm.cdhidalgo.tiendaregalos

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import mx.tecnm.cdhidalgo.tiendaregalos.adaptadores.AdaptadorArtesania
import mx.tecnm.cdhidalgo.tiendaregalos.dataclass.Producto
import mx.tecnm.cdhidalgo.tiendaregalos.dataclass.Usuario

class Tienda : AppCompatActivity() {

    private lateinit var btnRegresar: ImageButton
    private lateinit var texto: TextView
    private lateinit var usuario: Usuario

    private lateinit var rvArtesanias: RecyclerView
    private lateinit var listaArtesanias: ArrayList<Producto>
    private lateinit var adaptadorArtesania: AdaptadorArtesania

    // No necesitas 'auth' en esta actividad si solo regresas al menú
    // private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() suele ser para layouts más complejos, puedes omitirlo si no lo usas
        setContentView(R.layout.activity_tienda)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- INICIALIZACIÓN DE VISTAS Y DATOS ---

        // Ya no se necesita 'auth = FirebaseAuth.getInstance()' aquí

        // Recibir el objeto usuario de la actividad anterior es CRUCIAL
        if (intent.hasExtra("usuario")) {
            usuario = intent.getParcelableExtra("usuario")!!
        } else {
            // Si no se recibe, es un error grave. Salimos para evitar un crash.
            // Puedes mostrar un Toast si quieres.
            finish()
            return // Detiene la ejecución de onCreate
        }

        texto = findViewById(R.id.usuario_tienda)
        btnRegresar = findViewById(R.id.btn_cerrar_sesion_tienda) // Renombrado para claridad
        rvArtesanias = findViewById(R.id.artesanias_tienda)

        // --- CONFIGURACIÓN DE LA UI ---
        val nombreCompleto = "${usuario.nombre} ${usuario.apaterno} ${usuario.amaterno}"
        texto.text = nombreCompleto

        // --- LÓGICA DEL BOTÓN "REGRESAR" (antes Cerrar Sesión) ---
        btnRegresar.setOnClickListener {
            // La forma CORRECTA de volver a la actividad anterior.
            // Simplemente cierra la actividad actual (`Tienda`). Android se encarga
            // de mostrar la actividad que estaba antes en la pila (`MainActivity`).
            finish()
        }

        // --- MANEJO DEL BOTÓN "ATRÁS" DEL SISTEMA PARA EVITAR BUCLES ---
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Al igual que el botón de la UI, solo cerramos la actividad.
                finish()
            }
        })

        // --- CARGA DE DATOS PARA EL RECYCLERVIEW ---
        cargarDatosDeEjemplo()

        // --- CONFIGURACIÓN DEL RECYCLERVIEW ---
        rvArtesanias.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvArtesanias.setHasFixedSize(true)
        adaptadorArtesania = AdaptadorArtesania(listaArtesanias)
        rvArtesanias.adapter = adaptadorArtesania

        // Navegación al hacer clic en un producto
        adaptadorArtesania.onProductoClick = { producto ->
            val intent = Intent(this, DetalleProducto::class.java)
            intent.putExtra("usuario", usuario)
            intent.putExtra("producto", producto)
            startActivity(intent)
            // NO llamamos a finish() aquí, para poder volver desde DetalleProducto a Tienda
        }
    }

    private fun cargarDatosDeEjemplo() {
        listaArtesanias = ArrayList()
        listaArtesanias.add(Producto(
            R.drawable.artesania1,
            "Xico Hoja",
            "Xico-Talavera Hoja",
            3309.00,
            "XICO es un personaje que busca generar un cambio positivo " +
                    "a través del arte y la cultura. Respalda el talento " +
                    "emergente y provee una plataforma comercial de impulso " +
                    "creativo.",
            "artesania"
        ))
        listaArtesanias.add(Producto(
            R.drawable.artesania2,
            "Xico Rojo",
            "Xico-Piel de Alebrije Rojo",
            2959.00,
            "XICO es un personaje que busca generar un cambio positivo" +
                    " a través del arte y la cultura. Respalda el talento emergente" +
                    " y provee una plataforma comercial de impulso creativo.",
            "artesania"
        ))
    }
}
