package matt.fx.graphics.service

import javafx.event.EventTarget
import javafx.scene.Node
import matt.fx.graphics.fxthread.runLaterReturn
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.text.text
import matt.lang.NEVER
import matt.model.op.convert.Converter
import matt.service.MattService
import matt.service.ServiceHub
import matt.service.action.ActionAbilitiesService

interface WrapperService: MattService {
  fun <E: EventTarget> wrapped(e: E): EventTargetWrapper

  fun <E: EventTarget, W: EventTargetWrapper> uncheckedWrapperConverter() = object: Converter<E, W> {


	override fun convertToB(a: E): W {
	  @Suppress("UNCHECKED_CAST") return wrapped(a) as W
	}

	override fun convertToA(b: W): E {
	  @Suppress("UNCHECKED_CAST") return b.node as E
	}


  }
}

object WrapperServiceHub: ServiceHub<WrapperService>()

internal fun EventTarget.wrapped() = WrapperServiceHub.get().wrapped(this)
internal fun Node.wrapped() = WrapperServiceHub.get().wrapped(this) as NodeWrapper

fun <E: EventTarget, W: EventTargetWrapper> uncheckedWrapperConverter() =
  WrapperServiceHub.get().uncheckedWrapperConverter<E, W>()

fun <E: EventTarget, W: EventTargetWrapper> uncheckedNullableWrapperConverter(): Converter<E?, W?> =
  uncheckedWrapperConverter<E, W>().nullable()

val nullableNodeConverter by lazy { uncheckedNullableWrapperConverter<Node, NodeWrapper>() }



