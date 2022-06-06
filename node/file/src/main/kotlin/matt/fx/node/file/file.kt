package matt.fx.node.file

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.TextArea
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView
import javafx.scene.image.Image
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.web.WebView
import matt.async.daemon
import matt.async.date.sec
import matt.auto.CHROME
import matt.auto.Finder
import matt.auto.VIVALDI
import matt.auto.open
import matt.auto.openInIntelliJ
import matt.auto.openInSublime
import matt.fx.web.specialZooming
import matt.fx.graphics.clip.copyToClipboard
import matt.gui.core.context.mcontextmenu
import matt.gui.core.refresh.refreshWhileInSceneEvery
import matt.gui.draggableIcon
import matt.gui.fxlang.onSelect
import matt.gui.loop.runLaterReturn
import matt.gui.setview.autoResizeColumns
import matt.gui.setview.simpleCellFactory
import matt.gui.win.interact.doubleClickToOpenInWindow
import matt.gui.win.interact.openImageInWindow
import matt.gui.win.interact.openInNewWindow
import matt.gui.win.interact.safe
import matt.gui.win.stage.WMode.CLOSE
import matt.hurricanefx.bindFitTo
import matt.hurricanefx.exactHeight
import matt.hurricanefx.exactWidth
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.eye.prop.div
import matt.hurricanefx.tornadofx.async.runLater
import matt.hurricanefx.tornadofx.control.button
import matt.hurricanefx.tornadofx.control.imageview
import matt.hurricanefx.tornadofx.control.textarea
import matt.hurricanefx.tornadofx.item.column
import matt.hurricanefx.tornadofx.layout.hbox
import matt.hurricanefx.tornadofx.layout.vbox
import matt.hurricanefx.tornadofx.nodes.add
import matt.hurricanefx.tornadofx.nodes.clear
import matt.hurricanefx.tornadofx.nodes.hgrow
import matt.hurricanefx.tornadofx.nodes.onDoubleClick
import matt.hurricanefx.tornadofx.nodes.populate
import matt.hurricanefx.tornadofx.nodes.selectedItem
import matt.hurricanefx.tornadofx.nodes.vgrow
import matt.kjlib.file.recursiveChildren
import matt.kjlib.file.size
import matt.klib.file.MFile
import matt.klib.file.ext.abspath
import matt.klib.file.ext.isImage
import matt.klib.lang.err
import org.intellij.lang.annotations.Language
import java.lang.ref.WeakReference
import java.net.URL
import kotlin.concurrent.thread
import javafx.scene.layout.Priority.ALWAYS


private const val LINE_LIMIT = 1000


