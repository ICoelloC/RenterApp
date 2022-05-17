package com.icoelloc.renter.objects

import java.io.Serializable

class User {
    data class User(val emai:String, val name:String, var profilePic: String):
        Serializable {
    }
}