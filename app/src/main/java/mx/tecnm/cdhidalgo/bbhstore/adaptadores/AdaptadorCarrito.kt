package mx.tecnm.cdhidalgo.bbhstore.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // ⭐ Importación de Glide añadida
import mx.tecnm.cdhidalgo.bbhstore.R
import mx.tecnm.cdhidalgo.bbhstore.dataclass.CarritoManager

class AdaptadorCarrito :
    RecyclerView.Adapter<AdaptadorCarrito.CarritoViewHolder>() {

    // Lista local basada en el manager
    private val items: MutableList<CarritoManager.ItemCarrito> =
        CarritoManager.obtenerItems().toMutableList()

    // Posiciones seleccionadas
    private val seleccionados = mutableSetOf<Int>()

    var onCantidadCambiada: (() -> Unit)? = null

    class CarritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProducto: ImageView = itemView.findViewById(R.id.img_carrito_producto)
        val txtNombre: TextView = itemView.findViewById(R.id.txt_carrito_nombre)
        val txtPrecio: TextView = itemView.findViewById(R.id.txt_carrito_precio)
        val txtStock: TextView = itemView.findViewById(R.id.txt_carrito_stock)
        val txtCantidad: TextView = itemView.findViewById(R.id.txt_carrito_cantidad)
        val btnMas: TextView = itemView.findViewById(R.id.btn_mas)
        val btnMenos: TextView = itemView.findViewById(R.id.btn_menos)
        val chkSeleccionado: CheckBox = itemView.findViewById(R.id.chk_seleccionado) // CheckBox re-añadida
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(vista)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val item = items[position]
        val producto = item.producto

        // ⭐ LÓGICA DE CARGA DE IMAGEN CON GLIDE AÑADIDA AQUÍ
        if (!producto.imagenUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(producto.imagenUrl)
                .placeholder(producto.imagen)  // Usa el drawable como placeholder
                .centerCrop()
                .into(holder.imgProducto)
        } else {
            // Si no hay URL, usa el recurso local (imagen)
            holder.imgProducto.setImageResource(producto.imagen)
        }
        // ⭐ FIN LÓGICA GLIDE

        holder.txtNombre.text = producto.nombreCorto ?: producto.nombre ?: "Producto"
        holder.txtPrecio.text = "$${String.format("%.2f", producto.precio)}"
        holder.txtStock.text = "Stock: ${producto.stock}"
        holder.txtCantidad.text = item.cantidad.toString()

        // CheckBox: reflejar estado actual
        holder.chkSeleccionado.setOnCheckedChangeListener(null)
        holder.chkSeleccionado.isChecked = seleccionados.contains(position)
        holder.chkSeleccionado.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                seleccionados.add(position)
            } else {
                seleccionados.remove(position)
            }
        }

        holder.btnMas.setOnClickListener {
            val nuevaCantidad = item.cantidad + 1
            val ok = CarritoManager.actualizarCantidad(producto, nuevaCantidad)
            if (ok) {
                item.cantidad = nuevaCantidad
                // Usamos notifyItemChanged, es más eficiente que notifyDataSetChanged
                holder.txtCantidad.text = nuevaCantidad.toString()
                onCantidadCambiada?.invoke()
            } else {
                Toast.makeText(
                    holder.itemView.context,
                    "No hay stock suficiente",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        holder.btnMenos.setOnClickListener {
            val nuevaCantidad = item.cantidad - 1
            val ok = CarritoManager.actualizarCantidad(producto, nuevaCantidad)
            if (ok) {
                if (nuevaCantidad <= 0) {
                    // Si se eliminó (cantidad 0), refrescamos toda la lista
                    refrescarDatos()
                } else {
                    item.cantidad = nuevaCantidad
                    // Usamos notifyItemChanged
                    holder.txtCantidad.text = nuevaCantidad.toString()
                }
                onCantidadCambiada?.invoke()
            }
        }
    }

    /**
     * Devuelve solo los items seleccionados.
     */
    fun obtenerItemsSeleccionados(): List<CarritoManager.ItemCarrito> {
        return items.filterIndexed { index, _ -> seleccionados.contains(index) }
    }

    /**
     * Recarga datos desde el CarritoManager (por ejemplo tras vaciar o eliminar algunos).
     */
    fun refrescarDatos() {
        items.clear()
        items.addAll(CarritoManager.obtenerItems())
        seleccionados.clear()
        notifyDataSetChanged()
        onCantidadCambiada?.invoke() // Opcional: invocar también si los totales cambian tras refrescar
    }
}