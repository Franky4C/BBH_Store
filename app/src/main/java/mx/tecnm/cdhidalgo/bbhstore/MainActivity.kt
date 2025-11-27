package mx.tecnm.cdhidalgo.bbhstore

// 1. AÑADE ESTAS IMPORTACIONES NECESARIAS
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import mx.tecnm.cdhidalgo.bbhstore.dataclass.CarritoManager
import mx.tecnm.cdhidalgo.bbhstore.dataclass.FavoritosManager
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Usuario
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var usuario: Usuario

    // 2. DECLARA EL NUEVO BOTÓN DE IDIOMA
    private lateinit var btnChangeLanguage: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 3. CARGA EL IDIOMA GUARDADO ANTES DE MOSTRAR LA PANTALLA
        loadLocale()

        setContentView(R.layout.activity_main)

        // Este bloque de 'WindowInsetsListener' a veces causa problemas de layout si no se usa a fondo.
        // Si notas que tus botones o vistas se mueven a lugares extraños, puedes comentar o eliminar este bloque.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- TU CÓDIGO EXISTENTE (SIN CAMBIOS) ---
        if (intent.hasExtra("usuario")) {
            usuario = intent.getParcelableExtra("usuario")!!
        } else {
            Toast.makeText(this, "Error: No se recibieron los datos del usuario.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        CarritoManager.configurarUsuario(usuario.correo) { _, _ -> }
        FavoritosManager.configurarUsuario(usuario.correo) { _, _ -> }

        val btnTienda: ImageButton = findViewById(R.id.btnTienda)
        val btnPlanes: ImageButton = findViewById(R.id.btnPlanes)
        val btnTorneos: ImageButton = findViewById(R.id.btnTorneo)
        val btnPanelAdmin: MaterialButton = findViewById(R.id.btnPanelAdmin)
        val btnCerrarSesion: MaterialButton = findViewById(R.id.btnCerrarSesion)

        // 4. INICIALIZA EL BOTÓN DE IDIOMA Y ASÍGNALE SU FUNCIÓN
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)
        btnChangeLanguage.setOnClickListener {
            showChangeLanguageDialog()
        }

        // --- TU CÓDIGO EXISTENTE (SIN CAMBIOS) ---
        btnTienda.setOnClickListener {
            val intent = Intent(this, Tienda::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        btnPlanes.setOnClickListener {
            val intent = Intent(this, Planes::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        btnTorneos.setOnClickListener {
            val intent = Intent(this, Torneos::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        if (usuario.rol == "admin") {
            btnPanelAdmin.visibility = View.VISIBLE
        } else {
            btnPanelAdmin.visibility = View.GONE
        }

        btnPanelAdmin.setOnClickListener {
            val intent = Intent(this, AdminDashboardActivity::class.java)
            startActivity(intent)
        }

        btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            CarritoManager.limpiarSesion()
            FavoritosManager.limpiarSesion()

            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // 5. AÑADE LAS TRES FUNCIONES DE IDIOMA (CASI IDÉNTICAS A LAS DE LOGIN.KT)
    private fun showChangeLanguageDialog() {
        val languages = arrayOf("Español", "English")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccionar Idioma / Select Language")
        builder.setSingleChoiceItems(languages, -1) { dialog, which ->
            val langCode = if (which == 0) "es" else "en"
            setLocale(langCode)
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun setLocale(langCode: String) {
        // Guarda la preferencia de idioma para que la app la recuerde
        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("My_Lang", langCode)
        editor.apply()

        // Recarga la actividad para que se apliquen los cambios
        val intent = Intent(this, MainActivity::class.java)

        // ¡¡MUY IMPORTANTE!! Volvemos a pasar el objeto 'usuario' a la actividad
        // recargada para no perder la sesión del usuario actual.
        intent.putExtra("usuario", usuario)

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun loadLocale() {
        // Carga la preferencia de idioma que guardamos antes
        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val language = prefs.getString("My_Lang", "") ?: ""

        if (language.isNotEmpty()) {
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = Configuration()
            config.setLocale(locale)
            baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        }
    }
}
