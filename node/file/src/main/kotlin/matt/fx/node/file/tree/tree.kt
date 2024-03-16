package matt.fx.node.file.tree

import javafx.geometry.Pos.CENTER_LEFT
import javafx.scene.control.SelectionMode.MULTIPLE
import javafx.scene.layout.Priority.ALWAYS
import matt.auto.actions
import matt.auto.moveToTrash
import matt.auto.open
import matt.collect.map.lazyMap
import matt.file.construct.mFile
import matt.file.ext.singleExtension
import matt.file.toJioFile
import matt.fx.control.inter.graphic
import matt.fx.control.wrapper.control.row.TreeCellWrapper
import matt.fx.control.wrapper.control.row.TreeTableRowWrapper
import matt.fx.control.wrapper.control.tree.TreeViewWrapper
import matt.fx.control.wrapper.control.tree.like.TreeLikeWrapper
import matt.fx.control.wrapper.control.treetable.TreeTableViewWrapper
import matt.fx.control.wrapper.control.treetable.autoResizeColumns
import matt.fx.control.wrapper.treeitem.TreeItemWrapper
import matt.fx.fxauto.actionitem
import matt.fx.fxauto.fxActions
import matt.fx.graphics.icon.view
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
import matt.kjlib.socket.client.clients.InterAppServices
import matt.lang.common.NEVER
import matt.lang.common.inList
import matt.lang.common.scope
import matt.lang.common.unsafeErr
import matt.lang.model.file.FsFile
import matt.lang.model.file.MacFileSystem
import matt.obs.col.olist.BasicObservableListImpl
import matt.obs.col.olist.toBasicObservableList
import matt.obs.math.double.op.div
import matt.obs.prop.writable.BindableProperty
import matt.shell.common.context.shell.UnixDirectCommandsOnly
import matt.shell.commonj.context.ReapingShellExecutionContext
import matt.shell.commonj.context.UnknownShellExecutionContext

private const val HEIGHT = 300.0

context(ReapingShellExecutionContext, InterAppServices)
fun fileTreeAndViewerPane(
    rootFile: FsFile,
    doubleClickInsteadOfSelect: Boolean = false
) = HBoxWrapperImpl<NodeWrapper>(childClass = NodeWrapper::class).apply {
    val hBox = this
    alignment = CENTER_LEFT
    val treeTableView =
        fileTableTree(rootFile).apply {
            prefHeightProperty.value = HEIGHT
            maxWidthProperty.bind(hBox.widthProperty / 2.0)
            hgrow = ALWAYS
        }
    val viewBox =
        vbox<NodeWrapper> {
            prefWidthProperty.bind(hBox.widthProperty / 2.0) /*less aggressive to solve these issues?*/
            prefHeightProperty.bind(hBox.heightProperty)
            hgrow = ALWAYS
        }

    if (doubleClickInsteadOfSelect) {
        treeTableView.setOnDoubleClick {
            treeTableView.selectedItem?.let {
                viewBox.clear()
                viewBox.add(
                    it.value.toJioFile().createNode(renderHTMLAndSVG = true).apply {
                        perfectBind(viewBox)
                        specialTransferingToWindowAndBack(viewBox)
                    }
                )
            }
        }
    } else {
        treeTableView.onSelect { file ->
            viewBox.clear()
            if (file != null) {
                viewBox.add(
                    file.value.toJioFile().createNode(renderHTMLAndSVG = true).apply {
                        perfectBind(viewBox)
                        specialTransferingToWindowAndBack(viewBox)
                    }
                )
            }
        }
    }
}

context(ReapingShellExecutionContext, InterAppServices)
fun PaneWrapperImpl<*, *>.fileTree(
    rootFile: FsFile,
    strategy: FileTreePopulationStrategy = AUTOMATIC,
    op: (TreeViewWrapper<FsFile>.() -> Unit)? = null
): TreeViewWrapper<out FsFile> = fileTree(rootFile.inList().toBasicObservableList(), strategy, op)

context(ReapingShellExecutionContext, InterAppServices)
fun PaneWrapperImpl<*, *>.fileTableTree(
    rootFile: FsFile,
    strategy: FileTreePopulationStrategy = AUTOMATIC,
    op: (TreeTableViewWrapper<FsFile>.() -> Unit)? = null
): TreeTableViewWrapper<FsFile> = fileTableTree(rootFile.inList().toBasicObservableList(), strategy, op)


