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
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Usuario

class ConfirmarRegistro : AppCompatActivity() {

    private lateinit var etiqueta_nombre: TextView
    private lateinit var etiqueta_credenciales: TextView
    private lateinit var btn_confirmar_datos: Button
    private lateinit var btn_corregir_datos: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confirmar_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Inicializar la autenticación de Firebase
        auth = FirebaseAuth.getInstance()

        //Acceso a la base de datos Cloud Firestore
        val db = Firebase.firestore

        val nombre = intent.getStringExtra("nombre")
        val apaterno = intent.getStringExtra("apaterno")
        val amaterno = intent.getStringExtra("amaterno")
        val correo = intent.getStringExtra("correo")
        val password = intent.getStringExtra("password")

        etiqueta_nombre = findViewById(R.id.etiqueta_nombre)
        etiqueta_credenciales = findViewById(R.id.etiqueta_credenciales)

        btn_confirmar_datos = findViewById(R.id.btn_confirmar_datos)
        btn_corregir_datos = findViewById(R.id.btn_corregir_datos)

        etiqueta_nombre.text = "$nombre $apaterno $amaterno"
        etiqueta_credenciales.text = "Tus credenciales son:\nCorreo: $correo\nContraseña: $password"

        val usuario = Usuario(
            correo.toString(),
            nombre.toString(),
            apaterno.toString(),
            amaterno.toString())

        btn_confirmar_datos.setOnClickListener {
            if(correo.toString().isNotEmpty() && password.toString().isNotEmpty()){
                auth.createUserWithEmailAndPassword(correo.toString(), password.toString())
                    .addOnCompleteListener {
                        if(it.isSuccessful) {
                            val intent = Intent(this, Login::class.java).apply {
                                db.collection("usuarios")
                                    .document(correo.toString())
                                    .set(usuario)
                            }
                            startActivity(intent)
                        } else {
                            showAlert()
                        }
                    }
            }
        }

        btn_corregir_datos.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
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