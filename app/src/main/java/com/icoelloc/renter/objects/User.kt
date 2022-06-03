package com.icoelloc.renter.objects

import java.util.*

data class User(
    var id: String = "",
    var nombre: String = "",
    var email: String = "",
    var image: String = ""
) {

    constructor(
        id: String,
        nombre: String,
        email: String,
        image: String,
        fechaCreacion: Date,
    ) : this(id, nombre, email, image) {
    }

    override fun toString(): String {
        return "User(nombre='$nombre', email='$email', image='$image')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        if (this.nombre != other.nombre) return false
        if (this.email != other.email) return false
        if (this.image != other.image) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + nombre.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + image.hashCode()
        return result
    }
}