package com.icoelloc.renter.objects

data class Property(
    var id: String = "",
    var nombre: String = "",
    var latitud: String = "",
    var longitud: String = "",
    var inquilino: String = "",
    var localidad: String = "",
    var propietario: String = "",
    var telefono: String = "",
    var banios: Int = 0,
    var habitaciones: Int = 0,
    var metros: Int = 0,
    var precio: Int = 0,
    var foto1: String = "",
    var foto2: String = "",
    var foto3: String = "",
    var foto4: String = "",
    var foto5: String = "",
) {

    override fun toString(): String {
        return "Lugar(id='$id', nombre='$nombre' localidad='$localidad',latitud='$latitud', longitud='$longitud', inquilino='$inquilino', propietario=$propietario, telefono=$telefono, banios=$banios, habitaciones='$habitaciones', metros='$metros', precio='$precio', foto1='$foto1', foto2='$foto2', foto3='$foto3', foto4='$foto4', foto5='$foto5')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Property) return false
        if (id != other.id) return false
        if (nombre != other.nombre) return false
        if (latitud != other.latitud) return false
        if (localidad != other.localidad) return false
        if (longitud != other.longitud) return false
        if (inquilino != other.inquilino) return false
        if (propietario != other.propietario) return false
        if (telefono != other.telefono) return false
        if (banios != other.banios) return false
        if (habitaciones != other.habitaciones) return false
        if (metros != other.metros) return false
        if (precio != other.precio) return false
        if (foto1 != other.foto1) return false
        if (foto2 != other.foto2) return false
        if (foto3 != other.foto3) return false
        if (foto4 != other.foto4) return false
        if (foto5 != other.foto5) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + latitud.hashCode()
        result = 31 * result + localidad.hashCode()
        result = 31 * result + longitud.hashCode()
        result = 31 * result + inquilino.hashCode()
        result = 31 * result + propietario.hashCode()
        result = 31 * result + telefono.hashCode()
        result = 31 * result + banios.hashCode()
        result = 31 * result + habitaciones.hashCode()
        result = 31 * result + metros.hashCode()
        result = 31 * result + precio.hashCode()
        result = 31 * result + foto1.hashCode()
        result = 31 * result + foto2.hashCode()
        result = 31 * result + foto3.hashCode()
        result = 31 * result + foto4.hashCode()
        result = 31 * result + foto5.hashCode()
        return result
    }
}