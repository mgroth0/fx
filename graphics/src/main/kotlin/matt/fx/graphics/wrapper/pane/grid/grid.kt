package matt.fx.graphics.wrapper.pane.grid

import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import matt.fx.base.wrapper.obs.collect.list.createMutableWrapper
import matt.fx.base.wrapper.obs.obsval.prop.NonNullFXBackedBindableProp
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.graphics.style.inset.MarginableConstraints
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl
import matt.fx.graphics.wrapper.pane.grid.GridPaneWrapper.GridDSLType.COL
import matt.fx.graphics.wrapper.pane.grid.GridPaneWrapper.GridDSLType.ROW
import matt.lang.assertions.require.requireNotEqual


fun <T : NodeWrapper> T.gridpaneConstraints(op: (GridPaneConstraint.() -> Unit)): T {
    val gpc = GridPaneConstraint(node)
    gpc.op()
    return gpc.applyToNode(this)
}

fun ET.grid(op: GridPaneWrapper<NW>.() -> Unit = {}) = gridpane<NW>(op)

fun <C : NodeWrapper> ET.gridpane(op: GridPaneWrapper<C>.() -> Unit = {}) = attach(GridPaneWrapper(), op)
open class GridPaneWrapper<C : NodeWrapper>(node: GridPane = GridPane()) : PaneWrapperImpl<GridPane, C>(node) {



    companion object {
        fun setConstraints(
            child: Node,
            columnIndex: Int,
            rowIndex: Int
        ) =
            GridPane.setConstraints(child, columnIndex, rowIndex)
    }

    val columnConstraints by lazy { node.columnConstraints.createMutableWrapper() }
    val rowConstraints by lazy { node.rowConstraints.createMutableWrapper() }

    enum class GridDSLType {
        ROW, COL
    }


    val gridLinesVisibleProp: NonNullFXBackedBindableProp<Boolean> by lazy {
        node.gridLinesVisibleProperty().toNonNullableProp()
    }
    var gridLinesVisible by gridLinesVisibleProp

    private var dslType: GridDSLType? = null

    private var globalRowIndex = 0
    fun row(op: GridPaneWrapper<C>.() -> Unit) {
        requireNotEqual(dslType, COL)
        dslType = ROW
        val newChildren = stupidChildAdderDSL(op)
        var localColIndex = 0
        newChildren.forEach {
            it.gridpaneConstraints {
                rowIndex = this@GridPaneWrapper.globalRowIndex
                columnIndex = localColIndex++
            }
        }
        while (columnConstraints.size < localColIndex) {
            columnConstraints += ColumnConstraints()
        }
        globalRowIndex++
        while (rowConstraints.size < globalRowIndex) {
            rowConstraints += RowConstraints()
        }
    }

    private var globalColIndex = 0
    fun column(op: GridPaneWrapper<C>.() -> Unit) {
        requireNotEqual(dslType, ROW)
        dslType = COL
        val newChildren = stupidChildAdderDSL(op)
        var localRowIndex = 0
        newChildren.forEach {
            it.gridpaneConstraints {
                columnIndex = this@GridPaneWrapper.globalColIndex
                rowIndex = localRowIndex++
            }
        }
        while (rowConstraints.size < localRowIndex) {
            rowConstraints += RowConstraints()
        }
        globalColIndex++
        while (columnConstraints.size < globalColIndex) {
            columnConstraints += ColumnConstraints()
        }
    }

    private fun stupidChildAdderDSL(op: GridPaneWrapper<C>.() -> Unit): List<NW> {
        val oldChildren = children.toList()
        @Suppress("UNUSED_EXPRESSION") op()
        return children.filter { it !in oldChildren }
    }

    val hgapProperty by lazy { node.hgapProperty().toNonNullableProp().cast<Double>() }
    var hGap by hgapProperty
    val vgapProperty by lazy { node.hgapProperty().toNonNullableProp().cast<Double>() }
    var vGap by vgapProperty
}


class GridPaneConstraint(
    node: Node,
    var columnIndex: Int? = null,
    var rowIndex: Int? = null,
    var hGrow: Priority? = null,
    var vGrow: Priority? = null,
    override var margin: Insets? = GridPane.getMargin(node),
    var fillHeight: Boolean? = null,
    var fillWidth: Boolean? = null,
    var hAlignment: HPos? = null,
    var vAlignment: VPos? = null,
    var columnSpan: Int? = null,
    var rowSpan: Int? = null

) : MarginableConstraints() {
    var vhGrow: Priority? = null
        set(value) {
            vGrow = value
            hGrow = value
            field = value
        }

    var fillHeightWidth: Boolean? = null
        set(value) {
            fillHeight = value
            fillWidth = value
            field = value
        }

    fun columnRowIndex(
        columnIndex: Int,
        rowIndex: Int
    ) {
        this.columnIndex = columnIndex
        this.rowIndex = rowIndex
    }

    fun fillHeightWidth(fill: Boolean) {
        fillHeight = fill
        fillWidth = fill
    }

    fun <T : NodeWrapper> applyToNode(node: T): T {
        columnIndex?.let { GridPane.setColumnIndex(node.node, it) }
        rowIndex?.let { GridPane.setRowIndex(node.node, it) }
        hGrow?.let { GridPane.setHgrow(node.node, it) }
        vGrow?.let { GridPane.setVgrow(node.node, it) }
        margin.let { GridPane.setMargin(node.node, it) }
        fillHeight?.let { GridPane.setFillHeight(node.node, it) }
        fillWidth?.let { GridPane.setFillWidth(node.node, it) }
        hAlignment?.let { GridPane.setHalignment(node.node, it) }
        vAlignment?.let { GridPane.setValignment(node.node, it) }
        columnSpan?.let { GridPane.setColumnSpan(node.node, it) }
        rowSpan?.let { GridPane.setRowSpan(node.node, it) }
        return node
    }
}
