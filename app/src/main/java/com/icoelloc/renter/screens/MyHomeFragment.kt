package com.icoelloc.renter.screens

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.icoelloc.renter.R
import com.icoelloc.renter.objects.Property
import java.util.*


class MyHomeFragment : Fragment() {

    private lateinit var storage: FirebaseStorage

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private var misDomicilios = mutableListOf<Property>()
    private lateinit var misDomiciliosAdapter: PropertyListAdapter

    private var tagLog = "MyHome"

    private lateinit var personEmail: String

    private lateinit var usuario: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recyclerView = view?.findViewById<RecyclerView>(R.id.myhome_list)
        val adapter = PropertyListAdapter(misDomicilios)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = adapter

        val acct = GoogleSignIn.getLastSignedInAccount(activity)
        if (acct != null) {
            personEmail = acct.email.toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()

    }

    private fun initUI() {
        cargarDomicilios()

    }

    /*

    private fun getDatosLocalizacion(latitud: Double, longitud:Double){
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(this, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            latitud,
            longitud,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        val address: String =
            addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        val city: String = addresses[0].getLocality()
        val state: String = addresses[0].getAdminArea()
        val country: String = addresses[0].getCountryName()
        val postalCode: String = addresses[0].getPostalCode()
        val knownName: String = addresses[0].getFeatureName() // Only if available else return NULL
    }

     */
    private fun cargarDomicilios() {
        firebaseAuth = Firebase.auth
        firebaseFirestore = Firebase.firestore
        storage = Firebase.storage

        usuario = firebaseAuth.currentUser!!

        firebaseFirestore.collection("users").document(usuario.uid).collection("domicilios").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                Log.e(tagLog, "Error al cargar los domicilios", firebaseFirestoreException)
                return@addSnapshotListener
            }

            for (documentChange in querySnapshot!!.documentChanges) {
                when (documentChange.type) {
                    DocumentChange.Type.ADDED -> {
                        val property = documentChange.document.toObject(Property::class.java)
                        misDomicilios.add(property)
                        misDomiciliosAdapter.notifyDataSetChanged()
                    }
                    DocumentChange.Type.MODIFIED -> {
                        val property = documentChange.document.toObject(Property::class.java)
                        misDomicilios.add(property)
                        misDomiciliosAdapter.notifyDataSetChanged()
                    }
                    DocumentChange.Type.REMOVED -> {
                        val property = documentChange.document.toObject(Property::class.java)
                        misDomicilios.remove(property)
                        misDomiciliosAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    //CAMBIAR LOS PARÁMETROS DE LA PROPIEDAD estos parámetros son de la App de lugares

    /**
     * Devuelve un lugar de un documento (mapa)
     * @param doc Map<String, Any>
     * @return Lugar
     */
    private fun documentToLugar(doc: Map<String, Any>) = Property(
        id = doc["id"].toString(),
        nombre = doc["nombre"].toString(),
        latitud = doc["latitud"].toString(),
        longitud = doc["longitud"].toString(),
        inquilino = doc["inquilino"].toString(),
        propietario = doc["inquilino"].toString(),
        banios = doc["banios"].toString().toInt(),
        habitaciones = doc["habitaciones"].toString().toInt(),
    )

    /**
     * Inserta el dato en una lista
     * @param doc MutableMap<String, Any>
     */
    private fun insertarDocumento(doc: MutableMap<String, Any>) {
        val miDomicilio = documentToLugar(doc)
        Log.i(tagLog, "Añadiendo domicilio: ${miDomicilio.id}")
        val existe = misDomicilios.any { domicilio -> domicilio.id == miDomicilio.id }
        if (!existe)
            insertarItemLista(miDomicilio)
    }

    private fun insertarItemLista(item: Property) {
        this.misDomiciliosAdapter.addItem(item)
        misDomiciliosAdapter.notifyDataSetChanged()
    }


    /**
     * Visualiza la lista de items
     */
    private fun visualizarListaItems() {
        mostrarMisDomicilios()
        try {
            actualizarVistaLista()
        } catch (ex: Exception) {
        }
    }

    private fun actualizarVistaLista() {

    }

    private fun mostrarMisDomicilios() {
        firebaseFirestore.collection("Propiedades")
            .whereEqualTo("inquilino", usuario.email)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Toast.makeText(
                        context,
                        "Error al acceder al servicio: " + e.localizedMessage,
                        Toast.LENGTH_LONG
                    )
                        .show()
                    return@addSnapshotListener
                }

                for (dc in value!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            insertarDocumento(dc.document.data)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            actualizarItemLista(dc.document.data)
                        }
                        DocumentChange.Type.REMOVED -> {
                            eliminarItemLista(dc.document.data)
                        }
                    }
                }
            }
    }

    private fun eliminarItemLista(data: Map<String, Any>) {
        val id = data["id"].toString()
        val item = misDomicilios.find { domicilio -> domicilio.id == id }
        if (item != null) {
            misDomicilios.remove(item)
            misDomiciliosAdapter.notifyDataSetChanged()
        }
    }

    private fun actualizarItemLista(data: Map<String, Any>) {
        val miDomicilio = documentToLugar(data)
        val index = misDomicilios.indexOfFirst { domicilio -> domicilio.id == miDomicilio.id }
        if (index != -1) {
            misDomicilios[index] = miDomicilio
            misDomiciliosAdapter.notifyItemChanged(index)
        }
    }
}