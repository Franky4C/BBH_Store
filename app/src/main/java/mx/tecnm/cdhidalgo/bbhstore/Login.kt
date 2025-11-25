package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
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

    // --- 1. Declarar los nuevos botones de redes sociales ---
    private lateinit var btnInstagram: ImageButton
    private lateinit var btnFacebook: ImageButton
    private lateinit var btnWhatsapp: ImageButton

    private lateinit var auth: FirebaseAuth
    private lateinit var usuario: Usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //inicializar la autenticacion
        auth = FirebaseAuth.getInstance()
        //auth = Firebase.auth

        //Acceso a la base de datos Cloud Firestore
        val db = Firebase.firestore

        correo = findViewById(R.id.correo_login)
        password = findViewById(R.id.password_login)
        btn_ingresar = findViewById(R.id.btn_login)
        btn_registrar = findViewById(R.id.btn_registrar)
        btn_olvidar = findViewById(R.id.btn_olvidar)

        // --- 2. Inicializar los botones de redes sociales ---
        btnInstagram = findViewById(R.id.btn_instagram)
        btnFacebook = findViewById(R.id.btn_facebook)
        btnWhatsapp = findViewById(R.id.btn_whatsapp)

        usuario = Usuario("", "", "", "")

        btn_ingresar.setOnClickListener {
            val email = correo.editText?.text.toString()
            val pass = password.editText?.text.toString()
            if(email.isNotEmpty() && pass.isNotEmpty())
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener {
                        if(it.isSuccessful) {
                            db.collection("usuarios")
                                .whereEqualTo("correo", email)
                                .get()
                                .addOnSuccessListener { documents ->
                                    for (document in documents){
                                        usuario = Usuario(
                                            document.data["correo"].toString(),
                                            document.data["nombre"].toString(),
                                            document.data["apaterno"].toString(),
                                            document.data["amaterno"].toString()
                                        )

                                    }
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.putExtra("usuario", usuario)
                                    startActivity(intent)
                                    finish()
                                }
                        } else {
                            showAlert()
                        }
                    }
        }

        btn_registrar.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }

        btn_olvidar.setOnClickListener {
            val intent = Intent(this, RecuperaPassword::class.java)
            startActivity(intent)
        }

        // --- 3. Agregar los listeners para los botones de redes sociales ---

        btnInstagram.setOnClickListener {
            val url = "https://www.instagram.com/barracudabbh/reel/DBjZ3aFurbe/" // <-- CAMBIA ESTO
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        btnFacebook.setOnClickListener {
            val url = "https://www.facebook.com/barracudaboxing/videos/1452571902113045/" // <-- CAMBIA ESTO
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        btnWhatsapp.setOnClickListener {
            // Reemplaza "1234567890" con tu número de teléfono, incluyendo el código de país (ej. 52 para México)
            val numeroWhatsapp = "tel:+527866880131" // <-- CAMBIA ESTO
            val url = "https://api.whatsapp.com/send?phone=$numeroWhatsapp"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
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
