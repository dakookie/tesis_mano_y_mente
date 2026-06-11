package com.damaris.suquillo.manoymente.data.repository

import com.damaris.suquillo.manoymente.data.local.Estudiante
import com.damaris.suquillo.manoymente.data.local.EstudianteDao

class EstudianteRepository(private val estudianteDao: EstudianteDao) {

    suspend fun insertar(estudiante: Estudiante) {
        estudianteDao.insertarEstudiante(estudiante)
    }

    suspend fun obtenerTodos(): List<Estudiante> {
        return estudianteDao.obtenerTodosLosEstudiantes()
    }

    suspend fun actualizar(estudiante: Estudiante) {
        estudianteDao.actualizarEstudiante(estudiante)
    }

    suspend fun eliminar(estudiante: Estudiante) {
        estudianteDao.eliminarEstudiante(estudiante)
    }

    suspend fun guardarUltimoPuntajeAnimales(id: Int, aciertos: Int, errores: Int, resumen: String) {
        estudianteDao.guardarUltimoPuntajeAnimales(id, aciertos, errores, resumen)
    }

    suspend fun guardarUltimoPuntajeFiguras(id: Int, aciertos: Int, errores: Int, resumen: String) {
        estudianteDao.guardarUltimoPuntajeFiguras(id, aciertos, errores, resumen)
    }
}