package mx.tecnm.cdhidalgo.bbhstore

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import mx.tecnm.cdhidalgo.bbhstore.adaptadores.AdminUsuariosAdapter
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Usuario

class AdminUsuariosActivity : AppCompatActivity() {

    private val db = Firebase.firestore

    private lateinit var btnRegresar: ImageButton
    private lateinit var txtSinUsuarios: TextView
    private lateinit var rvUsuarios: RecyclerView

    private lateinit var adaptador: AdminUsuariosAdapter
    private val listaAdmin = mutableListOf<Pair<String, Usuario>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_usuarios)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnRegresar = findViewById(R.id.btn_regresar_admin_usuarios)
        txtSinUsuarios = findViewById(R.id.txt_sin_usuarios)
        rvUsuarios = findViewById(R.id.rv_admin_usuarios)

        rvUsuarios.layoutManager = LinearLayoutManager(this)
        adaptador = AdminUsuariosAdapter(listaAdmin)
        rvUsuarios.adapter = adaptador

        btnRegresar.setOnClickListener { finish() }

        adaptador.onEditarClick = { idDoc, usuario ->
            mostrarDialogoEditarUsuario(idDoc, usuario)
        }

        adaptador.onBloquearClick = { idDoc, usuario ->
            cambiarEstadoBloqueo(idDoc, usuario)
        }

        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        db.collection("bbh_usuarios")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    txtSinUsuarios.visibility = android.view.View.VISIBLE
                    adaptador.actualizar(emptyList())
                    return@addOnSuccessListener
                }

                txtSinUsuarios.visibility = android.view.View.GONE

                val nuevaLista = snapshot.documents.mapNotNull { doc ->
                    val usuario = doc.toObject(Usuario::class.java)
                    if (usuario != null) doc.id to usuario else null
                }

                adaptador.actualizar(nuevaLista)
            }
            .addOnFailureListener { e ->
                txtSinUsuarios.visibility = android.view.View.VISIBLE
                Toast.makeText(
                    this,
                    "Error al cargar usuarios: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun mostrarDialogoEditarUsuario(idDocumento: String, usuario: Usuario) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar usuario")

        val inflater: LayoutInflater = layoutInflater
        val vista = inflater.inflate(R.layout.dialog_editar_usuario, null)

        val edtNombre = vista.findViewById<EditText>(R.id.edt_usuario_nombre)
        val edtApaterno = vista.findViewById<EditText>(R.id.edt_usuario_apaterno)
        val edtAmaterno = vista.findViewById<EditText>(R.id.edt_usuario_amaterno)
        val edtTelefono = vista.findViewById<EditText>(R.id.edt_usuario_telefono)
        val edtRol = vista.findViewById<EditText>(R.id.edt_usuario_rol)

        edtNombre.setText(usuario.nombre ?: "")
        edtApaterno.setText(usuario.apaterno ?: "")
        edtAmaterno.setText(usuario.amaterno ?: "")
        edtTelefono.setText(usuario.telefono ?: "")
        edtRol.setText(usuario.rol ?: "cliente")

        builder.setView(vista)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val nombre = edtNombre.text.toString().trim()
            val apaterno = edtApaterno.text.toString().trim()
            val amaterno = edtAmaterno.text.toString().trim()
            val telefono = edtTelefono.text.toString().trim()
            val rol = edtRol.text.toString().trim().ifEmpty { "cliente" }

            val data = hashMapOf(
                "nombre" to nombre,
                "apaterno" to apaterno,
                "amaterno" to amaterno,
                "telefono" to telefono,
                "rol" to rol,
                "correo" to (usuario.correo ?: ""),
                "bloqueado" to usuario.bloqueado
            )

            db.collection("bbh_usuarios")
                .document(idDocumento)
                .set(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                    cargarUsuarios()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error al actualizar usuario: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun cambiarEstadoBloqueo(idDocumento: String, usuario: Usuario) {
        val nuevoEstado = !usuario.bloqueado
        val mensaje = if (nuevoEstado) "Usuario bloqueado" else "Usuario desbloqueado"

        db.collection("bbh_usuarios")
            .document(idDocumento)
            .update("bloqueado", nuevoEstado)
            .addOnSuccessListener {
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                cargarUsuarios()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al cambiar estado: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
