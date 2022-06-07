package com.icoelloc.renter.screens

import android.graphics.*
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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


class MyPropertiesFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
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
        return inflater.inflate(R.layout.fragment_my_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        fireStore = FirebaseFirestore.getInstance()
        this.usuario = auth.currentUser!!
        initUI()
    }

    private fun initUI() {
        iniciarSwipeRefresh()
        cargarDomicilios()
        iniciarSwipeHorizontal()
        domiciliosRecycler.layoutManager = LinearLayoutManager(context)
        domiciliosFabNuevo.setOnClickListener { nuevoElemento() }
    }

    private fun iniciarSwipeRefresh() {
        domiciliosSwipeRefresh.setColorSchemeColors(
            resources.getColor(R.color.renter_nav_drawer)
        )
    }

    private fun iniciarSwipeHorizontal() {
        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT or
                        ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    when (direction) {
                        ItemTouchHelper.LEFT -> {
                            borrarElemento(position)
                        }
                        else -> {
                            editarElemento(position)
                        }
                    }
                }

                override fun onChildDraw(
                    canvas: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean,
                ) {
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        val itemView = viewHolder.itemView
                        val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                        val width = height / 3
                        if (dX > 0) {
                            botonIzquierdo(canvas, dX, itemView, width)
                        } else {
                            botonDerecho(canvas, dX, itemView, width)
                        }
                    }
                    super.onChildDraw(
                        canvas,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }
        // Añadimos los eventos al RV
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(domiciliosRecycler)
    }

    private fun botonDerecho(canvas: Canvas, dX: Float, itemView: View, width: Float) {
        paintSweep.color = Color.RED
        val background = RectF(
            itemView.right.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.trash)
        val iconDest = RectF(
            itemView.right.toFloat() - 2 * width,
            itemView.top.toFloat() + width,
            itemView.right.toFloat() - width,
            itemView.bottom.toFloat() - width
        )
        canvas.drawBitmap(icon, null, iconDest, paintSweep)
    }

    private fun botonIzquierdo(canvas: Canvas, dX: Float, itemView: View, width: Float) {
        paintSweep.color = Color.GREEN
        val background = RectF(
            itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.editing)
        val iconDest = RectF(
            itemView.left.toFloat() + width,
            itemView.top.toFloat() + width,
            itemView.left.toFloat() + 2 * width,
            itemView.bottom.toFloat() - width
        )
        canvas.drawBitmap(icon, null, iconDest, paintSweep)
    }


    private fun nuevoElemento() {
        abrirDetalle(null, Modo.INSERTAR)
    }

    private fun insertarItemLista(item: Property) {
        this.domiciliosAdapter.addItem(item)
        domiciliosAdapter.notifyDataSetChanged()
    }

    private fun editarElemento(position: Int) {
        Log.i(TAG, "Editando el elemento pos: $position")
        actualizarVistaLista()

        if (domicilios[position].propietario == auth.currentUser?.email) {
            abrirDetalle(domicilios[position], Modo.ACTUALIZAR)
        } else {
            Toast.makeText(
                requireContext(),
                "No eres el propietario del domicilio",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun actualizarItemLista(item: Property, position: Int) {
        this.domiciliosAdapter.updateItem(item, position)
        domiciliosAdapter.notifyDataSetChanged()
    }

    private fun borrarElemento(position: Int) {
        Log.i(TAG, "Borrando el elemento pos: $position")
        if (domicilios[position].propietario == auth.currentUser?.email && domicilios[position].inquilino == "") {
            abrirDetalle(domicilios[position], Modo.ELIMINAR)
        } else {
            Toast.makeText(requireContext(), "No tienes permiso para borrarlo", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun eliminarItemLista(position: Int) {
        this.domiciliosAdapter.removeItem(position)
        domiciliosAdapter.notifyDataSetChanged()
    }

    private fun actualizarVistaLista() {
        domiciliosRecycler.adapter = domiciliosAdapter
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
        domiciliosSwipeRefresh.isRefreshing = true
        domiciliosAdapter = PropertyListAdapter(domicilios) {
            eventoClicFila(it)
        }
        domiciliosRecycler.adapter = domiciliosAdapter
        fireStore.collection("Propiedades").whereEqualTo("Propuietario", usuario.email)
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