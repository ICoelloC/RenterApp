package com.icoelloc.renter.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.icoelloc.renter.R
import com.icoelloc.renter.main.MainActivity
import com.icoelloc.renter.objects.Shared


class LoginActivity : AppCompatActivity() {

    public var gRcSignIn = 1
    private lateinit var mGoogleSignInClient:GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {

        Thread.sleep(2000)
        setTheme(R.style.Theme_Renter)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Shared.context = this
        val btnLogin = findViewById<Button>(R.id.btn_login_googleSignIn)
        val btnTwitter = findViewById<ImageView>(R.id.login_img_twitter)
        val btnGithub = findViewById<ImageView>(R.id.login_img_github)
        val btnInstagram = findViewById<ImageView>(R.id.login_img_instagram)

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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == gRcSignIn) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            irMainActivity()
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("GSO", "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun loginGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, gRcSignIn)
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