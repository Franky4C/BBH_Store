package mx.tecnm.cdhidalgo.bbhstore

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Usuario

// 1. Convierte la clase en una Activity que hereda de AppCompatActivity
class Planes : AppCompatActivity() {

    // Es buena práctica tener el objeto usuario, aunque no lo usemos ahora
    private lateinit var usuario: Usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 2. Vincula esta clase de Kotlin con su archivo de diseño XML
        setContentView(R.layout.activity_planes)

        // (Opcional pero recomendado) Configuración para que el layout ocupe toda la pantalla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recibimos los datos del usuario, aunque de momento no los mostremos.
        if (intent.hasExtra("usuario")) {
            usuario = intent.getParcelableExtra("usuario")!!
        } else {
            // Si por algún motivo no llegan los datos, cerramos para evitar errores.
            finish()
            return
        }

        // 3. LÓGICA DEL BOTÓN "REGRESAR"
        // Asegúrate de que tu botón en activity_planes.xml tenga el id "btn_regresar_planes"
        val btnRegresar: ImageButton = findViewById(R.id.btn_regresar_planes)
        btnRegresar.setOnClickListener {
            // La forma CORRECTA de "volver atrás": simplemente cierra la actividad actual.
            // Android se encarga de mostrar la pantalla anterior (MainActivity).
            finish()
        }

        // 4. MANEJO DEL BOTÓN "ATRÁS" DEL SISTEMA (Gesto o botón de navegación)
        // Esto asegura que el comportamiento sea consistente.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // También llamamos a finish() para cerrar la actividad.
                finish()
            }
        })
    }
}
