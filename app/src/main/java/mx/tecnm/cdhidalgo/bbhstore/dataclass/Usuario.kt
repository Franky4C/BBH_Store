package mx.tecnm.cdhidalgo.bbhstore.dataclass

import android.os.Parcel
import android.os.Parcelable

data class Usuario(
    val nombre: String? = null,
    val apaterno: String? = null,
    val amaterno: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    val rol: String? = "cliente",       // "admin" o "cliente"
    val bloqueado: Boolean = false      // true = sin acceso a la tienda
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nombre)
        parcel.writeString(apaterno)
        parcel.writeString(amaterno)
        parcel.writeString(correo)
        parcel.writeString(telefono)
        parcel.writeString(rol)
        parcel.writeByte(if (bloqueado) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Usuario> {
        override fun createFromParcel(parcel: Parcel): Usuario = Usuario(parcel)
        override fun newArray(size: Int): Array<Usuario?> = arrayOfNulls(size)
    }
}
