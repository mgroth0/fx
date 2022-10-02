package matt.fx.control.proto.functionbutton

import matt.fx.control.control.dsl.button
import matt.fx.graphics.wrapper.ET
import kotlin.reflect.KFunction

fun ET.functionButton(func: KFunction<Unit>) {
  button(func.name) {
	setOnAction {
	  func.call() // "instance" is automatically included in KFunction I think!
	  //            this_obj.inv
	}
  }
}