package matt.fx.node.file

import javafx.application.Platform.runLater
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority.ALWAYS
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.img
import kotlinx.html.stream.createHTML
import matt.async.thread.daemon
import matt.css.Color.black
import matt.css.sty
import matt.file.MFile
import matt.hurricanefx.async.runLaterReturn
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
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.wrapper.control.text.area.TextAreaWrapper
import matt.hurricanefx.wrapper.pane.SimplePaneWrapper
import matt.hurricanefx.wrapper.pane.vbox.VBoxWrapper
import matt.hurricanefx.wrapper.region.RegionWrapper
import matt.hurricanefx.wrapper.text.TextWrapper
import matt.klib.lang.err
import java.lang.ref.WeakReference


private const val LINE_LIMIT = 1000

fun MFile.createNode(renderHTMLAndSVG: Boolean = false): RegionWrapper {
  val node = createNodeInner(renderHTMLAndSVG = renderHTMLAndSVG)
  node.mcontextmenu {
	item(s = "", g = draggableIcon())
  }
  return node
}

private fun MFile.createNodeInner(renderHTMLAndSVG: Boolean = false): RegionWrapper {
  if (exists()) {
	println("opening file")
	if (isImage()) {
	  return SimplePaneWrapper().apply {
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
	  val viewbox = SimplePaneWrapper()
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
		ta.textProperty().onChange {
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






	return when (extension) {
	  "log"  -> TextAreaWrapper().also { ta ->
		ta.addEventFilter(KeyEvent.KEY_TYPED) { it.consume() }
		val weakRef = WeakReference(ta)
		runLater {
		  daemon {
			var lastMod = 0L
			while (exists()) {
			  val taa = weakRef.get() ?: break
			  val mod = lastModified()
			  if (mod != lastMod) {
				var lines = readText().lines()
				if (lines.size > LINE_LIMIT) {
				  lines = lines.subList(lines.size - LINE_LIMIT, lines.size)
				}
				val newText = lines.joinToString("\n")
				runLaterReturn {
				  taa.text = newText
				  runLater {
					taa.end()
				  }
				}
				lastMod = mod
			  }
			  Thread.sleep(1000)
			}
		  }

		}
	  }

	  "html" -> WebViewPane(this@createNodeInner).apply {
		specialZooming()
	  }

	  "svg"  -> WebViewWrapper().apply {
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
			for (scroll in deadSeaScrolls) { scroll.isVisible = false }
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
		val root = VBoxWrapper().apply {
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
		  hbox { exactHeight = 10.0 }
		  hbox {
			vgrow = ALWAYS
			hgrow = ALWAYS
			vbox { exactWidth = 10.0 }
			add(wv)
			vbox { exactWidth = 10.0 }
		  }
		  hbox { exactHeight = 10.0 }
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
			  Thread.sleep(1000)
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

	  else   -> err("how to make node for files with extension:${extension}")
	}
  } else return VBoxWrapper(TextWrapper("file $this does not exist"))
}





