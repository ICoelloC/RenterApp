package com.icoelloc.renter.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.icoelloc.renter.R


class MyAccountFragment : Fragment() {


    private lateinit var txtNombreUsuario: TextView
    private lateinit var txtEmail: TextView
    private lateinit var imgPerfil: ImageView

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        auth = Firebase.auth

        val user = auth.currentUser

        val root = inflater.inflate(R.layout.fragment_my_account, container, false)
        txtNombreUsuario = root.findViewById(R.id.myaccc_name)
        txtEmail = root.findViewById(R.id.myaccc_email)
        imgPerfil = root.findViewById(R.id.myacc_profpic)

        user?.let { inicializar(it) }

        return root

    }

    private fun inicializar(user: FirebaseUser) {
        cargarUsuarios(user)
    }

    private fun cargarUsuarios(user: FirebaseUser) {
        txtNombreUsuario.text = auth.currentUser?.displayName
        txtEmail.text = auth.currentUser?.email
        imgPerfil.setImageURI(auth.currentUser?.photoUrl)
    }

}