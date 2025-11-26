package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Usuario

class Login : AppCompatActivity() {

    private lateinit var correo: TextInputLayout
    private lateinit var password: TextInputLayout
    private lateinit var btn_ingresar: MaterialButton
    private lateinit var btn_registrar: MaterialButton
    private lateinit var btn_olvidar: MaterialButton

    private lateinit var btnInstagram: ImageButton
    private lateinit var btnFacebook: ImageButton
    private lateinit var btnWhatsapp: ImageButton

    private lateinit var auth: FirebaseAuth
    private var usuario: Usuario = Usuario()   // usa valores por defecto del data class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
                        // Leer datos del usuario en Firestore
                        db.collection("bbh_usuarios")   // OJO: usa el mismo nombre que en AdminUsuariosActivity
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

                                // Validar bloqueo
                                if (usuario.bloqueado) {
                                    Toast.makeText(
                                        this,
                                        "Tu cuenta está bloqueada. Contacta al administrador.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    FirebaseAuth.getInstance().signOut()
                                    return@addOnSuccessListener
                                }

                                // Si todo OK → ir a MainActivity
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

        // INSTAGRAM
        btnInstagram.setOnClickListener {
            val url = "https://www.instagram.com/barracudabbh/reel/DBjZ3aFurbe/"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        // FACEBOOK
        btnFacebook.setOnClickListener {
            val url = "https://www.facebook.com/barracudaboxing/videos/1452571902113045/"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        // WHATSAPP (número en formato internacional sin 'tel:')
        btnWhatsapp.setOnClickListener {
            val numeroWhatsapp = "527866880131" // 52 + número
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
}