context(ReapingShellExecutionContext, InterAppServices)
fun PaneWrapperImpl<*, *>.fileTree(
    rootFiles: BasicObservableListImpl<out FsFile>,
    strategy: FileTreePopulationStrategy = AUTOMATIC,
    op: (TreeViewWrapper<FsFile>.() -> Unit)? = null
): TreeViewWrapper<out FsFile> =
    TreeViewWrapper<FsFile>().apply {
        this@fileTree.add(this)

        UnknownShellExecutionContext(UnixDirectCommandsOnly).scope {
            setupGUI()
        }

        unsafeErr("FX IS DEAD: here content was set up. Basically, it added file tree items and set it up to automatically repopulate in the acbkground")
        /*setupContent(rootFiles, strategy)*/


        root!!.isExpanded = true
        if (op != null) op()
    }

context(ReapingShellExecutionContext, InterAppServices)
fun PaneWrapperImpl<*, *>.fileTableTree(
    rootFiles: BasicObservableListImpl<FsFile>,
    strategy: FileTreePopulationStrategy = AUTOMATIC,
    op: (TreeTableViewWrapper<FsFile>.() -> Unit)? = null
): TreeTableViewWrapper<FsFile> =
    TreeTableViewWrapper<FsFile>().apply {
        this@fileTableTree.add(this)

        UnknownShellExecutionContext(UnixDirectCommandsOnly).scope {
            setupGUI()
        }
        unsafeErr("FX IS DEAD: here content was set up. Basically, it added file tree items and set it up to automatically repopulate in the acbkground")
        /*setupContent(rootFiles, strategy)*/


        root!!.isExpanded = true
        autoResizeColumns()
        if (op != null) op()
    }


context(ReapingShellExecutionContext, InterAppServices)
private fun TreeLikeWrapper<*, FsFile>.setupGUI() {

    selectionModel.selectionMode = MULTIPLE


    var showSizesProp: BindableProperty<Boolean>? = null

    when (this) {
        is TreeViewWrapper<FsFile>      -> {

            node.setCellFactory {
                TreeCellWrapper<FsFile>().apply {
                    setOnDoubleClick {
                        treeItem?.value?.toJioFile()?.open()
                    }

                    val dragIconCache =
                        lazyMap<FsFile, NodeWrapper> {
                            /*without this cache, performance suffers because i think new drag icons are being created constantly?*/
                            it.toJioFile().draggableIcon()
                        }
                    updateItemOv = { item: FsFile?, empty: Boolean ->
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

        is TreeTableViewWrapper<FsFile> -> {
            node.setRowFactory {
                TreeTableRowWrapper<FsFile>().apply {
                    setOnDoubleClick {
                        treeItem?.value?.toJioFile()?.open()
                    }
                }.node
            }
            val nameCol =
                column("name", FsFile::abspath) {
                    simpleCellFactory { value ->
                        mFile(
                            value,
                            MacFileSystem
                        ).let { it.name to it.toJioFile().draggableIcon() }
                    }
                }
            column("ext", FsFile::singleExtension)

            showSizesProp = BindableProperty(false)
            showSizesProp.onChange { b ->
                if (b) {
                    column("size") {
                        BindableProperty(it.value.value.toJioFile().size().formattedBinary.toString())
                    }
                    autoResizeColumns()
                } else {
                    columns.firstOrNull { col -> col.text == "size" }?.let { node.columns.remove(it) }
                    autoResizeColumns()
                }
            }

            node.sortOrder.setAll(nameCol.node) /*not working, but can click columns*/
        }

        else                            -> NEVER
    }




    mcontextmenu {
        if (this@setupGUI is TreeTableViewWrapper<*>) {
            checkitem("show sizes", showSizesProp!!)
        }

        onRequest {
            val selects = this@setupGUI.selectionModel.selectedItems
            when (selects.size) {
                0    -> Unit
                1    -> {
                    "open in new window" does {
                        this@setupGUI.selectedValue?.let {
                            VBoxWrapperImpl<NodeWrapper>().apply {
                                val container = this
                                add(
                                    it.toJioFile().createNode(renderHTMLAndSVG = true).apply {
                                        perfectBind(container)
                                        specialTransferingToWindowAndBack(container)
                                    }
                                )
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
                    this@setupGUI.selectedValue?.let { it.toJioFile().actions() + it.toJioFile().fxActions() }
                        ?.forEach { action ->
                            actionitem(action) {
                                graphic = action.icon?.view()?.node
                            }
                        }
                }

                else -> {
                    "move all to trash" does {
                        selects.forEach {
                            it.value.toJioFile().moveToTrash()
                        }
                    }
                }
            }
        }
    }
}






private fun matt.file.JioFile.childs() =
    listFilesAsList()
        ?.sortedWith(FILE_SORT_RULE)


private val FILE_SORT_RULE = compareBy<FsFile> { !it.toJioFile().isDirectory }.then(compareBy { it.name })
private val FILE_SORT_RULE_ITEMS =
    compareBy<TreeItemWrapper<FsFile>> { !it.value.toJioFile().isDirectory }.then(compareBy { it.value.name })

enum class FileTreePopulationStrategy {
    AUTOMATIC, EFFICIENT
}
