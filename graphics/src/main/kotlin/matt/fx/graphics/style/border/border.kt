package matt.fx.graphics.style.border

import javafx.geometry.Insets
import javafx.scene.layout.Border
import javafx.scene.layout.BorderImage
import javafx.scene.layout.BorderStroke
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.BorderWidths
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Paint

fun Paint.solidBorder() = FXBorder.solid(this)
fun Paint.dashedBorder() = FXBorder.solid(this)

object FXBorder {
    fun solid(color: Paint, widths: Double? = null) =
        Border(BorderStroke(color, BorderStrokeStyle.SOLID, null, widths?.let { BorderWidths(widths) }))

    fun dashed(color: Paint) = Border(BorderStroke(color, BorderStrokeStyle.DASHED, null, null))
    val EMPTY: Border by lazy { Border.EMPTY }
}


fun Border.copy(
    strokes: List<BorderStroke> = this.strokes.toList(),
    images: List<BorderImage> = this.images.toList()
): Border = Border(strokes, images)

fun BorderStroke.copy(
    topStroke: Paint = this.topStroke,
    rightStroke: Paint = this.rightStroke,
    bottomStroke: Paint = this.bottomStroke,
    leftStroke: Paint = this.leftStroke,
    topStyle: BorderStrokeStyle = this.topStyle,
    rightStyle: BorderStrokeStyle = this.rightStyle,
    bottomStyle: BorderStrokeStyle = this.bottomStyle,
    leftStyle: BorderStrokeStyle = this.leftStyle,
    radii: CornerRadii = this.radii,
    widths: BorderWidths = this.widths,
    insets: Insets = this.insets
): BorderStroke {
    Unit.run {
        return BorderStroke(
            topStroke,
            rightStroke,
            bottomStroke,
            leftStroke,
            topStyle,
            rightStyle,
            bottomStyle,
            leftStyle,
            radii,
            widths,
            insets
        )
    }
}
