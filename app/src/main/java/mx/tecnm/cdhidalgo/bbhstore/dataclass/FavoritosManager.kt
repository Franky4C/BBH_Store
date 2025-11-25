package mx.tecnm.cdhidalgo.bbhstore.dataclass

object FavoritosManager {

    private val favoritos = mutableListOf<Producto>()

    fun obtenerFavoritos(): List<Producto> = favoritos

    fun esFavorito(producto: Producto): Boolean {
        return favoritos.any {
            it.nombreCorto == producto.nombreCorto &&
                    it.categoria == producto.categoria
        }
    }

    fun agregarFavorito(producto: Producto) {
        if (!esFavorito(producto)) {
            favoritos.add(producto)
        }
    }

    fun quitarFavorito(producto: Producto) {
        val item = favoritos.firstOrNull {
            it.nombreCorto == producto.nombreCorto &&
                    it.categoria == producto.categoria
        }
        if (item != null) {
            favoritos.remove(item)
        }
    }

    /**
     * @return true si queda como favorito, false si se quitó
     */
    fun alternarFavorito(producto: Producto): Boolean {
        return if (esFavorito(producto)) {
            quitarFavorito(producto)
            false
        } else {
            agregarFavorito(producto)
            true
        }
    }
}
