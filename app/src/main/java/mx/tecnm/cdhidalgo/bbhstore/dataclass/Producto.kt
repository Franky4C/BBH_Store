package mx.tecnm.cdhidalgo.bbhstore.dataclass

import android.os.Parcel
import android.os.Parcelable

data class Producto(
    val imagen: Int,
    val nombreCorto: String?,
    val nombre: String?,
    val precio: Double,
    val descripcion: String?,
    val categoria: String?,
    val stock: Int            // NUEVO
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()      // NUEVO
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(imagen)
        parcel.writeString(nombreCorto)
        parcel.writeString(nombre)
        parcel.writeDouble(precio)
        parcel.writeString(descripcion)
        parcel.writeString(categoria)
        parcel.writeInt(stock)  // NUEVO
    }

    companion object CREATOR : Parcelable.Creator<Producto> {
        override fun createFromParcel(parcel: Parcel): Producto = Producto(parcel)
        override fun newArray(size: Int): Array<Producto?> = arrayOfNulls(size)
    }
}
