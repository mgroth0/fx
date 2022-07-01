package matt.fx.node.file.tree

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableRow
import javafx.scene.control.TreeTableView
import javafx.scene.control.TreeView
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.layout.VBox
import matt.async.date.sec
import matt.auto.actions
import matt.auto.open
import matt.file.MFile
import matt.file.mFile
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
import matt.hurricanefx.eye.collect.bind
import matt.hurricanefx.eye.collect.sortBy
import matt.hurricanefx.eye.collect.toObservable
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.eye.prop.div
import matt.hurricanefx.tornadofx.item.column
import matt.hurricanefx.tornadofx.nodes.add
import matt.hurricanefx.tornadofx.nodes.clear
import matt.hurricanefx.tornadofx.nodes.populateTree
import matt.hurricanefx.tornadofx.nodes.selectedItem
import matt.hurricanefx.tornadofx.nodes.setOnDoubleClick
import matt.hurricanefx.tornadofx.tree.selectedValue
import matt.hurricanefx.wrapper.TreeLikeWrapper
import matt.hurricanefx.wrapper.TreeTableViewWrapper
import matt.hurricanefx.wrapper.TreeViewWrapper
import matt.kjlib.file.size
import matt.klib.lang.inList
import matt.klib.str.taball
import matt.stream.recurse.recurse
import matt.stream.sameContentsAnyOrder

private const val HEIGHT = 300.0

fun fileTreeAndViewerPane(
  rootFile: MFile, doubleClickInsteadOfSelect: Boolean = false
) = HBox().apply {
  val hBox = this
  alignment = Pos.CENTER_LEFT
  val treeTableView = fileTableTree(rootFile).apply {
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

fun Pane.fileTree(
  rootFile: MFile,
  strategy: FileTreePopulationStrategy = AUTOMATIC,
  op: (TreeView<MFile>.()->Unit)? = null,
): TreeView<MFile> = fileTree(rootFile.inList().toObservable(), strategy, op)

fun Pane.fileTableTree(
  rootFile: MFile,
  strategy: FileTreePopulationStrategy = AUTOMATIC,
  op: (TreeTableView<MFile>.()->Unit)? = null,
): TreeTableView<MFile> = fileTableTree(rootFile.inList().toObservable(), strategy, op)


fun Pane.fileTree(
  rootFiles: ObservableList<MFile>,
  strategy: FileTreePopulationStrategy = AUTOMATIC,
  op: (TreeView<MFile>.()->Unit)? = null,
): TreeView<MFile> {
  return TreeView<MFile>().apply {
	this@fileTree.add(this)

	setupGUI()
	TreeViewWrapper(this).setupContent(rootFiles, strategy)


	root.isExpanded = true
	if (op != null) op()
  }
}

fun Pane.fileTableTree(
  rootFiles: ObservableList<MFile>,
  strategy: FileTreePopulationStrategy = AUTOMATIC,
  op: (TreeTableView<MFile>.()->Unit)? = null,
): TreeTableView<MFile> {
  return TreeTableView<MFile>().apply {
	this@fileTableTree.add(this)

	setupGUI()
	TreeTableViewWrapper(this).setupContent(rootFiles, strategy)


	root.isExpanded = true
	autoResizeColumns()
	if (op != null) op()
  }
}


private fun TreeView<MFile>.setupGUI() {

  setCellFactory {
	object: TreeCell<MFile>() {
	  init {
		setOnDoubleClick {
		  this.treeItem?.value?.open()
		}
	  }

	  override fun updateItem(item: MFile?, empty: Boolean) {
		super.updateItem(item, empty)
		if (empty || item == null) {
		  this.text = null
		  graphic = null
		} else {
		  this.text = item.name
		  graphic = item.draggableIcon()
		}

	  }
	}
  }

  mcontextmenu {
	"open in new window" does {
	  selectedValue?.let {
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

	  selectedValue?.let { it.actions() + it.fxActions() }?.forEach {
		actionitem(it)
	  }
	}
  }

}


private fun TreeTableView<MFile>.setupGUI() {
  setRowFactory {
	TreeTableRow<MFile>().apply {
	  setOnDoubleClick {
		this.treeItem.value?.open()
	  }
	}
  }

  val nameCol = column("name", matt.file.MFile::abspath) {
	simpleCellFactory { value -> mFile(value).let { it.name to it.draggableIcon() } }
  }
  column("ext", matt.file.MFile::extension)


  val showSizesProp = SimpleBooleanProperty(false)
  showSizesProp.onChange { b ->
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

	checkitem("show sizes", showSizesProp)

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

private fun TreeLikeWrapper<*, MFile>.setupContent(
  rootFiles: ObservableList<MFile>,
  strategy: FileTreePopulationStrategy,
) {
  root = TreeItem()
  root.children.bind(rootFiles) {
	TreeItem(it)
  }
  isShowRoot = false
  setupPopulating(strategy)
}

private fun TreeLikeWrapper<*, MFile>.rePop() {
  root.children.forEach { child ->
	populateTree(child, { TreeItem(it) }) { item ->
	  item.value.childs()
	}
  }
  (node as? TreeTableView<*>)?.autoResizeColumns()
}

private fun TreeLikeWrapper<*, MFile>.setupPopulating(strategy: FileTreePopulationStrategy) {
  if (strategy == AUTOMATIC) {
	rePop()
	refreshWhileInSceneEvery(5.sec) {
	  if (root.children.flatMap { it.recurse { it.children } }.any {
		  !it.children.map { it.value }.sameContentsAnyOrder(it.value.listFiles()?.toList() ?: listOf())
		}) {
		rePop()
	  }
	}
  } else setOnSelectionChange { v ->
	if (v != null) {
	  v.refreshChilds()
	  v.children.forEach { it.refreshChilds() } /*so i can always see which have children*/
	}
  }
}

private fun TreeItem<MFile>.refreshChilds() {
  val childs = value.childs() ?: listOf()
  children.removeIf { it.value !in childs }
  childs.filter { it !in children.map { it.value } }.forEach {
	children.add(TreeItem(it))
  }
  children.sortWith(FILE_SORT_RULE_ITEMS)
}

private fun MFile.childs() = listFilesAsList()
  ?.sortedWith(FILE_SORT_RULE)?.apply {
	taball("children", this)
  }


private val FILE_SORT_RULE = compareBy<MFile> { !it.isDirectory }.then(compareBy { it.name })
private val FILE_SORT_RULE_ITEMS =
  compareBy<TreeItem<MFile>> { !it.value.isDirectory }.then(compareBy { it.value.name })

enum class FileTreePopulationStrategy {
  AUTOMATIC, EFFICIENT
}

