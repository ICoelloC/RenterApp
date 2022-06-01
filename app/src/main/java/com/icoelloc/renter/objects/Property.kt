package com.icoelloc.renter.objects

import java.util.*

data class Property(
    var id: String = "",
    var nombre: String = "",
    var latitud: String = "",
    var longitud: String = "",
    var inquilino: String = "",
    var propietario: String = "",
    var banios: Int = 0,
    var habitaciones: Int = 0
) {

    constructor(
        id: String,
        nombre: String,
        latitud: String,
        longitud: String,
        inquilino: String,
        propietario: String,
        banios: Int,
        habitaciones: Int,
        fecha: Date
    ) : this(
        id,
        nombre,
        latitud,
        longitud,
        inquilino,
        propietario,
        banios,
        habitaciones
    )

    override fun toString(): String {
        return "Lugar(id='$id', nomre='$nombre',latitud='$latitud', longitud='$longitud', inquilino='$inquilino', propietario=$propietario, banios=$banios, habitaciones='$habitaciones')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Property) return false
        if (id != other.id) return false
        if (nombre != other.nombre) return false
        if (latitud != other.latitud) return false
        if (longitud != other.longitud) return false
        if (inquilino != other.inquilino) return false
        if (propietario != other.propietario) return false
        if (banios != other.banios) return false
        if (habitaciones != other.habitaciones) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + latitud.hashCode()
        result = 31 * result + longitud.hashCode()
        result = 31 * result + inquilino.hashCode()
        result = 31 * result + propietario.hashCode()
        result = 31 * result + banios
        result = 31 * result + habitaciones.hashCode()
        return result
    }
}