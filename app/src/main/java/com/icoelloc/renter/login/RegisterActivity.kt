package com.icoelloc.renter.login

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.icoelloc.renter.R
import com.icoelloc.renter.main.MainActivity
import com.icoelloc.renter.objects.Shared
import com.icoelloc.renter.objects.User
import com.icoelloc.renter.utils.Utils

class RegisterActivity : AppCompatActivity() {

    private val signInLink = findViewById<TextView>(R.id.register_signin_link)
    private val registerButton = findViewById<TextView>(R.id.register_register)

    val email = findViewById<TextView>(R.id.register_email)!!
    private val password = findViewById<TextView>(R.id.register_password)

    private val fireAuth: FirebaseAuth = Firebase.auth
    private lateinit var storage: FirebaseStorage

    private lateinit var txtPassword: String
    private lateinit var txtEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)




        initUI()

    }

    private fun initUI() {
        storage = Firebase.storage
        initButtons()
    }

    private fun initButtons() {
        signInLink.setOnClickListener {
            goLoginScreen()
        }

        registerButton.setOnClickListener {
            checkInputs()
            insertarUsuario()
        }
    }

    private fun checkInputs() {
        val email = findViewById<TextView>(R.id.register_email)
        val password = findViewById<TextView>(R.id.register_password)
        //check if email and password are not empty
        if (email.text.isEmpty() || password.text.isEmpty()) {
            Utils.showToast(this, "Please fill all fields")
        }else{
            //check if email is valid
            if (!Utils.isEmailValid(email.text.toString())) {
                Utils.showToast(this, "Please enter a valid email")
            }else{
                if (!Utils.isPasswordValid(password.text.toString())) {
                    Utils.showToast(this, "Please enter a valid password")
                }
            }
        }
    }

    private fun insertarUsuario() {
        txtPassword = Utils.encrypt(password.text.toString().trim()).toString()
        txtEmail = email.text.toString().trim()
        if (!isCorrect(txtEmail)) {
            return
        }
        Log.d("Firebase", "createAccount:$txtEmail")
        fireAuth.createUserWithEmailAndPassword(txtEmail, txtPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("firebase", "createUserWithEmail:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("firebase", "createUserWithEmail:failure", task.exception)
                    email.error = resources.getString(R.string.isAlreadyExist)
                }
            }

        Shared.SignInMode = "NORMAL"
    }

    private fun isCorrect(txtEmail: String): Boolean {
        var valide = false
        if (Utils.isNetworkAvailable(this)) {
            valide = true
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
        return valide
    }

    private fun registerAction() {

        val email = findViewById<TextView>(R.id.register_email)
        val password = findViewById<TextView>(R.id.register_password)

        fireAuth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    goMainActivity()
                } else {
                    Utils.showToast(this, "Authentication failed.")
                }
            }

        Shared.SignInMode = "NORMAL"
    }

    private fun goMainActivity() {
        intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun goLoginScreen() {
        intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}