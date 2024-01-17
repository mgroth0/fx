package matt.fx.control.wrapper.wrapped.util

import javafx.event.EventTarget
import kotlin.reflect.KClass


fun EventTarget.cannotFindWrapper(): Nothing = throw (CannotFindWrapperException(this::class))

class CannotFindWrapperException(val cls: KClass<out EventTarget>) : Exception(
    "what is the wrapper for ${cls.qualifiedName}?"
)

/*?: run {
  val theMap = constructorMap
//	  W::class.starProjectedType
  val theConstructor = theMap[W::class.starProjectedType]
  theConstructor.call(this) as W
}*//*?: W::class.primaryConstructor!!.call(this)*/


