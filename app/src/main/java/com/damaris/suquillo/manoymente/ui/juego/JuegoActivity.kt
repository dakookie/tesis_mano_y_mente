package com.damaris.suquillo.manoymente.ui.juego

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.damaris.suquillo.manoymente.R
import com.damaris.suquillo.manoymente.data.hardware.LectorRfidManager
import com.damaris.suquillo.manoymente.ui.GestorAudioGlobal

class JuegoActivity : AppCompatActivity(), LectorRfidManager.RfidListener {

    // --- Variables de Interfaz ---
    private lateinit var ivTituloAnimal: ImageView
    private lateinit var ivAnimalCentro: ImageView
    private lateinit var ivMascotaJuego: ImageView
    private lateinit var ivProgresoManos: ImageView

    // --- Lógica, Audio y Hardware ---
    private val logica = JuegoLogica()

    private var mediaPlayerVoz: MediaPlayer? = null
    private var mediaPlayerEfecto: MediaPlayer? = null

    private var bloqueado = false
    private var lectorRfid: LectorRfidManager? = null // Para manejar la conexión Bluetooth y lectura de RFID

    // --- Datos del Niño (para mostrar su perfil y pasar a la pantalla final) ---
    private var idNino: Int = -1
    private var nombreNino: String = "Invitado"
    private var avatarNino: Int = R.drawable.monster_astronaut

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego)

        ivTituloAnimal = findViewById(R.id.ivTituloAnimal)
        ivAnimalCentro = findViewById(R.id.ivAnimalCentro)
        ivMascotaJuego = findViewById(R.id.ivMascotaJuego)
        ivProgresoManos = findViewById(R.id.ivProgresoManos)

        val ibBack = findViewById<ImageButton>(R.id.ibBackJuego)
        val ibSonido = findViewById<ImageButton>(R.id.ibSonidoJuego)

        val categoriaRecibida = intent.getStringExtra("CATEGORIA_SELECCIONADA") ?: "animales"
        idNino = intent.getIntExtra("ESTUDIANTE_ID", -1)
        nombreNino = intent.getStringExtra("ESTUDIANTE_NOMBRE") ?: "Invitado"
        avatarNino = intent.getIntExtra("ESTUDIANTE_AVATAR", R.drawable.monster_astronaut)

        val llPerfilJugador = findViewById<android.widget.LinearLayout>(R.id.llPerfilJugador)
        val tvNombreJugador = findViewById<android.widget.TextView>(R.id.tvNombreJugador)
        val ivAvatarJugador = findViewById<ImageView>(R.id.ivAvatarJugador)

        if (idNino == -1) {
            llPerfilJugador.visibility = android.view.View.GONE // Es Juego Libre (MainActivity), ocultamos todo el cuadrito
        } else {
            llPerfilJugador.visibility = android.view.View.VISIBLE // Es un niño registrado (DocenteActivity), mostramos sus datos
            tvNombreJugador.text = nombreNino
            ivAvatarJugador.setImageResource(avatarNino)
        }

        logica.iniciarPartida(categoriaRecibida)

        ibBack.setOnClickListener { finish() }
        actualizarIconoSonido(ibSonido)
        ibSonido.setOnClickListener {
            GestorAudioGlobal.alternarMute()
            actualizarIconoSonido(ibSonido)
            if (GestorAudioGlobal.isMuted) detenerAudiosLocal()
        }

        // CONEXIÓN BLUETOOTH
        lectorRfid = LectorRfidManager(this)
        solicitarPermisosBluetooth()

        cargarSiguienteReto(reproducirVoz = false)
        Handler(Looper.getMainLooper()).postDelayed({
            val piezaActual = logica.obtenerPiezaActual()
            if (piezaActual != null) {
                reproducirVozMonstruo(piezaActual.idAudioVoz)
            }
        }, 800)
    }


    private fun actualizarIconoSonido(boton: ImageButton) {
        val icono = if (GestorAudioGlobal.isMuted) R.drawable.ic_baseline_volume_off_24 else R.drawable.ic_baseline_volume_up_24
        boton.setImageResource(icono)
    }

    // --- MÉTODOS DE LA INTERFAZ BLUETOOTH (RfidListener) ---
    override fun onTagLeido(tag: String) {
        if (!bloqueado) {
            procesarLecturaRfid(tag)
        }
    }

    override fun onConectado() {
        Toast.makeText(this, "¡Mesa Montessori Conectada!", Toast.LENGTH_SHORT).show()
    }

    override fun onError(mensaje: String) {
        Toast.makeText(this, "Hardware: $mensaje", Toast.LENGTH_LONG).show()
    }

    // --- SEGURIDAD: PERMISOS DE BLUETOOTH PARA ANDROID ---
    private fun solicitarPermisosBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN), 100)
                return
            }
        }
        lectorRfid?.conectar()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            lectorRfid?.conectar()
        } else {
            Toast.makeText(this, "Permisos de Bluetooth denegados. Juega en Modo Táctil.", Toast.LENGTH_LONG).show()
        }
    }


    private fun cargarSiguienteReto(reproducirVoz: Boolean = true) {
        if (logica.juegoTerminado()) {
            detenerAudiosLocal()
            val intentFinal = Intent(this, FelicidadesActivity::class.java)
            val listaJugada = ArrayList(logica.obtenerListaCompleta())

            intentFinal.putParcelableArrayListExtra("LISTA_PIEZAS", listaJugada)
            intentFinal.putExtra("CATEGORIA_JUGADA", intent.getStringExtra("CATEGORIA_SELECCIONADA") ?: "animales")

            // Pasamos los datos del niño...
            intentFinal.putExtra("ESTUDIANTE_ID", idNino)
            intentFinal.putExtra("ESTUDIANTE_NOMBRE", nombreNino)
            intentFinal.putExtra("ESTUDIANTE_AVATAR", avatarNino)

            // Pasamos los puntos de esta partida
            intentFinal.putExtra("PUNTOS_ACIERTOS", logica.totalAciertos)
            intentFinal.putExtra("PUNTOS_ERRORES", logica.totalErrores)

            // SOLUCIÓN DEFINITIVA: Armamos el texto garantizando que entren todas las piezas
            val textoParaGuardar = logica.detallesPartidaActual.joinToString(";") { detalle ->
                val textoErrores = if (detalle.errores == 0) "Acierto" else "${detalle.errores} Error(es)"
                val nombreLimpio = detalle.nombrePieza.replace("el ", "").replace("la ", "").replaceFirstChar { it.uppercase() }

                // Formato: Vaca|Acierto|5s
                "$nombreLimpio|$textoErrores|${detalle.tiempoSegundos}s"
            }

            // Mandamos el String listo
            intentFinal.putExtra("RESUMEN_TEXTO", textoParaGuardar)

            startActivity(intentFinal)
            finish()
            return
        }

        // ==========================================
        // Apagar el sonido del animal anterior si seguía sonando
        // ==========================================
        if (mediaPlayerEfecto?.isPlaying == true) {
            mediaPlayerEfecto?.stop()
        }
        mediaPlayerEfecto?.release()
        mediaPlayerEfecto = null
        // ==========================================

        bloqueado = false
        val piezaActual = logica.obtenerPiezaActual()!!

        ivTituloAnimal.setImageResource(piezaActual.idTitulo)
        ivAnimalCentro.setImageResource(piezaActual.imagenSilueta)
        ivMascotaJuego.setImageResource(R.drawable.monster_indicate_right)

        val imagenProgreso = logica.obtenerImagenProgreso()
        if (imagenProgreso != 0) {
            ivProgresoManos.setImageResource(imagenProgreso)
        }

        if (reproducirVoz) {
            reproducirVozMonstruo(piezaActual.idAudioVoz)
        }
    }

    private fun procesarLecturaRfid(tagLeido: String) {
        val acerto = logica.verificarRfid(tagLeido)
        bloqueado = true

        if (acerto) {
            ejecutarAnimacionAcierto()
        } else {
            ejecutarAnimacionError()
        }
    }

    private fun ejecutarAnimacionAcierto() {
        val piezaCompletada = logica.obtenerPiezaAnterior()
        ivAnimalCentro.setImageResource(piezaCompletada.imagenColor)
        ivMascotaJuego.setImageResource(R.drawable.monster_celebrating)

        // Reproduce "¡Excelente! Lo lograste."
        reproducirVozMonstruo(R.raw.voz_acierto)
        // Reproduce el sonido del animal al mismo tiempo si es que tiene
        if (!GestorAudioGlobal.isMuted && piezaCompletada.idSonidoEfecto != null) {
            mediaPlayerEfecto?.release()
            mediaPlayerEfecto = MediaPlayer.create(this, piezaCompletada.idSonidoEfecto)
            mediaPlayerEfecto?.start()
        }

        // Damos un poco de tiempo extra (3500ms) para que terminen ambos audios
        Handler(Looper.getMainLooper()).postDelayed({
            cargarSiguienteReto(reproducirVoz = true)
        }, 3500)
    }

    private fun ejecutarAnimacionError() {
        ivMascotaJuego.setImageResource(R.drawable.monster_pensative_third)
        // Reproduce "¡Ups! Inténtalo de nuevo."
        reproducirVozMonstruo(R.raw.voz_error)
        Handler(Looper.getMainLooper()).postDelayed({
            ivMascotaJuego.setImageResource(R.drawable.monster_indicate_right)
            bloqueado = false
        }, 3000)
    }

    private fun reproducirVozMonstruo(idAudio: Int) {
        detenerVozMonstruo() // Detiene cualquier voz anterior para no traslaparse
        if (GestorAudioGlobal.isMuted) return

        GestorAudioGlobal.atenuarParaVoz()
        mediaPlayerVoz = MediaPlayer.create(this, idAudio)
        mediaPlayerVoz?.setOnCompletionListener {
            GestorAudioGlobal.restaurarVolumen()
        }
        mediaPlayerVoz?.start()
    }

    private fun detenerVozMonstruo() {
        if (mediaPlayerVoz?.isPlaying == true) {
            mediaPlayerVoz?.stop()
        }
        mediaPlayerVoz?.release()
        mediaPlayerVoz = null
        GestorAudioGlobal.restaurarVolumen()
    }

    private fun detenerAudiosLocal() {
        detenerVozMonstruo()
        if (mediaPlayerEfecto?.isPlaying == true) {
            mediaPlayerEfecto?.stop()
        }
        mediaPlayerEfecto?.release()
        mediaPlayerEfecto = null
    }

    override fun onDestroy() {
        lectorRfid?.desconectar()
        detenerAudiosLocal()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        detenerAudiosLocal()
        GestorAudioGlobal.pausarMusica()
    }

    // Se ejecuta cuando vuelves a abrir la app
    override fun onResume() {
        super.onResume()
        GestorAudioGlobal.reanudarMusica()
    }
}