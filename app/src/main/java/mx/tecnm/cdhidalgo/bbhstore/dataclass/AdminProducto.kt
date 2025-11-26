package mx.tecnm.cdhidalgo.bbhstore.dataclass

data class AdminProducto(
    val id: String,
    val nombreCorto: String?,
    val nombre: String?,
    val descripcion: String?,
    val categoria: String?,
    val precio: Double,
    val stock: Int,
    val imagenName: String?
)
