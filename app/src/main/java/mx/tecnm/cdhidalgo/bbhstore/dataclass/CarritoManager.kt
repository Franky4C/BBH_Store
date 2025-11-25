package mx.tecnm.cdhidalgo.bbhstore.dataclass

object CarritoManager {

    data class ItemCarrito(
        val producto: Producto,
        var cantidad: Int
    )

    private val items = mutableListOf<ItemCarrito>()

    fun obtenerItems(): List<ItemCarrito> = items

    fun obtenerCantidadTotal(): Int = items.sumOf { it.cantidad }

    fun obtenerImporteTotal(): Double =
        items.sumOf { it.cantidad * it.producto.precio }

    fun vaciarCarrito() {
        items.clear()
    }

    /**
     * Elimina del carrito solo los items que vengan en [itemsAEliminar].
     * Se usa el nombre del producto para identificarlos, igual que en agregar/actualizar.
     */
    fun eliminarItems(itemsAEliminar: List<ItemCarrito>) {
        items.removeAll { item ->
            itemsAEliminar.any { sel ->
                sel.producto.nombre == item.producto.nombre
            }
        }
    }

    /**
     * Agrega [cantidad] unidades de un producto al carrito.
     * Respeta el stock y no permite cantidades negativas.
     * Devuelve true si se pudo agregar, false si no había stock suficiente.
     */
    fun agregarProducto(producto: Producto, cantidad: Int = 1): Boolean {
        if (cantidad <= 0) return false

        val existente = items.find { it.producto.nombre == producto.nombre }

        val cantidadActual = existente?.cantidad ?: 0
        val nuevaCantidad = cantidadActual + cantidad

        if (nuevaCantidad > producto.stock) {
            return false
        }

        if (existente == null) {
            items.add(ItemCarrito(producto, cantidad))
        } else {
            existente.cantidad = nuevaCantidad
        }
        return true
    }

    /**
     * Cambia la cantidad de un producto en el carrito.
     * - Si nuevaCantidad <= 0 → elimina el producto.
     * - Si nuevaCantidad > stock → no hace nada y devuelve false.
     */
    fun actualizarCantidad(producto: Producto, nuevaCantidad: Int): Boolean {
        val existente = items.find { it.producto.nombre == producto.nombre }
            ?: return false

        if (nuevaCantidad <= 0) {
            items.remove(existente)
            return true
        }

        if (nuevaCantidad > producto.stock) {
            return false
        }

        existente.cantidad = nuevaCantidad
        return true
    }
}
