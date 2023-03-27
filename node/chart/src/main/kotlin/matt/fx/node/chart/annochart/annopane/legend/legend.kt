package matt.fx.node.chart.annochart.annopane.legend

import javafx.geometry.Insets
import javafx.scene.layout.Border
import javafx.scene.paint.Color
import matt.fx.control.wrapper.label.label
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.grid.GridPaneWrapper
import matt.obs.col.olist.MutableObsList

class MyLegend(
  val items: MutableObsList<LegendItem>
): GridPaneWrapper<NW>() {

  fun update() {
	clear()
	items.forEach {
	  row {
		+it.nodeGen()
		label(it.label) {
		  padding = Insets(5.0)
		}
	  }
	}
  }

  init {
	border = Border.stroke(Color.WHITE)
	padding = Insets(8.0)
	update()
	items.onChange {
	  update()
	}
  }

  class LegendItem(
	val nodeGen: ()->NodeWrapper,
	val label: String
  )
}