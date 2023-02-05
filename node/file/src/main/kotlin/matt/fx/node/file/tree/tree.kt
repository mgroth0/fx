package matt.fx.node.file.tree

import javafx.geometry.Pos.CENTER_LEFT
import javafx.scene.control.SelectionMode.MULTIPLE
import javafx.scene.layout.Priority.ALWAYS
import matt.auto.actions
import matt.auto.moveToTrash
import matt.auto.open
import matt.collect.itr.recurse.chain
import matt.collect.itr.recurse.recurse
import matt.collect.itr.sameContentsAnyOrder
import matt.collect.map.lazyMap
import matt.file.MFile
import matt.file.construct.mFile
import matt.fx.control.inter.graphic
import matt.fx.control.wrapper.cellfact.SimpleFactory
import matt.fx.control.wrapper.control.row.TreeCellWrapper
import matt.fx.control.wrapper.control.row.TreeTableRowWrapper
import matt.fx.control.wrapper.control.tree.TreeViewWrapper
import matt.fx.control.wrapper.control.tree.like.TreeLikeWrapper
import matt.fx.control.wrapper.control.tree.like.populateTree
import matt.fx.control.wrapper.control.treetable.TreeTableViewWrapper
import matt.fx.control.wrapper.control.treetable.autoResizeColumns
import matt.fx.control.wrapper.treeitem.TreeItemWrapper
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.fxauto.actionitem
import matt.fx.fxauto.fxActions
import matt.fx.graphics.icon.view
import matt.fx.graphics.refresh.refreshWhileInSceneEvery
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.setOnDoubleClick
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.hbox.HBoxWrapperImpl
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapperImpl
import matt.fx.graphics.wrapper.pane.vbox.vbox
import matt.fx.node.file.createNode
import matt.fx.node.file.draggableIcon
import matt.fx.node.file.tree.FileTreePopulationStrategy.AUTOMATIC
import matt.fx.web.specialTransferingToWindowAndBack
import matt.gui.interact.WinGeom
import matt.gui.interact.openInNewWindow
import matt.gui.menu.context.mcontextmenu
import matt.gui.mstage.WMode.CLOSE
import matt.lang.NEVER
import matt.lang.inList
import matt.log.taball
import matt.log.todo.todo
import matt.obs.col.olist.BasicObservableListImpl
import matt.obs.col.olist.toBasicObservableList
import matt.obs.listen.OldAndNewListenerImpl
import matt.obs.math.double.op.div
import matt.obs.prop.BindableProperty
import matt.time.dur.sec

private const val HEIGHT = 300.0

