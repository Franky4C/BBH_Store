package mx.tecnm.cdhidalgo.bbhstore.adaptadores

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.tecnm.cdhidalgo.bbhstore.R
import mx.tecnm.cdhidalgo.bbhstore.dataclass.FavoritosManager
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Producto
import com.bumptech.glide.Glide


class AdaptadorArtesania(
    private val listaProductos: ArrayList<Producto>
) : RecyclerView.Adapter<AdaptadorArtesania.ProductoViewHolder>() {

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vistaImagen: ImageView = itemView.findViewById(R.id.imagen_producto)
        val vistaNombre: TextView = itemView.findViewById(R.id.nombre_producto)
        val btnFavorito: TextView = itemView.findViewById(R.id.btn_favorito)
    }

    var onProductoClick: ((Producto) -> Unit)? = null
    var onProductoLongClick: ((Producto) -> Unit)? = null
    var onFavoritoCambiado: ((Producto, Boolean) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductoViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_producto, parent, false)

        return ProductoViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = listaProductos[position]

        // Cargar imagen desde URL si existe, si no usar drawable local
        if (!producto.imagenUrl.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(producto.imagenUrl)
                .placeholder(producto.imagen)   // usa el drawable como placeholder
                .error(R.drawable.logo)
                .into(holder.vistaImagen)
        } else {
            holder.vistaImagen.setImageResource(producto.imagen)
        }

        holder.vistaNombre.text = producto.nombreCorto

        // resto de tu lógica (favoritos, clicks, etc.) queda igual
        val esFav = FavoritosManager.esFavorito(producto)
        aplicarEstiloFavorito(holder.btnFavorito, esFav)

        holder.itemView.setOnClickListener {
            onProductoClick?.invoke(producto)
        }

        holder.itemView.setOnLongClickListener {
            onProductoLongClick?.invoke(producto)
            true
        }

        holder.btnFavorito.setOnClickListener {
            val nuevoEstado = FavoritosManager.alternarFavorito(producto)
            aplicarEstiloFavorito(holder.btnFavorito, nuevoEstado)
            onFavoritoCambiado?.invoke(producto, nuevoEstado)
        }
    }


    private fun aplicarEstiloFavorito(view: TextView, esFavorito: Boolean) {
        if (esFavorito) {
            view.text = "❤"
            view.setTextColor(Color.parseColor("#c42217"))
        } else {
            view.text = "♡"
            view.setTextColor(Color.WHITE)
        }
    }

    override fun getItemCount(): Int = listaProductos.size
}
