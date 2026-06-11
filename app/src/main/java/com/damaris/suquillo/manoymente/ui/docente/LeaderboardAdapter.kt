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

class LeaderboardAdapter(
    private var listaEstudiantes: List<Estudiante>,
    private var categoriaActual: String = "animales", // Por defecto empezamos viendo animales
    private val onDetalleClick: (Estudiante) -> Unit
) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPosicion: TextView = itemView.findViewById(R.id.tvPosicionLeaderboard)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreLeaderboard)
        val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatarLeaderboard)
        val tvEdad: TextView = itemView.findViewById(R.id.tvEdadLeaderboard)
        val tvScore: TextView = itemView.findViewById(R.id.tvScoreLeaderboard)
        val ibVerDetalle: ImageButton = itemView.findViewById(R.id.ibVerDetalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val estudiante = listaEstudiantes[position]

        // La posición es simplemente el índice + 1
        holder.tvPosicion.text = (position + 1).toString()
        holder.tvNombre.text = estudiante.nombre
        holder.ivAvatar.setImageResource(estudiante.avatarId)
        holder.tvEdad.text = estudiante.edad.toString()

        // Mostramos el puntaje correcto según la pestaña que esté activa
        if (categoriaActual == "animales") {
            holder.tvScore.text = "${estudiante.aciertosAnimales}/${estudiante.erroresAnimales}"
        } else {
            holder.tvScore.text = "${estudiante.aciertosFiguras}/${estudiante.erroresFiguras}"
        }

        holder.ibVerDetalle.setOnClickListener {
            onDetalleClick(estudiante)
        }
    }

    override fun getItemCount(): Int = listaEstudiantes.size

    fun actualizarLista(nuevaLista: List<Estudiante>, nuevaCategoria: String) {
        listaEstudiantes = nuevaLista
        categoriaActual = nuevaCategoria
        notifyDataSetChanged()
    }
}