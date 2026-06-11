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
import com.damaris.suquillo.manoymente.ui.docente.DocenteActivity

class MainActivity : AppCompatActivity() {

    private var mediaPlayerVoz: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Iniciar la música global apenas se abre la app
        GestorAudioGlobal.iniciarMusica(this)

        val botonIniciar = findViewById<ImageView>(R.id.ivBotonIniciar)
        val ibSonido = findViewById<ImageButton>(R.id.ibSonido)
        val botonUsuario = findViewById<ImageView>(R.id.ibDocente)

        // 2. Configurar el ícono inicial del sonido por si ya estaba muteado
        actualizarIconoSonido(ibSonido)

        // 3. Lógica del botón de sonido
        ibSonido.setOnClickListener {
            GestorAudioGlobal.alternarMute()
            actualizarIconoSonido(ibSonido)
            // Si el niño silencia la app mientras el monstruo habla, lo callamos
            if (GestorAudioGlobal.isMuted) {
                detenerVoz()
            }
        }

        // 4. Lógica del botón iniciar
        botonIniciar.setOnClickListener {
            detenerVoz()
            val intencion = Intent(this, SeleccionActivity::class.java)
            startActivity(intencion)
        }

        botonUsuario.setOnClickListener {
            detenerVoz()
            val intencion = Intent(this, DocenteActivity::class.java)
            startActivity(intencion)
        }

        // 5. Reproducir la voz de bienvenida con un pequeño retraso
        Handler(Looper.getMainLooper()).postDelayed({
            reproducirVozBienvenida()
        }, 500) // Medio segundo de pausa para que la pantalla termine de cargar
    }

    private fun reproducirVozBienvenida() {
        // Solo reproducimos si la app no está silenciada
        if (!GestorAudioGlobal.isMuted) {
            GestorAudioGlobal.atenuarParaVoz() // Bajamos la música
            // Reemplaza "audio_bienvenida_main" con el nombre real de tu archivo en res/raw
            mediaPlayerVoz = MediaPlayer.create(this, R.raw.audio_bienvenida_main)
            // Cuando termine de hablar, subimos la música de nuevo
            mediaPlayerVoz?.setOnCompletionListener {
                GestorAudioGlobal.restaurarVolumen()
            }
            mediaPlayerVoz?.start()
        }
    }

    private fun detenerVoz() {
        if (mediaPlayerVoz?.isPlaying == true) {
            mediaPlayerVoz?.stop()
        }
        mediaPlayerVoz?.release()
        mediaPlayerVoz = null
        GestorAudioGlobal.restaurarVolumen() // Aseguramos que la música regrese a la normalidad
    }

    private fun actualizarIconoSonido(boton: ImageButton) {
        val icono = if (GestorAudioGlobal.isMuted) R.drawable.ic_baseline_volume_off_24 else R.drawable.ic_baseline_volume_up_24
        boton.setImageResource(icono)
    }

    override fun onResume() {
        super.onResume()
        // Asegurar que la música siga sonando si regresamos a esta pantalla desde otra
        GestorAudioGlobal.iniciarMusica(this)
        GestorAudioGlobal.reanudarMusica()
    }

    override fun onPause() {
        super.onPause()
        // Si el usuario sale de la app o cambia de pantalla, cortamos el audio limpiamente
        detenerVoz()
        GestorAudioGlobal.pausarMusica()
    }
}