fun MFile.createNode(): Region {
  if (exists()) {
	println("opening file")
	return when (extension) {
	  "log" -> TextArea().also { ta ->
		ta.addEventFilter(KeyEvent.KEY_TYPED) { it.consume() }
		val weakRef = WeakReference(ta)
		Platform.runLater {
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
				  Platform.runLater {
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
	  "svg" -> WebView().apply {
		Platform.runLater {
		  vgrow = ALWAYS
		  hgrow = ALWAYS



		  specialZooming()

		  blendMode

		  val cacheBreaker = "?${System.currentTimeMillis()}" /*omfg it works...*/

		  @Language("html") /*because i need that black background, and this is the only way i think*/
		  val svgHTML = """
				  
				  <!DOCTYPE html>
				  <html>
				     <body style="background-color:black;">
				        <img src="${toURI().toURL()}$cacheBreaker" alt="bad svg">
				     </body>
				  </html>
				  
				  
				  
				""".trimIndent()

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
		val root = VBox().apply {
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

		val svgText = this@createNode.readText()
		val svgWidthPx = svgText.substringAfter("width=\"").substringBefore("px").toInt()
		val svgHeightPx = svgText.substringAfter("height=\"").substringBefore("px").toInt()

		runLater {
		  val widthRatio = wv.width/svgWidthPx
		  val heightRatio = wv.height/svgHeightPx
		  wv.zoom *= minOf(widthRatio, heightRatio)
		  wv.zoom *= 2 /*idk*/
		}


		Platform.runLater {
		  thread {
			var mtime = lastModified()
			while (true) {
			  Thread.sleep(1000)
			  val wvv = weakRef.get() ?: break
			  val newmtime = lastModified()
			  if (newmtime != mtime) {
				mtime = newmtime
				Platform.runLater {
				  wvv.engine.reload()
				}
			  }
			}
		  }
		}
		root
	  }
	  else  -> err("how to make node for files with extension:${extension}")
	}
  } else return VBox(Text("file $this does not exist"))
}


fun FileTreeAndViewerPane(
  rootFile: MFile,
  doubleClickInsteadOfSelect: Boolean = false
) = HBox().apply {
  val hbox = this

  alignment = Pos.CENTER_LEFT
  val HEIGHT = 300.0

  val treeTableView = filetree(rootFile).apply {
	prefHeightProperty().set(HEIGHT)
	maxWidthProperty().bind(hbox.widthProperty()/2)
	hgrow = Priority.ALWAYS
  }
  val viewbox = vbox {
	prefWidthProperty().bind(hbox.widthProperty()/2) /*less aggressive to solve these issues?*/
	prefHeightProperty().bind(hbox.heightProperty())
	hgrow = Priority.ALWAYS
  }

  if (doubleClickInsteadOfSelect) {
	treeTableView.onDoubleClick {
	  treeTableView.selectedItem?.let {
		viewbox.clear()
		viewbox.fillWithFileNode(it, renderHTMLAndSVG = true)
	  }
	}
  } else {
	treeTableView.onSelect { file ->
	  viewbox.clear()
	  if (file != null) {
		viewbox.fillWithFileNode(file)
	  }
	}
  }
}

fun Pane.fillWithFileNode(file: MFile, renderHTMLAndSVG: Boolean = false) {
  val viewbox = this
  if (file.isImage()) {
	imageview {
	  image = Image(file.toURI().toString())
	  isPreserveRatio = true
	  bindFitTo(viewbox)
	  isSmooth = true
	  mcontextmenu {
		item("os open") {
		  setOnAction {
			java.awt.Desktop.getDesktop().open(file)
		  }
		}
		item("test open in new window") { onAction = EventHandler { file.openImageInWindow() } }
	  }
	  doubleClickToOpenInWindow()
	}
  } else if (file.extension in listOf(
	  "txt",
	  "log",
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
	) || (!renderHTMLAndSVG && file.extension in (listOf("html", "svg")))
  ) {
	var fsText = file.readText()
	val ta = textarea {
	  text = fsText
	  prefHeightProperty().bind(viewbox.prefHeightProperty())
	  prefWidthProperty().bind(viewbox.prefWidthProperty())
	}
	viewbox.button("matt.gui.ser.save changes") {
	  isDisable = true
	  setOnAction {
		file.writeText(ta.text)
	  }
	  ta.textProperty().onChange {
		val fsTextCurrent = file.readText()
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
	if (file.extension in (listOf("html", "svg")) && !renderHTMLAndSVG) {
	  viewbox.button("render") {
		setOnAction {
		  WebViewPane(file).openInNewWindow(wMode = CLOSE)
		}
	  }
	}
  } else if (file.extension in (listOf("html", "svg")) && renderHTMLAndSVG) {
	viewbox.add(WebViewPane(file).apply {
	  specialZooming()
	  perfectBind(viewbox)
	  specialTransferingToWindowAndBack(viewbox)
	})
  }
  mcontextmenu {
	item(s = "", g = file.draggableIcon())
  }
}




fun Pane.filetree(
  rootFile: MFile,
  table: Boolean = true,
  op: (TreeTableView<MFile>.()->Unit)? = null,
): TreeTableView<MFile> {
  return object: TreeTableView<MFile>() {
	override fun resize(width: Double, height: Double) {
	  super.resize(width, height)
	  if (!table) {
		val header = lookup("TableHeaderRow") as Pane
		header.minHeight = 0.0
		header.prefHeight = 0.0
		header.maxHeight = 0.0
		header.isVisible = false
	  }
	}
  }.apply {
	this@filetree.add(this)
	/*column<MFile, String>("") {
	  *//*the first matt.hurricanefx.tableview.coolColumn is where the branch expander arrows go, regardless of if theres also other data*//*
	  SimpleStringProperty("")
	  *//*and if there is other data, its pretty ugly (at least it has been)*//*
	}*/


	val nameCol = column("name", matt.klib.file.MFile::abspath) {
	  simpleCellFactory { MFile(it).let { it.name to it.draggableIcon() } }
	}
	if (table) column("ext", matt.klib.file.MFile::extension)
	else nameCol.prefWidthProperty().bind(this.widthProperty())


	val showSizesProp = SimpleBooleanProperty(false)
	if (table) showSizesProp.onChange { b ->
	  if (b) {
		column<MFile, String>("size") {
		  SimpleStringProperty(it.value.value.size().formatted)
		}
		autoResizeColumns()
	  } else {
		columns.firstOrNull { col -> col.text == "size" }?.let { columns.remove(it) }
		autoResizeColumns()
	  }
	}

	mcontextmenu {

	  if (table) checkitem("show sizes", showSizesProp)

	  actionitem("open in new window") {
		selectedItem?.let {
		  VBox().apply {
			fillWithFileNode(
			  it,
			  renderHTMLAndSVG = true
			)
		  }.openInNewWindow(
			wMode = CLOSE
		  )
		}
	  }
	  actionitem("open in intelliJ") {
		selectedItem?.let { it.openInIntelliJ() }
	  }
	  actionitem("open in Sublime") {
		selectedItem?.let { it.openInSublime() }
	  }
	  actionitem("open in finder") {
		selectedItem?.let { Finder.open(it) }
	  }
	  actionitem("open in chrome") {
		selectedItem?.let { CHROME.open(it) }
	  }
	  actionitem("open in vivaldi") {
		selectedItem?.let { VIVALDI.open(it) }
	  }
	  actionitem("open with webd") {
		selectedItem?.let { URL(it.toURI().toURL().toString()).open() }
	  }
	  actionitem("copy full path") {
		selectedItem?.absolutePath?.copyToClipboard()
	  }
	  actionitem("copy as file") {
		selectedItem?.copyToClipboard()
	  }

	}

	sortOrder.setAll(nameCol) /*not working, but can click columns*/
	root = TreeItem(rootFile)
	isShowRoot = true

	populate {
	  it.value.listFiles()?.toList() ?: listOf()
	}
	var files: Set<MFile> = rootFile.recursiveChildren().toSet()
	refreshWhileInSceneEvery(5.sec) {
	  val tempfiles = rootFile.recursiveChildren().toSet()
	  if (!(tempfiles.containsAll(files.toSet()) && files.containsAll(tempfiles))) {
		files = rootFile.recursiveChildren().toSet()
		refresh()
		if (table) autoResizeColumns()
	  }
	}
	refresh()
	root.isExpanded = true
	if (table) autoResizeColumns()
	if (op != null) op()
  }
}

