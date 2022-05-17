package com.icoelloc.renter.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.icoelloc.renter.R
import com.icoelloc.renter.main.MainActivity
import com.icoelloc.renter.objects.Shared

class LoginActivity : AppCompatActivity() {

    private var continues: Boolean = false
    public var RC_SIGN_IN = 1


    override fun onCreate(savedInstanceState: Bundle?) {

        Thread.sleep(2000)
        setTheme(R.style.Theme_Renter)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Shared.context = this
        val btnLogin = findViewById<Button>(R.id.btn_login_googleSignIn)
        var btnTwitter = findViewById<ImageView>(R.id.login_img_twitter)
        var btnGithub = findViewById<ImageView>(R.id.login_img_github)
        var btnInstagram = findViewById<ImageView>(R.id.login_img_instagram)

        btnLogin.setOnClickListener {
            loginGoogle()
        }

        btnGithub.setOnClickListener{
            irGithub()
        }

        btnTwitter.setOnClickListener{
            irTwitter()
        }

        btnInstagram.setOnClickListener{
            irInstagram()
        }

    }

    private fun loginGoogle() {

    }

    private fun irInstagram() {
        val url = "https://www.instagram.com/icoello_/"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun irTwitter() {
        val url = "https://twitter.com/ICoelloC"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun irGithub() {
        val url = "https://github.com/ICoelloC"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        Shared.context = this
    }

    private fun irMainActivity() {
        val mainIntent = Intent(this, MainActivity::class.java).apply {}
        startActivity(mainIntent)
    }
}