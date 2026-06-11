package com.damaris.suquillo.manoymente.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "estudiantes")
data class Estudiante(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,
    val edad: Int,
    val avatarId: Int,

    // --- CAMPOS PARA EL LEADERBOARD ---
    val aciertosAnimales: Int = 0,
    val erroresAnimales: Int = 0,

    val aciertosFiguras: Int = 0,
    val erroresFiguras: Int = 0,

    // --- NUEVOS CAMPOS PARA EL RESUMEN DETALLADO ---
    @ColumnInfo(name = "resumen_animales") var resumenAnimales: String = "",
    @ColumnInfo(name = "resumen_figuras") var resumenFiguras: String = ""
)