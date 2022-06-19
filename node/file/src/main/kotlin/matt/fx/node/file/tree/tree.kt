package matt.fx.node.file.tree

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.layout.VBox
import matt.async.date.sec
import matt.auto.CHROME
import matt.auto.Finder
import matt.auto.VIVALDI
import matt.auto.open
import matt.auto.openInIntelliJ
import matt.auto.openInSublime
import matt.fx.graphics.clip.copyToClipboard
import matt.fx.graphics.layout.hgrow
import matt.fx.graphics.layout.vbox
import matt.fx.graphics.menu.context.mcontextmenu
import matt.fx.graphics.refreshWhileInSceneEvery
import matt.fx.graphics.win.interact.openInNewWindow
import matt.fx.graphics.win.stage.WMode.CLOSE
import matt.fx.node.file.fillWithFileNode
import matt.gui.draggableIcon
import matt.gui.fxlang.onSelect
import matt.gui.setview.autoResizeColumns
import matt.gui.setview.simpleCellFactory
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.eye.prop.div
import matt.hurricanefx.tornadofx.item.column
import matt.hurricanefx.tornadofx.nodes.add
import matt.hurricanefx.tornadofx.nodes.clear
import matt.hurricanefx.tornadofx.nodes.onDoubleClick
import matt.hurricanefx.tornadofx.nodes.populate
import matt.hurricanefx.tornadofx.nodes.selectedItem
import matt.kjlib.file.recursiveChildren
import matt.kjlib.file.size
import matt.klib.file.MFile
import matt.klib.file.ext.abspath
import java.net.URL

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
	hgrow = ALWAYS
  }
  val viewbox = vbox {
	prefWidthProperty().bind(hbox.widthProperty()/2) /*less aggressive to solve these issues?*/
	prefHeightProperty().bind(hbox.heightProperty())
	hgrow = ALWAYS
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

