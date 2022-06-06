package com.icoelloc.renter.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.icoelloc.renter.R
import com.icoelloc.renter.objects.Property
import com.squareup.picasso.Picasso
import java.lang.Double.parseDouble
import java.util.*


class PropertyListAdapter(

    private val listaDomicilios: MutableList<Property>,
    private val accionPrincipal: (Property) -> Unit,

    ) : RecyclerView.Adapter<PropertyListAdapter.DomiciliosViewHolder>() {

    companion object {
        private const val TAG = "AdapterDomicilios"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DomiciliosViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_property, parent, false)
        return DomiciliosViewHolder(view)
    }

    override fun onBindViewHolder(holder: DomiciliosViewHolder, position: Int) {
        cargarFotoDomicilio(listaDomicilios[position], holder)
        holder.itemNombre.text = listaDomicilios[position].nombre ?: ""
        cargarLocalidad(listaDomicilios[position], holder)
        holder.itemHabitaciones.text = listaDomicilios[position].habitaciones.toString()
        holder.itemBanios.text = listaDomicilios[position].banios.toString()
        holder.itemPrecio.text = listaDomicilios[position].precio.toString()
    }

    fun removeItem(position: Int) {
        listaDomicilios.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaDomicilios.size)
    }

    fun updateItem(item: Property, position: Int) {
        listaDomicilios[position] = item
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listaDomicilios.size)
    }

    fun addItem(item: Property) {
        listaDomicilios.add(item)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return listaDomicilios.size
    }


    private fun cargarLocalidad(domicilio: Property, holder: DomiciliosViewHolder) {
        val geocoder = Geocoder(holder.itemView.context, Locale.getDefault())
        //si la latuitud o longuitud son nylas, no se puede geolocalizar
        if (domicilio.latitud != null || domicilio.longitud != null) {


            //convertir cadena en double

            val addresses: List<Address>? =
                geocoder.getFromLocation(
                    parseDouble(domicilio.latitud),
                    parseDouble(domicilio.longitud),
                    1
                )
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                holder.itemLocalidad.text = address.locality
            }
        }
    }

    private fun cargarFotoDomicilio(domicilio: Property, holder: DomiciliosViewHolder) {
        if (domicilio.foto1 != "") {
            Picasso.get()
                .load(domicilio.foto1)
                .resize(160, 160)
                .into(holder.itemFoto)
        } else {
            imagenPorDefecto(holder)
        }
    }

    private fun imagenPorDefecto(holder: DomiciliosViewHolder) {
        holder.itemFoto.setImageBitmap(
            BitmapFactory.decodeResource(
                holder.itemView.context.resources,
                R.drawable.temp_image
            )
        )
    }

    class DomiciliosViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemFoto: ImageView = itemView.findViewById(R.id.itemPropiedadImagen)
        val itemNombre: TextView = itemView.findViewById(R.id.itemPropiedadName)
        val itemLocalidad: TextView = itemView.findViewById(R.id.itemLocalidad)
        val itemBanios: TextView = itemView.findViewById(R.id.itemBanios)
        val itemHabitaciones: TextView = itemView.findViewById(R.id.itemHabitaciones)
        val itemPrecio: TextView = itemView.findViewById(R.id.itemPrecio)
        var context: Context? = itemView.context
    }

}

