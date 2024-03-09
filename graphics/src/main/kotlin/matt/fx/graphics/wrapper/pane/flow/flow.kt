package matt.fx.graphics.wrapper.pane.flow

import javafx.geometry.HPos
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.layout.FlowPane
import matt.fx.base.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl

fun <C: NodeWrapper> ET.flowpane(op: FlowPaneWrapper<C>.() -> Unit = {}) = attach(FlowPaneWrapper(), op)

open class FlowPaneWrapper<C: NodeWrapper>(node: FlowPane = FlowPane()): PaneWrapperImpl<FlowPane, C>(node) {

    var orientation: Orientation
        get() = node.orientation
        set(value) {
            node.orientation = value
        }

    val orientationProperty by lazy { node.orientationProperty().toNonNullableProp() }

    var alignment: Pos
        get() = node.alignment
        set(value) {
            node.alignment = value
        }

    val alignmentProperty by lazy { node.alignmentProperty().toNonNullableProp() }

    var rowValignment: VPos
        get() = node.rowValignment
        set(value) {
            node.rowValignment = value
        }

    val rowValignmentProperty by lazy { node.rowValignmentProperty().toNonNullableProp() }

    var columnHalignment: HPos
        get() = node.columnHalignment
        set(value) {
            node.columnHalignment = value
        }

    val columnHalignmentProperty by lazy { node.columnHalignmentProperty().toNonNullableProp() }


    var hgap
        get() = node.hgap
        set(value) {
            node.hgap = value
        }

    val hgapProperty by lazy { node.hgapProperty().toNonNullableProp() }


    var vgap
        get() = node.vgap
        set(value) {
            node.vgap = value
        }

    val vgapProperty by lazy { node.vgapProperty().toNonNullableProp() }


    var prefWrapLength
        get() = node.prefWrapLength
        set(value) {
            node.prefWrapLength = value
        }

    val prefWrapLengthProperty by lazy { node.prefWrapLengthProperty().toNonNullableProp() }
}
