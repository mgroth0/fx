package matt.fx.graphics.service

import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.service.MattService
import matt.service.ServiceHub
import org.w3c.dom.events.EventTarget

interface WrapperService: MattService {
  fun <E: EventTarget> wrapped(et: E): EventTargetWrapper
}

object WrapperServiceHub: ServiceHub<WrapperService>()