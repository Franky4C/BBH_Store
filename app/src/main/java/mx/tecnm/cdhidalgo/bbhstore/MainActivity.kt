package mx.tecnm.cdhidalgo.bbhstore

// 1. IMPORTACIONES (sin cambios)
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
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

    // 2. DECLARACIÓN DE TODOS LOS BOTONES DE LA CLASE
    private lateinit var btnChangeLanguage: ImageButton
    private lateinit var btnTienda: ImageButton
    private lateinit var btnPlanes: ImageButton
    private lateinit var btnTorneos: ImageButton
    private lateinit var btnPanelAdmin: MaterialButton
    private lateinit var btnCerrarSesion: MaterialButton

    // BOTONES DE REDES SOCIALES
    private lateinit var btnInstagram: ImageButton
    private lateinit var btnFacebook: ImageButton
    private lateinit var btnWhatsapp: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 3. CARGA EL IDIOMA GUARDADO ANTES DE MOSTRAR LA PANTALLA
        loadLocale()

        setContentView(R.layout.activity_main)

        // Listener para los márgenes del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- VERIFICACIÓN DE USUARIO (SIN CAMBIOS) ---
        if (intent.hasExtra("usuario")) {
            usuario = intent.getParcelableExtra("usuario")!!
        } else {
            Toast.makeText(this, "Error: No se recibieron los datos del usuario.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // CONFIGURACIÓN DE GESTORES (SIN CAMBIOS)
        CarritoManager.configurarUsuario(usuario.correo) { _, _ -> }
        FavoritosManager.configurarUsuario(usuario.correo) { _, _ -> }

        // --- INICIALIZACIÓN DE TODAS LAS VISTAS ---
        btnTienda = findViewById(R.id.btnTienda)
        btnPlanes = findViewById(R.id.btnPlanes)
        btnTorneos = findViewById(R.id.btnTorneo)
        btnPanelAdmin = findViewById(R.id.btnPanelAdmin)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)

        // INICIALIZACIÓN DE BOTONES DE REDES SOCIALES
        btnInstagram = findViewById(R.id.iconInstagram)
        btnFacebook = findViewById(R.id.iconFacebook)
        btnWhatsapp = findViewById(R.id.iconWhatsapp)


        // --- CONFIGURACIÓN DE LISTENERS ---

        // Listener de idioma
        btnChangeLanguage.setOnClickListener {
            showChangeLanguageDialog()
        }

        // Listeners de navegación principal
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

        // Listeners de redes sociales (MOVIDOS AQUÍ DENTRO DE ONCREATE)
        btnInstagram.setOnClickListener {
            // URL actualizada para que no de error de contenido no disponible
            val url = "https://www.instagram.com/barracudabbh/"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        btnFacebook.setOnClickListener {
            val url = "https://www.facebook.com/barracudaboxing/"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        btnWhatsapp.setOnClickListener {
            val numeroWhatsapp = "527866880131"
            val url = "https://api.whatsapp.com/send?phone=$numeroWhatsapp"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        // Lógica de visibilidad del panel de admin
        if (usuario.rol == "admin") {
            btnPanelAdmin.visibility = View.VISIBLE
        } else {
            btnPanelAdmin.visibility = View.GONE
        }

        btnPanelAdmin.setOnClickListener {
            val intent = Intent(this, AdminDashboardActivity::class.java)
            startActivity(intent)
        }

        // Listener para cerrar sesión
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

    // --- FUNCIONES DE IDIOMA (SIN CAMBIOS) ---
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
        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("My_Lang", langCode)
        editor.apply()

        // Recarga la actividad para que se apliquen los cambios
        val intent = Intent(this, MainActivity::class.java)
        // Volvemos a pasar el objeto 'usuario' para no perder la sesión
        intent.putExtra("usuario", usuario)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun loadLocale() {
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
