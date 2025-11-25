package mx.tecnm.cdhidalgo.tiendaregalos.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import mx.tecnm.cdhidalgo.tiendaregalos.R
import mx.tecnm.cdhidalgo.tiendaregalos.dataclass.CarritoManager

class AdaptadorCarrito :
    RecyclerView.Adapter<AdaptadorCarrito.CarritoViewHolder>() {

    var onCantidadCambiada: (() -> Unit)? = null

    class CarritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProducto: ImageView = itemView.findViewById(R.id.img_carrito_producto)
        val txtNombre: TextView = itemView.findViewById(R.id.txt_carrito_nombre)
        val txtPrecio: TextView = itemView.findViewById(R.id.txt_carrito_precio)
        val txtStock: TextView = itemView.findViewById(R.id.txt_carrito_stock)
        val txtCantidad: TextView = itemView.findViewById(R.id.txt_carrito_cantidad)
        val btnMas: TextView = itemView.findViewById(R.id.btn_mas)
        val btnMenos: TextView = itemView.findViewById(R.id.btn_menos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(vista)
    }

    override fun getItemCount(): Int = CarritoManager.obtenerItems().size

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val item = CarritoManager.obtenerItems()[position]
        val producto = item.producto

        holder.imgProducto.setImageResource(producto.imagen)
        holder.txtNombre.text = producto.nombreCorto ?: producto.nombre ?: "Producto"
        holder.txtPrecio.text = "$${String.format("%.2f", producto.precio)}"
        holder.txtStock.text = "Stock: ${producto.stock}"
        holder.txtCantidad.text = item.cantidad.toString()

        holder.btnMas.setOnClickListener {
            val nuevaCantidad = item.cantidad + 1
            val ok = CarritoManager.actualizarCantidad(producto, nuevaCantidad)
            if (ok) {
                holder.txtCantidad.text = nuevaCantidad.toString()
                notifyDataSetChanged()
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
                notifyDataSetChanged()
                onCantidadCambiada?.invoke()
            }
        }
    }
}
