package mx.tecnm.cdhidalgo.bbhstore.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.tecnm.cdhidalgo.bbhstore.R
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Producto

class AdminProductosAdapter(
    private val lista: MutableList<Pair<String, Producto>>
) : RecyclerView.Adapter<AdminProductosAdapter.AdminProductoViewHolder>() {

    // idDocumento, producto
    var onEditarClick: ((String, Producto) -> Unit)? = null
    var onEliminarClick: ((String, Producto) -> Unit)? = null

    fun actualizar(nuevaLista: List<Pair<String, Producto>>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    class AdminProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView = itemView.findViewById(R.id.txt_admin_nombre)
        val txtCategoria: TextView = itemView.findViewById(R.id.txt_admin_categoria)
        val txtPrecio: TextView = itemView.findViewById(R.id.txt_admin_precio)
        val txtStock: TextView = itemView.findViewById(R.id.txt_admin_stock)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btn_admin_editar)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btn_admin_eliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminProductoViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_producto, parent, false)
        return AdminProductoViewHolder(vista)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: AdminProductoViewHolder, position: Int) {
        val (idDocumento, producto) = lista[position]

        holder.txtNombre.text = producto.nombreCorto ?: producto.nombre ?: "Producto"
        holder.txtCategoria.text = producto.categoria ?: "Sin categoría"
        holder.txtPrecio.text = "$${String.format("%.2f", producto.precio)}"
        holder.txtStock.text = "Stock: ${producto.stock}"

        holder.btnEditar.setOnClickListener {
            onEditarClick?.invoke(idDocumento, producto)
        }

        holder.btnEliminar.setOnClickListener {
            onEliminarClick?.invoke(idDocumento, producto)
        }
    }
}
