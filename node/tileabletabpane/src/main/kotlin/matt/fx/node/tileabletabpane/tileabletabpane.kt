package matt.fx.node.tileabletabpane

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.geometry.Orientation.HORIZONTAL
import javafx.geometry.Orientation.VERTICAL
import javafx.scene.layout.Priority.ALWAYS
import matt.async.safe.with
import matt.fx.control.menu.context.mcontextmenu
import matt.fx.control.wrapper.control.tab.TabWrapper
import matt.fx.control.wrapper.tab.TabPaneWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.PaneWrapper
import matt.fx.graphics.wrapper.pane.hbox.hbox
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.eye.prop.getValue
import matt.hurricanefx.eye.prop.setValue
import matt.obs.math.double.op.div
import java.util.concurrent.Semaphore

open class TileableTabPane(
  protected vararg var panes: Pair<String, RegionWrapper<NodeWrapper>>,
  orientation: Orientation = VERTICAL
): VBoxWrapperImpl<NodeWrapper>() {
  private val mysem = Semaphore(1)
  var lastSelected: Int? = null
  val istabmodeprop = SimpleBooleanProperty(true).apply {
	onChange {
	  reset()
	}
  }
  var istabmode: Boolean by istabmodeprop

  var orientationProp: SimpleObjectProperty<Orientation> = SimpleObjectProperty(orientation).apply {
	onChange {
	  reset()
	}
  }
  var orientation by orientationProp

  init {
	mcontextmenu {
	  val switch_to_tiles = "switch to tiles"
	  item(switch_to_tiles) {

		this@TileableTabPane.istabmodeprop.onChange {
		  text = if (it) switch_to_tiles
		  else "switch to tabs"
		}
		setOnAction { this@TileableTabPane.switch() }
	  }
	}
	reset()
  }

  private fun switch() {
	istabmode = !istabmode
  }

  protected fun reset() = mysem.with {
	if (istabmode) tabmode()
	else tilemode()
  }

  private fun tabmode() {
	clear()
	add(TabPaneWrapper(*panes.map {
	  TabWrapper(it.first, it.second).apply {
		isClosable = false
		it.second.prefHeightProperty.bind(this@TileableTabPane.heightProperty)
		it.second.prefWidthProperty.bind(this@TileableTabPane.widthProperty)
	  }
	}.toTypedArray()).apply {
	  vgrow = ALWAYS

	  if (this@TileableTabPane.lastSelected != null && this@TileableTabPane.lastSelected!! < this@TileableTabPane.panes.size) {
		selectionModel.select(this@TileableTabPane.lastSelected!!)
	  }
	  selectionModel.selectedIndexProperty().onChange {
		this@TileableTabPane.lastSelected = it
	  }
	})
  }

  private fun tilemode() {
	clear()
	var root: PaneWrapper<*> = this
	if (orientation == HORIZONTAL) {
	  root = root.hbox<NodeWrapper> {
		vgrow = ALWAYS
	  }
	}
	for (pane in panes) {
	  root.add(pane.second)
	}
	val nchildren = panes.size.toDouble()
	panes.map { it.second }.forEach {
	  if (orientation == VERTICAL) {
		it.minHeightProperty.bind(root.heightProperty/(nchildren))
		it.maxHeightProperty.bind(root.heightProperty/(nchildren))
		it.vgrow = ALWAYS
	  } else {
		it.minWidthProperty.bind(root.widthProperty/(nchildren))
		it.maxWidthProperty.bind(root.widthProperty/(nchildren))
		it.hgrow = ALWAYS
	  }
	}
  }
}

