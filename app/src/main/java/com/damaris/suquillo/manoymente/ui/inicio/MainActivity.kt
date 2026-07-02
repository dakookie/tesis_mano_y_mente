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
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private var mediaPlayerVoz: MediaPlayer? = null
    private var dialogoAyuda: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Iniciar la música global apenas se abre la app
        GestorAudioGlobal.iniciarMusica(this)

        val botonIniciar = findViewById<ImageView>(R.id.ivBotonIniciar)
        val ibSonido = findViewById<ImageButton>(R.id.ibSonido)
        val botonUsuario = findViewById<ImageView>(R.id.ibDocente)
        val botonAyuda = findViewById<ImageView>(R.id.ibAyuda)

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

        botonAyuda.setOnClickListener {
            detenerVoz()
            mostrarDialogoAyuda()
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

    private fun mostrarDialogoAyuda() {
        // Evita abrir dos diálogos al mismo tiempo.
        dialogoAyuda?.dismiss()

        // 1. SILENCIAMOS LA MÚSICA INMEDIATAMENTE AL ENTRAR A LA AYUDA
        GestorAudioGlobal.pausarMusica()

        val dialog = Dialog(this)
        dialogoAyuda = dialog

        dialog.setContentView(R.layout.dialog_ayuda)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // REFERENCIAS DEL MENÚ IZQUIERDO
        val btnCerrar = dialog.findViewById<ImageButton>(R.id.btnCerrarDialogAyuda)
        val tabGuia = dialog.findViewById<TextView>(R.id.btnTabGuia)
        val tabVideos = dialog.findViewById<TextView>(R.id.btnTabVideos)
        val tabSoporte = dialog.findViewById<TextView>(R.id.btnTabSoporte)

        // REFERENCIAS DEL PANEL DERECHO
        val vistaInstrucciones = dialog.findViewById<View>(R.id.vistaInstrucciones)
        val vistaSoporte = dialog.findViewById<View>(R.id.vistaSoporte)
        val vistaTutoriales = dialog.findViewById<View>(R.id.vistaTutoriales)

        // REFERENCIAS A LOS PLAYERVIEW
        val videoConexion = dialog.findViewById<PlayerView>(R.id.videoConexion)
        val videoRfid = dialog.findViewById<PlayerView>(R.id.videoRfid)
        val videoGestion = dialog.findViewById<PlayerView>(R.id.videoGestion)

        // Lista para controlar todos los reproductores
        val reproductores = mutableListOf<ExoPlayer>()

        fun crearReproductor(
            playerView: PlayerView,
            recursoVideo: Int
        ): ExoPlayer {
            val reproductor = ExoPlayer.Builder(this@MainActivity).build()
            val uriVideo = Uri.parse("android.resource://${this@MainActivity.packageName}/$recursoVideo")

            playerView.player = reproductor
            reproductor.apply {
                setMediaItem(MediaItem.fromUri(uriVideo))
                playWhenReady = false
                repeatMode = Player.REPEAT_MODE_OFF
                prepare()
            }
            reproductores.add(reproductor)
            return reproductor
        }

        // Crear los reproductores antes de utilizarlos
        val reproductorConexion: ExoPlayer = crearReproductor(playerView = videoConexion, recursoVideo = R.raw.tuto_conexion)
        val reproductorRfid: ExoPlayer = crearReproductor(playerView = videoRfid, recursoVideo = R.raw.tuto_juegomesa)
        val reproductorGestion: ExoPlayer = crearReproductor(playerView = videoGestion, recursoVideo = R.raw.tuto_pantallas)

        fun pausarTodosLosVideos() {
            reproductores.forEach { reproductor ->
                reproductor.pause()
            }
        }

        fun detenerOtrosVideos(reproductorActivo: ExoPlayer) {
            reproductores.forEach { reproductor ->
                if (reproductor !== reproductorActivo) {
                    reproductor.pause()
                }
            }
        }

        // ESCUCHAR CUANDO UN REPRODUCTOR INICIA PARA PAUSAR LOS DEMÁS
        fun agregarListenerControl(reproductorActivo: ExoPlayer) {
            reproductorActivo.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        detenerOtrosVideos(reproductorActivo)
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        reproductorActivo.seekTo(0)
                        reproductorActivo.pause()
                    }
                }
            })
        }

        agregarListenerControl(reproductorConexion)
        agregarListenerControl(reproductorRfid)
        agregarListenerControl(reproductorGestion)

        // LIBERAR LOS REPRODUCTORES
        fun liberarReproductores() {
            videoConexion.player = null
            videoRfid.player = null
            videoGestion.player = null

            reproductores.forEach { reproductor ->
                reproductor.stop()
                reproductor.clearMediaItems()
                reproductor.release()
            }
            reproductores.clear()
        }

        // ESTILO VISUAL DE LAS PESTAÑAS
        fun actualizarPestañas(pestañaActiva: TextView) {
            val colorInactivo = Color.parseColor("#8D6E63")
            val colorActivo = Color.parseColor("#5D4037")

            tabGuia.setTextColor(colorInactivo)
            tabGuia.setTypeface(null, Typeface.NORMAL)
            tabVideos.setTextColor(colorInactivo)
            tabVideos.setTypeface(null, Typeface.NORMAL)
            tabSoporte.setTextColor(colorInactivo)
            tabSoporte.setTypeface(null, Typeface.NORMAL)

            pestañaActiva.setTextColor(colorActivo)
            pestañaActiva.setTypeface(null, Typeface.BOLD)
        }

        // PESTAÑA DE INSTRUCCIONES
        tabGuia.setOnClickListener {
            pausarTodosLosVideos()
            actualizarPestañas(tabGuia)
            vistaInstrucciones.visibility = View.VISIBLE
            vistaTutoriales.visibility = View.GONE
            vistaSoporte.visibility = View.GONE
        }

        // PESTAÑA DE VIDEOS
        tabVideos.setOnClickListener {
            actualizarPestañas(tabVideos)
            vistaInstrucciones.visibility = View.GONE
            vistaTutoriales.visibility = View.VISIBLE
            vistaSoporte.visibility = View.GONE
        }

        // PESTAÑA DE SOPORTE
        tabSoporte.setOnClickListener {
            pausarTodosLosVideos()
            actualizarPestañas(tabSoporte)
            vistaInstrucciones.visibility = View.GONE
            vistaTutoriales.visibility = View.GONE
            vistaSoporte.visibility = View.VISIBLE
        }

        // BOTÓN PARA CERRAR EL DIÁLOGO
        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        // ACCIONES AL CERRAR EL DIÁLOGO
        dialog.setOnDismissListener {
            liberarReproductores()

            // 2. REANUDAMOS LA MÚSICA SOLO SI EL USUARIO NO LA HABÍA MUTADO ANTES
            if (!GestorAudioGlobal.isMuted) {
                GestorAudioGlobal.reanudarMusica()
            }

            if (dialogoAyuda === dialog) {
                dialogoAyuda = null
            }
        }

        dialog.show()
        val anchoPantalla = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(anchoPantalla, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onResume() {
        super.onResume()
        // Asegurar que la música siga sonando si regresamos a esta pantalla desde otra
        GestorAudioGlobal.iniciarMusica(this)
        GestorAudioGlobal.reanudarMusica()
    }

    override fun onPause() {
        super.onPause()
        dialogoAyuda?.dismiss()
        // Si el usuario sale de la app o cambia de pantalla, cortamos el audio limpiamente
        detenerVoz()
        GestorAudioGlobal.pausarMusica()
    }
}