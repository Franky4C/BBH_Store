package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Usuario

class MainActivity : AppCompatActivity() {

    private lateinit var usuario: Usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recibir usuario desde Login
        if (intent.hasExtra("usuario")) {
            usuario = intent.getParcelableExtra("usuario")!!
        } else {
            Toast.makeText(this, "Error: No se recibieron los datos del usuario.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val btnTienda: ImageButton = findViewById(R.id.btnTienda)
        val btnPlanes: ImageButton = findViewById(R.id.btnPlanes)
        val btnTorneos: ImageButton = findViewById(R.id.btnTorneo)
        val btnPanelAdmin: MaterialButton = findViewById(R.id.btnPanelAdmin)
        val btnCerrarSesion: MaterialButton = findViewById(R.id.btnCerrarSesion)

        // Ir a Tienda
        btnTienda.setOnClickListener {
            val intent = Intent(this, Tienda::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        // Ir a Planes
        btnPlanes.setOnClickListener {
            val intent = Intent(this, Planes::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        // Ir a Torneos
        btnTorneos.setOnClickListener {
            val intent = Intent(this, Torneos::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        // Panel Admin (solo admins)
        if (usuario.rol == "admin") {
            btnPanelAdmin.visibility = View.VISIBLE
        } else {
            btnPanelAdmin.visibility = View.GONE
        }

        btnPanelAdmin.setOnClickListener {
            val intent = Intent(this, AdminDashboardActivity::class.java)
            startActivity(intent)
        }

        // Cerrar sesión
        btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
