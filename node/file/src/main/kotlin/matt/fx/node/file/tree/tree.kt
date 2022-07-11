package matt.fx.node.file.tree

import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.SelectionMode.MULTIPLE
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableRow
import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.layout.VBox
import matt.async.date.sec
import matt.auto.actions
import matt.auto.moveToTrash
import matt.auto.open
import matt.file.MFile
import matt.file.mFile
import matt.file.size
import matt.fx.fxauto.actionitem
import matt.fx.fxauto.fxActions
import matt.fx.graphics.icon.Icon
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
import matt.hurricanefx.eye.collect.toObservable
import matt.hurricanefx.eye.lang.BProp
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.eye.prop.div
import matt.hurricanefx.tornadofx.item.column
import matt.hurricanefx.tornadofx.nodes.add
import matt.hurricanefx.tornadofx.nodes.clear
import matt.hurricanefx.tornadofx.nodes.populateTree
import matt.hurricanefx.tornadofx.nodes.setOnDoubleClick
import matt.hurricanefx.wrapper.HBoxWrapper
import matt.hurricanefx.wrapper.PaneWrapper
import matt.hurricanefx.wrapper.TreeLikeWrapper
import matt.hurricanefx.wrapper.TreeTableViewWrapper
import matt.hurricanefx.wrapper.TreeViewWrapper
import matt.hurricanefx.wrapper.wrapped
import matt.klib.lang.inList
import matt.klib.todo
import matt.stream.recurse.chain
import matt.stream.recurse.recurse
import matt.stream.sameContentsAnyOrder

private const val HEIGHT = 300.0

