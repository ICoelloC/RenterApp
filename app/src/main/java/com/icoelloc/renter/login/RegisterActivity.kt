package com.icoelloc.renter.login

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.icoelloc.renter.R
import com.icoelloc.renter.utils.Utils

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private var txtPassword: String = ""
    private var txtEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val registerBtnRegistrarse: Button = findViewById(R.id.register_register)
        val registerLinkSignIn: TextView = findViewById<Button>(R.id.register_signin_link)

        registerLinkSignIn.setOnClickListener {
            goLoginScreen()
        }

        registerBtnRegistrarse.setOnClickListener {
            checkInputs()
            createAccount()
        }

        initUI()
    }

    private fun createAccount() {
        val registerEmail: TextView = findViewById(R.id.register_email)
        val registerPassword: TextView = findViewById(R.id.register_password)

        txtPassword = Utils.encrypt(registerPassword.text.toString().trim())!!
        txtEmail = registerEmail.text.toString().trim()
        Log.d("Firebase", "createAccount:$txtEmail")
        auth.createUserWithEmailAndPassword(txtEmail, txtPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("fairbase", "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateProfile(user!!)
                    startLogin()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("fairbase", "createUserWithEmail:failure", task.exception)
                    registerEmail.error = resources.getString(R.string.isAlreadyExist)
                }
            }
    }

    private fun startLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun updateProfile(user: FirebaseUser) {

        val registerUsername: TextView = findViewById(R.id.register_username)

        val profileUpdates = userProfileChangeRequest {
            displayName = registerUsername.text.toString()
        }
        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "User profile updated.")
                }
            }
    }

    private fun initUI() {
        storage = Firebase.storage
        auth = Firebase.auth
    }


    private fun checkInputs() {
        val email = findViewById<TextView>(R.id.register_email)
        val password = findViewById<TextView>(R.id.register_password)
        //check if email and password are not empty
        if (email.text.isEmpty() || password.text.isEmpty()) {
            Utils.showToast(this, "Please fill all fields")
        } else {
            //check if email is valid
            if (!Utils.isEmailValid(email.text.toString())) {
                Utils.showToast(this, "Please enter a valid email")
            } else {
                if (!Utils.isPasswordValid(password.text.toString())) {
                    Utils.showToast(this, "Please enter a valid password")
                }
            }
        }
    }

    private fun goLoginScreen() {
        intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}