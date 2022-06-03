package com.icoelloc.renter.objects

import android.content.Context
class Shared{
    companion object {
        lateinit var user: User
        lateinit var context: Context
        lateinit var SignInMode: String
    }
}