package com.icoelloc.renter.screens

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.icoelloc.renter.R
import com.icoelloc.renter.objects.Property
import com.icoelloc.renter.utils.Modo
import com.icoelloc.renter.utils.MyApp
import com.icoelloc.renter.utils.PhotosUtils
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_property_full_data.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Double
import java.util.*
import kotlin.Boolean
import kotlin.Exception
import kotlin.Int
import kotlin.String
import kotlin.arrayOf
import kotlin.isInitialized
import kotlin.toString
import kotlin.with

class PropertyFullDataFragment(
    private var domicilio: Property? = null,
    private val modo: Modo? = Modo.INSERTAR
) : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var usuario: FirebaseUser
    private var permisos: Boolean = false

    private lateinit var mMap: GoogleMap
    private var mPosicion: FusedLocationProviderClient? = null
    private var marcadorTouch: Marker? = null
    private var localizacion: Location? = null
    private var posicion: LatLng? = null

    private val galeria = 1
    private val camara = 2
    private lateinit var imagenURI: Uri
    private val imagenDirectorio = "/Renter"
    private val imagenProporcion = 600
    private lateinit var foto: Bitmap
    private var imagenCompresion = 60
    private val imagenPrefijo = "domicilio"
    private val imagenExtension = ".jpg"

    companion object {
        private const val TAG = "Propiedad"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_property_full_data, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        fireStore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        Log.i(TAG, "Creando Domicilio Detalle")
        view.setOnTouchListener { _, _ ->
            return@setOnTouchListener true
        }
        initIU()
    }

    /**
     * M??todo para inicialzar la interfaz de usuario
     * 1?? inicializamos de nuevo los permisos si hiciera falta alguno
     * 2?? obtenemos el usuario actual Firebase, a la hora de asignar propietario de la vivienda
     * 3?? seg??n el modo de visualizaci??n de la interfaz mostraremos algunos campos o no
     * 4?? leemos nuesta posici??n actual
     * 5?? inicializamos el mapa
     */
    private fun initIU() {
        initPermisos()
        initUsuario()
        when (this.modo) {
            Modo.INSERTAR -> initModoInsertar()
            Modo.VISUALIZAR -> initModoVisualizar()
            Modo.ELIMINAR -> initModoEliminar()
            Modo.ACTUALIZAR -> initModoActualizar()
            else -> {
            }
        }
        leerPoscionGPSActual()
        initMapa()
    }

    private fun initUsuario() {
        this.usuario = auth.currentUser!!
    }

    private fun initPermisos() {
        this.permisos = (activity?.application as MyApp).appPermisos
    }

    /**
     * Modo insertar
     * Asignamos un valor por defecto  a los campos, e inicializamos los botones con sus acciones necesarias
     */
    private fun initModoInsertar() {
        detalleDomicilioInputNombre.setText("")
        detalleDomicilioInputContacto.setText("")
        detalleDomicilioInputMetros.setText("0")
        detalleDomicilioInputPrecio.setText("0")
        detalleDomicilioInputHabitaciones.setText("1")
        detalleDomicilioInputBanios.setText("1")
        detalleDomicilioInputInquilino.setText("")
        detalleDomicilioEditarBtn.visibility = View.GONE
        detalleDomicilioBorrarBtn.visibility = View.GONE
        icono_telefono.visibility = View.GONE
        itemDetalleLL.visibility = View.GONE
        detalleDomicilioGuardarBtn.setOnClickListener { insertarDomicilio() }
        detalleDomicilioFabCamara.setOnClickListener { elegirFotoGaleria() }
        //detalleDomicilioFabCamara.setOnClickListener { initDialogFoto() }

    }

    /**
     * Modo visualizar
     * Recogemos los valores del domicilio y los mostramos en los campos pertinentes, y los hacemos
     * no editables, ocultamos los botones para que no puedane hacer nada.
     * Si pulsamos en el tel??fono podremos llamar por tel??fono
     * Cargamos la foto de la vivienda
     */
    private fun initModoVisualizar() {

        detalleDomicilioInputNombre.setText(domicilio?.nombre)
        detalleDomicilioInputNombre.isEnabled = false
        detalleDomicilioInputContacto.setText(domicilio?.telefono)
        detalleDomicilioInputContacto.isEnabled = false
        detalleDomicilioInputMetros.setText(domicilio?.metros.toString())
        detalleDomicilioInputMetros.isEnabled = false
        detalleDomicilioInputPrecio.setText(domicilio?.precio.toString())
        detalleDomicilioInputPrecio.isEnabled = false
        detalleDomicilioInputHabitaciones.setText(domicilio?.habitaciones.toString())
        detalleDomicilioInputHabitaciones.isEnabled = false
        detalleDomicilioInputBanios.setText(domicilio?.banios.toString())
        detalleDomicilioInputBanios.isEnabled = false
        detalleDomicilioInputInquilino.setText(domicilio?.inquilino.toString())
        detalleDomicilioInputInquilino.isEnabled = false
        detalleDomicilioEditarBtn.visibility = View.GONE
        detalleDomicilioBorrarBtn.visibility = View.GONE
        detalleDomicilioGuardarBtn.visibility = View.GONE
        detalleDomicilioFabCamara.visibility = View.GONE
        if (domicilio?.telefono != null) {
            detalleDomicilioPropietarioTelefono.text = domicilio?.telefono
            detalleDomicilioPropietarioTelefono.setOnClickListener {
                llamarPorTelefono()
            }
        }
        cargarFoto()

        detalleDomicilioEditarBtn.setOnClickListener { insertarDomicilio() }
    }

    private fun llamarPorTelefono() {
        val i = Intent(
            Intent.ACTION_CALL,
            Uri.parse("tel:" + domicilio?.telefono)
        )
        startActivity(i)
    }

    private fun cargarFoto() {
        val docRef = fireStore.collection("Propiedades").document(domicilio?.id.toString())
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    if (domicilio?.foto1 != "") {
                        Picasso.get()
                            .load(domicilio?.foto1)
                            .into(
                                itemDetalleDomicilioFoto1,
                                object : com.squareup.picasso.Callback {
                                    override fun onSuccess() {
                                        foto =
                                            (itemDetalleDomicilioFoto1.drawable as BitmapDrawable).bitmap
                                    }

                                    override fun onError(ex: Exception?) {
                                        Log.i(TAG, "Error: Descargar fotografia Picasso")
                                    }
                                })
                    } else {
                        imagenPorDefecto()
                    }

                } else {
                    Log.i(TAG, "Error: No exite fotograf??a")
                    imagenPorDefecto()
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "ERROR: " + exception.localizedMessage)
                imagenPorDefecto()
            }
    }

    private fun imagenPorDefecto() {
        itemDetalleDomicilioFoto1.setImageBitmap(
            BitmapFactory.decodeResource(
                context?.resources,
                R.drawable.renta
            )
        )
    }

    private fun initModoEliminar() {
        Log.i(TAG, "Modo Eliminar")
        initModoVisualizar()
        detalleDomicilioBorrarBtn.visibility = View.VISIBLE
        detalleDomicilioBorrarBtn.setOnClickListener { eliminarDomicilio() }
    }

    private fun initModoActualizar() {
        Log.i(TAG, "Modo Actualizar")
        initModoVisualizar()
        detalleDomicilioEditarBtn.visibility = View.VISIBLE
        detalleDomicilioInputNombre.isEnabled = true
        detalleDomicilioInputContacto.isEnabled = true
        detalleDomicilioInputMetros.isEnabled = true
        detalleDomicilioInputPrecio.isEnabled = true
        detalleDomicilioInputHabitaciones.isEnabled = true
        detalleDomicilioInputBanios.isEnabled = true
        detalleDomicilioInputInquilino.isEnabled = true
        detalleDomicilioInputContacto.isEnabled = true
        detalleDomicilioFabCamara.visibility = View.VISIBLE
        icono_telefono.visibility = View.GONE
        detalleDomicilioPropietarioTelefono.visibility = View.GONE
        detalleDomicilioFabCamara.setOnClickListener { elegirFotoGaleria() }
        //detalleDomicilioFabCamara.setOnClickListener { initDialogFoto() }
        detalleDomicilioGuardarBtn.setOnClickListener { actualizarDomicilio() }
    }

    private fun insertarDomicilio() {
        if (comprobarFormulario()) {
            alertaDialogo("Insertar Domicilio", "??Desea guardar este domicilio?")
        }
    }

    private fun insertar() {
        domicilio = Property(
            id = UUID.randomUUID().toString(),
            nombre = detalleDomicilioInputNombre.text.toString().trim(),
            latitud = posicion?.latitude.toString(),
            localidad = cargarLocalidad(
                posicion?.latitude.toString(),
                posicion?.longitude.toString()
            ),
            longitud = posicion?.longitude.toString(),
            inquilino = detalleDomicilioInputInquilino.text.toString(),
            propietario = auth.currentUser?.email.toString(),
            telefono = detalleDomicilioInputContacto.text.toString().trim(),
            banios = detalleDomicilioInputBanios.text.toString().trim().toInt(),
            habitaciones = detalleDomicilioInputHabitaciones.text.toString().trim().toInt(),
            metros = detalleDomicilioInputMetros.text.toString().trim().toInt(),
            precio = detalleDomicilioInputPrecio.text.toString().trim().toInt(),
            foto1 = ""
        )

        fireStore.collection("Propiedades")
            .document(domicilio!!.id)
            .set(domicilio!!)
            .addOnSuccessListener {
                Log.i(TAG, "Domicilio insertado con ??xito con id: $domicilio")
                cambiarVisibilidadBotones()
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error insertar Domicilio", e) }
        insertarFoto(domicilio!!.nombre)

    }

    private fun insertarFoto(nombre: String) {
        val storageRef = storage.reference
        val baos = ByteArrayOutputStream()
        foto.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        // no hace falta borrarla si no solo sobre escribirla
        val domicilioImagenRef = storageRef.child("images/${nombre}.jpg")
        val uploadTask = domicilioImagenRef.putBytes(data)
        uploadTask.addOnFailureListener {
            Log.i(TAG, "storage:failure: " + it.localizedMessage)
        }.addOnSuccessListener { taskSnapshot ->
            val downloadUti = taskSnapshot.metadata!!.reference!!.downloadUrl
            downloadUti.addOnSuccessListener {
                val fotoRef = fireStore.collection("Propiedades").document(domicilio?.id.toString())
                fotoRef.update("foto1", it.toString())
                    .addOnSuccessListener {
                        volver()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error actualizar imagen", e)
                    }
            }
        }
    }

    private fun cambiarVisibilidadBotones() {
        detalleDomicilioInputNombre.isEnabled = false
        detalleDomicilioInputContacto.isEnabled = false
        detalleDomicilioInputMetros.isEnabled = false
        detalleDomicilioInputPrecio.isEnabled = false
        detalleDomicilioInputHabitaciones.isEnabled = false
        detalleDomicilioInputBanios.isEnabled = false
        detalleDomicilioInputInquilino.isEnabled = false
        detalleDomicilioEditarBtn.visibility = View.GONE
        detalleDomicilioBorrarBtn.visibility = View.GONE
        detalleDomicilioGuardarBtn.visibility = View.GONE
        detalleDomicilioFabCamara.visibility = View.GONE
    }


    private fun eliminarDomicilio() {
        alertaDialogo("Eliminar Domicilio", "??Quieres eliminarlo?")
    }

    private fun eliminar() {
        fireStore.collection("Propiedades")
            .document(domicilio!!.id)
            .delete()
            .addOnSuccessListener {
                Log.i(TAG, "Domicilio eliminado con ??xito")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
            }
    }

    private fun actualizarDomicilio() {
        if (comprobarFormulario()) {
            alertaDialogo("Modificar Domicilio", "??Desea modificar este Domicilio?")
        }
    }

    private fun actualizar() {
        with(domicilio!!) {
            nombre = detalleDomicilioInputNombre.text.toString().trim()
            telefono = detalleDomicilioInputContacto.text.toString().trim()
            metros = detalleDomicilioInputMetros.text.toString().trim().toInt()
            precio = detalleDomicilioInputPrecio.text.toString().trim().toInt()
            habitaciones = detalleDomicilioInputHabitaciones.text.toString().trim().toInt()
            banios = detalleDomicilioInputBanios.text.toString().trim().toInt()
            inquilino = detalleDomicilioInputInquilino.text.toString().trim()
            latitud = posicion?.latitude.toString()
            longitud = posicion?.longitude.toString()
            localidad =
                cargarLocalidad(posicion?.latitude.toString(), posicion?.longitude.toString())
        }

        fireStore.collection("Propiedades")
            .document(domicilio!!.id)
            .set(domicilio!!, SetOptions.merge())
            .addOnSuccessListener {
                Log.i(TAG, "Domicilio actualizado con ??xito con id: " + domicilio!!.id)
                actualizarFoto()
                cambiarVisibilidadBotones()
                volver()
            }.addOnFailureListener { e -> Log.w(TAG, "Error actualizar lugar", e) }
    }

    private fun actualizarFoto() {
        val storageRef = storage.reference
        val baos = ByteArrayOutputStream()
        foto.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        // no hace falta borrarla si no solo sobre escribirla
        val domicilioImagenRef = storageRef.child("images/${domicilio?.nombre}.jpg")
        val uploadTask = domicilioImagenRef.putBytes(data)
        uploadTask.addOnFailureListener {
            Log.i(TAG, "storage:failure: " + it.localizedMessage)
        }.addOnSuccessListener { taskSnapshot ->
            val downloadUti = taskSnapshot.metadata!!.reference!!.downloadUrl
            downloadUti.addOnSuccessListener {
                val fotoRef = fireStore.collection("Propiedades").document(domicilio?.id.toString())
                fotoRef.update("foto1", it.toString())
                    .addOnSuccessListener {
                        volver()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error actualizar imagen", e)
                    }
            }
        }
    }

    private fun cargarLocalidad(
        latitud: String,
        longitud: String
    ): String {
        var localidad = ""
        val geocoder = Geocoder(context, Locale.getDefault())
        //si la latuitud o longuitud son nylas, no se puede geolocalizar
        val addresses: List<Address>? =
            geocoder.getFromLocation(
                Double.parseDouble(latitud),
                Double.parseDouble(longitud),
                1
            )
        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            localidad = address.locality
        }
        return localidad
    }

    private fun volver() {
        activity?.onBackPressed()
    }

    private fun comprobarFormulario(): Boolean {
        var sal = true
        if (detalleDomicilioInputNombre.text?.isEmpty()!!) {
            detalleDomicilioInputNombre.error = "El nombre del domicilio no puede estar vac??o"
            sal = false
        }
        if (detalleDomicilioInputContacto.text?.isEmpty()!!) {
            detalleDomicilioInputContacto.error =
                "El tel??fono de contacto del domicilio no puede estar vac??o"
            sal = false
        }
        if (detalleDomicilioInputMetros.text?.isEmpty()!!) {
            detalleDomicilioInputMetros.error = "El tama??o del domicilio no puede estar vac??o"
            sal = false
        }
        if (detalleDomicilioInputPrecio.text?.isEmpty()!!) {
            detalleDomicilioInputPrecio.error = "El precio del domicilio no puede estar vac??o"
            sal = false
        }
        if (detalleDomicilioInputBanios.text?.isEmpty()!!) {
            detalleDomicilioInputBanios.error =
                "El n??mero de ba??os del domicilio no puede estar vac??o"
            sal = false
        }
        if (detalleDomicilioInputHabitaciones.text?.isEmpty()!!) {
            detalleDomicilioInputHabitaciones.error =
                "El n??mero de habitaciones del domicilio no puede estar vac??o"
            sal = false
        }

        if (!this::foto.isInitialized) {
            this.foto = (itemDetalleDomicilioFoto1.drawable as BitmapDrawable).bitmap
            Toast.makeText(context, "La imagen no puede estar vac??a", Toast.LENGTH_SHORT).show()
            sal = false
        }
        return sal
    }

    private fun alertaDialogo(titulo: String, texto: String) {
        val builder = AlertDialog.Builder(context)
        with(builder)
        {
            setIcon(R.drawable.renta)
            setTitle(titulo)
            setMessage(texto)
            setPositiveButton(R.string.accept) { _, _ ->
                when (modo) {
                    Modo.INSERTAR -> insertar()
                    Modo.ELIMINAR -> eliminar()
                    Modo.ACTUALIZAR -> actualizar()
                    else -> {
                    }
                }
            }
            setNegativeButton(R.string.cancel, null)
            // setNeutralButton("Maybe", neutralButtonClick)
            show()
        }
    }

    private fun leerPoscionGPSActual() {
        mPosicion = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun initMapa() {
        Log.i("Mapa", "Iniciando Mapa")
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.detalleDomicilioMapa) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configurarIUMapa()
        modoMapa()
    }

    private fun configurarIUMapa() {
        Log.i("Mapa", "Configurando IU Mapa")
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        val uiSettings: UiSettings = mMap.uiSettings
        uiSettings.isScrollGesturesEnabled = true
        uiSettings.isTiltGesturesEnabled = true
        uiSettings.isCompassEnabled = true
        uiSettings.isZoomControlsEnabled = true
        uiSettings.isMapToolbarEnabled = true
        mMap.setMinZoomPreference(6.0f)
        mMap.setOnMarkerClickListener(this)
    }

    private fun modoMapa() {
        Log.i("Mapa", "Configurando Modo Mapa")
        when (this.modo) {
            Modo.INSERTAR -> mapaInsertar()
            Modo.VISUALIZAR -> mapaVisualizar()
            Modo.ELIMINAR -> mapaVisualizar()
            Modo.ACTUALIZAR -> mapaActualizar()
            else -> {
            }
        }
    }

    private fun mapaInsertar() {
        Log.i("Mapa", "Configurando Modo Insertar")
        if (this.permisos) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mMap.isMyLocationEnabled = true
        }
        activarEventosMarcadores()
        obtenerPosicion()
    }

    private fun mapaVisualizar() {
        Log.i("Mapa", "Configurando Modo Visualizar")
        posicion = LatLng(domicilio!!.latitud.toDouble(), domicilio!!.longitud.toDouble())
        mMap.addMarker(
            MarkerOptions() // Posici??n
                .position(posicion!!) // T??tulo
                .title(domicilio!!.nombre) // Subtitulo
                .snippet("${domicilio!!.metros}m2, ${domicilio!!.banios} ba??os, ${domicilio!!.habitaciones} habitaciones")// Descripci??n
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
        )
        posicion?.let { CameraUpdateFactory.newLatLng(it) }?.let { mMap.moveCamera(it) }
    }

    private fun mapaActualizar() {
        Log.i("Mapa", "Configurando Modo Actualizar")
        if (this.permisos) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mMap.isMyLocationEnabled = true
        }
        activarEventosMarcadores()
        mapaVisualizar()
    }

    private fun activarEventosMarcadores() {
        mMap.setOnMapClickListener { point -> // Creamos el marcador
            // Borramos el marcador Touch si est?? puesto
            marcadorTouch?.remove()
            marcadorTouch = mMap.addMarker(
                MarkerOptions() // Posici??n
                    .position(point) // T??tulo
                    .title("Posici??n Actual") // Subtitulo
                    .snippet(detalleDomicilioInputNombre.text.toString()) // Color o tipo d icono
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLng(point))
            posicion = point
        }
    }

    private fun obtenerPosicion() {
        Log.i("Mapa", "Opteniendo posici??n")
        try {
            if (this.permisos) {
                val local: Task<Location> = mPosicion!!.lastLocation
                local.addOnCompleteListener(
                    requireActivity()
                ) { task ->
                    if (task.isSuccessful) {
                        localizacion = task.result
                        posicion = LatLng(
                            localizacion!!.latitude,
                            localizacion!!.longitude
                        )
                        posicion?.let { CameraUpdateFactory.newLatLng(it) }
                            ?.let { mMap.moveCamera(it) }
                    } else {
                        Log.i("GPS", "No se encuetra la ??ltima posici??n.")
                        Log.e("GPS", "Exception: %s", task.exception)
                    }
                }
            }
        } catch (e: SecurityException) {
            Snackbar.make(
                requireView(),
                "No se ha encontrado su posoci??n actual o el GPS est?? desactivado",
                Snackbar.LENGTH_LONG
            ).show()
            Log.e("Exception: %s", e.message.toString())
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.i("Mapa", marker.toString())
        return false
    }

    private fun initDialogFoto() {
        val fotoDialogoItems = arrayOf(
            "Seleccionar fotograf??a de galer??a",
            "Capturar fotograf??a desde la c??mara"
        )
        AlertDialog.Builder(context)
            .setTitle("Seleccionar Acci??n")
            .setItems(fotoDialogoItems) { _, modo ->
                when (modo) {
                    0 -> elegirFotoGaleria()
                    1 -> tomarFotoCamara()
                }
            }
            .show()
    }

    private fun elegirFotoGaleria() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, galeria)
    }

    private fun tomarFotoCamara() {
        val buildr = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(buildr.build())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val nombreFoto = PhotosUtils.crearNombreFoto(imagenPrefijo, imagenExtension)
        val file = PhotosUtils.salvarFoto(imagenDirectorio, nombreFoto, requireContext())
        imagenURI = Uri.fromFile(file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenURI)
        startActivityForResult(intent, camara)
    }


    /**
     * Siempre se ejecuta al realizar una acci??n
     * @param requestCode Int
     * @param resultCode Int
     * @param data Intent?
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("FOTO", "Opci??n:--->$requestCode")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.i("FOTO", "Se ha cancelado")
        }
        // Procesamos la foto de la galeria
        if (requestCode == galeria) {
            Log.i("FOTO", "Entramos en Galer??a")
            if (data != null) {
                // Obtenemos su URI con su direcci??n temporal
                val contentURI = data.data!!
                try {
                    // Obtenemos el bitmap de su almacenamiento externo
                    // Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    if (Build.VERSION.SDK_INT < 28) {
                        this.foto =
                            MediaStore.Images.Media.getBitmap(context?.contentResolver, contentURI)
                    } else {
                        val source: ImageDecoder.Source =
                            ImageDecoder.createSource(context?.contentResolver!!, contentURI)
                        this.foto = ImageDecoder.decodeBitmap(source)
                    }
                    // Para jugar con las proporciones y ahorrar en memoria no cargando toda la foto, solo carga 600px max
                    val prop = this.imagenProporcion / this.foto.width.toFloat()
                    // Actualizamos el bitmap para ese tama??o, luego podr??amos reducir su calidad
                    this.foto = Bitmap.createScaledBitmap(
                        this.foto,
                        this.imagenProporcion,
                        (this.foto.height * prop).toInt(),
                        false
                    )
                    // Vamos a copiar nuestra imagen en nuestro directorio comprimida por si acaso.
                    val nombre = PhotosUtils.crearNombreFoto(imagenPrefijo, imagenExtension)
                    val fichero =
                        PhotosUtils.copiarFoto(
                            this.foto,
                            nombre,
                            imagenDirectorio,
                            imagenCompresion,
                            context!!
                        )
                    imagenURI = Uri.fromFile(fichero)
                    Toast.makeText(context, "??Foto rescatada de la galer??a!", Toast.LENGTH_SHORT)
                        .show()
                    itemDetalleDomicilioFoto1.setImageBitmap(this.foto)

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, "??Fallo Galeria!", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == camara) {
            Log.i("FOTO", "Entramos en Camara")
            try {
                if (Build.VERSION.SDK_INT < 28) {
                    this.foto =
                        MediaStore.Images.Media.getBitmap(context?.contentResolver, imagenURI)
                } else {
                    val source: ImageDecoder.Source =
                        ImageDecoder.createSource(context?.contentResolver!!, imagenURI)
                    this.foto = ImageDecoder.decodeBitmap(source)
                }
                PhotosUtils.comprimirFoto(imagenURI.toFile(), this.foto, this.imagenCompresion)
                itemDetalleDomicilioFoto1.setImageBitmap(this.foto)
                Toast.makeText(context, "??Foto Salvada!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "??Fallo Camara!", Toast.LENGTH_SHORT).show()
            }
        }
    }


}