package matt.fx.node.proto.indicatorcirc

import javafx.scene.paint.Color
import matt.hurricanefx.wrapper.shape.circle.CircleWrapper
import matt.obs.prop.ValProp
import matt.obs.bind.binding

fun indicatorCircle(booleanProperty: ValProp<Boolean>) = CircleWrapper(8.0).apply {
  fillProperty.bind(booleanProperty.binding {
	val colo = if (it == true) Color.LIGHTGREEN else Color.DARKRED
	//	val colo = if (it == true) null else Color.DARKRED
	//	println("colo=$colo")
	colo
  })
}