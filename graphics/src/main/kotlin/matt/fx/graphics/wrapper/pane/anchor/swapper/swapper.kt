package matt.fx.graphics.wrapper.pane.anchor.swapper

import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Region
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.anchor.AnchorPaneWrapper
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.obs.listen.MyListener
import matt.obs.prop.ObsVal


open class Swapper<P, C: NodeWrapper>: RegionWrapperImpl<Region, C>(AnchorPane()) {


  private val anchorWrapper by lazy {
	object: AnchorPaneWrapper<C>, NodeWrapper by this@Swapper {
	  override val node get() = this@Swapper.node as AnchorPane
	  override fun add(nw: NodeWrapper) {
		addChild(nw, null)
	  }

	  override fun addChild(child: NodeWrapper, index: Int?) {
		if (index == null) {
		  node.children.add(child.node)
		} else {
		  node.children.add(index, child.node)
		}
	  }
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
	this.prop = prop

	fun refresh(value: P?) {
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