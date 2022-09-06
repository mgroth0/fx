package matt.fx.node.file

import javafx.application.Platform.runLater
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority.ALWAYS
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.img
import kotlinx.html.stream.createHTML
import matt.async.schedule.AccurateTimer
import matt.async.schedule.every
import matt.async.thread.daemon
import matt.css.Color.black
import matt.css.sty
import matt.file.HTMLFile
import matt.file.LogFile
import matt.file.MFile
import matt.file.SvgFile
import matt.fx.graphics.lang.actionbutton
import matt.fx.graphics.menu.context.mcontextmenu
import matt.fx.graphics.win.interact.doubleClickToOpenInWindow
import matt.fx.graphics.win.interact.openImageInWindow
import matt.fx.graphics.win.interact.openInNewWindow
import matt.fx.graphics.win.interact.safe
import matt.fx.graphics.win.stage.WMode.CLOSE
import matt.fx.web.WebViewPane
import matt.fx.web.WebViewWrapper
import matt.fx.web.specialZooming
import matt.gui.draggableIcon
import matt.hurricanefx.async.runLaterReturn
import matt.hurricanefx.eye.lang.BProp
import matt.hurricanefx.eye.lang.IProp
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.tornadofx.item.spinner
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.hurricanefx.wrapper.node.disableWhen
import matt.hurricanefx.wrapper.pane.SimplePaneWrapper
import matt.hurricanefx.wrapper.pane.vbox.VBoxWrapper
import matt.hurricanefx.wrapper.region.RegionWrapper
import matt.hurricanefx.wrapper.text.TextWrapper
import matt.lang.err
import matt.time.dur.sec
import java.lang.Thread.sleep
import java.lang.ref.WeakReference


fun MFile.createNode(renderHTMLAndSVG: Boolean = false): RegionWrapper<NodeWrapper> {
  val node = createNodeInner(renderHTMLAndSVG = renderHTMLAndSVG)
  node.mcontextmenu {
	item(s = "", g = draggableIcon())
  }
  return node
}

