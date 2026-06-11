package com.damaris.suquillo.manoymente.ui

import android.content.Context
import android.media.MediaPlayer
import com.damaris.suquillo.manoymente.R

object GestorAudioGlobal {
    private var mediaPlayer: MediaPlayer? = null
    var isMuted = false

    fun iniciarMusica(context: Context) {
        // Si no existe la música, la creamos usando el contexto de TODA la app, no de una sola pantalla
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.musica_fondo)
            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(0.4f, 0.4f)
        }

        // Si no está silenciada y no está sonando, le damos play
        if (!isMuted && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    fun pausarMusica() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun reanudarMusica() {
        if (mediaPlayer != null && !isMuted && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    // Esta función la usarán los botones de sonido de TODAS tus pantallas
    fun alternarMute(): Boolean {
        isMuted = !isMuted
        if (isMuted) {
            pausarMusica()
        } else {
            mediaPlayer?.start()
        }
        return isMuted // Devuelve el estado para cambiar el ícono
    }

    // Funciones para hacer "Ducking" (Bajar el volumen cuando el monstruo habla)
    fun atenuarParaVoz() {
        mediaPlayer?.setVolume(0.1f, 0.1f)
    }

    fun restaurarVolumen() {
        if (!isMuted) mediaPlayer?.setVolume(0.4f, 0.4f)
    }
}