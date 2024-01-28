package matt.fx.control.popup.tooltip

import com.sun.javafx.geom.PickRay
import com.sun.javafx.scene.NodeHelper
import com.sun.javafx.scene.input.PickResultChooser
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.input.MouseEvent
import javafx.scene.input.PickResult
import javafx.scene.text.Font
import javafx.stage.Window
import javafx.util.Duration
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNullableProp
import matt.fx.control.inter.TextAndGraphic
import matt.fx.control.inter.graphic
import matt.fx.control.popup.popupcontrol.PopupControlWrapper
import matt.fx.control.popup.tooltip.node.MyTooltip
import matt.fx.graphics.service.nullableNodeConverter
import matt.fx.graphics.stylelock.toNonNullableStyleProp
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.lang.go
import matt.lang.assertions.require.requireNull
import matt.obs.prop.Var
import matt.obs.prop.VarProp
/*
fun NW.install(newToolTip: TooltipWrapper) {
  *//*if (this is ControlWrapperImpl<*>) {
	tooltip = newToolTip
  } else {*//*
  MyTooltip.install(
	this.node, newToolTip.node
  )
  *//*}*//*
}

fun NW.tooltip(text: String = "", graphic: NW? = null, op: TooltipWrapper.()->Unit = {}): TooltipWrapper {
  val newToolTip = TooltipWrapper(text).apply {
	this.graphic = graphic
	comfortablyShowForeverUntilEscaped()
	op()
  }
  install(newToolTip)
  return newToolTip
}*/


open class TooltipWrapper(node: MyTooltip = MyTooltip()): PopupControlWrapper<MyTooltip>(node), TextAndGraphic {

  constructor(s: String): this(MyTooltip(s))


  final override val textProperty: Var<String?> by lazy { node.textProperty().toNullableProp() }
  final override val fontProperty: Var<Font> by lazy { node.fontProperty().toNonNullableProp() }
  final override val graphicProperty by lazy { node.graphicProperty().toNullableProp().proxy(nullableNodeConverter) }
  final override val contentDisplayProp by lazy {
	node.contentDisplayProperty().toNonNullableStyleProp()
  }

  final override fun addChild(child: NodeWrapper, index: Int?) {
	requireNull(index)
	graphic = child
  }


  val wrapTextProp by lazy {
	node.wrapTextProperty().toNonNullableProp()
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
	  synchronized(this) {
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

sealed interface SendMouseEvents
object AutoDetectUnderlyingScene: SendMouseEvents
object Owner: SendMouseEvents
class SpecificScene(val scene: Scene): SendMouseEvents


/*https://stackoverflow.com/questions/31437758/how-to-make-a-tooltip-transparent-to-mouse-events*/

private val processMouseEvent by lazy {
  Scene::class.java.getDeclaredMethod("processMouseEvent", MouseEvent::class.java).apply {
	isAccessible = true
  }
}

internal fun correctTooltipNativeMouseEvent(
  event: MouseEvent, target: Scene? = null, exclude: Scene
): Boolean {
  val targetScene = target ?: getTargetScene(event, exclude)
  if (targetScene != null) {
	val chooser = PickResultChooser()


	val local = targetScene.root.screenToLocal(
	  event.screenX, event.screenY
	)

	NodeHelper.pickNode(
	  targetScene.root, PickRay(
		local.x,        /*event.screenX - targetScene.window.x - targetScene.x,*/
		local.y,        /*event.screenY - targetScene.window.y - targetScene.y,*/
		1.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY
	  ), chooser
	)

	/*	targetScene.root.impl_pickNode(
		  PickRay(
			local.x,
			*//*event.screenX - targetScene.window.x - targetScene.x,*//*
		local.y,
		*//*event.screenY - targetScene.window.y - targetScene.y,*//*
		1.0,
		Double.NEGATIVE_INFINITY,
		Double.POSITIVE_INFINITY
	  ),
	  chooser
	)*/


	val res: PickResult? = chooser.toPickResult()
	if (res != null) {    /*val pos: Point2D =
		res.getIntersectedNode().localToScene(res.getIntersectedPoint().getX(), res.getIntersectedPoint().getY())*/

	  val pos = res.intersectedNode.localToScene(
		res.intersectedPoint.x, res.intersectedPoint.y
	  )


	  val newEvent = MouseEvent(
		null, null, event.eventType, pos.x, pos.y, event.screenX, event.screenY, event.button, event.clickCount,
		event.isShiftDown, event.isControlDown, event.isAltDown, event.isMetaDown, event.isPrimaryButtonDown,
		event.isMiddleButtonDown, event.isSecondaryButtonDown, event.isSynthesized, event.isPopupTrigger,
		event.isStillSincePress, res
	  )

	  processMouseEvent.invoke(targetScene, newEvent)    //	  m.invoke(targetScene, newEvent)
	  /*targetScene.root.fireEvent(newEvent)*/    /*targetScene.eventDispatcher.dispatchEvent(newEvent,)*/    /*targetScene.impl_processMouseEvent(newEvent)*/



	  return true
	}
  }
  return false
}

private fun getTargetScene(event: MouseEvent, exclude: Scene): Scene? {
  val x: Double = event.screenX
  val y: Double = event.screenY
  var sx: Double
  var sy: Double
  var sw: Double
  var sh: Double/*val itr: Iterator<Window> = Window.impl_getWindows()*/
  val itr: Iterator<Window> = Window.getWindows().iterator()
  if (itr.hasNext()) {
	var w: Window = itr.next()
	while (itr.hasNext()) {
	  sx = w.x
	  sy = w.y
	  sw = w.width
	  sh = w.height
	  if (sx < x && x < sx + sw && sy < y && y < sy + sh && w.scene !== exclude) return w.scene
	  w = itr.next()
	}

  }
  return null
}