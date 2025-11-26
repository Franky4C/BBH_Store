package mx.tecnm.cdhidalgo.bbhstore.adaptadores

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import mx.tecnm.cdhidalgo.bbhstore.R
import mx.tecnm.cdhidalgo.bbhstore.dataclass.FavoritosManager
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Producto

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

        // Imagen desde URL (Storage) o drawable local
        if (!producto.imagenUrl.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(producto.imagenUrl)
                .placeholder(producto.imagen)   // placeholder mientras carga
                .error(R.drawable.logo)
                .centerCrop()
                .into(holder.vistaImagen)
        } else {
            holder.vistaImagen.setImageResource(producto.imagen)
        }

        // Nombre
        holder.vistaNombre.text = producto.nombreCorto ?: producto.nombre ?: "Producto"

        // Estado inicial de favorito
        val esFav = FavoritosManager.esFavorito(producto)
        aplicarEstiloFavorito(holder.btnFavorito, esFav)

        // Click normal → Detalle
        holder.itemView.setOnClickListener {
            onProductoClick?.invoke(producto)
        }

        // Long click → agregar al carrito (Tienda lo usa así)
        holder.itemView.setOnLongClickListener {
            onProductoLongClick?.invoke(producto)
            true
        }

        // Click en corazón → alternar favorito en Firestore
        holder.btnFavorito.setOnClickListener {
            // Deshabilita mientras se actualiza para evitar doble tap
            holder.btnFavorito.isEnabled = false

            FavoritosManager.alternarFavorito(producto) { ok, error ->
                holder.btnFavorito.isEnabled = true

                if (!ok) {
                    Toast.makeText(
                        holder.itemView.context,
                        "Error al actualizar favoritos: ${error ?: "desconocido"}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@alternarFavorito
                }

                // Consulta el estado final en FavoritosManager
                val nuevoEstado = FavoritosManager.esFavorito(producto)
                aplicarEstiloFavorito(holder.btnFavorito, nuevoEstado)
                onFavoritoCambiado?.invoke(producto, nuevoEstado)

                val msg = if (nuevoEstado) "Añadido a favoritos" else "Quitado de favoritos"
                Toast.makeText(holder.itemView.context, msg, Toast.LENGTH_SHORT).show()
            }
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
