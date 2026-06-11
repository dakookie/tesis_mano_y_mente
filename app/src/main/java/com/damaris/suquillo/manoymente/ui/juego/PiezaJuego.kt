package com.damaris.suquillo.manoymente.ui.juego

/**
 * Data class que representa cada pieza física del juego.
 * Mantiene toda la información visual, auditiva y de hardware (RFID) organizada.
 */
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PiezaJuego(
    val idRfid: String,           // El código de la tarjeta física (ej. "A1 B2 C3 D4")
    val nombrePronunciable: String, // Lo que dirá el TTS (ej. "la vaquita")
    val imagenSilueta: Int,       // ID de la imagen en escala de grises (R.drawable.hand_chicken)
    val imagenColor: Int,         // ID de la imagen a color (R.drawable.hand_chicken_fill)
    val idTitulo: Int,           // ID de la imagen del título (R.drawable.title_chicken)
    val idSonidoEfecto: Int? = null,       // ID del mp3 con el sonido del animal (R.raw.sonido_vaca)
    val idAudioVoz: Int
) : Parcelable
