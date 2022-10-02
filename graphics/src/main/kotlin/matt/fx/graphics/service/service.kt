package matt.fx.graphics.service

import javafx.scene.Node
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.service.MattService
import matt.service.ServiceHub
import org.w3c.dom.events.EventTarget

interface WrapperService: MattService {
  fun <E: EventTarget> wrapped(et: E): EventTargetWrapper
}

object WrapperServiceHub: ServiceHub<WrapperService>()

internal fun EventTarget.wrapped() = WrapperServiceHub.get().wrapped(this)
internal fun Node.wrapped() = WrapperServiceHub.get().wrapped(this)