fun fileTreeAndViewerPane(
  rootFile: MFile, doubleClickInsteadOfSelect: Boolean = false
) = HBoxWrapperImpl<NodeWrapper>().apply {
  val hBox = this
  alignment = CENTER_LEFT
  val treeTableView = fileTableTree(rootFile).apply {
	prefHeightProperty.value = HEIGHT
	maxWidthProperty.bind(hBox.widthProperty/2.0)
	hgrow = ALWAYS
  }
  val viewBox = vbox<NodeWrapper> {
	prefWidthProperty.bind(hBox.widthProperty/2.0) /*less aggressive to solve these issues?*/
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

  val roo = root ?: return

  val fam = f.chain { it.parentFile }.toList()

  if (f.doesNotExist) return

  roo.children.firstOrNull { it.value in fam }?.let { subRoot ->
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
		  println("found treeitem: $it")
		  it.parent?.chain { it.parent }?.forEach { it.isExpanded = true }
		  selectionModel.select(it.node)
		  scrollTo(getRow(it.node))
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
): TreeViewWrapper<MFile> = fileTree(rootFile.inList().toBasicObservableList(), strategy, op)

fun PaneWrapperImpl<*, *>.fileTableTree(
  rootFile: MFile,
  strategy: FileTreePopulationStrategy = AUTOMATIC,
  op: (TreeTableViewWrapper<MFile>.()->Unit)? = null,
): TreeTableViewWrapper<MFile> = fileTableTree(rootFile.inList().toBasicObservableList(), strategy, op)


fun PaneWrapperImpl<*, *>.fileTree(
  rootFiles: BasicObservableListImpl<MFile>,
  strategy: FileTreePopulationStrategy = AUTOMATIC,
  op: (TreeViewWrapper<MFile>.()->Unit)? = null,
): TreeViewWrapper<MFile> {
  return TreeViewWrapper<MFile>().apply {
	this@fileTree.add(this)

	setupGUI()
	setupContent(rootFiles, strategy)


	root!!.isExpanded = true
	if (op != null) op()
  }
}

fun PaneWrapperImpl<*, *>.fileTableTree(
  rootFiles: BasicObservableListImpl<MFile>,
  strategy: FileTreePopulationStrategy = AUTOMATIC,
  op: (TreeTableViewWrapper<MFile>.()->Unit)? = null,
): TreeTableViewWrapper<MFile> {
  return TreeTableViewWrapper<MFile>().apply {
	this@fileTableTree.add(this)

	setupGUI()
	setupContent(rootFiles, strategy)


	root!!.isExpanded = true
	autoResizeColumns()
	if (op != null) op()
  }
}


private fun TreeLikeWrapper<*, MFile>.setupGUI() {

  selectionModel.selectionMode = MULTIPLE


  var showSizesProp: BindableProperty<Boolean>? = null

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
	  val nameCol = column("name", MFile::abspath) {
		simpleCellFactory(SimpleFactory { value -> mFile(value).let { it.name to it.draggableIcon() } })
	  }
	  column("ext", MFile::extension)

	  showSizesProp = BindableProperty(false)
	  showSizesProp.onChange { b ->
		if (b) {
		  column<String>("size") {
			BindableProperty(it.value.value.size().formatted.toString())
		  }
		  autoResizeColumns()
		} else {
		  columns.firstOrNull { col -> col.text == "size" }?.let { node.columns.remove(it) }
		  autoResizeColumns()
		}
	  }

	  node.sortOrder.setAll(nameCol.node) /*not working, but can click columns*/
	}

	else                           -> NEVER
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
			  VBoxWrapperImpl<NodeWrapper>().apply {
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
  rootFiles: BasicObservableListImpl<MFile>,
  strategy: FileTreePopulationStrategy,
) {
  root = TreeItemWrapper<MFile>()
  root!!.children.bind(rootFiles) {
	(FileTreeItem(it))
  }
  isShowRoot = false
  setupPopulating(strategy)
}

private fun TreeLikeWrapper<*, MFile>.rePop() {
  root!!.children.forEach { child ->
	populateTree(child, {
	  FileTreeItem(it)
	}) { item ->
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
	  if (root!!.children.flatMap { it.recurse { it.children } }.any {
		  !it.children.map { it.value }.sameContentsAnyOrder(it.value.listFiles()?.toList() ?: listOf())
		}) {
		rePop()
	  }
	}
  } else {
	root!!.children.forEach { (it as FileTreeItem).refreshChilds() }
	setOnSelectionChange { v ->
	  if (v != null) {
		(v.wrapped() as FileTreeItem).refreshChilds()
		v.children.forEach { (it.wrapped() as FileTreeItem).refreshChilds() } /*so i can always see which have matt.fx.control.layout.children*/
	  }
	}
  }
}


private fun MFile.childs() = listFilesAsList()
  ?.sortedWith(FILE_SORT_RULE)


private val FILE_SORT_RULE = compareBy<MFile> { !it.isDirectory }.then(compareBy { it.name })
private val FILE_SORT_RULE_ITEMS =
  compareBy<TreeItemWrapper<MFile>> { !it.value.isDirectory }.then(compareBy { it.value.name })

enum class FileTreePopulationStrategy {
  AUTOMATIC, EFFICIENT
}

private class FileTreeItem(file: MFile): TreeItemWrapper<MFile>(file) {

  init {
	expandedProperty.addListener(OldAndNewListenerImpl { o, n ->
	  if (o != n && !n) {
		(this@FileTreeItem as TreeItemWrapper<MFile>).recurse { it.children }.forEach { it.isExpanded = false }
	  }
	})
  }

  fun refreshChilds() {
	//	println("refreshing childs of ${value}")
	val childs = value.childs() ?: listOf()
	//	taball("childs", childs)
	children.removeAll { it.value !in childs }
	childs.filter { it !in children.map { it.value } }.forEach {
	  children.add(FileTreeItem(it))
	}
	children.sortWith(FILE_SORT_RULE_ITEMS)
  }
}