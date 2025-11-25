package mx.tecnm.cdhidalgo.tiendaregalos

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
// 1. IMPORTA LA CLASE 'Usuario' PARA QUE LA ACTIVIDAD SEPA QUÉ ES
import mx.tecnm.cdhidalgo.tiendaregalos.dataclass.Usuario

class MainActivity : AppCompatActivity() {

    // 2. DECLARA UNA VARIABLE A NIVEL DE CLASE PARA GUARDAR EL OBJETO 'usuario'
    // 'lateinit' significa que prometemos inicializarla antes de usarla.
    private lateinit var usuario: Usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Este bloque es para que la UI se vea bien de borde a borde.
        // Asegúrate de tener un id "main" en el layout raíz de activity_main.xml
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 3. RECIBE EL OBJETO 'usuario' QUE VIENE DESDE Login.kt
        // Esta es la parte más importante que faltaba.
        // Usamos un 'if' para seguridad y evitar crashes.
        if (intent.hasExtra("usuario")) {
            // Si el Intent contiene un extra llamado "usuario", lo extraemos y lo guardamos
            // en nuestra variable de clase. El '!!' es seguro aquí por el 'if'.
            usuario = intent.getParcelableExtra("usuario")!!
        } else {
            // Si por alguna razón no se recibieron los datos, mostramos un error y cerramos.
            Toast.makeText(this, "Error: No se recibieron los datos del usuario.", Toast.LENGTH_LONG).show()
            finish()
            // 'return' detiene la ejecución del resto del 'onCreate'.
            return
        }

        // --- El resto de tu código que ya tenías ---

        // Enlazar los botones del layout
        val btnTienda: ImageButton = findViewById(R.id.btnTienda)
        val btnPlanes: ImageButton = findViewById(R.id.btnPlanes)
        val btnTorneos: ImageButton = findViewById(R.id.btnTorneo)

        // Acción para el botón de Tienda
        btnTienda.setOnClickListener {
            val intent = Intent(this, Tienda::class.java)
            // 4. AHORA 'usuario' SÍ TIENE VALOR Y PODEMOS PASARLO A Tienda.kt
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        // Acciones para los otros botones (funcionalidad futura)
        btnPlanes.setOnClickListener {
            Toast.makeText(this, "La sección de Planes estará disponible próximamente.", Toast.LENGTH_SHORT).show()
        }

        btnTorneos.setOnClickListener {
            Toast.makeText(this, "La sección de Torneos estará disponible próximamente.", Toast.LENGTH_SHORT).show()
        }

    }
}
