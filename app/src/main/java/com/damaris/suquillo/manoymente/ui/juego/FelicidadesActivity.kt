package com.damaris.suquillo.manoymente.ui.juego

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.damaris.suquillo.manoymente.R
import com.damaris.suquillo.manoymente.data.local.AppDatabase
import com.damaris.suquillo.manoymente.data.repository.EstudianteRepository
import com.damaris.suquillo.manoymente.ui.EfectosUi
import com.damaris.suquillo.manoymente.ui.GestorAudioGlobal
import com.damaris.suquillo.manoymente.ui.docente.DocenteActivity
import com.damaris.suquillo.manoymente.ui.inicio.MainActivity
import com.damaris.suquillo.manoymente.ui.inicio.SeleccionActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FelicidadesActivity : AppCompatActivity() {

    private lateinit var ivMonsterCelebrando: ImageView
    private var mediaPlayerVictoria: MediaPlayer? = null

    // Herramientas para el ciclo de animación del monstruo
    private val handlerAnimacion = Handler(Looper.getMainLooper())
    private var runnableAnimacion: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_felicidades)

        val categoriaJugada = intent.getStringExtra("CATEGORIA_JUGADA") ?: "animales"
        val ivPanelMadera = findViewById<ConstraintLayout>(R.id.ivPanelMadera)

        if (categoriaJugada == "figuras") {
            ivPanelMadera.setBackgroundResource(R.drawable.shapes)
        } else {
            ivPanelMadera.setBackgroundResource(R.drawable.animals)
        }

        ivMonsterCelebrando = findViewById(R.id.ivMonsterCelebrando)
        val ibInicioJuego = findViewById<ImageView>(R.id.ibInicioJuego)
        val ibJugarNuevo = findViewById<ImageView>(R.id.ibJugarNuevo)
        val ibBackJuego = findViewById<ImageButton>(R.id.ibBackJuego)
        val ibSonidoJuego = findViewById<ImageButton>(R.id.ibSonidoJuego)

        // ==========================================
        // 1. RECUPERAR DATOS DEL JUGADOR Y PUNTOS
        // ==========================================
        val idNino = intent.getIntExtra("ESTUDIANTE_ID", -1)
        val nombreNino = intent.getStringExtra("ESTUDIANTE_NOMBRE") ?: "Invitado"
        val avatarNino = intent.getIntExtra("ESTUDIANTE_AVATAR", R.drawable.monster_astronaut)

        val aciertosPartida = intent.getIntExtra("PUNTOS_ACIERTOS", 0)
        val erroresPartida = intent.getIntExtra("PUNTOS_ERRORES", 0)

        val resumenTexto = intent.getStringExtra("RESUMEN_TEXTO") ?: ""

        // ==========================================
        // 2. LÓGICA DE PERFIL (JUEGO LIBRE VS NIÑO)
        // ==========================================
        val llPerfilJugador = findViewById<LinearLayout>(R.id.llPerfilJugador)
        val tvNombreJugador = findViewById<TextView>(R.id.tvNombreJugador)
        val ivAvatarJugador = findViewById<ImageView>(R.id.ivAvatarJugador)

        if (idNino == -1) {
            llPerfilJugador.visibility = View.GONE
            ibInicioJuego.setImageResource(R.drawable.button_home)
        } else {
            llPerfilJugador.visibility = View.VISIBLE
            tvNombreJugador.text = nombreNino
            ivAvatarJugador.setImageResource(avatarNino)
            ibInicioJuego.setImageResource(R.drawable.button_panel)

            guardarPuntajeEnBaseDeDatos(idNino, categoriaJugada, aciertosPartida, erroresPartida, resumenTexto)
        }

        // ==========================================
        // 3. AUDIO Y CELEBRACIÓN
        // ==========================================
        GestorAudioGlobal.iniciarMusica(this)
        actualizarIconoSonido(ibSonidoJuego)

        Handler(Looper.getMainLooper()).postDelayed({
            reproducirSonidoVictoria(categoriaJugada)
        }, 800)

        // ==========================================
        // 4. LÓGICA DE BOTONES
        // ==========================================
        ibSonidoJuego.setOnClickListener {
            GestorAudioGlobal.alternarMute()
            actualizarIconoSonido(ibSonidoJuego)
            if (GestorAudioGlobal.isMuted) {
                apagarAudioYSalir()
            }
        }

        ibBackJuego.setOnClickListener {
            apagarAudioYSalir()
            val intent = if (idNino != -1) Intent(this, DocenteActivity::class.java) else Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        ibInicioJuego.setOnClickListener {
            apagarAudioYSalir()
            val intent = if (idNino != -1) Intent(this, DocenteActivity::class.java) else Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        ibJugarNuevo.setOnClickListener {
            apagarAudioYSalir()
            val intent = Intent(this, SeleccionActivity::class.java)
            intent.putExtra("ESTUDIANTE_ID", idNino)
            intent.putExtra("ESTUDIANTE_NOMBRE", nombreNino)
            intent.putExtra("ESTUDIANTE_AVATAR", avatarNino)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    // === REPRODUCTOR DEL MP3 DE VICTORIA Y ANIMACIÓN ===
    private fun reproducirSonidoVictoria(categoria: String) {
        if (GestorAudioGlobal.isMuted) return
        val sonidoId = if (categoria == "figuras") R.raw.sound_shapes else R.raw.sound_animals

        // 1. Preparamos el reproductor PRIMERO
        mediaPlayerVictoria = MediaPlayer.create(this, sonidoId)
        // 2. Programamos qué hacer cuando termine
        mediaPlayerVictoria?.setOnCompletionListener {
            GestorAudioGlobal.restaurarVolumen()
            detenerAnimacionMonstruo()
        }
        // 3. Atenuamos la música JUSTO ANTES de darle play
        GestorAudioGlobal.atenuarParaVoz()
        mediaPlayerVictoria?.start()
        iniciarAnimacionMonstruo()
    }

    private fun iniciarAnimacionMonstruo() {
        runnableAnimacion = object : Runnable {
            override fun run() {
                if (mediaPlayerVictoria?.isPlaying == true) {
                    EfectosUi.aplicarEfectoHablar(ivMonsterCelebrando)
                    handlerAnimacion.postDelayed(this, 600)
                }
            }
        }
        // Disparamos la primera vuelta de la animación
        handlerAnimacion.post(runnableAnimacion!!)
    }

    private fun detenerAnimacionMonstruo() {
        runnableAnimacion?.let { handlerAnimacion.removeCallbacks(it) }
    }

    private fun actualizarIconoSonido(boton: ImageButton) {
        val icono = if (GestorAudioGlobal.isMuted) R.drawable.ic_baseline_volume_off_24 else R.drawable.ic_baseline_volume_up_24
        boton.setImageResource(icono)
    }

    private fun guardarPuntajeEnBaseDeDatos(idEstudiante: Int, categoria: String, aciertosNuevos: Int, erroresNuevos: Int, resumenTexto: String = "") {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val repo = EstudianteRepository(db.estudianteDao())

                if (categoria == "animales") {
                    repo.guardarUltimoPuntajeAnimales(idEstudiante, aciertosNuevos, erroresNuevos, resumenTexto)
                } else {
                    repo.guardarUltimoPuntajeFiguras(idEstudiante, aciertosNuevos, erroresNuevos, resumenTexto)
                }

                withContext(Dispatchers.Main) {
                    //Toast.makeText(applicationContext, "¡Puntaje guardado exitosamente!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun apagarAudioYSalir() {
        GestorAudioGlobal.restaurarVolumen()
        detenerAnimacionMonstruo()
        if (mediaPlayerVictoria?.isPlaying == true) {
            mediaPlayerVictoria?.stop()
        }
        mediaPlayerVictoria?.release()
        mediaPlayerVictoria = null
    }

    override fun onDestroy() {
        apagarAudioYSalir()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        GestorAudioGlobal.pausarMusica()
    }

    // Se ejecuta cuando vuelves a abrir la app
    override fun onResume() {
        super.onResume()
        GestorAudioGlobal.reanudarMusica()
    }
}