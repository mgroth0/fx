package matt.fx.graphics.wrapper.target

import javafx.beans.value.ObservableValue
import javafx.collections.ObservableMap
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.Scene
import javafx.stage.Window
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.hurricanefx.eye.lib.onChange
import sun.tools.jconsole.Tab


fun <T: EventTargetWrapper> T.replaceChildren(op: T.()->Unit) {
  childList?.clear()
  op(this)
}


/**
 * Listen for changes to an observable value and replace all content in this Node with the
 * new content created by the onChangeBuilder. The builder operates on the node and receives
 * the new value of the observable as it's only parameter.
 *
 * The onChangeBuilder is run immediately with the current value of the property.
 */
fun <S: EventTargetWrapper, T> S.dynamicContent(property: ObservableValue<T>, onChangeBuilder: S.(T?)->Unit) {
  val onChange: (T?)->Unit = {
	childList?.clear()
	onChangeBuilder(this@dynamicContent, it)
  }
  property.onChange(onChange)
  onChange(property.value)
}





