package mx.tecnm.cdhidalgo.bbhstore.dataclass

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ItemOrden(
    val nombre: String? = null,
    val nombreCorto: String? = null,
    val categoria: String? = null,
    val precioUnitario: Double = 0.0,
    val cantidad: Int = 0,
    val subtotal: Double = 0.0
)

@IgnoreExtraProperties
data class Orden(
    val idCompra: String = "",
    val fecha: Long = 0L,
    val usuarioCorreo: String? = null,
    val items: List<ItemOrden> = emptyList(),
    val total: Double = 0.0
)
