package matt.fx.control.control.nodedsl

import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.ToggleGroup
import javafx.scene.control.Tooltip
import matt.fx.control.tfx.control.bind
import matt.fx.control.tfx.nodes.SplitPaneConstraint
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.scroll.ScrollPaneWrapper
import matt.fx.graphics.tfx.nodes.GridPaneConstraint
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.node.parent.parent
import matt.fx.graphics.wrapper.region.RegionWrapper
import matt.stream.recurse.chain


fun NW.add(newToolTip: Tooltip) {
  if (this is ControlWrapperImpl<*>) node.tooltip = newToolTip else Tooltip.install(this.node, newToolTip)
}

fun NW.tooltip(text: String? = null, graphic: Node? = null, op: Tooltip.()->Unit = {}): Tooltip {
  val newToolTip = Tooltip(text)
  graphic?.apply { newToolTip.graphic = this }
  newToolTip.op()
  add(newToolTip)
  return newToolTip
}

fun NW.minYRelativeTo(ancestor: NodeWrapper): Double? { //  println("${this} minYRelative to ${ancestor}")
  var p: ParentWrapper<*>? = parent
  var y = boundsInParent.minY //  matt.prim.str.build.tab("y = ${y}")
  while (true) {
	when (p) {
	  null     -> {
		return null
	  }

	  ancestor -> {
		return y
	  }

	  else     -> {
		y += p.boundsInParent.minY
		p = p.parent
	  }
	}
  }
}

fun NW.maxYRelativeTo(ancestor: NodeWrapper): Double? {
  return minYRelativeTo(ancestor)?.plus(boundsInParent.height)
}


fun NW.isFullyVisibleIn(sp: ScrollPaneWrapper<*>): Boolean {
  require(sp.vmin == 0.0)
  require(sp.vmax == 1.0)
  if (this.parent!!.chain { it.parent }.none { it == sp }) return false
  if (!this.isVisible) return false
  if (!this.isManaged) return false
  val minY = this.minYRelativeTo(sp.content)
  val maxY =
	this.maxYRelativeTo(
	  sp.content
	) // /* println("vValueConverted=${sp.vValueConverted},vValueConvertedMax=${sp.vValueConvertedMax},minY=${minY},maxY=${maxY}")*/ /*,boundsInParent.height=${boundsInParent.height},boundsInLocal.height=${boundsInLocal.height},boundsInScene.height=${boundsInScene.height}*/
  require(minY != null && maxY != null)
  return minY >= sp.vValueConverted && maxY <= sp.vValueConvertedMax
}


fun <T: NodeWrapper> T.gridpaneConstraints(op: (GridPaneConstraint.()->Unit)): T {
  val gpc = GridPaneConstraint(this.node)
  gpc.op()
  return gpc.applyToNode(this)
}


fun NW.togglegroup(property: ObservableValue<Any>? = null, op: ToggleGroup.()->Unit = {}) =
  ToggleGroup().also { tg ->
	properties["matt.tornadofx.togglegroup"] = tg
	property?.let { tg.bind(it) }
	op(tg)
  }


inline fun <T: NodeWrapper> T.splitpaneConstraints(op: SplitPaneConstraint.()->Unit): T {
  val c = SplitPaneConstraint()
  c.op()
  return c.applyToNode(this)
}

infix fun RegionWrapper<*>.wrappedIn(sp: ScrollPaneWrapper<in NodeWrapper>): ScrollPaneWrapper<out NodeWrapper> {
  this minBind sp
  sp.backgroundProperty.bindBidirectional(backgroundProperty)
  return sp.apply {
	content = this@wrappedIn
  }
}


