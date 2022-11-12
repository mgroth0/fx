package matt.fx.control.proto.functionbutton

import matt.fx.control.wrapper.control.button.ButtonWrapper
import matt.fx.control.wrapper.control.button.button
import matt.fx.graphics.wrapper.ET
import kotlin.reflect.KFunction

fun ET.functionButton(func: KFunction<*>, vararg args: Any?, opNotArg: ButtonWrapper.()->Unit = {}) {
  button(func.name) {
	setOnAction {
	  func.call(*args) // "instance" is automatically included in KFunction I think!
	  //            this_obj.inv
	}
	opNotArg()
  }
}