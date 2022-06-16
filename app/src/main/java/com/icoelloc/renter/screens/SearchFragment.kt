package com.icoelloc.renter.screens

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.icoelloc.renter.R
import com.icoelloc.renter.objects.Property
import com.icoelloc.renter.utils.Modo
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore

    private var domicilios = mutableListOf<Property>()

    private lateinit var domiciliosAdapter: PropertyListAdapter
    private lateinit var usuario: FirebaseUser


    companion object {
        private const val TAG = "MisDomiciliosFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        fireStore = FirebaseFirestore.getInstance()
        this.usuario = auth.currentUser!!
        initUI()
    }

    /**
     * inicilizamos la interfaz, al escribir en el buscador mostraremos las viviendas que se encuentren
     * en esa localidad o cadena superiores de tamaño, por ejemplo si tenemos una localidad en
     * Argamasilla de Calatrava, y escribimos Argamasilla de, mostrará primero Argamasilla de Calatrava o de Alba,
     * pero si hay alguna localidad cuya cadena sea más grande aparecerá también, pero primero mostrará anteriormente
     * las dichas
     */
    private fun initUI() {
        cargarDomicilios()
        domiciliosSearchRecycler.layoutManager = LinearLayoutManager(context)

        /*buscadorVerDisponiblesBTN.setOnClickListener {
            mostrarTodosLosDomiciliosDisponibles()
        }*/


        buscadorInputLocalidad.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                domicilios.clear()
                domiciliosAdapter = PropertyListAdapter(domicilios) {
                    eventoClicFila(it)
                }
                domiciliosSearchRecycler.adapter = domiciliosAdapter
                fireStore.collection("Propiedades")
                    .whereGreaterThanOrEqualTo("localidad", buscadorInputLocalidad.text.toString())
                    .get().addOnSuccessListener { value ->
                        for (doc in value!!.documentChanges) {
                            Log.i("Resultado = ", doc.document.data.toString())
                            when (doc.type) {
                                DocumentChange.Type.ADDED -> {
                                    insertarDocumento(doc.document.data)
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    modificarDocumento(doc.document.data)
                                }
                                DocumentChange.Type.REMOVED -> {
                                    eliminarDocumento(doc.document.data)
                                }
                            }
                        }
                    }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {


            }
        })


    }

    private fun mostrarTodosLosDomiciliosDisponibles() {
        buscadorInputLocalidad.setText("")
        domicilios.clear()
        domiciliosAdapter = PropertyListAdapter(domicilios) {
            eventoClicFila(it)
        }
        domiciliosSearchRecycler.adapter = domiciliosAdapter
        val query = fireStore.collection("Propiedades")
        query.get().addOnSuccessListener { value ->
            for (doc in value!!.documentChanges) {
                Log.i("Resultado = ", doc.document.data.toString())
                when (doc.type) {
                    DocumentChange.Type.ADDED -> {
                        insertarDocumento(doc.document.data)
                    }
                    DocumentChange.Type.MODIFIED -> {
                        modificarDocumento(doc.document.data)
                    }
                    DocumentChange.Type.REMOVED -> {
                        eliminarDocumento(doc.document.data)
                    }
                }
            }
        }
    }

    private fun insertarItemLista(item: Property) {
        this.domiciliosAdapter.addItem(item)
        domiciliosAdapter.notifyDataSetChanged()
    }

    private fun actualizarItemLista(item: Property, position: Int) {
        this.domiciliosAdapter.updateItem(item, position)
        domiciliosAdapter.notifyDataSetChanged()
    }

    private fun eliminarItemLista(position: Int) {
        this.domiciliosAdapter.removeItem(position)
        domiciliosAdapter.notifyDataSetChanged()
    }

    private fun actualizarVistaLista() {
        domiciliosSearchRecycler.adapter = domiciliosAdapter
    }

    private fun abrirElemento(domicilio: Property) {
        abrirDetalle(domicilio)
    }

    /**
     * al pulsar en la imagen abrimos la vivienda para mostrar la información de está
     */
    private fun abrirDetalle(domicilio: Property?) {
        val estadioDetalle = PropertyFullDataFragment(domicilio, Modo.VISUALIZAR)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.nav_host_fragment, estadioDetalle)
        transaction.addToBackStack(null)
        transaction.commit()
        actualizarVistaLista()
    }

    private fun eventoClicFila(domicilio: Property) {
        abrirElemento(domicilio)
    }

    /**
     * Cargamos todos los domicilios
     */
    private fun cargarDomicilios() {
        domicilios.clear()
        domiciliosAdapter = PropertyListAdapter(domicilios) {
            eventoClicFila(it)
        }
        domiciliosSearchRecycler.adapter = domiciliosAdapter
        fireStore.collection("Propiedades")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Toast.makeText(
                        context,
                        "Error al acceder al servicio: " + e.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }
                for (doc in value!!.documentChanges) {
                    when (doc.type) {
                        // Documento agregado
                        DocumentChange.Type.ADDED -> {
                            insertarDocumento(doc.document.data)
                        }
                        // Documento modificado
                        DocumentChange.Type.MODIFIED -> {
                            modificarDocumento(doc.document.data)
                        }
                        // Documento eliminado
                        DocumentChange.Type.REMOVED -> {
                            eliminarDocumento(doc.document.data)
                        }
                    }
                }
            }
    }

    private fun eliminarDocumento(doc: Map<String, Any>) {
        val miDomicilio = documentToDomicilio(doc)
        Log.i(TAG, "Eliminando lugar: ${miDomicilio.id}")
        val index = domicilios.indexOf(miDomicilio)
        if (index >= 0)
            eliminarItemLista(index)
    }

    private fun modificarDocumento(doc: Map<String, Any>) {
        val miEstadio = documentToDomicilio(doc)
        Log.i(TAG, "Modificando lugar: ${miEstadio.id}")
        val index = domicilios.indexOf(miEstadio)
        if (index >= 0)
            actualizarItemLista(miEstadio, index)
    }

    private fun documentToDomicilio(doc: Map<String, Any>) = Property(
        id = doc["id"].toString(),
        nombre = doc["nombre"].toString(),
        latitud = doc["latitud"].toString(),
        longitud = doc["longitud"].toString(),
        localidad = doc["localidad"].toString(),
        inquilino = doc["inquilino"].toString(),
        telefono = doc["telefono"].toString(),
        propietario = doc["propietario"].toString(),
        banios = doc["banios"]?.toString()?.toInt() ?: 0,
        habitaciones = doc["habitaciones"]?.toString()?.toInt() ?: 0,
        metros = doc["metros"]?.toString()?.toInt() ?: 0,
        precio = doc["precio"]?.toString()?.toInt() ?: 0,
        foto1 = doc["foto1"].toString(),
        foto2 = doc["foto2"].toString(),
        foto3 = doc["foto3"].toString(),
        foto4 = doc["foto4"].toString(),
        foto5 = doc["foto5"].toString()
    )

    private fun insertarDocumento(doc: MutableMap<String, Any>) {
        val miDomicilio = documentToDomicilio(doc)
        val existe = domicilios.any { dom -> dom.id == miDomicilio.id }
        if (!existe) {
            insertarItemLista(miDomicilio)
        }
    }


}