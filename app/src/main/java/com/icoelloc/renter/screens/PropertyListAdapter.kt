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
import com.icoelloc.renter.utils.CirculoTransformacion
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
        holder.itemLocalidad.text = listaDomicilios[position].localidad
        holder.itemTelefono.text = listaDomicilios[position].telefono
        holder.itemLocalidad.text

        if (listaDomicilios[position].inquilino == "") {
            holder.itemDisponibilidad.text = "Disponible"
        } else {
            holder.itemDisponibilidad.text = "Alquilada"
        }

        holder.itemHabitaciones.text = listaDomicilios[position].habitaciones.toString()
        holder.itemBanios.text = listaDomicilios[position].banios.toString()
        holder.itemPrecio.text = listaDomicilios[position].precio.toString()
        holder.itemFoto.setOnClickListener {
            accionPrincipal(listaDomicilios[position])
        }

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

    private fun cargarFotoDomicilio(domicilio: Property, holder: DomiciliosViewHolder) {
        if (domicilio.foto1 != "") {
            Picasso.get()
                .load(domicilio.foto1)
                .transform(CirculoTransformacion())
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
                R.drawable.renta
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
        val itemDisponibilidad: TextView = itemView.findViewById(R.id.itemDisponibilidad)
        val itemTelefono: TextView = itemView.findViewById(R.id.itemTelefono)
        var context: Context? = itemView.context
    }

}

