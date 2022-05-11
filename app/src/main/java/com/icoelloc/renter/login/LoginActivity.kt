package com.icoelloc.renter.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.icoelloc.renter.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        Thread.sleep(2000)

        setTheme(R.style.Theme_Renter)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }
}