package com.icoelloc.renter.screens

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.icoelloc.renter.R
import com.icoelloc.renter.objects.Property

class PropertyListAdapter(
    private val listaDomicilios: MutableList<Property>,
) : RecyclerView.Adapter<PropertyListAdapter.DomicilioViewHolder>() {

    // Firebase
    private var FireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "Adapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DomicilioViewHolder {
        return DomicilioViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card_layout, parent, false)
        )
    }

    /**
     * Procesamos los lugares y las metemos en un Holder
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: DomicilioViewHolder, position: Int) {
        holder.itemDomicilioNombre.text = listaDomicilios[position].nombre
        holder.itemDomicilioHabitaciones.text = listaDomicilios[position].habitaciones.toString()
        holder.itemDomicilioBanios.text = listaDomicilios[position].banios.toString()
        /*imagenLugar(listaDomicilios[position], holder)

        // Programamos el clic de cada fila (itemView)
        holder.itemLugarImagen
            .setOnClickListener {
                // Devolvemos la noticia
                accionPrincipal(listaDomicilios[position])
            }*/
    }

    /**
     * Elimina un item de la lista
     *
     * @param position
     */
    fun removeItem(position: Int) {
        listaDomicilios.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaDomicilios.size)
    }

    /**
     * Recupera un Item de la lista
     *
     * @param item
     * @param position
     */
    fun updateItem(item: Property, position: Int) {
        listaDomicilios[position] = item
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listaDomicilios.size)
    }

    /**
     * Para añadir un elemento
     * @param item
     */
    fun addItem(item: Property) {
        listaDomicilios.add(item)
        notifyDataSetChanged()
    }


    /**
     * Devuelve el número de items de la lista
     *
     * @return
     */
    override fun getItemCount(): Int {
        return listaDomicilios.size
    }

    /*
    /**
     * Devuelve la imagen de un lugar
     */
    private fun imagenLugar(lugar: Property, holder: DomicilioViewHolder) {
        // Buscamos la fotografia
        val docRef = FireStore.collection("imagenes").document(lugar.imagenID)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    /*val miImagen = document.toObject(Fotografia::class.java)
                    Log.i(TAG, "fotografiasGetById ok: ${document.data}")
                    Picasso.get()
                        // .load(R.drawable.user_avatar)
                        .load(miImagen?.uri)
                        .into(holder.itemLugarImagen)*/
                } else {
                    Log.i(TAG, "Error: No exite fotografía")
                    imagenPorDefecto(holder)
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "ERROR: " + exception.localizedMessage)
                imagenPorDefecto(holder)
            }
    }
*/

    /*
    /**
     * Inserta una imagen por defecto
     * @param holder LugarViewHolder
     */
    private fun imagenPorDefecto(holder: DomicilioViewHolder) {
        holder.itemLugarImagen.setImageBitmap(
            BitmapFactory.decodeResource(holder.context?.resources,
            R.drawable.temp_image))
    }*/


    /**
     * Holder que encapsula los objetos a mostrar en la lista
     */
    class DomicilioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemDomicilioNombre: TextView = itemView.findViewById(R.id.myHome_name)
        val itemDomicilioHabitaciones: TextView = itemView.findViewById(R.id.myHome_bedrooms)
        val itemDomicilioBanios: TextView = itemView.findViewById(R.id.myHome_bathrooms)
        val context = itemView.context

    }
}
