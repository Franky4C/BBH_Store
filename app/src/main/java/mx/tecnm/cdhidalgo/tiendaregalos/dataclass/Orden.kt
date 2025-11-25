package mx.tecnm.cdhidalgo.tiendaregalos.dataclass

data class Orden(
    val idCompra: String,
    val fecha: Long,
    val usuarioCorreo: String?,      // o idUsuario si luego lo tienes
    val items: List<CarritoManager.ItemCarrito>,
    val total: Double
)
