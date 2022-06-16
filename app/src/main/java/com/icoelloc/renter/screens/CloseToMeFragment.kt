package com.icoelloc.renter.screens

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.icoelloc.renter.R
import com.icoelloc.renter.objects.Property
import com.icoelloc.renter.utils.Modo
import com.squareup.picasso.Picasso
import kotlin.math.ceil

class CloseToMeFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore

    private lateinit var mMap: GoogleMap
    private lateinit var usuario: FirebaseUser

    companion object {
        private const val TAG = "MAPA"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_close_to_me, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        fireStore = FirebaseFirestore.getInstance()
        view.setOnTouchListener { _, _ ->
            return@setOnTouchListener true
        }

        this.usuario = auth.currentUser!!
        initUI()
    }

    private fun initUI() {
        initMapa()
    }

    /**
     * inicializamos el mapa
     */
    private fun initMapa() {
        val mapFragment =
            (childFragmentManager.findFragmentById(R.id.miMapa) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
    }

    /**
     * Cuándo el mapa este incializados, configuraremos este
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configurarUIMapa()
        puntosMapa()
    }

    /**
     * mostrar los puntos en el mapa si hay vivendas registradas
     */
    private fun puntosMapa() {
        fireStore.collection("Propiedades")
            .get()
            .addOnSuccessListener { result ->
                val listaDomicilios = mutableListOf<Property>()
                for (document in result) {
                    val miLugar = document.toObject(Property::class.java)
                    listaDomicilios.add(miLugar)
                }
                if (listaDomicilios.size > 0) {
                    procesarDomicilios(listaDomicilios)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    "Error al acceder al servicio: " + exception.localizedMessage,
                    Toast.LENGTH_LONG
                )
                    .show()
            }
    }

    /**
     * Por cada domicilio añadimos el marcador
     */
    private fun procesarDomicilios(listaDomicilios: MutableList<Property>) {
        listaDomicilios.forEach {
            addMarcador(it)
        }
        actualizarCamara(listaDomicilios)
        mMap.setOnMarkerClickListener(this)
    }

    /**
     * Método para añadir el marcador en forma de logo circular con la imagen de la vivienda, al
     * pinchar en esta, mostrará un mini resumen de esta
     */
    private fun addMarcador(domicilio: Property) {
        // Buscamos la fotografia
        val docRef = fireStore.collection("Propiedades").document(domicilio.id)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val posicion =
                        LatLng(domicilio.latitud.toDouble(), domicilio.longitud.toDouble())
                    val imageView = ImageView(context)

                    if (domicilio.foto1.isNotEmpty()) {
                        Picasso.get()
                            .load(domicilio.foto1)
                            .into(imageView, object : com.squareup.picasso.Callback {
                                override fun onSuccess() {
                                    val temp = (imageView.drawable as BitmapDrawable).bitmap
                                    val pin: Bitmap = crearPin(temp)!!
                                    val marker = mMap.addMarker(
                                        MarkerOptions() // Posición
                                            .position(posicion) // Título
                                            .title(domicilio.nombre) // Subtitulo
                                            //mostrar los metros cuadrados, baños y habitaciones
                                            .snippet("${domicilio.metros}m2, ${domicilio.banios} baños, ${domicilio.habitaciones} habitaciones")// Descripción
                                            .anchor(0.5f, 0.907f)
                                            .icon(BitmapDescriptorFactory.fromBitmap(pin))
                                    )
                                    // Le añado como tag el lugar para recuperarlo
                                    if (marker != null) {
                                        marker.tag = domicilio
                                    }
                                }

                                override fun onError(e: Exception) {
                                    Log.d(TAG, "Error al descargar imagen")
                                }
                            })
                    } else {
                        Picasso.get()
                            .load(R.drawable.renta)
                            .into(imageView, object : com.squareup.picasso.Callback {
                                override fun onSuccess() {
                                    val temp = (imageView.drawable as BitmapDrawable).bitmap
                                    val pin: Bitmap = crearPin(temp)!!
                                    val marker = mMap.addMarker(
                                        MarkerOptions() // Posición
                                            .position(posicion) // Título
                                            .title(domicilio.nombre) // Subtitulo
                                            .snippet("${domicilio.metros}m2, ${domicilio.banios} baños, ${domicilio.habitaciones} habitaciones")// Descripción
                                            .anchor(0.5f, 0.907f)
                                            .icon(BitmapDescriptorFactory.fromBitmap(pin))
                                    )
                                    // Le añado como tag el lugar para recuperarlo
                                    if (marker != null) {
                                        marker.tag = domicilio
                                    }
                                }

                                override fun onError(e: Exception) {
                                    Log.d(TAG, "Error al descargar imagen")
                                }
                            })
                    }

                } else {
                    Log.i(TAG, "Error: No exite fotografía")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "ERROR: " + exception.localizedMessage)
            }
    }

    /**
     * Creamos el pin del mapa, en mi caso no le puse logo, si no que solo quiero el efecto del
     * fondo de la imagen
     */
    private fun crearPin(bitmap: Bitmap?): Bitmap? {
        var result: Bitmap? = null
        try {
            result = Bitmap.createBitmap(dp(62f), dp(76f), Bitmap.Config.ARGB_8888)
            result.eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(result)
            /*val drawable = ContextCompat.getDrawable(requireContext(),R.drawable.ic_location)
            drawable?.setBounds(0, 0, dp(62f), dp(76f))
            drawable?.draw(canvas)*/
            val roundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val bitmapRect = RectF()
            canvas.save()
            if (bitmap != null) {
                val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                val matrix = Matrix()
                val scale = dp(52f) / bitmap.width.toFloat()
                matrix.postTranslate(dp(5f).toFloat(), dp(5f).toFloat())
                matrix.postScale(scale, scale)
                roundPaint.shader = shader
                shader.setLocalMatrix(matrix)
                bitmapRect[dp(5f).toFloat(), dp(5f).toFloat(), dp(52f + 5).toFloat()] =
                    dp(52f + 5).toFloat()
                canvas.drawRoundRect(bitmapRect, dp(26f).toFloat(), dp(26f).toFloat(), roundPaint)
            }
            canvas.restore()
            try {
                canvas.setBitmap(null)
            } catch (e: Exception) {
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return result
    }

    private fun dp(value: Float): Int {
        return if (value == 0f) {
            0
        } else
            ceil((resources.displayMetrics.density * value).toDouble()).toInt()
    }

    /**
     * Método para mostrar en pantalla todos los domicilios insertados, si en España hay un domcilio
     * y Otro en Londres, se mostraran ambos en pantalla
     */
    private fun actualizarCamara(listaDomicilios: MutableList<Property>?) {
        val bc = LatLngBounds.Builder()
        for (item in listaDomicilios!!) {
            bc.include(LatLng(item.latitud.toDouble(), item.longitud.toDouble()))
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 120))
    }

    /**
     * Configuramos el mapa, para visualizarlo en modo normal de Firebase
     * permitimos el scroll, permitumos girar el mapa, el compás para localizarnos, el zoom,
     * y los controles del zoom con los dedos
     * Permitimos que se vean los edificios y el modelo 3D de los edificios
     */
    private fun configurarUIMapa() {
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        val uiConfig: UiSettings = mMap.uiSettings
        uiConfig.isScrollGesturesEnabled = true
        uiConfig.isTiltGesturesEnabled = true
        uiConfig.isCompassEnabled = true
        uiConfig.isZoomControlsEnabled = true
        mMap.isBuildingsEnabled = true
        mMap.isIndoorEnabled = true
    }

    /**
     * Al pulsar sobre el punto, nos abrirá la ventana con los datos del fragment en modo visualizar
     *
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        marker.tag as Property
        val propiedad = marker.tag as Property
        Log.i("Mapa", propiedad.toString())
        abrirDetalle(propiedad)
        return false

    }

    /**
     * Abrir la ventana con los datos del fragment en modo visualizar
     */
    private fun abrirDetalle(domicilio: Property?) {
        val estadioDetalle = PropertyFullDataFragment(domicilio, Modo.VISUALIZAR)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.nav_host_fragment, estadioDetalle)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}