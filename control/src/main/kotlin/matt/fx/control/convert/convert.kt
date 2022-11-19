package matt.fx.control.convert

import javafx.event.EventTarget
import matt.fx.graphics.wrapper.EventTargetWrapperImpl
import matt.model.op.convert.Converter

interface ETWrapperConverter<N: EventTarget, W: EventTargetWrapperImpl<N>>: Converter<N, W> {
  override fun convertToA(b: W) = toNode(b)
  override fun convertToB(a: N) = toWrapper(a)
  fun toNode(w: W): N
  fun toWrapper(n: N): W
}
