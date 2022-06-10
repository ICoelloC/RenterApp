package com.icoelloc.renter.screens

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.icoelloc.renter.R
import com.icoelloc.renter.objects.Property
import com.icoelloc.renter.utils.Modo
import kotlinx.android.synthetic.main.fragment_property_full_data.*
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var fireStore: FirebaseFirestore

    private var domicilios = mutableListOf<Property>()

    private lateinit var domiciliosAdapter: PropertyListAdapter
    private var paintSweep = Paint()
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

    private fun initUI() {
        cargarDomicilios()
        domiciliosSearchRecycler.layoutManager = LinearLayoutManager(context)

        buscadorVerDisponiblesBTN.setOnClickListener {
            mostrarTodosLosDomiciliosDisponibles()
        }

        buscadorBuscarBTN.setOnClickListener {
            buscarDomiciliosPorParametro()
        }
/*
        buscadorInputLocalidad.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

                buscarDomiciliosPorParametro()

            }
        })
        
 */
    }

    private fun mostrarTodosLosDomiciliosDisponibles() {
        /*buscadorInputMetros.setText("")
        buscadorInputPrecio.setText("")
        buscadorInputHabitaciones.setText("")
        buscadorInputBanios.setText("")*/
        buscadorInputLocalidad.setText("")
        domicilios.clear()
        domiciliosAdapter = PropertyListAdapter(domicilios) {
            eventoClicFila(it)
        }
        domiciliosSearchRecycler.adapter = domiciliosAdapter
        val query = fireStore.collection("Propiedades").whereEqualTo("inquilino", "")
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

    private fun comprobarFormulario(): Boolean {
        var sal = true
        if (buscadorInputLocalidad.text?.isEmpty()!!) {
            buscadorInputLocalidad.error = "Introduzca la localidad"
            sal = false
        }
        /*
        if (buscadorInputMetros.text?.isEmpty()!!) {
            buscadorInputMetros.error = "Introduzca el mínimo de metros cuadrados"
            sal = false
        }
        if (buscadorInputPrecio.text?.isEmpty()!!) {
            buscadorInputPrecio.error = "Introduzca el máximo que quiera pagar por el domicilio"
            sal = false
        }
        if (buscadorInputBanios.text?.isEmpty()!!) {
            buscadorInputBanios.error = "Introduzca el mínimo número de baños"
            sal = false
        }
        if (buscadorInputHabitaciones.text?.isEmpty()!!) {
            buscadorInputHabitaciones.error = "Introduzca el mínimo número de habitaciones"
            sal = false
        }
        */
        return sal
    }

    private fun buscarDomiciliosPorParametro() {
        if (comprobarFormulario()){
            domicilios.clear()
            domiciliosAdapter = PropertyListAdapter(domicilios) {
                eventoClicFila(it)
            }
            domiciliosSearchRecycler.adapter = domiciliosAdapter
            var localidad = buscadorInputLocalidad.text.toString()
            val query = fireStore.collection("Propiedades").whereEqualTo("inquilino", "").whereGreaterThanOrEqualTo("localidad", localidad)
            /*
            query.whereLessThanOrEqualTo("precio", buscadorInputPrecio.text.toString().toInt())
                .whereEqualTo("metros", buscadorInputMetros.text.toString().trim().toInt())
                .whereEqualTo("habitaciones", buscadorInputHabitaciones.text.toString().trim().toInt())
                .whereEqualTo("banios", buscadorInputBanios.text.toString().trim())
             */
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
        abrirDetalle(domicilio, Modo.VISUALIZAR)
    }

    private fun abrirDetalle(domicilio: Property?, modo: Modo?) {
        val estadioDetalle = PropertyFullDataFragment(domicilio, modo)
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

    private fun cargarDomicilios() {
        domicilios.clear()
        domiciliosAdapter = PropertyListAdapter(domicilios) {
            eventoClicFila(it)
        }
        domiciliosSearchRecycler.adapter = domiciliosAdapter
        fireStore.collection("Propiedades").whereEqualTo("inquilino", "")
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