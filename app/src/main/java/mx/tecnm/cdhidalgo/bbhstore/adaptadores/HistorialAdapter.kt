package mx.tecnm.cdhidalgo.bbhstore.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.tecnm.cdhidalgo.bbhstore.R
import mx.tecnm.cdhidalgo.bbhstore.dataclass.Orden
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistorialAdapter(
    private var lista: List<Orden>
) : RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {

    var onOrdenClick: ((Orden) -> Unit)? = null

    fun actualizarDatos(nuevaLista: List<Orden>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    class HistorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtId: TextView = itemView.findViewById(R.id.txt_hist_id)
        val txtFecha: TextView = itemView.findViewById(R.id.txt_hist_fecha)
        val txtTotal: TextView = itemView.findViewById(R.id.txt_hist_total)
        val txtItems: TextView = itemView.findViewById(R.id.txt_hist_items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_orden, parent, false)
        return HistorialViewHolder(vista)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val orden = lista[position]

        holder.txtId.text = orden.idCompra

        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaTexto = formato.format(Date(orden.fecha))
        holder.txtFecha.text = "Fecha: $fechaTexto"

        holder.txtTotal.text = "Total: $" + String.format("%.2f", orden.total)

        val numItems = orden.items.sumOf { it.cantidad }
        holder.txtItems.text = "$numItems artículos"

        holder.itemView.setOnClickListener {
            onOrdenClick?.invoke(orden)
        }
    }
}
