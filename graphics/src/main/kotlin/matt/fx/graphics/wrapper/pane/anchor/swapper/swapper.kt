package matt.fx.graphics.wrapper.pane.anchor.swapper

import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Region
import matt.fx.graphics.anim.animation.fade
import matt.fx.graphics.fxthread.ts.nonBlockingFXWatcher
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.ProxyEventTargetWrapper
import matt.fx.graphics.wrapper.node.NW
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.fx.graphics.wrapper.text.TextWrapper
import matt.hurricanefx.eye.time.toFXDuration
import matt.obs.listen.MyListenerInter
import matt.obs.prop.BindableProperty
import matt.obs.prop.ObsVal
import kotlin.time.Duration

fun ET.swap(nodeProp: BindableProperty<out NW?>) = swapper(
  prop = nodeProp
) {
  this
}

fun <P: Any, N: NodeWrapper> ET.swapperNeverNull(
  prop: ObsVal<P>,
  fadeOutDur: Duration? = null,
  fadeInDur: Duration? = null,
  op: (P).()->N,

  ): Swapper<P, N> {
  val swapper = Swapper<P, N>()
  swapper.setupSwapping(prop, fadeOutDur = fadeOutDur, fadeInDur = fadeInDur, op = op)
  return attach(swapper)
}

fun <P, N: NodeWrapper> ET.swapper(
  prop: ObsVal<P>,
  nullMessage: String? = null,
  fadeOutDur: Duration? = null,
  fadeInDur: Duration? = null,
  op: (P & Any).()->N,

  ): Swapper<P, N> {
  val swapper = Swapper<P, N>()
  swapper.setupSwapping(prop, nullMessage = nullMessage, fadeOutDur = fadeOutDur, fadeInDur = fadeInDur, op)
  return attach(swapper)
}

fun <P> ET.swapperR(
  prop: ObsVal<P>,
  nullMessage: String? = null,
  fadeOutDur: Duration? = null,
  fadeInDur: Duration? = null,
  op: (ET).(P & Any)->Unit,
): Swapper<P, NW> {
  val swapper = Swapper<P, NW>()
  swapper.setupSwappingWithReceiver(prop, nullMessage = nullMessage, fadeOutDur = fadeOutDur, fadeInDur = fadeInDur, op)
  return attach(swapper)
}

fun <P> ET.swapperRNullable(
  prop: ObsVal<P>,
  fadeOutDur: Duration? = null,
  fadeInDur: Duration? = null,
  op: (ET).(P)->Unit,
): Swapper<P, NW> {
  val swapper = Swapper<P, NW>()
  swapper.setupSwappingWithReceiverNullable(prop, fadeOutDur = fadeOutDur, fadeInDur = fadeInDur, op)
  return attach(swapper)
}

open class Swapper<P, C: NodeWrapper>: RegionWrapperImpl<Region, C>(AnchorPane()) {

  private val anchor get() = this@Swapper.node as AnchorPane


  private var fxWatcherProp: ObsVal<P>? = null
  private var listener: (MyListenerInter<*>)? = null

  @Synchronized
  fun setupSwappingWithReceiver(
	prop: ObsVal<P>,
	nullMessage: String? = null,
	fadeOutDur: Duration? = null,
	fadeInDur: Duration? = null,
	op: (ET).(P & Any)->Unit,

	) {
	initSetup(prop)
	fun Swapper<P, C>.refresh(value: P?) {
	  if (value == null) {
		if (nullMessage != null) {
		  setInnerNode(nullMessageNode(nullMessage), fadeOutDur = fadeOutDur, fadeInDur = fadeInDur)
		} else {
		  nullValueButNoMessage()
		}
	  } else {
		val proxy = ProxyEventTargetWrapper {
		  setInnerNode(it, fadeOutDur = fadeOutDur, fadeInDur = fadeInDur)
		}
		proxy.op(value)
	  }
	}
	listener = fxWatcherProp!!.onChangeWithWeak(this) { swap, it ->
	  swap.refresh(it)
	}
	refresh(fxWatcherProp!!.value)
  }

