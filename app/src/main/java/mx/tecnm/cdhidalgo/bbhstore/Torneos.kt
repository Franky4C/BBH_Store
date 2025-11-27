package mx.tecnm.cdhidalgo.bbhstore // O el nombre de tu paquete

// 1. IMPORTACIONES AÑADIDAS
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class Torneos : AppCompatActivity() {

    // 2. DECLARACIÓN DEL BOTÓN DE IDIOMA
    private lateinit var btnChangeLanguag: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 3. CARGA EL IDIOMA GUARDADO ANTES DE MOSTRAR LA VISTA
        loadLocale()

        setContentView(R.layout.activity_torneos)

        // Tu código de inicialización existente
        val btnRegresar: ImageButton = findViewById(R.id.btn_regresar_torneos)
        btnRegresar.setOnClickListener {
            finish() // Cierra esta actividad y vuelve a la anterior
        }

        // 4. INICIALIZA EL BOTÓN DE IDIOMA Y ASÍGNALE SU FUNCIÓN
        btnChangeLanguag = findViewById(R.id.btnChangeLanguag)

        btnChangeLanguag.setOnClickListener {
            showChangeLanguageDialog()
        }
    }

    // 5. AÑADE LAS TRES FUNCIONES DE IDIOMA
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

        // Recarga esta actividad para aplicar los cambios de idioma
        val intent = Intent(this, Torneos::class.java)
        // Si 'Torneos' necesita datos de la actividad anterior (como el usuario),
        // pásalos aquí de nuevo. Ejemplo:
        // intent.putExtra("usuario", getIntent().getParcelableExtra("usuario"))
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
