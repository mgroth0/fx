package matt.fx.graphics.wrapper.pane.anchor.swapper

import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Region
import matt.fx.graphics.fxthread.ts.nonBlockingFXWatcher
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.ProxyEventTargetWrapper
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.obs.listen.MyListenerInter
import matt.obs.prop.ObsVal


fun <P, N: NodeWrapper> ET.swapper(
  prop: ObsVal<P>,
  nullMessage: String? = null,
  op: (P & Any).()->N
): Swapper<P, N> {
  val swapper = Swapper<P, N>()
  swapper.setupSwapping(prop, nullMessage = nullMessage, op)
  return attach(swapper)
}

fun <P> ET.swapperR(
  prop: ObsVal<P>,
  nullMessage: String? = null,
  op: (ET).(P & Any)->Unit
): Swapper<P, NW> {
  val swapper = Swapper<P, NW>()
  swapper.setupSwappingWithReceiver(prop, nullMessage = nullMessage, op)
  return attach(swapper)
}

fun <P> ET.swapperRNullable(
  prop: ObsVal<P>,
  op: (ET).(P)->Unit
): Swapper<P, NW> {
  val swapper = Swapper<P, NW>()
  swapper.setupSwappingWithReceiverNullable(prop, op)
  return attach(swapper)
}

open class Swapper<P, C: NodeWrapper>: RegionWrapperImpl<Region, C>(AnchorPane()) {

  private val anchor get() = this@Swapper.node as AnchorPane

  //  @Suppress("UNUSED_PARAMETER")
  //  private val anchorWrapper by lazy {
  //	object: AnchorPaneWrapper<C>, NodeWrapper by this@Swapper {
  //	  override val node get() = this@Swapper.node as AnchorPane
  //	  override fun add(nw: NodeWrapper) {
  //		addChild(nw, null)
  //	  }
  //
  //	  override fun addChild(child: NodeWrapper, index: Int?) {
  //
  //	/*	val debug = """
  //		  children
  //		  ${children.joinToString("") { "\n\t$it" }}
  //
  //		  node.children
  //		  ${node.children.joinToString("") { "\n\t$it" }}
  //		""".trimIndent()*/
  //		try {
  //		  if (index == null) {
  //			node.children.add(child.node)
  //		  } else {
  //			node.children.add(index, child.node)
  //		  }
  //		} catch (e: Exception) {
  //		  /*println(debug)*/
  //		  throw e
  //		}
  //	  }
  //
  //	  override val children: ObsList<C>
  //		get() = TODO("Not yet implemented")
  //
  //	  override var padding: Insets
  //		get() = TODO("Not yet implemented")
  //		set(value) {}
  //	  override val paddingProperty: Var<Insets>
  //		get() = TODO("Not yet implemented")
  //	  override val backgroundProperty: Var<Background?>
  //		get() = TODO("Not yet implemented")
  //	  override var background: Background?
  //		get() = TODO("Not yet implemented")
  //		set(value) {}
  //	  override val paddingVerticalProperty: Var<Double>
  //		get() = TODO("Not yet implemented")
  //	  override var paddingVertical: Double
  //		get() = TODO("Not yet implemented")
  //		set(value) {}
  //	}
  //  }

  private var fxWatcherProp: ObsVal<P>? = null
  private var listener: (MyListenerInter<*>)? = null

  @Synchronized
  fun setupSwappingWithReceiver(
	prop: ObsVal<P>,
	nullMessage: String? = null,
	op: (ET).(P & Any)->Unit
  ) {
	fxWatcherProp?.removeListener(listener!!)
	fxWatcherProp = prop.nonBlockingFXWatcher()

	fun refresh(value: P?) {
	  anchor.children.clear()
	  if (value == null) {
		if (nullMessage != null) {
		  setInnerNode(TextWrapper(nullMessage))
		} else {
		  clearLayoutProxyNetwork()
		}
	  } else {
		val proxy = ProxyEventTargetWrapper {
		  setInnerNode(it)
		}
		proxy.op(value)
	  }
	}
	listener = fxWatcherProp!!.onChange {
	  refresh(it)
	}
	refresh(fxWatcherProp!!.value)
  }

  @Synchronized
  fun setupSwappingWithReceiverNullable(
	prop: ObsVal<P>,
	op: (ET).(P)->Unit
  ) {
	fxWatcherProp?.removeListener(listener!!)
	fxWatcherProp = prop.nonBlockingFXWatcher()

	fun refresh(value: P) {
	  anchor.children.clear()
	  val proxy = ProxyEventTargetWrapper {
		setInnerNode(it)
	  }
	  proxy.op(value)
	}
	listener = fxWatcherProp!!.onChange {
	  refresh(it)
	}
	refresh(fxWatcherProp!!.value)
  }

  @Synchronized
  fun setupSwapping(
	prop: ObsVal<P>,
	nullMessage: String? = null,
	op: (P & Any).()->C
  ) {
	fxWatcherProp?.removeListener(listener!!)
	fxWatcherProp = prop.nonBlockingFXWatcher()
	fun refresh(value: P?) {
	  anchor.children.clear()
	  if (value == null) {
		if (nullMessage != null) {
		  setInnerNode(TextWrapper(nullMessage))
		} else {
		  clearLayoutProxyNetwork()
		}
	  } else {
		setInnerNode(op(value))
	  }
	}
	this.listener = fxWatcherProp!!.onChange {
	  refresh(it)
	}
	refresh(fxWatcherProp!!.value)
  }


  private fun setInnerNode(node: NodeWrapper) {
	anchor.children.removeAll { it != node.node }
	setAsLayoutProxyForAndProxiedFrom(node)
	if (node.node !in anchor.children) anchor.children.add(node.node)
	node.setAsTopAnchor(0.0)
	node.setAsBottomAnchor(0.0)
	node.setAsLeftAnchor(0.0)
	node.setAsRightAnchor(0.0)
  }

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}

