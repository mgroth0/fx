package matt.fx.graphics.wrapper.style.gradient

import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import matt.lang.function.Dsl
import matt.model.code.idea.LinearGradientIdea


fun linearGradient(op: Dsl<LinearGradientDSL>): LinearGradient = LinearGradientDSL().apply(op).getGradient()



class LinearGradientDSL(): LinearGradientIdea {
    var startX = 0.0
    var startY = 0.0
    var endX = 0.1
    var endY = 0.1
    var proportional = true
    var cycleMethod = CycleMethod.REFLECT
    private val stops = mutableListOf<Stop>()
    fun stop(
        offset: Double,
        color: Color
    ) {
        stops += Stop(offset,color)
    }
    fun getGradient() = LinearGradient(
        startX,
        startY,
        endX,
        endY,
        proportional,
        cycleMethod,
        *stops.toTypedArray()
    )
}