fun fileTreeAndViewerPane(
  rootFile: MFile, doubleClickInsteadOfSelect: Boolean = false
) = HBoxWrapper {
  val hBox = this
  alignment = Pos.CENTER_LEFT
  val treeTableView = fileTableTree(rootFile).apply {
	prefHeightProperty.set(HEIGHT)
	maxWidthProperty.bind(hBox.widthProperty/2)
	hgrow = ALWAYS
  }
  val viewBox = vbox {
	prefWidthProperty.bind(hBox.widthProperty/2) /*less aggressive to solve these issues?*/
	prefHeightProperty.bind(hBox.heightProperty)
	hgrow = ALWAYS
  }

  if (doubleClickInsteadOfSelect) {
	treeTableView.setOnDoubleClick {
	  treeTableView.selectedItem?.let {
		viewBox.clear()
		viewBox.add(it.value.createNode(renderHTMLAndSVG = true).apply {
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

fun TreeLikeWrapper<*, MFile>.nav(f: MFile) {

  val fam = f.chain { it.parentFile }.toList()

  if (f.doesNotExist) return

  root.children.firstOrNull { it.value in fam }?.let { subRoot ->
	println("good to nav")
	var nextSubRoot = subRoot as FileTreeItem
	var keepGoing = true
	while (keepGoing) {
	  println("refreshing and checking ${nextSubRoot.value}")
	  nextSubRoot.refreshChilds()
	  nextSubRoot.children.forEach {
		(it as FileTreeItem).refreshChilds()
		todo("so I can see if they can expand... need a better way")
	  }
	  nextSubRoot.children.firstOrNull {
		it.value in fam
	  }?.let {
		println("${it}?")
		if (it.value == f) {
		  println("found treeitem: ${it}")
		  it.parent.chain { it.parent }.forEach { it.isExpanded = true }
		  selectionModel.select(it)
		  scrollTo(getRow(it))
		  keepGoing = false
		} else nextSubRoot = it as FileTreeItem
	  } ?: run {
		keepGoing = false
	  }
	}
  }


}

fun PaneWrapper.fileTree(
  rootFile: MFile,
  strategy: FileTreePopulationStrategy = AUTOMATIC,
  op: (TreeViewWrapper<MFile>.()->Unit)? = null,
): TreeViewWrapper<MFile> = fileTree(rootFile.inList().toObservable(), strategy, op)

fun PaneWrapper.fileTableTree(
  rootFile: MFile,
  strategy: FileTreePopulationStrategy = AUTOMATIC,
  op: (TreeTableViewWrapper<MFile>.()->Unit)? = null,
): TreeTableViewWrapper<MFile> = fileTableTree(rootFile.inList().toObservable(), strategy, op)


fun PaneWrapper.fileTree(
  rootFiles: ObservableList<MFile>,
  strategy: FileTreePopulationStrategy = AUTOMATIC,
  op: (TreeViewWrapper<MFile>.()->Unit)? = null,
): TreeViewWrapper<MFile> {
  return TreeViewWrapper<MFile>().apply {
	this@fileTree.add(this)

	setupGUI()
	setupContent(rootFiles, strategy)


	root.isExpanded = true
	if (op != null) op()
  }
}

fun PaneWrapper.fileTableTree(
  rootFiles: ObservableList<MFile>,
  strategy: FileTreePopulationStrategy = AUTOMATIC,
  op: (TreeTableViewWrapper<MFile>.()->Unit)? = null,
): TreeTableViewWrapper<MFile> {
  return TreeTableViewWrapper<MFile>().apply {
	this@fileTableTree.add(this)

	setupGUI()
	setupContent(rootFiles, strategy)


	root.isExpanded = true
	autoResizeColumns()
	if (op != null) op()
  }
}


private fun TreeLikeWrapper<*, MFile>.setupGUI() {

  selectionModel.selectionMode = MULTIPLE


  var showSizesProp: BProp? = null

  when (this) {
	is TreeViewWrapper<MFile>      -> {

	  node.setCellFactory {
		object: TreeCell<MFile>() {
		  init {
			setOnDoubleClick {
			  this.treeItem?.value?.open()
			}
		  }

		  override fun updateItem(item: MFile?, empty: Boolean) {
			val oldItem = this.item
			super.updateItem(item, empty)
			if (empty || item == null) {
			  text = null
			  graphic = null
			} else if (oldItem != item) {
			  text = item.name
			  graphic = item.draggableIcon()
			}
		  }
		}
	  }
	}

	is TreeTableViewWrapper<MFile> -> {
	  node.setRowFactory {
		TreeTableRow<MFile>().apply {
		  setOnDoubleClick {
			this.treeItem.value?.open()
		  }
		}
	  }
	  val nameCol = node.column("name", matt.file.MFile::abspath) {
		simpleCellFactory { value -> mFile(value).let { it.name to it.draggableIcon() } }
	  }
	  node.column("ext", matt.file.MFile::extension)

	  showSizesProp = BProp(false)
	  showSizesProp.onChange { b ->
		if (b) {
		  node.column<MFile, String>("size") {
			SimpleStringProperty(it.value.value.size().formatted.toString())
		  }
		  autoResizeColumns()
		} else {
		  node.columns.firstOrNull { col -> col.text == "size" }?.let { node.columns.remove(it) }
		  autoResizeColumns()
		}
	  }

	  node.sortOrder.setAll(nameCol) /*not working, but can click columns*/
	}

  }




  mcontextmenu {
	if (this@setupGUI is TreeTableViewWrapper<*>) {
	  checkitem("show sizes", showSizesProp!!)
	}




	onRequest {
	  val selects = selectionModel.selectedItems
	  if (selects.size == 1) {
		"open in new window" does {
		  selectedValue?.let {
			VBox().apply {
			  val container = this
			  add(it.createNode(renderHTMLAndSVG = true).apply {
				perfectBind(container.wrapped())
				specialTransferingToWindowAndBack(container.wrapped())
			  })
			}.openInNewWindow(
			  wMode = CLOSE
			)
		  }
		}
		selectedValue?.let { it.actions() + it.fxActions() }?.forEach { action ->
		  actionitem(action) {
			graphic = action.icon?.let { Icon(it) }
		  }
		}
	  } else if (selects.size > 1) {
		"move all to trash" does {
		  //		  confirm("delete all?") {
		  selects.forEach {
			it.value.moveToTrash()
		  }
		  //		  }
		}
	  }
	}
  }


}

private fun TreeLikeWrapper<*, MFile>.setupContent(
  rootFiles: ObservableList<MFile>,
  strategy: FileTreePopulationStrategy,
) {
  root = TreeItem()
  root.children.bind(rootFiles) {
	FileTreeItem(it)
  }
  isShowRoot = false
  setupPopulating(strategy)
}

private fun TreeLikeWrapper<*, MFile>.rePop() {
  root.children.forEach { child ->
	populateTree(child, { FileTreeItem(it) }) { item ->
	  item.value.childs()
	}
  }
  (this as? TreeTableViewWrapper<*>)?.autoResizeColumns()
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
  } else {
	root.children.forEach { (it as FileTreeItem).refreshChilds() }
	setOnSelectionChange { v ->
	  if (v != null) {
		(v as FileTreeItem).refreshChilds()
		v.children.forEach { (it as FileTreeItem).refreshChilds() } /*so i can always see which have children*/
	  }
	}
  }
}


private fun MFile.childs() = listFilesAsList()
  ?.sortedWith(FILE_SORT_RULE)


private val FILE_SORT_RULE = compareBy<MFile> { !it.isDirectory }.then(compareBy { it.name })
private val FILE_SORT_RULE_ITEMS =
  compareBy<TreeItem<MFile>> { !it.value.isDirectory }.then(compareBy { it.value.name })

enum class FileTreePopulationStrategy {
  AUTOMATIC, EFFICIENT
}

private class FileTreeItem(file: MFile): TreeItem<MFile>(file) {
  init {
	expandedProperty().addListener { _, o, n ->
	  if (o != n && !n) {
		(this as TreeItem<MFile>).recurse { it.children }.forEach { it.isExpanded = false }
	  }
	}
  }

  fun refreshChilds() {
	val childs = value.childs() ?: listOf()
	children.removeIf { it.value !in childs }
	childs.filter { it !in children.map { it.value } }.forEach {
	  children.add(FileTreeItem(it))
	}
	children.sortWith(FILE_SORT_RULE_ITEMS)
  }
}