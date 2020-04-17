package com.tonyjhuang.qfit.ui

import android.graphics.Color
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size

object Konfetti {
    fun show(konfetti: KonfettiView) {
        konfetti.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
            .setDirection(0.0, 359.0)
            .setSpeed(6f, 12f)
            .setFadeOutEnabled(true)
            .setTimeToLive(1000L)
            .addShapes(Shape.Square, Shape.Circle)
            .addSizes(Size(12), Size(16, 6f))
            .setPosition(konfetti.x + konfetti.width / 2, konfetti.y + konfetti.height / 3)
            .burst(100)
    }
}