package matt.fx.node.proto.indicatorcirc

import javafx.beans.value.ObservableValue
import javafx.scene.paint.Color
import matt.hurricanefx.eye.prop.objectBinding
import matt.hurricanefx.wrapper.shape.circle.CircleWrapper

fun indicatorCircle(booleanProperty: ObservableValue<Boolean>) = CircleWrapper(8.0).apply {
  fillProperty().bind(booleanProperty.objectBinding {
	val colo = if (it == true) Color.LIGHTGREEN else Color.DARKRED
	//	val colo = if (it == true) null else Color.DARKRED
	//	println("colo=$colo")
	colo
  })
}