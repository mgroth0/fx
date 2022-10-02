package matt.fx.control.toggle

import javafx.beans.value.ObservableValue
import javafx.scene.control.ToggleGroup
import matt.fx.control.tfx.control.bind
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NW

private object ToggleGroupKey

fun ET.getToggleGroup(): ToggleGroup? = properties[ToggleGroupKey] as ToggleGroup?


fun NW.toggleGroup(property: ObservableValue<Any>? = null, op: ToggleGroup.()->Unit = {}) =
  ToggleGroup().also { tg ->
	properties[ToggleGroupKey] = tg
	property?.let { tg.bind(it) }
	op(tg)
  }

