package matt.fx.graphics.style.background

import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.paint.Paint
import matt.obs.prop.writable.Var


fun backgroundFill(c: Paint) = BackgroundFill(c, null, null)
fun backgroundFromColor(c: Paint) = Background(backgroundFill(c))


fun Var<Background?>.ensureLastFillIsIfPresent(paint: Paint) {
    val currentBG = value
    if (currentBG != null) {
        val currentBGFills = currentBG.fills
        if (currentBGFills.last().fill != paint) {
            val standards = currentBGFills.filter { it.fill != paint }
            val example = standards.first()
            val cool = BackgroundFill(paint, example.radii, example.insets)
            value = Background(*standards.toTypedArray(), cool)
        }
    }
}

