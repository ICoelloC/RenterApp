package com.icoelloc.renter.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.icoelloc.renter.R
import com.icoelloc.renter.main.MainActivity
import com.icoelloc.renter.objects.Shared
import com.icoelloc.renter.utils.Utils


class LoginActivity : AppCompatActivity() {

    private var gRcSignIn = 1
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val fireAuth: FirebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {

        Thread.sleep(2000)
        setTheme(R.style.Theme_Renter)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (fireAuth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.req_id_token))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        Shared.context = this
        val linkSignUp = findViewById<TextView>(R.id.login_signup_link)
        val btnLogin = findViewById<Button>(R.id.btn_login_googleSignIn)
        val btnTwitter = findViewById<ImageView>(R.id.login_img_twitter)
        val btnGithub = findViewById<ImageView>(R.id.login_img_github)
        val btnInstagram = findViewById<ImageView>(R.id.login_img_instagram)
        val normalLogin = findViewById<Button>(R.id.normal_login)

        linkSignUp.setOnClickListener {
            goRegisterScreen()
        }

        btnLogin.setOnClickListener {
            loginGoogle()
        }

        btnGithub.setOnClickListener {
            Utils.abrirURL(this, "https://github.com/ICoelloC")
        }

        btnTwitter.setOnClickListener {
            Utils.abrirURL(this, "https://twitter.com/ICoelloC")
        }

        btnInstagram.setOnClickListener {
            Utils.abrirURL(this, "https://www.instagram.com/icoello_/")
        }

        normalLogin.setOnClickListener {
            normalLogin()
        }

    }

    private fun normalLogin() {

        val email = findViewById<TextView>(R.id.login_email).text.toString()
        val password = findViewById<TextView>(R.id.login_password).text.toString()
        fireAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                    Utils.showToast(this, "Authentication failed.")
                }
            }
    }

    private fun goRegisterScreen() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == gRcSignIn) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            completedTask.getResult(ApiException::class.java)
            irMainActivity()
        } catch (e: ApiException) {
            Log.w("GSO", "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun loginGoogle() {
       //login with google
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, gRcSignIn)
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