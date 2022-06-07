package com.icoelloc.renter.screens

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.icoelloc.renter.R
import com.icoelloc.renter.utils.CirculoTransformacion
import com.squareup.picasso.Picasso
import org.w3c.dom.Text


class MyAccountFragment : Fragment() {


    private lateinit var txtNombreUsuario: TextView
    private lateinit var txtEmail: TextView
    private lateinit var imgPerfil: ImageView
    private lateinit var numAlquileres: TextView
    private lateinit var numPropiedades: TextView

    private lateinit var auth: FirebaseAuth

    private lateinit var firebaseFirestore :FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        auth = Firebase.auth

        val user = auth.currentUser
        firebaseFirestore = FirebaseFirestore.getInstance()

        firebaseFirestore.collection("Propiedades")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("XLR", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("XLR", "Error getting documents.", exception)
            }

        val root = inflater.inflate(R.layout.fragment_my_account, container, false)
        txtNombreUsuario = root.findViewById(R.id.myaccc_name)
        txtEmail = root.findViewById(R.id.myaccc_email)
        imgPerfil = root.findViewById(R.id.myacc_profpic)
        numAlquileres = root.findViewById(R.id.myacc_numalquiladas)
        numPropiedades = root.findViewById(R.id.myacc_numpropiedades)

        user?.let { inicializar(it) }

        return root

    }

    private fun inicializar(user: FirebaseUser) {
        cargarUsuarios(user)
    }

    private fun cargarUsuarios(user: FirebaseUser) {
        txtNombreUsuario.text = auth.currentUser?.displayName
        txtEmail.text = auth.currentUser?.email
        cargarImagenUsuario(auth.currentUser?.photoUrl)
        cargarNumPropiedadesAlquiladas(auth.currentUser?.email)
        cargarNumPropiedades(auth.currentUser?.email)

    }

    private fun cargarNumPropiedades(email: String?) {


        firebaseFirestore.collection("Propiedades").whereEqualTo("Propietario", email).get()
            .addOnSuccessListener {
                numPropiedades.text = it.size().toString()
            }
    }

    private fun cargarNumPropiedadesAlquiladas(email: String?) {
        firebaseFirestore.collection("Propiedades").whereEqualTo("Inquilino", email).get()
            .addOnSuccessListener {
                numAlquileres.text = it.size().toString()
            }
    }

    private fun cargarImagenUsuario(photoUrl: Uri?) {

        if (photoUrl != null) {
            Picasso.get()
                .load(photoUrl)
                .transform(CirculoTransformacion())
                .into(imgPerfil)
        }
    }

}