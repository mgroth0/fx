package matt.fx.graphics.effect

import javafx.scene.effect.Blend
import javafx.scene.effect.BlendMode.DIFFERENCE
import javafx.scene.effect.ColorInput
import javafx.scene.paint.Color

val INVERSION_EFFECT by lazy {
  val color = ColorInput()
  color.paint = Color.WHITE
  color.width = Double.MAX_VALUE
  color.height = Double.MAX_VALUE
  val blend = Blend(DIFFERENCE)
  blend.bottomInput = color
  blend
}