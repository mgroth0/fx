package matt.fx.graphics.wrapper.pane.anchor.swapper

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Region
import matt.fx.graphics.fxthread.ts.nonBlockingFXWatcher
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.pane.anchor.AnchorPaneWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.obs.listen.MyListener
import matt.obs.prop.ObsVal
import matt.obs.prop.Var


fun <P, N: NodeWrapper> ET.swapper(
  prop: ObsVal<P>,
  nullMessage: String? = null,
  op: (P & Any).()->N
): Swapper<P, N> {
  val swapper = Swapper<P, N>()
  swapper.setupSwapping(prop, nullMessage = nullMessage, op)
  return attach(swapper)
}


open class Swapper<P, C: NodeWrapper>: RegionWrapperImpl<Region, C>(AnchorPane()) {


  private val anchorWrapper by lazy {
	object: AnchorPaneWrapper<C>, NodeWrapper by this@Swapper {
	  override val node get() = this@Swapper.node as AnchorPane
	  override fun add(nw: NodeWrapper) {
		addChild(nw, null)
	  }

	  override fun addChild(child: NodeWrapper, index: Int?) {
		val debug = """
		  children
		  ${children.joinToString("") { "\n\t$it" }}
		  
		  node.children
		  ${node.children.joinToString("") { "\n\t$it" }}
		""".trimIndent()
		try {
		  if (index == null) {
			node.children.add(child.node)
		  } else {
			node.children.add(index, child.node)
		  }
		} catch (e: Exception) {
		  println(debug)
		  throw e
		}
	  }

	  override var padding: Insets
		get() = TODO("Not yet implemented")
		set(value) {}
	  override val paddingProperty: Var<Insets>
		get() = TODO("Not yet implemented")
	  override val paddingVerticalProperty: Var<Double>
		get() = TODO("Not yet implemented")
	  override var paddingVertical: Double
		get() = TODO("Not yet implemented")
		set(value) {}
	}
  }

  private var prop: ObsVal<P>? = null
  private var listener: (MyListener<*>)? = null

  @Synchronized
  fun setupSwapping(
	prop: ObsVal<P>,
	nullMessage: String? = null,
	op: (P & Any).()->C
  ) {

	this.prop?.removeListener(listener!!)
	this.prop = prop.nonBlockingFXWatcher()


	fun refresh(value: P?) {
	  require(Platform.isFxApplicationThread()) /*DEBUG*/
	  anchorWrapper.clear()
	  if (value == null) {
		if (nullMessage != null) {
		  val node = TextWrapper(nullMessage)
		  setAsLayoutProxyForAndProxiedFrom(node)
		  anchorWrapper.allSides = node
		}
	  } else {
		val node = op(value)
		setAsLayoutProxyForAndProxiedFrom(node)
		anchorWrapper.allSides = node
	  }
	}


	this.listener = prop.onChange {
	  refresh(it)
	}
	refresh(prop.value)


  }

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}