  @Synchronized
  fun setupSwappingWithReceiverNullable(
	prop: ObsVal<P>,
	fadeOutDur: Duration? = null,
	fadeInDur: Duration? = null,
	op: (ET).(P)->Unit,

	) {
	initSetup(prop)
	fun Swapper<P, C>.refresh(value: P) {
	  val proxy = ProxyEventTargetWrapper {
		setInnerNode(it, fadeOutDur = fadeOutDur, fadeInDur = fadeInDur)
	  }
	  proxy.op(value)
	}
	listener = fxWatcherProp!!.onChangeWithWeak(this) { swap, it ->
	  swap.refresh(it)
	}
	refresh(fxWatcherProp!!.value)
  }

  @Synchronized
  fun setupSwapping(
	prop: ObsVal<P>,
	nullMessage: String? = null,
	fadeOutDur: Duration? = null,
	fadeInDur: Duration? = null,
	op: (P & Any).()->C,

	) {
	initSetup(prop)
	fun Swapper<P, C>.refresh(value: P?) {
	  if (value == null) {
		if (nullMessage != null) {
		  setInnerNode(nullMessageNode(nullMessage), fadeOutDur = fadeOutDur, fadeInDur = fadeInDur)
		} else {
		  nullValueButNoMessage()
		}
	  } else {
		setInnerNode(op(value), fadeOutDur = fadeOutDur, fadeInDur = fadeInDur)
	  }
	}
	this.listener = fxWatcherProp!!.onChangeWithWeak(this) { swap, it ->
	  swap.refresh(it)
	}
	refresh(fxWatcherProp!!.value)
  }


  private fun initSetup(prop: ObsVal<P>) {
	fxWatcherProp?.removeListener(listener!!)
	fxWatcherProp = prop.nonBlockingFXWatcher()
  }

  val nullNodeFact = BindableProperty<(String)->NodeWrapper> {
	TextWrapper(it)
  }

  private fun nullMessageNode(nullMessage: String) = nullNodeFact.value(nullMessage)


  private fun nullValueButNoMessage() {
	anchor.children.clear()
	clearLayoutProxyNetwork()
  }

  private fun setInnerNode(
	node: NodeWrapper,
	fadeOutDur: Duration? = null,
	fadeInDur: Duration? = null
  ) {
	if (fadeOutDur != null) {
	  fade(
		time = fadeOutDur.toFXDuration(),
		opacity = 0.0
	  ) {
		setOnFinished {
		  //		  println("half way! opacity is now $opacity")
		  anchor.children.removeAll { it != node.node }
		  addInnerNode(node, fadeInDur = fadeInDur)
		}
	  }
	} else {
	  anchor.children.removeAll { it != node.node }
	  addInnerNode(node, fadeInDur = fadeInDur)
	}
  }

  private fun addInnerNode(
	node: NodeWrapper,
	fadeInDur: Duration? = null
  ) {
	setAsLayoutProxyForAndProxiedFrom(node)
	if (node.node !in anchor.children) anchor.children.add(node.node)
	if (fadeInDur != null) {
	  fade(
		time = fadeInDur.toFXDuration(),
		opacity = 1.0,
	  ) {
		setOnFinished {
		  //		  println("done! opacity is now $opacity")
		  node.setAsTopAnchor(0.0)
		  node.setAsBottomAnchor(0.0)
		  node.setAsLeftAnchor(0.0)
		  node.setAsRightAnchor(0.0)
		}
	  }
	} else {
	  opacity = 1.0
	  node.setAsTopAnchor(0.0)
	  node.setAsBottomAnchor(0.0)
	  node.setAsLeftAnchor(0.0)
	  node.setAsRightAnchor(0.0)
	}

  }

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }
}

