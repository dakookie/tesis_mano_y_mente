package com.damaris.suquillo.manoymente.ui.inicio

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.damaris.suquillo.manoymente.R
import com.damaris.suquillo.manoymente.ui.GestorAudioGlobal
import com.damaris.suquillo.manoymente.ui.juego.JuegoActivity


class SeleccionActivity : AppCompatActivity(){
    private var mediaPlayerVoz: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion)

        val ivMascota = findViewById<ImageView>(R.id.ivMascotaSeleccion)
        val ibAnimales = findViewById<ImageButton>(R.id.ibAnimales)
        val ibFiguras = findViewById<ImageButton>(R.id.ibFiguras)
        val ibHome = findViewById<ImageButton>(R.id.ibHome)
        val ibSonido = findViewById<ImageButton>(R.id.ibSonido)

        // 1. Recuperar los datos del Intent
        val idNino = intent.getIntExtra("ESTUDIANTE_ID", -1)
        val nombreNino = intent.getStringExtra("ESTUDIANTE_NOMBRE") ?: "Invitado"
        val avatarNino = intent.getIntExtra("ESTUDIANTE_AVATAR", R.drawable.monster_astronaut)

        // 2. Pintarlos en la pantalla o apagar la vista si es Juego Libre
        val llPerfilJugador = findViewById<android.widget.LinearLayout>(R.id.llPerfilJugador)
        val tvNombreJugador = findViewById<android.widget.TextView>(R.id.tvNombreJugador)
        val ivAvatarJugador = findViewById<ImageView>(R.id.ivAvatarJugador)

        if (idNino == -1) {
            llPerfilJugador.visibility = android.view.View.GONE
        } else {
            llPerfilJugador.visibility = android.view.View.VISIBLE
            tvNombreJugador.text = nombreNino
            ivAvatarJugador.setImageResource(avatarNino)
        }

        // 1. ARRANCAR LA MÚSICA GLOBAL
        GestorAudioGlobal.iniciarMusica(this)
        actualizarIconoSonido(ibSonido)

        // 2. Reproducir la introducción al entrar a la pantalla (Sin acción extra al terminar)
        Handler(Looper.getMainLooper()).postDelayed({
            reproducirVoz(R.raw.audio_seleccion_intro)
        }, 500)

        // 3. Lógica al elegir Animales
        ibAnimales.setOnClickListener {
            // Bloqueamos los botones temporalmente para evitar dobles clics
            ibAnimales.isEnabled = false
            ibFiguras.isEnabled = false

            ivMascota.setImageResource(R.drawable.monster_indicate)

            // Le pasamos el audio y lo que debe hacer EXACTAMENTE al terminar de hablar
            reproducirVoz(R.raw.audio_seleccion_animales) {
                val intent = Intent(this@SeleccionActivity, JuegoActivity::class.java)
                intent.putExtra("CATEGORIA_SELECCIONADA", "animales")
                intent.putExtra("ESTUDIANTE_ID", idNino)
                intent.putExtra("ESTUDIANTE_NOMBRE", nombreNino)
                intent.putExtra("ESTUDIANTE_AVATAR", avatarNino)
                startActivity(intent)
                // Restauramos los botones por si el usuario regresa con la flecha atrás
                ibAnimales.isEnabled = true
                ibFiguras.isEnabled = true
            }
        }

        // 4. Lógica al elegir Figuras
        ibFiguras.setOnClickListener {
            ibAnimales.isEnabled = false
            ibFiguras.isEnabled = false

            ivMascota.setImageResource(R.drawable.monster_indicate_right_selection)

            reproducirVoz(R.raw.audio_seleccion_figuras) {
                val intent = Intent(this@SeleccionActivity, JuegoActivity::class.java)
                intent.putExtra("CATEGORIA_SELECCIONADA", "figuras")
                intent.putExtra("ESTUDIANTE_ID", idNino)
                intent.putExtra("ESTUDIANTE_NOMBRE", nombreNino)
                intent.putExtra("ESTUDIANTE_AVATAR", avatarNino)
                startActivity(intent)

                ibAnimales.isEnabled = true
                ibFiguras.isEnabled = true
            }
        }

        // 5. Botón de silenciar
        ibSonido.setOnClickListener {
            GestorAudioGlobal.alternarMute()
            actualizarIconoSonido(ibSonido)
            if (GestorAudioGlobal.isMuted) {
                detenerVoz()
            }
        }

        // 6. Botón de inicio
        ibHome.setOnClickListener {
            detenerVoz()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    // === FUNCIÓN ACTUALIZADA CON "CALLBACK" (Acción al terminar) ===
    private fun reproducirVoz(idAudio: Int, accionAlTerminar: (() -> Unit)? = null) {
        detenerVoz()
        // Si la app está silenciada, simulamos un pequeño retraso de 1 segundo
        // y ejecutamos la acción (cambiar de pantalla) para no trabar el juego.
        if (GestorAudioGlobal.isMuted) {
            if (accionAlTerminar != null) {
                Handler(Looper.getMainLooper()).postDelayed({
                    accionAlTerminar.invoke()
                }, 1000)
            }
            return
        }

        GestorAudioGlobal.atenuarParaVoz()
        mediaPlayerVoz = MediaPlayer.create(this, idAudio)
        // Aquí está la magia: Cuando el audio termina por sí solo, ejecuta la acción
        mediaPlayerVoz?.setOnCompletionListener {
            GestorAudioGlobal.restaurarVolumen()
            accionAlTerminar?.invoke()
        }
        mediaPlayerVoz?.start()
    }

    private fun detenerVoz() {
        if (mediaPlayerVoz?.isPlaying == true) {
            mediaPlayerVoz?.stop()
        }
        mediaPlayerVoz?.release()
        mediaPlayerVoz = null
        GestorAudioGlobal.restaurarVolumen()
    }

    private fun actualizarIconoSonido(boton: ImageButton) {
        val icono = if (GestorAudioGlobal.isMuted) R.drawable.ic_baseline_volume_off_24 else R.drawable.ic_baseline_volume_up_24
        boton.setImageResource(icono)
    }

    override fun onResume() {
        super.onResume()
        GestorAudioGlobal.iniciarMusica(this)
        GestorAudioGlobal.reanudarMusica()
    }

    override fun onPause() {
        super.onPause()
        detenerVoz()
        GestorAudioGlobal.pausarMusica()
    }
}