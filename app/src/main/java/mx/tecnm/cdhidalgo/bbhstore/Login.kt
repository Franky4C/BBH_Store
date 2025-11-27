package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.content.res.Configuration // 1. IMPORTACIÓN NECESARIA
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Usuario
import java.util.Locale // 2. IMPORTACIÓN NECESARIA

class Login : AppCompatActivity() {

    private lateinit var correo: TextInputLayout
    private lateinit var password: TextInputLayout
    private lateinit var btn_ingresar: MaterialButton
    private lateinit var btn_registrar: MaterialButton
    private lateinit var btn_olvidar: MaterialButton

    private lateinit var btnInstagram: ImageButton
    private lateinit var btnFacebook: ImageButton
    private lateinit var btnWhatsapp: ImageButton

    // 3. NUEVA DECLARACIÓN PARA EL BOTÓN DE IDIOMA
    private lateinit var btnChangeLanguage: ImageButton

    private lateinit var auth: FirebaseAuth
    private var usuario: Usuario = Usuario()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 4. INVOCAR LA CARGA DE IDIOMA ANTES DE MOSTRAR LA VISTA
        loadLocale()

        // enableEdgeToEdge() // Se recomienda quitar 'enableEdgeToEdge' y el listener de insets si no lo configuras a fondo,
        // ya que puede interferir con el layout. Si ves problemas de espaciado, elimínalos.
        setContentView(R.layout.activity_login)
        /* ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/

        auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

        correo = findViewById(R.id.correo_login)
        password = findViewById(R.id.password_login)
        btn_ingresar = findViewById(R.id.btn_login)
        btn_registrar = findViewById(R.id.btn_registrar)
        btn_olvidar = findViewById(R.id.btn_olvidar)

        btnInstagram = findViewById(R.id.btn_instagram)
        btnFacebook = findViewById(R.id.btn_facebook)
        btnWhatsapp = findViewById(R.id.btn_whatsapp)

        // 5. INICIALIZAR Y CONFIGURAR EL BOTÓN DE IDIOMA
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage)
        btnChangeLanguage.setOnClickListener {
            showChangeLanguageDialog()
        }

        // --- TU CÓDIGO EXISTENTE DE LOGIN, REGISTRO Y REDES SOCIALES (NO SE CAMBIA NADA) ---
        // LOGIN
        btn_ingresar.setOnClickListener {
            val email = correo.editText?.text.toString().trim()
            val pass = password.editText?.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        db.collection("bbh_usuarios")
                            .whereEqualTo("correo", email)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (documents.isEmpty) {
                                    FirebaseAuth.getInstance().signOut()
                                    Toast.makeText(
                                        this,
                                        "Usuario no encontrado en Firestore",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@addOnSuccessListener
                                }

                                val doc = documents.first()
                                usuario = Usuario(
                                    nombre = doc.getString("nombre"),
                                    apaterno = doc.getString("apaterno"),
                                    amaterno = doc.getString("amaterno"),
                                    correo = doc.getString("correo"),
                                    telefono = doc.getString("telefono"),
                                    rol = doc.getString("rol") ?: "cliente",
                                    bloqueado = doc.getBoolean("bloqueado") ?: false
                                )

                                if (usuario.bloqueado) {
                                    Toast.makeText(
                                        this,
                                        "Tu cuenta está bloqueada. Contacta al administrador.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    FirebaseAuth.getInstance().signOut()
                                    return@addOnSuccessListener
                                }
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("usuario", usuario)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                FirebaseAuth.getInstance().signOut()
                                Toast.makeText(
                                    this,
                                    "Error al leer datos del usuario: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    } else {
                        showAlert()
                    }
                }
        }

        // REGISTRO
        btn_registrar.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }

        // OLVIDÉ MI CONTRASEÑA
        btn_olvidar.setOnClickListener {
            val intent = Intent(this, RecuperaPassword::class.java)
            startActivity(intent)
        }

        // REDES SOCIALES
        btnInstagram.setOnClickListener {
            val url = "https://www.instagram.com/barracudabbh/reel/DBjZ3aFurbe/"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        btnFacebook.setOnClickListener {
            val url = "https://www.facebook.com/barracudaboxing/videos/1452571902113045/"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        btnWhatsapp.setOnClickListener {
            val numeroWhatsapp = "527866880131"
            val url = "https://api.whatsapp.com/send?phone=$numeroWhatsapp"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // 6. NUEVAS FUNCIONES PARA MANEJAR EL CAMBIO DE IDIOMA
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

        // Recarga la actividad para que se apliquen los cambios de idioma
        val intent = Intent(this, Login::class.java)
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
