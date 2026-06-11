package com.damaris.suquillo.manoymente.ui

import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation

object EfectosUi {
    fun aplicarEfectoHablar(view: View) {
        // Hace que la imagen crezca un 15% y vuelva a su tamaño rápido
        val animation = ScaleAnimation(
            1.0f, 1.15f, 1.0f, 1.15f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        animation.duration = 250
        animation.repeatCount = 1
        animation.repeatMode = Animation.REVERSE
        view.startAnimation(animation)
    }
}