package com.damaris.suquillo.manoymente.ui.docente

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.damaris.suquillo.manoymente.R
import com.damaris.suquillo.manoymente.data.local.AppDatabase
import com.damaris.suquillo.manoymente.data.local.Estudiante
import com.damaris.suquillo.manoymente.data.repository.EstudianteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.damaris.suquillo.manoymente.ui.GestorAudioGlobal
import com.damaris.suquillo.manoymente.ui.inicio.SeleccionActivity

class DocenteActivity : AppCompatActivity() {

    private var avatarSeleccionadoId: Int? = null
    private var edadSeleccionada: Int = 4

    // Vistas principales
    private lateinit var vistaRegistrar: View
    private lateinit var vistaMisEstudiantes: View
    private lateinit var vistaLeaderboard: View

    // Botones del menú lateral
    private lateinit var btnLeaderboard: Button
    private lateinit var btnMisEstudiantes: Button
    private lateinit var btnRegistrar: Button
    private lateinit var btnEdad3: Button
    private lateinit var btnEdad4: Button

    // Adaptadores
    private lateinit var adaptadorEstudiantes: EstudianteAdapter
    private lateinit var adaptadorLeaderboard: LeaderboardAdapter

    // Controles del Leaderboard
    private lateinit var btnTabAnimales: Button
    private lateinit var btnTabFiguras: Button
    private var categoriaLeaderboardActual = "animales"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docente)

        val ibBack = findViewById<ImageButton>(R.id.ibBackDocente)
        ibBack.setOnClickListener { finish() }
        val ibSonido = findViewById<ImageButton>(R.id.ibSonidoDocente)
        ibSonido.setImageResource(
            if (GestorAudioGlobal.isMuted)
                R.drawable.ic_baseline_volume_off_24
            else
                R.drawable.ic_baseline_volume_up_24
        )
        ibSonido.setOnClickListener {
            GestorAudioGlobal.alternarMute()
            if (GestorAudioGlobal.isMuted) {
                // Cambiamos el ícono a "silenciado"
                ibSonido.setImageResource(R.drawable.ic_baseline_volume_off_24)
                //Toast.makeText(this, "Sonido silenciado", Toast.LENGTH_SHORT).show()
            } else {
                // Cambiamos el ícono a "con volumen"
                ibSonido.setImageResource(R.drawable.ic_baseline_volume_up_24)
                //Toast.makeText(this, "Sonido encendido", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. INICIALIZAR VISTAS
        vistaRegistrar = findViewById(R.id.vistaRegistrar)
        vistaMisEstudiantes = findViewById(R.id.vistaMisEstudiantes)
        vistaLeaderboard = findViewById(R.id.vistaLeaderboard)

        btnLeaderboard = findViewById(R.id.btnLeaderboard)
        btnMisEstudiantes = findViewById(R.id.btnMisEstudiantes)
        btnRegistrar = findViewById(R.id.btnRegistrar)

        // 3. CONFIGURAR TABLAS
        configurarTablaEstudiantes()
        configurarTablaLeaderboard()

        // 4. CONFIGURAR CLICS DEL MENÚ LATERAL
        btnMisEstudiantes.setOnClickListener { mostrarPantallaMisEstudiantes() }
        btnRegistrar.setOnClickListener { mostrarPantallaRegistrar() }
        btnLeaderboard.setOnClickListener { mostrarPantallaLeaderboard() }

        // 5. CONFIGURAR REGISTRO Y EDAD
        btnEdad3 = vistaRegistrar.findViewById(R.id.btnEdad3)
        btnEdad4 = vistaRegistrar.findViewById(R.id.btnEdad4)
        btnEdad3.setOnClickListener { seleccionarEdad(3) }
        btnEdad4.setOnClickListener { seleccionarEdad(4) }
        configurarAvatares()

        val btnGuardarEstudiante = vistaRegistrar.findViewById<Button>(R.id.btnGuardarEstudiante)
        val etNombre = vistaRegistrar.findViewById<EditText>(R.id.etNombre)

        btnGuardarEstudiante.setOnClickListener {
            val nombreIngresado = etNombre.text.toString().trim()
            if (nombreIngresado.isEmpty() || avatarSeleccionadoId == null) {
                Toast.makeText(this, "Completa todos los datos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val nuevoEstudiante = Estudiante(
                nombre = nombreIngresado,
                edad = edadSeleccionada,
                avatarId = avatarSeleccionadoId!!
            )
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(applicationContext)
                val repositorio = EstudianteRepository(db.estudianteDao())
                repositorio.insertar(nuevoEstudiante)
                withContext(Dispatchers.Main) {
                    //Toast.makeText(this@DocenteActivity, "¡Estudiante guardado!", Toast.LENGTH_SHORT).show()
                    etNombre.text.clear()
                    avatarSeleccionadoId = null
                    mostrarPantallaMisEstudiantes()
                }
            }
        }
        mostrarPantallaRegistrar()
    }

    // ==========================================
    // FUNCIONES DEL LEADERBOARD
    // ==========================================
    private fun configurarTablaLeaderboard() {
        val rvListaLeaderboard = vistaLeaderboard.findViewById<RecyclerView>(R.id.rvListaLeaderboard)
        rvListaLeaderboard.layoutManager = LinearLayoutManager(this)

        adaptadorLeaderboard = LeaderboardAdapter(emptyList(), "animales") { estudianteSeleccionado ->
            mostrarDialogoDetalle(estudianteSeleccionado)
        }
        rvListaLeaderboard.adapter = adaptadorLeaderboard

        btnTabAnimales = vistaLeaderboard.findViewById(R.id.btnTabAnimales)
        btnTabFiguras = vistaLeaderboard.findViewById(R.id.btnTabFiguras)

        btnTabAnimales.setOnClickListener {
            categoriaLeaderboardActual = "animales"
            btnTabAnimales.setBackgroundResource(R.drawable.bg_boton_docente_activo)
            btnTabFiguras.setBackgroundResource(R.drawable.bg_boton_docente_inactivo)
            cargarDatosLeaderboard()
        }

        btnTabFiguras.setOnClickListener {
            categoriaLeaderboardActual = "figuras"
            btnTabFiguras.setBackgroundResource(R.drawable.bg_boton_docente_activo)
            btnTabAnimales.setBackgroundResource(R.drawable.bg_boton_docente_inactivo)
            cargarDatosLeaderboard()
        }
    }

    private fun cargarDatosLeaderboard() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val repositorio = EstudianteRepository(db.estudianteDao())
            val listaFresca = repositorio.obtenerTodos()

            // Ordenamos la lista con dos reglas:
            // 1. Mayor cantidad de aciertos primero (compareByDescending)
            // 2. En caso de empate, menor cantidad de errores primero (thenBy)
            val listaOrdenada = if (categoriaLeaderboardActual == "animales") {
                listaFresca.sortedWith(
                    compareByDescending<Estudiante> { it.aciertosAnimales }
                        .thenBy { it.erroresAnimales }
                )
            } else {
                listaFresca.sortedWith(
                    compareByDescending<Estudiante> { it.aciertosFiguras }
                        .thenBy { it.erroresFiguras }
                )
            }
            withContext(Dispatchers.Main) {
                adaptadorLeaderboard.actualizarLista(listaOrdenada, categoriaLeaderboardActual)
            }
        }
    }

    // ==========================================
    // FUNCIONES DE CONTROL DE PANTALLAS
    // ==========================================
    private fun mostrarPantallaMisEstudiantes() {
        vistaRegistrar.visibility = View.GONE
        vistaLeaderboard.visibility = View.GONE
        vistaMisEstudiantes.visibility = View.VISIBLE

        btnMisEstudiantes.setBackgroundResource(R.drawable.bg_boton_docente_activo)
        btnRegistrar.setBackgroundResource(R.drawable.bg_boton_docente_inactivo)
        btnLeaderboard.setBackgroundResource(R.drawable.bg_boton_docente_inactivo)

        cargarEstudiantesDeLaBaseDeDatos()
    }

    private fun mostrarPantallaRegistrar() {
        vistaMisEstudiantes.visibility = View.GONE
        vistaLeaderboard.visibility = View.GONE
        vistaRegistrar.visibility = View.VISIBLE

        btnRegistrar.setBackgroundResource(R.drawable.bg_boton_docente_activo)
        btnMisEstudiantes.setBackgroundResource(R.drawable.bg_boton_docente_inactivo)
        btnLeaderboard.setBackgroundResource(R.drawable.bg_boton_docente_inactivo)
    }

    private fun mostrarPantallaLeaderboard() {
        vistaRegistrar.visibility = View.GONE
        vistaMisEstudiantes.visibility = View.GONE
        vistaLeaderboard.visibility = View.VISIBLE

        btnLeaderboard.setBackgroundResource(R.drawable.bg_boton_docente_activo)
        btnRegistrar.setBackgroundResource(R.drawable.bg_boton_docente_inactivo)
        btnMisEstudiantes.setBackgroundResource(R.drawable.bg_boton_docente_inactivo)

        cargarDatosLeaderboard()
    }

    // ==========================================
    // FUNCIONES DE "MIS ESTUDIANTES" Y REGISTRO
    // ==========================================
    private fun configurarTablaEstudiantes() {
        val rvListaEstudiantes = vistaMisEstudiantes.findViewById<RecyclerView>(R.id.rvListaEstudiantes)
        rvListaEstudiantes.layoutManager = LinearLayoutManager(this)
        adaptadorEstudiantes = EstudianteAdapter(emptyList(),
            onEditarClick = { estudianteSeleccionado ->
                mostrarDialogoEditar(estudianteSeleccionado)
            },
            onJugarClick = { estudianteSeleccionado ->
                val intent = android.content.Intent(this, SeleccionActivity::class.java)
                intent.putExtra("ESTUDIANTE_ID", estudianteSeleccionado.id)
                intent.putExtra("ESTUDIANTE_NOMBRE", estudianteSeleccionado.nombre)
                intent.putExtra("ESTUDIANTE_AVATAR", estudianteSeleccionado.avatarId)
                startActivity(intent)
            }
        )
        rvListaEstudiantes.adapter = adaptadorEstudiantes
    }

    private fun cargarEstudiantesDeLaBaseDeDatos() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val repositorio = EstudianteRepository(db.estudianteDao())
            val listaFresca = repositorio.obtenerTodos()
            withContext(Dispatchers.Main) {
                adaptadorEstudiantes.actualizarLista(listaFresca)
            }
        }
    }

    private fun configurarAvatares() {
        val mis17Avatares = listOf(
            R.drawable.monster_teacher, R.drawable.monster_artist, R.drawable.monster_astronaut,
            R.drawable.monster_chef, R.drawable.monster_constructor, R.drawable.monster_doctor,
            R.drawable.monster_farmer, R.drawable.monster_firefighter, R.drawable.monster_journalist,
            R.drawable.monster_king, R.drawable.monster_pirate, R.drawable.monster_marine,
            R.drawable.monster_layer, R.drawable.monster_pilot, R.drawable.monster_police,
            R.drawable.monster_photographer, R.drawable.monster_magician
        )

        val rvAvatares = vistaRegistrar.findViewById<RecyclerView>(R.id.rvAvatares)
        rvAvatares.layoutManager = GridLayoutManager(this, 5)
        val adaptador = AvatarAdapter(mis17Avatares) { idSeleccionado ->
            avatarSeleccionadoId = idSeleccionado
        }
        rvAvatares.adapter = adaptador
    }

    private fun seleccionarEdad(edad: Int) {
        edadSeleccionada = edad
        btnEdad3.setBackgroundResource(R.drawable.bg_boton_docente_inactivo)
        btnEdad4.setBackgroundResource(R.drawable.bg_boton_docente_inactivo)
        when (edad) {
            3 -> btnEdad3.setBackgroundResource(R.drawable.bg_boton_docente_activo)
            4 -> btnEdad4.setBackgroundResource(R.drawable.bg_boton_docente_activo)
        }
    }

    private fun mostrarDialogoEditar(estudiante: Estudiante) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_editar_estudiante)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etNombre = dialog.findViewById<EditText>(R.id.etEditarNombre)
        val btnEdad3 = dialog.findViewById<Button>(R.id.btnEditarEdad3)
        val btnEdad4 = dialog.findViewById<Button>(R.id.btnEditarEdad4)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnDialogCancelar)
        val btnEliminar = dialog.findViewById<Button>(R.id.btnDialogEliminar)
        val btnGuardar = dialog.findViewById<Button>(R.id.btnDialogGuardar)
        val btnCerrar = dialog.findViewById<ImageButton>(R.id.btnCerrarDialog)

        var edadEditada = estudiante.edad
        etNombre.setText(estudiante.nombre)

        fun actualizarBotonesEdadDialogo() {
            btnEdad3.setBackgroundResource(if (edadEditada == 3) R.drawable.bg_boton_docente_activo else R.drawable.bg_boton_docente_inactivo)
            btnEdad4.setBackgroundResource(if (edadEditada == 4) R.drawable.bg_boton_docente_activo else R.drawable.bg_boton_docente_inactivo)
        }
        actualizarBotonesEdadDialogo()

        btnEdad3.setOnClickListener { edadEditada = 3; actualizarBotonesEdadDialogo() }
        btnEdad4.setOnClickListener { edadEditada = 4; actualizarBotonesEdadDialogo() }

        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnCerrar.setOnClickListener { dialog.dismiss() }

        btnEliminar.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(applicationContext)
                val repo = EstudianteRepository(db.estudianteDao())
                repo.eliminar(estudiante)
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    cargarEstudiantesDeLaBaseDeDatos()
                    //Toast.makeText(this@DocenteActivity, "Estudiante eliminado", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnGuardar.setOnClickListener {
            val nuevoNombre = etNombre.text.toString().trim()
            if (nuevoNombre.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val estudianteActualizado = estudiante.copy(nombre = nuevoNombre, edad = edadEditada)

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(applicationContext)
                val repo = EstudianteRepository(db.estudianteDao())
                repo.actualizar(estudianteActualizado)
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    cargarEstudiantesDeLaBaseDeDatos()
                    Toast.makeText(this@DocenteActivity, "Estudiante actualizado", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun mostrarDialogoDetalle(estudiante: Estudiante) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_detalle_partida)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitulo = dialog.findViewById<android.widget.TextView>(R.id.tvTituloDetalle)
        val tvCategoria = dialog.findViewById<android.widget.TextView>(R.id.tvCategoriaDetalle)
        val ibCerrarX = dialog.findViewById<ImageButton>(R.id.ibCerrarDetalle)

        val llTablaDetalles = dialog.findViewById<android.widget.LinearLayout>(R.id.llTablaDetalles)
        val tvMensajeVacio = dialog.findViewById<android.widget.TextView>(R.id.tvMensajeVacio)
        val tvCabeceraPieza = dialog.findViewById<android.widget.TextView>(R.id.tvCabeceraPieza)

        tvTitulo.text = "Resumen de ${estudiante.nombre}"

        val resumenCrudo = if (categoriaLeaderboardActual == "animales") {
            tvCategoria.text = "Categoría: Animales"
            tvCabeceraPieza.text = "Animal"
            estudiante.resumenAnimales
        } else {
            tvCategoria.text = "Categoría: Figuras"
            tvCabeceraPieza.text = "Figura"
            estudiante.resumenFiguras
        }

        if (resumenCrudo.isEmpty()) {
            llTablaDetalles.visibility = android.view.View.GONE
            tvMensajeVacio.visibility = android.view.View.VISIBLE
        } else {
            llTablaDetalles.visibility = android.view.View.VISIBLE
            tvMensajeVacio.visibility = android.view.View.GONE

            val filas = resumenCrudo.split(";")
            for (filaCruda in filas) {
                if (filaCruda.isNotBlank()) {
                    val datos = filaCruda.split("|")

                    if (datos.size == 3) {
                        // Creamos una fila estable con LinearLayout horizontal
                        val filaLayout = android.widget.LinearLayout(this).apply {
                            orientation = android.widget.LinearLayout.HORIZONTAL
                            setPadding(0, 24, 0, 24)
                        }

                        // Parámetro para que cada columna tome el 33% del espacio exacto
                        val params = android.widget.LinearLayout.LayoutParams(
                            0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                        )

                        val tvPieza = android.widget.TextView(this).apply {
                            text = datos[0]
                            setTextColor(android.graphics.Color.parseColor("#5D4037"))
                            textSize = 16f
                            gravity = android.view.Gravity.CENTER
                            layoutParams = params
                        }

                        val tvResultado = android.widget.TextView(this).apply {
                            text = datos[1]
                            val colorTexto = if (datos[1] == "Acierto") "#388E3C" else "#D32F2F"
                            setTextColor(android.graphics.Color.parseColor(colorTexto))
                            setTypeface(null, android.graphics.Typeface.BOLD)
                            textSize = 16f
                            gravity = android.view.Gravity.CENTER
                            layoutParams = params
                        }

                        val tvTiempo = android.widget.TextView(this).apply {
                            text = datos[2]
                            setTextColor(android.graphics.Color.parseColor("#5D4037"))
                            textSize = 16f
                            gravity = android.view.Gravity.CENTER
                            layoutParams = params
                        }

                        filaLayout.addView(tvPieza)
                        filaLayout.addView(tvResultado)
                        filaLayout.addView(tvTiempo)
                        llTablaDetalles.addView(filaLayout)

                        // Divisor seguro
                        val divisor = android.view.View(this).apply {
                            layoutParams = android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 2
                            )
                            setBackgroundColor(android.graphics.Color.parseColor("#EFEBE9"))
                        }
                        llTablaDetalles.addView(divisor)
                    }
                }
            }
        }
        ibCerrarX?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // Se ejecuta cuando minimizas la app o pasas a otra
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