private fun MFile.createNodeInner(renderHTMLAndSVG: Boolean = false): RegionWrapper<NodeWrapper> {
  if (exists()) {
	println("opening file")
	if (isImage()) {
	  return SimplePaneWrapper<NodeWrapper>().apply {
		imageview {
		  image = Image(toURI().toString())
		  isPreserveRatio = true
		  bindFitTo(this@apply)
		  isSmooth = true
		  mcontextmenu {
			item("os open") {
			  setOnAction {
				java.awt.Desktop.getDesktop().open(this@createNodeInner)
			  }
			}
			item("test open in new window") { onAction = EventHandler { openImageInWindow() } }
		  }
		  doubleClickToOpenInWindow()
		}
	  }
	}




	if (extension in listOf(
		"txt",
		"yml",
		"json",
		"sh",
		"kt",
		"java",
		"py",
		"kts",
		"gradle",
		"css",
		"less",
		"js",
		"tags",
		"coffeescript"
	  ) || (!renderHTMLAndSVG && extension in (listOf("html", "svg")))
	) {
	  val viewbox = SimplePaneWrapper<NodeWrapper>()
	  var fsText = readText()
	  val ta = viewbox.textarea {
		text = fsText
		prefHeightProperty.bind(viewbox.prefHeightProperty)
		prefWidthProperty.bind(viewbox.prefWidthProperty)
	  }
	  viewbox.button("matt.gui.ser.save changes") {
		isDisable = true
		setOnAction {
		  writeText(ta.text)
		}
		ta.textProperty.onChange {
		  val fsTextCurrent = readText()
		  if (fsTextCurrent != fsText) {
			safe("file content on system changed. Reload?") {
			  text = fsTextCurrent
			  fsText = fsTextCurrent
			}
		  } else {
			isDisable = fsTextCurrent == text
		  }
		}
	  }
	  if (extension in (listOf("html", "svg")) && !renderHTMLAndSVG) {
		viewbox.button("render") {
		  setOnAction {
			WebViewPane(this@createNodeInner).openInNewWindow(wMode = CLOSE)
		  }
		}
	  }
	  return viewbox
	}






	return when (this) {
	  is LogFile  -> VBoxWrapper<NodeWrapper>().apply {
		val lineLimit = IProp(1000)
		val infiniteLines = BProp(false)
		hbox<NodeWrapper> {
		  checkbox("infinite", property = infiniteLines)
		  spinner(property = lineLimit, amountToStepBy = 1000) {
			disableWhen { infiniteLines }
		  }
		  actionbutton("perma-clear") {
			this@createNodeInner.write("")
		  }
		}

		textarea {
		  vgrow = ALWAYS
		  addEventFilter(KeyEvent.KEY_TYPED) { it.consume() }
		  addEventFilter(KeyEvent.KEY_PRESSED) {
			if (it.code in listOf(
				KeyCode.DELETE, KeyCode.BACK_SPACE
			  )
			) it.consume()
		  }
		  val weakRef = WeakReference(this)

		  @Synchronized
		  fun refresh() {
			val ta = weakRef.get() ?: return
			val linLim = if (infiniteLines.get()) Integer.MAX_VALUE else lineLimit.get()
			var lines = readText().lines()
			if (lines.size > linLim) {
			  lines = lines.subList(lines.size - linLim, lines.size)
			}
			val newText = lines.joinToString("\n")
			runLaterReturn {
			  ta.text = newText
			  runLater { ta.end() }
			}
		  }

		  lineLimit.onChange { daemon { refresh() } }
		  infiniteLines.onChange { daemon { refresh() } }

		  var lastMod = 0L
		  every(1.sec, timer = AccurateTimer(), zeroDelayFirst = true) {
			if (weakRef.get() == null) cancel()
			val mod = lastModified()
			if (mod != lastMod) {
			  refresh()
			  lastMod = mod
			}
		  }
		}
	  }

	  is HTMLFile -> WebViewPane(this@createNodeInner).apply {
		specialZooming()
	  }

	  is SvgFile  -> WebViewWrapper().apply {
		runLater {
		  vgrow = ALWAYS
		  hgrow = ALWAYS

		  //		  this.scroll

		  specialZooming()

		  blendMode


		  /*  engine.loadWorker.stateProperty().addListener { _, _, newValue ->
			  if (newValue == State.RUNNING || newValue == State.SUCCEEDED) {
				engine.executeScript("document.body.style.overflow = 'hidden';")
			  }
			}*/

		  // hide webview scrollbars whenever they appear.
		  // hide webview scrollbars whenever they appear.
		  childrenUnmodifiable.addListener(ListChangeListener<Any?> {
			val deadSeaScrolls: Set<Node> = lookupAll(".scroll-bar")
			for (scroll in deadSeaScrolls) {
			  scroll.isVisible = false
			}
		  })


		  val cacheBreaker = "?${System.currentTimeMillis()}" /*omfg it works...*/


		  /*because i need that black background, and this is the only way i think*/
		  val svgHTML = createHTML().apply {
			html {
			  body {
				sty.background = black
				img {
				  src = "${toURI().toURL()}$cacheBreaker"
				  alt = "bad svg"
				}
			  }
			}
		  }.finalize()

		  engine.loadContent(svgHTML)

		  //		  thread {
		  //			while (true) {
		  //			  sleep(1000)
		  //			  runLater { engine.reload() }
		  //			}
		  //		  }

		  println("opening window")
		}

	  }.let { wv ->

		val weakRef = WeakReference(wv)

		/*areas around for right clicking!*/
		val root = VBoxWrapper<NodeWrapper>().apply {
		  mcontextmenu {
			"refresh" does {


			  /*wv.engine.document.normalizeDocument()*/ /*shot in the dark, but not supported yet*/

			  //			  wv.engine.


			  wv.engine.reload()
			}
		  }
		  //		  yellow()
		  //		  vgrow = ALWAYS
		  //		  hgrow = ALWAYS
		  hbox<NodeWrapper> { exactHeight = 10.0 }
		  hbox<NodeWrapper> {
			vgrow = ALWAYS
			hgrow = ALWAYS
			vbox<NodeWrapper> { exactWidth = 10.0 }
			add(wv)
			vbox<NodeWrapper> { exactWidth = 10.0 }
		  }
		  hbox<NodeWrapper> { exactHeight = 10.0 }
		}

		val svgText = this@createNodeInner.readText()
		val svgWidthPx = svgText.substringAfter("width=\"").substringBefore("px").toInt()
		val svgHeightPx = svgText.substringAfter("height=\"").substringBefore("px").toInt()

		runLater {
		  val widthRatio = wv.width/svgWidthPx
		  val heightRatio = wv.height/svgHeightPx
		  wv.zoom *= minOf(widthRatio, heightRatio)
		  wv.zoom *= 2 /*idk*/
		}


		runLater {
		  daemon {
			var mtime = lastModified()
			while (true) {
			  sleep(1000)
			  val wvv = weakRef.get() ?: break
			  val newmtime = lastModified()
			  if (newmtime != mtime) {
				mtime = newmtime
				runLater {
				  wvv.engine.reload()
				}
			  }
			}
		  }
		}
		root
	  }

	  else        -> err("how to make node for files with extension:${extension}")
	}
  } else return VBoxWrapper(TextWrapper("file $this does not exist"))
}





