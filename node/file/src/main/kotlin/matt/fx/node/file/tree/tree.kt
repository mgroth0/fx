package matt.fx.node.file.tree

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.layout.VBox
import matt.async.date.sec
import matt.auto.actions
import matt.auto.open
import matt.auto.openInFinder
import matt.fx.fxauto.actionitem
import matt.fx.fxauto.fxActions
import matt.fx.graphics.layout.hgrow
import matt.fx.graphics.layout.perfectBind
import matt.fx.graphics.layout.vbox
import matt.fx.graphics.menu.context.mcontextmenu
import matt.fx.graphics.refreshWhileInSceneEvery
import matt.fx.graphics.win.interact.openInNewWindow
import matt.fx.graphics.win.stage.WMode.CLOSE
import matt.fx.node.file.createNode
import matt.fx.node.file.tree.FileTreePopulationStrategy.AUTOMATIC
import matt.fx.web.specialTransferingToWindowAndBack
import matt.gui.draggableIcon
import matt.gui.fxlang.onSelect
import matt.gui.setview.autoResizeColumns
import matt.gui.setview.simpleCellFactory
import matt.hurricanefx.TreeTableTreeView
import matt.hurricanefx.eye.collect.bind
import matt.hurricanefx.eye.collect.toObservable
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.eye.prop.div
import matt.hurricanefx.tornadofx.item.column
import matt.hurricanefx.tornadofx.nodes.add
import matt.hurricanefx.tornadofx.nodes.clear
import matt.hurricanefx.tornadofx.nodes.setOnDoubleClick
import matt.hurricanefx.tornadofx.nodes.populateTree
import matt.hurricanefx.tornadofx.nodes.selectedItem
import matt.kjlib.file.size
import matt.klib.file.MFile
import matt.klib.file.mFile
import matt.klib.lang.inList
import matt.stream.recurse.recurse
import matt.stream.sameContentsAnyOrder

private const val HEIGHT = 300.0

fun fileTreeAndViewerPane(
  rootFile: MFile, doubleClickInsteadOfSelect: Boolean = false
) = HBox().apply {
  val hBox = this
  alignment = Pos.CENTER_LEFT
  val treeTableView = filetree(rootFile).apply {
	prefHeightProperty().set(HEIGHT)
	maxWidthProperty().bind(hBox.widthProperty()/2)
	hgrow = ALWAYS
  }
  val viewBox = vbox {
	prefWidthProperty().bind(hBox.widthProperty()/2) /*less aggressive to solve these issues?*/
	prefHeightProperty().bind(hBox.heightProperty())
	hgrow = ALWAYS
  }

  if (doubleClickInsteadOfSelect) {
	treeTableView.setOnDoubleClick {
	  treeTableView.selectedItem?.let {
		viewBox.clear()
		viewBox.add(it.createNode(renderHTMLAndSVG = true).apply {
		  perfectBind(viewBox)
		  specialTransferingToWindowAndBack(viewBox)
		})
	  }
	}
  } else {
	treeTableView.onSelect { file ->
	  viewBox.clear()
	  if (file != null) {
		viewBox.add(file.createNode(renderHTMLAndSVG = true).apply {
		  perfectBind(viewBox)
		  specialTransferingToWindowAndBack(viewBox)
		})
	  }
	}
  }
}

fun Pane.filetree(
  rootFile: MFile,
  table: Boolean = true,
  strategy: FileTreePopulationStrategy = AUTOMATIC,
  op: (TreeTableView<MFile>.()->Unit)? = null,
): TreeTableView<MFile> = filetree(rootFile.inList().toObservable(), table, strategy, op)

fun Pane.filetree(
  rootFiles: ObservableList<MFile>,
  table: Boolean = true,
  strategy: FileTreePopulationStrategy = AUTOMATIC,
  op: (TreeTableView<MFile>.()->Unit)? = null,
): TreeTableView<MFile> {
  return TreeTableTreeView<MFile>(table = table).apply {
	this@filetree.add(this)

	setupGUI(table = table)
	setupContent(rootFiles, strategy, table)


	root.isExpanded = true
	if (table) autoResizeColumns()
	if (op != null) op()
  }
}

private fun TreeTableView<MFile>.setupGUI(table: Boolean) {

  setRowFactory {
	rowFactory.call(it).apply {
	  setOnDoubleClick {
		this.treeItem.value.open()
	  }
	}
  }

  val nameCol = column("name", matt.klib.file.MFile::abspath) {
	simpleCellFactory { value -> mFile(value).let { it.name to it.draggableIcon() } }
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

	"open in new window" does {
	  selectedItem?.let {
		VBox().apply {
		  val container = this
		  add(it.createNode(renderHTMLAndSVG = true).apply {
			perfectBind(container)
			specialTransferingToWindowAndBack(container)
		  })
		}.openInNewWindow(
		  wMode = CLOSE
		)
	  }
	}
	onRequest {
	  selectedItem?.let { it.actions() + it.fxActions() }?.forEach {
		actionitem(it)
	  }
	}
  }

  sortOrder.setAll(nameCol) /*not working, but can click columns*/
}

private fun TreeTableView<MFile>.setupContent(
  rootFiles: ObservableList<MFile>,
  strategy: FileTreePopulationStrategy,
  table: Boolean
) {


  root = TreeItem()
  root.children.bind(rootFiles) {
	TreeItem(it)
  }
  isShowRoot = false

  if (strategy == AUTOMATIC) {
	fun rePop() {
	  root.children.forEach {
		populateTree(it, { TreeItem(it) }) {
		  it.value.listFiles()?.toList() ?: listOf()
		}
	  }
	  if (table) autoResizeColumns()
	}
	rePop()

	refreshWhileInSceneEvery(5.sec) {
	  if (root.children.flatMap { it.recurse { it.children } }.any {
		  !it.children.map { it.value }.sameContentsAnyOrder(it.value.listFiles()?.toList() ?: listOf())
		}) {
		rePop()
	  }
	}
  } else {
	selectionModel.selectedItemProperty().onChange { v ->
	  v?.children?.setAll(v.value.listFiles()?.map { TreeItem(it) } ?: listOf())
	}
  }

}


enum class FileTreePopulationStrategy {
  AUTOMATIC, EFFICIENT
}

