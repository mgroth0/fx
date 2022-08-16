package matt.fx.node.proto.indicatorcirc

import javafx.beans.property.BooleanProperty
import javafx.scene.paint.Color
import matt.hurricanefx.eye.prop.objectBinding
import matt.hurricanefx.wrapper.shape.circle.CircleWrapper

fun indicatorCircle(booleanProperty: BooleanProperty) = CircleWrapper(8.0).apply {
  fillProperty().bind(booleanProperty.objectBinding {
	val colo = if (it == true) Color.LIGHTGREEN else Color.DARKRED
	//	val colo = if (it == true) null else Color.DARKRED
	//	println("colo=$colo")
	colo
  })
}