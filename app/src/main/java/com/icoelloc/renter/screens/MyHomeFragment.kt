package com.icoelloc.renter.screens

import android.os.Bundle
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
import kotlinx.android.synthetic.main.fragment_my_home.*


class MyHomeFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_my_home, container, false)
    }

    override fun onResume() {
        super.onResume()
        auth = Firebase.auth
        fireStore = FirebaseFirestore.getInstance()
        this.usuario = auth.currentUser!!
        initUI()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        fireStore = FirebaseFirestore.getInstance()
        this.usuario = auth.currentUser!!
        initUI()
    }

    /**
     * inicializamos ls interfaz
     */
    private fun initUI() {
        iniciarSwipeRefresh()
        cargarDomicilios()
        //iniciarSwipeHorizontal()
        domiciliosRecycler.layoutManager = LinearLayoutManager(context)
    }

    /**
     * inicializamos el swipe refresh que nos permitirá actualizar la lista de domicilios
     */
    private fun iniciarSwipeRefresh() {
        domiciliosSwipeRefresh.setColorSchemeResources(R.color.renter_nav_drawer_header)
        domiciliosSwipeRefresh.setProgressBackgroundColorSchemeResource(R.color.white)
        domiciliosSwipeRefresh.setOnRefreshListener {
            cargarDomicilios()
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
        domiciliosRecycler.adapter = domiciliosAdapter
    }

    private fun abrirElemento(domicilio: Property) {
        abrirDetalle(domicilio)
    }
    /**
     * Abriremos la pantalla de la vivienda en modo de visualizar
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
     * mostramos los domicilios cuyo inquilino sea el mismo de la sesión actual
     * Insertamos,modificamos o eliminamos elementos de la lista
     */
    private fun cargarDomicilios() {
        domicilios.clear()
        domiciliosSwipeRefresh.isRefreshing = true
        domiciliosAdapter = PropertyListAdapter(domicilios) {
            eventoClicFila(it)
        }
        domiciliosRecycler.adapter = domiciliosAdapter
        fireStore.collection("Propiedades").whereEqualTo("inquilino", usuario.email)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Toast.makeText(
                        context,
                        "Error al acceder al servicio: " + e.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }
                domiciliosSwipeRefresh.isRefreshing = false
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
        val miDomicilio = documentToDomicilio(doc)
        Log.i(TAG, "Modificando lugar: ${miDomicilio.id}")
        val index = domicilios.indexOf(miDomicilio)
        if (index >= 0)
            actualizarItemLista(miDomicilio, index)
    }
    /**
     * Dibujamos el fondo al deslizar para borrar
     */
    private fun documentToDomicilio(doc: Map<String, Any>) = Property(
        id = doc["id"].toString(),
        nombre = doc["nombre"].toString(),
        latitud = doc["latitud"].toString(),
        localidad = doc["localidad"].toString(),
        longitud = doc["longitud"].toString(),
        inquilino = doc["inquilino"].toString(),
        telefono = doc["telefono"].toString(),
        propietario = doc["propietario"].toString(),
        banios = doc["banios"]?.toString()?.toInt() ?: 0,
        habitaciones = doc["habitaciones"]?.toString()?.toInt() ?: 0,
        metros = doc["metros"]?.toString()?.toInt() ?: 0,
        precio = doc["precio"]?.toString()?.toInt() ?: 0,
        foto1 = doc["foto1"].toString()
    )

    private fun insertarDocumento(doc: MutableMap<String, Any>) {
        val miDomicilio = documentToDomicilio(doc)
        val existe = domicilios.any { dom -> dom.id == miDomicilio.id }
        if (!existe) {
            insertarItemLista(miDomicilio)
        }
    }


}