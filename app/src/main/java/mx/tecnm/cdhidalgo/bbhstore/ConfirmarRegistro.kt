package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class ConfirmarRegistro : AppCompatActivity() {

    private lateinit var etiqueta_nombre: TextView
    private lateinit var etiqueta_credenciales: TextView
    private lateinit var btn_confirmar_datos: Button
    private lateinit var btn_corregir_datos: Button

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private var nombre: String = ""
    private var apaterno: String = ""
    private var amaterno: String = ""
    private var correo: String = ""
    private var password: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confirmar_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        nombre   = intent.getStringExtra("nombre") ?: ""
        apaterno = intent.getStringExtra("apaterno") ?: ""
        amaterno = intent.getStringExtra("amaterno") ?: ""
        correo   = intent.getStringExtra("correo") ?: ""
        password = intent.getStringExtra("password") ?: ""

        etiqueta_nombre = findViewById(R.id.etiqueta_nombre)
        etiqueta_credenciales = findViewById(R.id.etiqueta_credenciales)
        btn_confirmar_datos = findViewById(R.id.btn_confirmar_datos)
        btn_corregir_datos = findViewById(R.id.btn_corregir_datos)

        etiqueta_nombre.text = "$nombre $apaterno $amaterno"
        etiqueta_credenciales.text = "Tus credenciales son:\nCorreo: $correo\nContraseña: $password"

        // Confirmar datos → crear cuenta en Auth + guardar en bbh_usuarios
        btn_confirmar_datos.setOnClickListener {
            if (correo.isNotEmpty() && password.isNotEmpty()) {
                crearCuentaEnFirebaseAuth()
            } else {
                showAlert("Correo o contraseña vacíos")
            }
        }

        // Corregir datos → regresar a Registro
        btn_corregir_datos.setOnClickListener {
            startActivity(Intent(this, Registro::class.java))
            finish()
        }
    }

    private fun crearCuentaEnFirebaseAuth() {
        auth.createUserWithEmailAndPassword(correo, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val data = hashMapOf(
                        "correo"    to correo,
                        "nombre"    to nombre,
                        "apaterno"  to apaterno,
                        "amaterno"  to amaterno,
                        "rol"       to "cliente",   // rol por defecto
                        "bloqueado" to false        // no bloqueado por defecto
                    )

                    val uid = task.result?.user?.uid
                    val docId = uid ?: correo   // puedes usar uid o correo como ID

                    db.collection("bbh_usuarios")
                        .document(docId)
                        .set(data)
                        .addOnSuccessListener {
                            startActivity(Intent(this, Login::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            showAlert("Error al guardar datos del usuario: ${e.message}")
                        }
                } else {
                    showAlert("Se ha producido un error creando la cuenta: ${task.exception?.message}")
                }
            }
    }

    private fun showAlert(mensaje: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar", null)
        builder.create().show()
    }
}
