package com.damaris.suquillo.manoymente.ui.docente

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.damaris.suquillo.manoymente.R

class AvatarAdapter(
    private val listaAvatares: List<Int>,
    private val onAvatarSeleccionado: (Int) -> Unit // Para avisar qué avatar se eligió
) : RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {

    // Guardamos la posición del avatar que está seleccionado actualmente. -1 significa ninguno.
    private var posicionSeleccionada = -1

    inner class AvatarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatarMuestra)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        // Aquí "inflamos" el molde XML que creamos antes
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_avatar, parent, false)
        return AvatarViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        // 1. Ponemos la imagen correspondiente de la lista
        val idImagen = listaAvatares[position]
        holder.ivAvatar.setImageResource(idImagen)

        // 2. Pintamos el fondo verde si está seleccionado, o transparente si no lo está
        if (position == posicionSeleccionada) {
            holder.ivAvatar.setBackgroundColor(Color.parseColor("#AEEA00")) // Verde brillante
        } else {
            holder.ivAvatar.setBackgroundColor(Color.TRANSPARENT)
        }

        // 3. ¿Qué pasa cuando el docente toca este avatar?
        holder.itemView.setOnClickListener {
            // SOLUCIÓN: Usamos adapterPosition, que es compatible con tu versión
            val posicionActual = holder.adapterPosition

            // Verificamos que el monstruito siga existiendo en la lista (por seguridad)
            if (posicionActual != RecyclerView.NO_POSITION) {
                // Actualizamos la posición seleccionada
                val posicionAnterior = posicionSeleccionada
                posicionSeleccionada = posicionActual

                // Le decimos al RecyclerView que redibuje el viejo y el nuevo
                notifyItemChanged(posicionAnterior)
                notifyItemChanged(posicionSeleccionada)

                // Le avisamos a la pantalla principal cuál imagen se eligió
                onAvatarSeleccionado(idImagen)
            }
        }
    }

    override fun getItemCount(): Int {
        return listaAvatares.size
    }
}