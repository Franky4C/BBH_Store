package mx.tecnm.cdhidalgo.bbhstore.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.tecnm.cdhidalgo.bbhstore.R
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Usuario

class AdminUsuariosAdapter(
    private var lista: List<Pair<String, Usuario>>
) : RecyclerView.Adapter<AdminUsuariosAdapter.UsuarioViewHolder>() {

    var onEditarClick: ((String, Usuario) -> Unit)? = null
    var onBloquearClick: ((String, Usuario) -> Unit)? = null

    fun actualizar(nuevaLista: List<Pair<String, Usuario>>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView = itemView.findViewById(R.id.txt_admin_usuario_nombre)
        val txtCorreo: TextView = itemView.findViewById(R.id.txt_admin_usuario_correo)
        val txtRol: TextView = itemView.findViewById(R.id.txt_admin_usuario_rol)
        val txtEstado: TextView = itemView.findViewById(R.id.txt_admin_usuario_estado)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btn_admin_usuario_editar)
        val btnBloquear: ImageButton = itemView.findViewById(R.id.btn_admin_usuario_bloquear)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_usuario, parent, false)
        return UsuarioViewHolder(vista)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val (idDoc, usuario) = lista[position]

        val nombreCompleto = listOfNotNull(
            usuario.nombre,
            usuario.apaterno,
            usuario.amaterno
        ).joinToString(" ")

        holder.txtNombre.text = if (nombreCompleto.isNotBlank()) nombreCompleto else "Sin nombre"
        holder.txtCorreo.text = usuario.correo ?: "Sin correo"

        val rol = usuario.rol ?: "cliente"
        holder.txtRol.text = "Rol: $rol"

        val bloqueado = usuario.bloqueado
        if (bloqueado) {
            holder.txtEstado.text = "Estado: Bloqueado"
            holder.txtEstado.setTextColor(0xFFC42217.toInt())
        } else {
            holder.txtEstado.text = "Estado: Activo"
            holder.txtEstado.setTextColor(0xFF8AFF8E.toInt())
        }

        holder.btnEditar.setOnClickListener {
            onEditarClick?.invoke(idDoc, usuario)
        }

        holder.btnBloquear.setOnClickListener {
            onBloquearClick?.invoke(idDoc, usuario)
        }
    }
}
