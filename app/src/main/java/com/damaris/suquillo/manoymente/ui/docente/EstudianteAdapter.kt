package com.damaris.suquillo.manoymente.ui.docente

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.damaris.suquillo.manoymente.R
import com.damaris.suquillo.manoymente.data.local.Estudiante

class EstudianteAdapter(
    private var listaEstudiantes: List<Estudiante>,
    private val onEditarClick: (Estudiante) -> Unit,
    private val onJugarClick: (Estudiante) -> Unit // NUEVA ACCIÓN
) : RecyclerView.Adapter<EstudianteAdapter.EstudianteViewHolder>() {

    class EstudianteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvItemNombre)
        val ivAvatar: ImageView = itemView.findViewById(R.id.ivItemAvatar)
        val tvEdad: TextView = itemView.findViewById(R.id.tvItemEdad)
        val ibEditar: ImageButton = itemView.findViewById(R.id.ibItemEditar)
        val btnJugar: android.widget.Button = itemView.findViewById(R.id.btnItemJugar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstudianteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_estudiante, parent, false)
        return EstudianteViewHolder(view)
    }

    override fun onBindViewHolder(holder: EstudianteViewHolder, position: Int) {
        val estudiante = listaEstudiantes[position]

        holder.tvNombre.text = estudiante.nombre
        holder.tvEdad.text = estudiante.edad.toString()
        holder.ivAvatar.setImageResource(estudiante.avatarId)

        holder.ibEditar.setOnClickListener { onEditarClick(estudiante) }

        // Al tocar JUGAR, enviamos el estudiante a la Actividad
        holder.btnJugar.setOnClickListener { onJugarClick(estudiante) }
    }

    override fun getItemCount(): Int = listaEstudiantes.size

    fun actualizarLista(nuevaLista: List<Estudiante>) {
        listaEstudiantes = nuevaLista
        notifyDataSetChanged()
    }
}