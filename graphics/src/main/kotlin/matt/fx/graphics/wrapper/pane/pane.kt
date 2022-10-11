package matt.fx.graphics.wrapper.pane

import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import matt.fx.graphics.service.uncheckedWrapperConverter
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.node.shape.rect.RectangleWrapper
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.hurricanefx.eye.wrapper.obs.collect.createMutableWrapper
import matt.log.warn.warnOnce
import matt.model.corner.Corner
import matt.model.corner.Corner.NE
import matt.model.corner.Corner.NW
import matt.model.corner.Corner.SE
import matt.model.corner.Corner.SW
import matt.obs.col.olist.mappedlist.toSyncedList

fun <C: NodeWrapper> ET.pane(op: PaneWrapperImpl<*, C>.()->Unit = {}) = attach(SimplePaneWrapper(), op)

interface PaneWrapper<C: NodeWrapper>: RegionWrapper<C> {


  override val node: Pane

  override fun addChild(child: NodeWrapper, index: Int?) {
	/*TODO: not type checking C*/
	require(index == null)
	@Suppress("UNCHECKED_CAST")
	children.add(child as C)
  }

  operator fun Collection<C>.unaryPlus() {
	addAll(this)
  }


  val children get() = node.children.createMutableWrapper().toSyncedList(uncheckedWrapperConverter<Node, C>())


  fun resizer(corner: Corner) {/*var y = 0.0
  var x = 0.0*/
	var initEventX = 0.0
	var initEventY = 0.0
	var initStageX = 0.0
	var initStageY = 0.0
	var initStageWidth = 0.0
	var initStageHeight = 0.0
	var initStageMaxX = 0.0
	var initStageMaxY = 0.0/*val MIN = 100.0*/
	var dragging = false
	fun isInDraggableZone(
	  @Suppress(
		"UNUSED_PARAMETER"
	  ) event: MouseEvent
	): Boolean {    /*return event.y > region.height - RESIZE_MARGIN*/
	  return true
	}
	add(RectangleWrapper(50.0, 50.0, Color.BLUE).apply {
	  setOnMouseReleased {
		dragging = false
		cursor = Cursor.DEFAULT
	  }

	  setOnMouseMoved {
		cursor = if (isInDraggableZone(it) || dragging) {
		  when (corner) {
			NW -> Cursor.NW_RESIZE
			NE -> Cursor.NE_RESIZE
			SW -> Cursor.SW_RESIZE
			SE -> Cursor.SE_RESIZE
		  }
		} else {
		  Cursor.DEFAULT
		}
	  }
	  setOnMouseDragged {
		if (dragging) {

		  when (corner) {
			NW -> {
			  stage!!.y = initStageY + (it.screenY - initEventY)
			  stage!!.height = initStageMaxY - stage!!.y
			  stage!!.x = initStageX + (it.screenX - initEventX)
			  stage!!.width = initStageMaxX - stage!!.x
			}

			NE -> {
			  stage!!.y = initStageY + (it.screenY - initEventY)
			  stage!!.height = initStageMaxY - stage!!.y
			  stage!!.width = initStageWidth + (it.screenX - initEventX)
			}

			SW -> {
			  stage!!.height = initStageHeight + (it.screenY - initEventY)
			  stage!!.x = initStageX + (it.screenX - initEventX)
			  stage!!.width = initStageMaxX - stage!!.x
			}

			SE -> {
			  stage!!.height = initStageHeight + (it.screenY - initEventY)
			  stage!!.width = initStageWidth + (it.screenX - initEventX)
			}
		  }
		}
	  }
	  setOnMousePressed {
		if (isInDraggableZone(it)) {
		  dragging = true
		  initEventX = it.screenX
		  initEventY = it.screenY
		  initStageHeight = stage!!.height
		  initStageWidth = stage!!.width
		  initStageX = stage!!.x
		  initStageY = stage!!.y
		  initStageMaxY = initStageHeight + initStageY
		  initStageMaxX = initStageWidth + initStageX
		}
	  }
	})

  }

  fun addAll(vararg nodes: C) = children.addAll(nodes)
  fun addAll(nodes: Iterable<C>) = children.addAll(nodes)

  fun clear() = children.clear()
}

fun <C: NodeWrapper> PaneWrapper<C>.spacer(size: Double = 20.0) {
  warnOnce("UNCHECKED ADD C SPACER")
  @Suppress("UNCHECKED_CAST")
  this.children.add(SimplePaneWrapper<VBoxWrapperImpl<*>>().apply {
	minWidth = size
	minHeight = size
  } as C)
}

fun <C: NodeWrapper> SimplePaneWrapper() = PaneWrapperImpl<_, C>(Pane())


open class PaneWrapperImpl<N: Pane, C: NodeWrapper>(
  node: N
): RegionWrapperImpl<N, C>(node), PaneWrapper<C> {
  override val childList get() = node.children
}