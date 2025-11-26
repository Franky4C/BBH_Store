package mx.tecnm.cdhidalgo.bbhstore.dataclass

import android.os.Parcel
import android.os.Parcelable

data class Producto(
    val imagen: Int = 0,                  // drawable de respaldo
    val nombreCorto: String? = null,
    val nombre: String? = null,
    val precio: Double = 0.0,
    val descripcion: String? = null,
    val categoria: String? = null,
    val stock: Int = 0,
    val imagenUrl: String? = null         // NUEVO: URL de Firebase Storage
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),                // imagen
        parcel.readString(),             // nombreCorto
        parcel.readString(),             // nombre
        parcel.readDouble(),             // precio
        parcel.readString(),             // descripcion
        parcel.readString(),             // categoria
        parcel.readInt(),                // stock
        parcel.readString()              // imagenUrl
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(imagen)
        parcel.writeString(nombreCorto)
        parcel.writeString(nombre)
        parcel.writeDouble(precio)
        parcel.writeString(descripcion)
        parcel.writeString(categoria)
        parcel.writeInt(stock)
        parcel.writeString(imagenUrl)
    }

    companion object CREATOR : Parcelable.Creator<Producto> {
        override fun createFromParcel(parcel: Parcel): Producto = Producto(parcel)
        override fun newArray(size: Int): Array<Producto?> = arrayOfNulls(size)
    }
}
