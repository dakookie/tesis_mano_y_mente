package com.damaris.suquillo.manoymente.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface EstudianteDao {
    @Insert
    suspend fun insertarEstudiante(estudiante: Estudiante)

    @Query("SELECT * FROM estudiantes ORDER BY nombre ASC")
    suspend fun obtenerTodosLosEstudiantes(): List<Estudiante>

    @Update
    suspend fun actualizarEstudiante(estudiante: Estudiante)

    @Delete
    suspend fun eliminarEstudiante(estudiante: Estudiante)

    // Actualiza tus funciones de guardado para que incluyan el parámetro "resumen":
    @Query("UPDATE estudiantes SET aciertosanimales = :aciertos, erroresanimales = :errores, resumen_animales = :resumen WHERE id = :id")
    suspend fun guardarUltimoPuntajeAnimales(id: Int, aciertos: Int, errores: Int, resumen: String)

    @Query("UPDATE estudiantes SET aciertosfiguras = :aciertos, erroresfiguras = :errores, resumen_figuras = :resumen WHERE id = :id")
    suspend fun guardarUltimoPuntajeFiguras(id: Int, aciertos: Int, errores: Int, resumen: String)
}