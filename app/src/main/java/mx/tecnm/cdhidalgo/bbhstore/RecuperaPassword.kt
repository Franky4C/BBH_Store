package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import android.widget.ImageButton // Importación para el botón de regreso (btn_back)

class RecuperaPassword : AppCompatActivity() {

    private lateinit var nombre: TextInputLayout
    private lateinit var apaterno: TextInputLayout
    private lateinit var correo: TextInputLayout
    private lateinit var btnRecuperar: Button
    private lateinit var btnBack: ImageButton // Botón para regresar

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recupera_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias de UI
        nombre = findViewById(R.id.campo_nombre_recupera)
        apaterno = findViewById(R.id.campo_apaterno_recupera)
        correo = findViewById(R.id.campo_correo_recupera)
        btnRecuperar = findViewById(R.id.btn_recuperar_pass_recupera)
        btnBack = findViewById(R.id.btn_back_recupera)

        // Listeners
        btnRecuperar.setOnClickListener { validarYBuscarUsuario() }
        btnBack.setOnClickListener { finish() }
    }

    private fun validarYBuscarUsuario() {
        val nombreTxt   = nombre.editText?.text?.toString()?.trim() ?: ""
        val apaternoTxt = apaterno.editText?.text?.toString()?.trim() ?: ""
        val correoTxt   = correo.editText?.text?.toString()?.trim() ?: ""

        // Validaciones de UI
        if (nombreTxt.isEmpty() || apaternoTxt.isEmpty() || correoTxt.isEmpty()) {
            Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correoTxt).matches()) {
            correo.error = "Formato de correo inválido"
            Toast.makeText(this, "Ingrese un correo válido", Toast.LENGTH_SHORT).show()
            return
        } else {
            correo.error = null
        }

        // Deshabilitar botón para evitar clics múltiples durante la búsqueda
        btnRecuperar.isEnabled = false

        // 1. Buscar en Firestore (bbh_usuarios) que coincidan correo + nombre + apaterno
        db.collection("bbh_usuarios")
            .whereEqualTo("correo", correoTxt)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                btnRecuperar.isEnabled = true // Re-habilitar

                if (snapshot.isEmpty) {
                    Toast.makeText(
                        this,
                        "Los datos no coinciden con un usuario registrado",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                val doc = snapshot.documents.first()
                val nombreDb = doc.getString("nombre") ?: ""
                val apaternoDb = doc.getString("apaterno") ?: ""

                // 2. Comprobar que Nombre y Apellido Paterno coincidan (ignorando mayúsculas/minúsculas)
                if (nombreDb.equals(nombreTxt, ignoreCase = true) &&
                    apaternoDb.equals(apaternoTxt, ignoreCase = true)
                ) {
                    // Coincide → mandar correo de reseteo
                    enviarCorreoRecuperacion(correoTxt)
                } else {
                    Toast.makeText(
                        this,
                        "Los datos no coinciden con el usuario registrado",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                btnRecuperar.isEnabled = true // Re-habilitar
                Toast.makeText(
                    this,
                    "Error al verificar usuario: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun enviarCorreoRecuperacion(correoTxt: String) {
        btnRecuperar.isEnabled = false // Deshabilitar durante el envío de correo

        auth.sendPasswordResetEmail(correoTxt)
            .addOnCompleteListener { task ->
                btnRecuperar.isEnabled = true // Re-habilitar (solo si no hacemos finish)

                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Se ha enviado un correo para recuperar la contraseña",
                        Toast.LENGTH_LONG
                    ).show()
                    // Si es exitoso, volvemos a la pantalla de Login
                    startActivity(Intent(this, Login::class.java))
                    finish()
                } else {
                    // Manejo específico de errores de Firebase Auth
                    val errorMsg = task.exception?.message ?: "Error desconocido al enviar el correo."
                    Toast.makeText(
                        this,
                        "Error al enviar correo: $errorMsg",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}