package matt.fx.graphics.wrapper.pane.flow

import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.geometry.HPos
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.layout.FlowPane
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.graphics.wrapper.pane.PaneWrapperImpl

open class FlowPaneWrapper<C: NodeWrapper>(node: FlowPane = FlowPane()): PaneWrapperImpl<FlowPane, C>(node) {

  var orientation: Orientation
	get() = node.orientation
	set(value) {
	  node.orientation = value
	}

  fun orientationProperty(): ObjectProperty<Orientation> = node.orientationProperty()

  var alignment: Pos
	get() = node.alignment
	set(value) {
	  node.alignment = value
	}

  fun alignmentProperty(): ObjectProperty<Pos> = node.alignmentProperty()

  var rowValignment: VPos
	get() = node.rowValignment
	set(value) {
	  node.rowValignment = value
	}

  fun rowValignmentProperty(): ObjectProperty<VPos> = node.rowValignmentProperty()

  var columnHalignment: HPos
	get() = node.columnHalignment
	set(value) {
	  node.columnHalignment = value
	}

  fun columnHalignmentProperty(): ObjectProperty<HPos> = node.columnHalignmentProperty()


  var hgap
	get() = node.hgap
	set(value) {
	  node.hgap = value
	}

  fun hgapProperty(): DoubleProperty = node.hgapProperty()


  var vgap
	get() = node.vgap
	set(value) {
	  node.vgap = value
	}

  fun vgapProperty(): DoubleProperty = node.vgapProperty()


  var prefWrapLength
	get() = node.prefWrapLength
	set(value) {
	  node.prefWrapLength = value
	}

  val prefWrapLengthProperty by lazy { node.prefWrapLengthProperty().toNonNullableProp() }


}