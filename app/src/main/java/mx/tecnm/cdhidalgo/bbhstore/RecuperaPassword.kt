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

class RecuperaPassword : AppCompatActivity() {

    private lateinit var correo: TextInputLayout
    private lateinit var btn_recuperar: Button

    private var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recupera_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        correo = findViewById(R.id.campo_correo_recupera)
        btn_recuperar = findViewById(R.id.btn_recuperar_pass_recupera)

        btn_recuperar.setOnClickListener {
            if(correo.editText?.text.toString().isNotEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(correo.editText?.text.toString()).matches()){
                    correo.error = null
                    enviarCorreoRecuperacion()
            } else {
                Toast.makeText(
                    this,
                    "Ingrese un correo válido",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enviarCorreoRecuperacion(){
        auth.sendPasswordResetEmail(correo.editText?.text.toString()).addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(
                    this,
                    "Se ha enviado un correo para recuperar la contraseña",
                    Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}