package matt.fx.node.procpane.status

import javafx.geometry.Insets
import javafx.geometry.Orientation.HORIZONTAL
import javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import matt.async.safe.with
import matt.async.thread.daemon
import matt.file.MFile
import matt.fx.control.wrapper.scroll.ScrollPaneWrapper
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.fxthread.runLaterReturn
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.fx.graphics.wrapper.text.text
import matt.fx.graphics.wrapper.text.textlike.applyConsoleStyle
import matt.fx.graphics.wrapper.textflow.TextFlowWrapper
import matt.fx.node.tileabletabpane.TileableTabPane
import matt.log.printlnWithTime
import java.util.concurrent.Semaphore

class StatusFolderWatchPane(val folder: MFile): TileableTabPane(
  orientation = HORIZONTAL
) {
  init {
	istabmode = false
  }

  companion object {
	const val REFRESH_RATE_MS = 1000L
  }

  private val mysem = Semaphore(1)
  private var running = false
  private var stopped = true
  fun start() = mysem.with {
	if (!running) {
	  // start here
	  running = true
	  daemon {
		while (!stopped) {
		  Thread.sleep(100)
		}
		stopped = false
		while (running) {
		  refresh()
		  Thread.sleep(REFRESH_RATE_MS)
		}
		stopped = true
	  }
	}
  }

  fun stop() = mysem.with {
	running = false
  }

  private val filenodes = mutableMapOf<String, ScrollPaneWrapper<*>>()
  private fun filenode(file: MFile): ScrollPaneWrapper<*> {

	return (if (filenodes.containsKey(file.name)) {
	  filenodes[file.name]!!
	} else {
	  ScrollPaneWrapper<RegionWrapper<*>>().apply {
		val sp = this
		vbarPolicy = NEVER
		hbarPolicy = NEVER
		content = TextFlowWrapper<NodeWrapper>().apply {
		  padding = Insets(5.0)
		  //                matt.fx.graphics.style.background.backgroundFill = matt.css.Color.BLACK
		  this minBind sp
		}
		backgroundProperty.bindBidirectional(content!!.backgroundProperty)
		this@StatusFolderWatchPane.filenodes[file.name] = this
	  }
	  //	  TextFlowWrapper<NodeWrapper>().apply {
	  //		padding = Insets(5.0)
	  //		//                matt.fx.graphics.style.background.backgroundFill = matt.css.Color.BLACK
	  //	  } wrappedIn ScrollPaneNoBars<TextFlowWrapper<NodeWrapper>>().apply {
	  //		this@StatusFolderWatchPane.filenodes[file.name] = this
	  //	  }
	}).apply {
	  (content as TextFlow).children.setAll(TextWrapper().applyConsoleStyle(size = 8.0).apply {
		text = System.currentTimeMillis().toString() + " | " + file.readText()
		fill = Color.LIGHTSALMON
	  }.node)
	  (content as TextFlow).wrapped().text("") /*matt.log.level.getDEBUG FORCE UPDATE*/
	}
  }

  private fun refresh() {
	if (!folder.exists()) {
	  return
	}
	runLaterReturn {
	  panes = folder.listFiles()!!.filter { it.extension == "status" }.map {
		it.nameWithoutExtension to filenode(it).apply {
		  if (false) {
			printlnWithTime("matt.log.level.getDEBUG:${(this.content as TextFlow).children.joinToString { t -> (t as Text).text }}")
		  }
		}

	  }.toTypedArray()
	  reset()
	}
  }
}