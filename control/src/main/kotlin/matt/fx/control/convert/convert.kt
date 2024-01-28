package matt.fx.control.convert

import javafx.event.EventTarget
import matt.fx.graphics.wrapper.EventTargetWrapperImpl
import matt.lang.anno.Open
import matt.lang.convert.BiConverter

interface ETWrapperConverter<N: EventTarget, W: EventTargetWrapperImpl<N>>: BiConverter<N, W> {
  @Open
  override fun convertToA(b: W) = toNode(b)
  @Open override fun convertToB(a: N) = toWrapper(a)
  fun toNode(w: W): N
  fun toWrapper(n: N): W
}
