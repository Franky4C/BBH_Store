package mx.tecnm.cdhidalgo.tiendaregalos.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.tecnm.cdhidalgo.tiendaregalos.R
import mx.tecnm.cdhidalgo.tiendaregalos.dataclass.Producto

class AdaptadorArtesania (private val listaProductos: ArrayList<Producto>)
    : RecyclerView.Adapter<AdaptadorArtesania.ProductoViewHolder>()
{
    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val vistaImagen: ImageView = itemView.findViewById(R.id.imagen_producto)
        val vistaNombre: TextView = itemView.findViewById(R.id.nombre_producto)
    }

    var onProductoClick: ((Producto)-> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductoViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_producto,parent,false)

        return ProductoViewHolder(vista)
    }

    override fun onBindViewHolder(
        holder: ProductoViewHolder,
        position: Int
    ) {
        val producto = listaProductos[position]
        holder.vistaImagen.setImageResource(producto.imagen)
        holder.vistaNombre.text = producto.nombreCorto

        holder.itemView.setOnClickListener {
            onProductoClick?.invoke(producto)
        }
    }

    override fun getItemCount(): Int {
        return listaProductos.size
    }
}

