package mx.tecnm.cdhidalgo.bbhstore.dataclass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Producto(
    val idDocumento: String? = null,   // <-- ID del doc en Firestore (bbh_productos)
    val imagen: Int = 0,               // para placeholders / carritos viejos
    val nombreCorto: String? = null,
    val nombre: String? = null,
    val precio: Double = 0.0,
    val descripcion: String? = null,
    val categoria: String? = null,
    val stock: Int = 0,
    val imagenUrl: String? = null      // si ya lo estabas usando con Storage
) : Parcelable
