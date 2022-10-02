package matt.fx.graphics.wrapper.inter.titled

import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.obs.prop.Var

interface Titled: EventTargetWrapper {
  val titleProperty: Var<String?>
  var title: String?
	get() = titleProperty.value
	set(value) = titleProperty v value
}