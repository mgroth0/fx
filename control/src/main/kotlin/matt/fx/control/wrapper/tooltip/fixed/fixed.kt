package matt.fx.control.wrapper.tooltip.fixed

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.util.Duration
import matt.fx.control.wrapper.label.LabelWrapper
import matt.fx.control.wrapper.popwinwrap.PopupWindowWrapper
import matt.fx.control.wrapper.tooltip.AutoDetectUnderlyingScene
import matt.fx.control.wrapper.tooltip.Owner
import matt.fx.control.wrapper.tooltip.SendMouseEvents
import matt.fx.control.wrapper.tooltip.SpecificScene
import matt.fx.control.wrapper.tooltip.correctTooltipNativeMouseEvent
import matt.fx.control.wrapper.tooltip.node.simple.MyFixedTooltip
import matt.fx.graphics.service.nullableParentConverter
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.lang.delegation.lazyVarDelegate
import matt.lang.go
import matt.lang.sync
import matt.obs.prop.VarProp

fun NW.install(newToolTip: FixedTooltipWrapper) {
  /*if (this is ControlWrapperImpl<*>) {
	matt.fx.control.wrapper.tooltip.fixed.tooltip = newToolTip
  } else {*/
  MyFixedTooltip.install(
	this.node, newToolTip.node
  )
  /*}*/
}

fun NW.tooltip(text: String = "", graphic: NW? = null, op: FixedTooltipWrapper.()->Unit = {}): FixedTooltipWrapper {
  val newToolTip = FixedTooltipWrapper().apply {


	this.content = LabelWrapper(text, graphic?.node)
	comfortablyShowForeverUntilEscaped()
	op()
  }
  install(newToolTip)
  return newToolTip
}

fun NW.tooltip(content: NW?, op: FixedTooltipWrapper.()->Unit = {}): FixedTooltipWrapper {
  val newToolTip = FixedTooltipWrapper().apply {
	this.content = content
	comfortablyShowForeverUntilEscaped()
	op()
  }
  install(newToolTip)
  return newToolTip
}


open class FixedTooltipWrapper(node: MyFixedTooltip = MyFixedTooltip()): PopupWindowWrapper<MyFixedTooltip>(node) {



  val contentProperty by lazy {
	node.contentProperty().toNullableProp().proxy(nullableParentConverter)
  }
  var content by lazyVarDelegate {
	contentProperty
  }

  override fun addChild(child: NodeWrapper, index: Int?) {
	require(index == null)
	contentProperty v child
  }


  fun comfortablyShowForeverUntilMouseMoved() {
	showDelay = Duration.millis(100.0)
	showDuration = Duration.INDEFINITE
  }

  fun comfortablyShowForeverUntilEscaped() {
	comfortablyShowForeverUntilMouseMoved()
	hideDelay = Duration.INDEFINITE
  }

  var showDelay: Duration
	get() = node.showDelay
	set(value) {
	  node.setShowDelay(value)
	}
  var hideDelay: Duration
	get() = node.hideDelay
	set(value) {
	  node.setHideDelay(value)
	}
  var showDuration: Duration
	get() = node.showDuration
	set(value) {
	  node.setShowDuration(value)
	}
  var consumeAutoHidingEvents by node::consumeAutoHidingEvents
  var isAutoFix by node.autoFixProperty().toNonNullableProp()
  var isAutoHide by node.autoHideProperty().toNonNullableProp()


  private var transparentMouseEventHandler: EventHandler<MouseEvent>? = null

  val sendMouseEventsToProp = VarProp<SendMouseEvents?>(null).apply {
	onChange { opt ->
	  sync {
		if (opt == null) {
		  transparentMouseEventHandler?.go {
			removeEventFilter(MouseEvent.ANY, it)
			transparentMouseEventHandler = null
		  }
		} else {

		  addEventFilter(MouseEvent.ANY, EventHandler<MouseEvent> {
			correctTooltipNativeMouseEvent(
			  it, target = when (opt) {
				AutoDetectUnderlyingScene -> null
				is Owner                  -> node.ownerWindow.scene
				is SpecificScene          -> opt.scene
			  }, exclude = scene!!.node
			)
			it.consume()
		  }.also {
			transparentMouseEventHandler = it
		  })
		}
	  }
	}
  }
  var sendMouseEventsTo by sendMouseEventsToProp

}