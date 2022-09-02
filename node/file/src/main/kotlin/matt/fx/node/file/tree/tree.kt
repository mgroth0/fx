package matt.fx.node.file.tree

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Pos.CENTER_LEFT
import javafx.scene.control.SelectionMode.MULTIPLE
import javafx.scene.control.TreeItem
import javafx.scene.layout.Priority.ALWAYS
import matt.async.date.sec
import matt.auto.actions
import matt.auto.moveToTrash
import matt.auto.open
import matt.file.MFile
import matt.file.construct.mFile
import matt.fx.fxauto.actionitem
import matt.fx.fxauto.fxActions
import matt.fx.graphics.icon.view
import matt.fx.graphics.menu.context.mcontextmenu
import matt.fx.graphics.refreshWhileInSceneEvery
import matt.fx.graphics.win.interact.WinGeom
import matt.fx.graphics.win.interact.openInNewWindow
import matt.fx.graphics.win.stage.WMode.CLOSE
import matt.fx.node.file.createNode
import matt.fx.node.file.tree.FileTreePopulationStrategy.AUTOMATIC
import matt.fx.web.specialTransferingToWindowAndBack
import matt.gui.draggableIcon
import matt.gui.setview.autoResizeColumns
import matt.hurricanefx.eye.collect.collectbind.bind
import matt.hurricanefx.eye.collect.toObservable
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.eye.prop.math.div
import matt.hurricanefx.wrapper.cellfact.SimpleFactory
import matt.hurricanefx.wrapper.control.row.TreeCellWrapper
import matt.hurricanefx.wrapper.control.row.TreeTableRowWrapper
import matt.hurricanefx.wrapper.control.tree.TreeViewWrapper
import matt.hurricanefx.wrapper.control.tree.like.TreeLikeWrapper
import matt.hurricanefx.wrapper.control.tree.like.populateTree
import matt.hurricanefx.wrapper.control.treetable.TreeTableViewWrapper
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.hurricanefx.wrapper.node.setOnDoubleClick
import matt.hurricanefx.wrapper.pane.PaneWrapperImpl
import matt.hurricanefx.wrapper.pane.hbox.HBoxWrapper
import matt.hurricanefx.wrapper.pane.vbox.VBoxWrapper
import matt.klib.lang.inList
import matt.klib.str.taball
import matt.klib.todo
import matt.stream.map.lazyMap
import matt.stream.recurse.chain
import matt.stream.recurse.recurse
import matt.stream.sameContentsAnyOrder

private const val HEIGHT = 300.0

fun fileTreeAndViewerPane(
  rootFile: MFile, doubleClickInsteadOfSelect: Boolean = false
) = HBoxWrapper<NodeWrapper>().apply {
  val hBox = this
  alignment = CENTER_LEFT
  val treeTableView = fileTableTree(rootFile).apply {
	prefHeightProperty.set(HEIGHT)
	maxWidthProperty.bind(hBox.widthProperty/2)
	hgrow = ALWAYS
  }
  val viewBox = vbox<NodeWrapper> {
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
		viewBox.add(file.value.createNode(renderHTMLAndSVG = true).apply {
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

fun PaneWrapperImpl<*, *>.fileTree(
  rootFile: MFile,
  strategy: FileTreePopulationStrategy = AUTOMATIC,
  op: (TreeViewWrapper<MFile>.()->Unit)? = null,
): TreeViewWrapper<MFile> = fileTree(rootFile.inList().toObservable(), strategy, op)

fun PaneWrapperImpl<*, *>.fileTableTree(
  rootFile: MFile,
  strategy: FileTreePopulationStrategy = AUTOMATIC,
  op: (TreeTableViewWrapper<MFile>.()->Unit)? = null,
): TreeTableViewWrapper<MFile> = fileTableTree(rootFile.inList().toObservable(), strategy, op)


fun PaneWrapperImpl<*, *>.fileTree(
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

fun PaneWrapperImpl<*, *>.fileTableTree(
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


  var showSizesProp: SimpleBooleanProperty? = null

  when (this) {
	is TreeViewWrapper<MFile>      -> {

	  node.setCellFactory {
		TreeCellWrapper<MFile>().apply {
		  setOnDoubleClick {
			this.treeItem?.value?.open()
		  }

		  val dragIconCache = lazyMap<MFile, NodeWrapper> {
			/*without this cache, performance suffers because i think new drag icons are being created constantly?*/
			it.draggableIcon()
		  }
		  updateItemOv = { item: MFile?, empty: Boolean ->
			if (empty || item == null) {
			  if (text.isNotBlank()) text = ""
			  if (graphic != null) graphic = null
			} else {
			  text = item.name
			  graphic = dragIconCache[item]
			}
		  }
		}.node
	  }
	}

	is TreeTableViewWrapper<MFile> -> {
	  node.setRowFactory {
		TreeTableRowWrapper<MFile>().apply {
		  setOnDoubleClick {
			this.treeItem?.value?.open()
		  }
		}.node
	  }
	  val nameCol = column("name", matt.file.MFile::abspath) {
		simpleCellFactory(SimpleFactory { value -> mFile(value).let { it.name to it.draggableIcon().node } })
	  }
	  column("ext", matt.file.MFile::extension)

	  showSizesProp = SimpleBooleanProperty(false)
	  showSizesProp.onChange { b ->
		if (b) {
		  column<String>("size") {
			SimpleStringProperty(it.value.value.size().formatted.toString())
		  }
		  autoResizeColumns()
		} else {
		  columns.firstOrNull { col -> col.text == "size" }?.let { node.columns.remove(it) }
		  autoResizeColumns()
		}
	  }

	  node.sortOrder.setAll(nameCol.node) /*not working, but can click columns*/
	}

  }




  mcontextmenu {
	if (this@setupGUI is TreeTableViewWrapper<*>) {
	  checkitem("show sizes", showSizesProp!!)
	}

	onRequest {
	  val selects = selectionModel.selectedItems
	  when (selects.size) {
		0    -> Unit
		1    -> {
		  "open in new window" does {
			selectedValue?.let {
			  VBoxWrapper<NodeWrapper>().apply {
				val container = this
				add(it.createNode(renderHTMLAndSVG = true).apply {
				  perfectBind(container)
				  specialTransferingToWindowAndBack(container)
				})
			  }.openInNewWindow(
				wMode = CLOSE,
				decorated = true,
				geom = WinGeom.Centered(bind = false),
				beforeShowing = {
				  title = it.name
				}
			  )
			}
		  }
		  selectedValue?.let { it.actions() + it.fxActions() }?.forEach { action ->
			actionitem(action) {
			  graphic = action.icon?.view()?.node
			}
		  }
		}

		else -> {
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
	  val cs = item.value.childs()
	  taball("cs of ${item.value}", cs)
	  cs
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
//	println("refreshing childs of ${value}")
	val childs = value.childs() ?: listOf()
//	taball("childs", childs)
	children.removeIf { it.value !in childs }
	childs.filter { it !in children.map { it.value } }.forEach {
	  children.add(FileTreeItem(it))
	}
	children.sortWith(FILE_SORT_RULE_ITEMS)
  }
}