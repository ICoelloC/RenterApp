package com.icoelloc.renter.login

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.icoelloc.renter.R
import com.icoelloc.renter.main.MainActivity
import com.icoelloc.renter.objects.Shared
import com.icoelloc.renter.utils.Utils


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignIn = 333

    private var email: String = ""
    private var pass: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {

        Thread.sleep(2000)
        setTheme(R.style.Theme_Renter)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        initGoogle()
        Shared.context = this
        initButtons()

        procesarSesiones()
    }

    private fun procesarSesiones() {
        // Vemos si hay sesión
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.i("Login", "SÍ hay sesión activa")
            Toast.makeText(baseContext, "Auth: Sesión activa", Toast.LENGTH_SHORT).show()
            abrirMain()
        } else {
            Log.i("Login", "NO hay sesión activa")
        }
    }

    private fun initButtons() {

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

    private fun initGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.req_id_token))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()
    }

    private fun normalLogin() {

        val loginEmail = findViewById<TextView>(R.id.login_email)
        val loginPassword = findViewById<TextView>(R.id.login_password)


        email = loginEmail.text.toString().trim()
        pass = Utils.encrypt(loginPassword.text.toString().trim())!!

        if (checkEmpty(email, pass)) {
            if (Utils.isNetworkAvailable(this)) {
                userExists(email, pass)
            } else {
                val snackbar = Snackbar.make(
                    findViewById(android.R.id.content),
                    R.string.no_net,
                    Snackbar.LENGTH_INDEFINITE
                )
                snackbar.setActionTextColor(getColor(R.color.renter_nav_drawer))
                snackbar.setAction("Conectar") {
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    startActivity(intent)
                    finish()
                }
                snackbar.show()
            }
            Log.i("realm", "usuario logeado")
        }
    }

    private fun userExists(email: String, pass: String) {

        val loginEmail = findViewById<TextView>(R.id.login_email)
        val loginPassword = findViewById<TextView>(R.id.login_password)


        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.i("Firebase", "signInWithEmail:success")
                    val user = auth.currentUser
                    Log.i("Firebase", user.toString())
                    abrirMain()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Firebase", "signInWithEmail:failure", task.exception)
                    loginEmail.error = resources.getString(R.string.userNotCorrect)
                }

            }
    }

    private fun checkEmpty(email: String, pass: String): Boolean {
        return email.isNotEmpty() && pass.isNotEmpty()
    }

    private fun goRegisterScreen() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == googleSignIn) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {

                val account = task.getResult(ApiException::class.java)
                if (account != null) {

                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                abrirMain()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Error al iniciar sesion con Google",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                }

            } catch (e: ApiException) {
                Toast.makeText(this, "Error al iniciar sesion con Google", Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }

    private fun abrirMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun loginGoogle() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, googleSignIn)
    }

    override fun onResume() {
        super.onResume()
        Shared.context = this
    }
}