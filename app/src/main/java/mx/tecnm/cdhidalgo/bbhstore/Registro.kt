package mx.tecnm.cdhidalgo.bbhstore

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout

class Registro : AppCompatActivity() {

    private lateinit var nombre: TextInputLayout
    private lateinit var apaterno: TextInputLayout
    private lateinit var amaterno: TextInputLayout
    private lateinit var correo: TextInputLayout
    private lateinit var password: TextInputLayout

    private lateinit var btn_registrar: Button
    private lateinit var btn_estoyregistrado: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
        }
    }
}