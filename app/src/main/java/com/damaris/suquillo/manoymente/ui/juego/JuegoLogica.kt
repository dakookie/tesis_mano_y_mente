package com.damaris.suquillo.manoymente.ui.juego

import com.damaris.suquillo.manoymente.R
import java.io.Serializable

data class DetalleFicha(
    val nombrePieza: String,
    val errores: Int,
    val tiempoSegundos: Int
) : Serializable

class JuegoLogica {

    // 1. LISTA DE ANIMALES
    private val listaAnimales = listOf(
        PiezaJuego("VACA", "vaca", R.drawable.hand_cow_luminous, R.drawable.hand_cow_fill, R.drawable.title_cow, R.raw.sonido_vaca, R.raw.voz_busca_vaca),
        PiezaJuego("GALLINA", "gallina", R.drawable.hand_chicken_luminous, R.drawable.hand_chicken_fill, R.drawable.title_chicken, R.raw.sonido_gallina, R.raw.voz_busca_gallina),
        PiezaJuego("CERDO", "cerdo", R.drawable.hand_pig_luminous, R.drawable.hand_pig_fill, R.drawable.title_pig, R.raw.sonido_cerdo, R.raw.voz_busca_cerdo),
        PiezaJuego("CONEJO", "conejo", R.drawable.hand_rabbit_luminous, R.drawable.hand_rabbit_fill, R.drawable.title_rabbit, R.raw.sonido_conejo, R.raw.voz_busca_conejo),
        PiezaJuego("PEZ", "pez", R.drawable.hand_fish_luminous, R.drawable.hand_fish_fill, R.drawable.title_fish, R.raw.sonido_pez, R.raw.voz_busca_pez),
        PiezaJuego("OVEJA", "oveja", R.drawable.hand_sheep_luminous, R.drawable.hand_sheep_fill, R.drawable.title_sheep, R.raw.sonido_oveja, R.raw.voz_busca_oveja)
    )
    // 2. LISTA DE FIGURAS
    private val listaFiguras = listOf(
        PiezaJuego("TRIÁNGULO", "triángulo", R.drawable.hand_triangle_luminous, R.drawable.hand_triangle_fill, R.drawable.title_triangle, null, R.raw.voz_busca_triangulo),
        PiezaJuego("CUADRADO", "cuadrado", R.drawable.hand_square_luminous, R.drawable.hand_square_fill, R.drawable.title_square, null, R.raw.voz_busca_cuadrado),
        PiezaJuego("CÍRCULO", "círculo", R.drawable.hand_circle_luminous, R.drawable.hand_circle_fill, R.drawable.title_circle, null, R.raw.voz_busca_circulo),
        PiezaJuego("ROMBO", "rombo", R.drawable.hand_diamond_luminous, R.drawable.hand_diamond_fill, R.drawable.title_diamond, null, R.raw.voz_busca_rombo),
        PiezaJuego("RECTÁNGULO", "rectángulo", R.drawable.hand_rectangle_luminous, R.drawable.hand_rectangle_fill, R.drawable.title_rectangle, null, R.raw.voz_busca_rectangulo),
        PiezaJuego("PENTÁGONO", "pentágono", R.drawable.hand_pentagon_luminous, R.drawable.hand_pentagon_fill, R.drawable.title_pentagon, null, R.raw.voz_busca_pentagono)
    )

    // 3. Control de estado dinámico
    private var listaActiva: List<PiezaJuego> = emptyList()
    private var indiceActual = 0

    var totalAciertos = 0
        private set
    var totalErrores = 0
        private set

    private var tiempoInicioFichaMilis: Long = 0
    private var erroresFichaActual = 0

    val detallesPartidaActual = mutableListOf<DetalleFicha>()

    // 4. Inicializar partida con la categoría elegida
    fun iniciarPartida(categoria: String) {
        listaActiva = if (categoria == "figuras") listaFiguras else listaAnimales
        indiceActual = 0
        totalAciertos = 0
        totalErrores = 0

        detallesPartidaActual.clear()
        erroresFichaActual = 0
        // Arrancamos el cronómetro de la primera ficha
        tiempoInicioFichaMilis = System.currentTimeMillis()
    }

    // 4. Obtener el animal que el niño debe buscar ahora mismo
    fun obtenerAnimalActual(): PiezaJuego? {
        return if (indiceActual < listaActiva.size) {
            listaActiva[indiceActual]
        } else {
            null
        }
    }

    // 5. Obtener la pieza actual
    fun obtenerPiezaActual(): PiezaJuego? {
        return if (indiceActual < listaActiva.size) {
            listaActiva[indiceActual]
        } else {
            null
        }
    }

    // 6. Obtener la pieza que se acaba de adivinar
    fun obtenerPiezaAnterior(): PiezaJuego {
        return listaActiva[indiceActual - 1]
    }

    // 7. Evaluar el Bluetooth
    fun verificarRfid(rfidLeido: String): Boolean {
        val rfidLimpio = rfidLeido.trim().uppercase()
        val piezaActual = obtenerPiezaActual() ?: return false

        return if (rfidLimpio == piezaActual.idRfid) {
            // ¡Acertó! Calculamos cuánto tiempo le tomó
            val tiempoFinMilis = System.currentTimeMillis()
            val tiempoTomadoSegundos = ((tiempoFinMilis - tiempoInicioFichaMilis) / 1000).toInt()

            // Guardamos el detalle de esta ficha en nuestra nueva lista
            detallesPartidaActual.add(
                DetalleFicha(
                    nombrePieza = piezaActual.nombrePronunciable,
                    errores = erroresFichaActual,
                    tiempoSegundos = tiempoTomadoSegundos
                )
            )
            // Preparamos los valores para la siguiente ficha
            totalAciertos++
            indiceActual++
            erroresFichaActual = 0 // Reiniciamos errores para la nueva ficha
            tiempoInicioFichaMilis = System.currentTimeMillis() // Reiniciamos cronómetro
            true
        } else {
            // ¡Se equivocó!
            totalErrores++
            erroresFichaActual++ // Sumamos un error a la ficha actual
            false
        }
    }

    // 8. Barra de progreso
    fun obtenerImagenProgreso(): Int {
        return when (indiceActual) {
            0 -> R.drawable.first_hand
            1 -> R.drawable.second_hand
            2 -> R.drawable.third_hand
            3 -> R.drawable.fourth_hand
            4 -> R.drawable.fifth_hand
            5 -> R.drawable.six_hand
            else -> 0
        }
    }

    // 9. Juego terminado
    fun juegoTerminado(): Boolean {
        return listaActiva.isNotEmpty() && indiceActual >= listaActiva.size
    }

    // 10. Obtener toda la lista jugada para el mural final
    fun obtenerListaCompleta(): List<PiezaJuego> {
        return listaActiva
    }
}