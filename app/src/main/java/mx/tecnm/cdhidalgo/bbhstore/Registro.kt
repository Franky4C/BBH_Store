package mx.tecnm.cdhidalgo.bbhstore

// 1. IMPORTACIONES AÑADIDAS
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class Registro : AppCompatActivity() {

    private lateinit var nombre: TextInputLayout
    private lateinit var apaterno: TextInputLayout
    private lateinit var amaterno: TextInputLayout
    private lateinit var correo: TextInputLayout
    private lateinit var password: TextInputLayout

    private lateinit var btn_registrar: Button
    private lateinit var btn_estoyregistrado: Button

    // 2. DECLARACIÓN DEL BOTÓN DE IDIOMA
    private lateinit var btnChangeLanguage: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 3. CARGA EL IDIOMA GUARDADO ANTES DE MOSTRAR LA VISTA
        loadLocale()

        // enableEdgeToEdge() // Se recomienda comentar si causa problemas de layout
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nombre = findViewById(R.id.campo_nombre)
        apaterno = findViewById(R.id.campo_apaterno)
        amaterno = findViewById(R.id.campo_amaterno)
        correo = findViewById(R.id.campo_correo)
        password = findViewById(R.id.campo_password)
        btn_registrar = findViewById(R.id.btn_registrar)
        btn_estoyregistrado = findViewById(R.id.btn_estoyregistrado)

        // 4. INICIALIZACIÓN Y LISTENER PARA EL BOTÓN DE IDIOMA
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)
        btnChangeLanguage.setOnClickListener {
            showChangeLanguageDialog()
        }

        btn_registrar.setOnClickListener {
            val intent = Intent(this, ConfirmarRegistro::class.java)
            intent.putExtra("nombre", nombre.editText?.text.toString())
            intent.putExtra("apaterno", apaterno.editText?.text.toString())
            intent.putExtra("amaterno", amaterno.editText?.text.toString())
            intent.putExtra("correo", correo.editText?.text.toString())
            intent.putExtra("password", password.editText?.text.toString())
            startActivity(intent)
        }

        btn_estoyregistrado.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish() // Es buena práctica cerrar Registro al volver a Login
        }
    }

    // 5. FUNCIONES PARA MANEJAR EL CAMBIO DE IDIOMA
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

        // Recarga esta misma actividad para aplicar los cambios de idioma
        val intent = Intent(this, Registro::class.java)
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
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        }
    }
}
