package mx.tecnm.cdhidalgo.bbhstore.dataclass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Producto(
    var imagen: Int = 0,
    var nombreCorto: String? = null,
    var nombre: String? = null,
    var precio: Double = 0.0,
    var descripcion: String? = null,
    var categoria: String? = null,
    var stock: Int = 0,
    var id: String = ""      // ID de documento en Firestore
) : Parcelable
