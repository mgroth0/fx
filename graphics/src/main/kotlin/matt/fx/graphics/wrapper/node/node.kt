package matt.fx.graphics.wrapper.node

import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.collections.ObservableMap
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.geometry.Bounds
import javafx.geometry.Point3D
import javafx.scene.CacheHint
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.SnapshotParameters
import javafx.scene.effect.BlendMode
import javafx.scene.effect.Effect
import javafx.scene.image.WritableImage
import javafx.scene.input.ContextMenuEvent
import javafx.scene.input.DragEvent
import javafx.scene.input.Dragboard
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.input.TransferMode
import javafx.scene.input.ZoomEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import matt.file.MFile
import matt.file.construct.toMFile
import matt.fx.graphics.fxthread.ts.nonBlockingFXWatcher
import matt.fx.graphics.service.uncheckedNullableWrapperConverter
import matt.fx.graphics.service.wrapped
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.SingularEventTargetWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.node.parent.parent
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.fx.graphics.wrapper.style.StyleableWrapper
import matt.fx.graphics.wrapper.style.StyleableWrapperImpl
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNullableROProp
import matt.lang.delegation.provider
import matt.lang.delegation.valProp
import matt.model.flowlogic.recursionblocker.RecursionBlocker
import matt.obs.bind.binding
import matt.obs.bindings.bool.ObsB
import matt.obs.bindings.bool.not
import matt.obs.prop.BindableProperty
import matt.obs.prop.MObservableValNewAndOld
import matt.obs.prop.ObsVal
import matt.obs.prop.ValProp
import matt.obs.prop.Var
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlin.properties.ReadOnlyProperty


typealias NW = NodeWrapper

interface NodeWrapper: EventTargetWrapper, StyleableWrapper {

  override fun isInsideRow(): Boolean = parent?.isInsideRow() ?: false

  override val node: Node
  val visibleAndManagedProp: BindableProperty<Boolean>

  val hoverProperty: ObsVal<Boolean>

  val boundsInLocal: Bounds get() = node.boundsInLocal
  val boundsInParent: Bounds get() = node.boundsInParent

  val layoutBoundsProperty: ObsVal<Bounds>

  fun setOnKeyPressed(listener: (KeyEvent)->Unit) {
	node.setOnKeyPressed(listener)
  }

  fun setOnKeyTyped(listener: (KeyEvent)->Unit) = node.setOnKeyTyped(listener)

  fun setOnMousePressed(listener: (MouseEvent)->Unit) {
	node.setOnMousePressed(listener)
  }


  fun setOnMouseClicked(listener: (MouseEvent)->Unit) {
	node.setOnMouseClicked(listener)
  }


  fun setOnMouseDragged(listener: (MouseEvent)->Unit) {
	node.setOnMouseDragged(listener)
  }

  fun setOnMouseReleased(listener: (MouseEvent)->Unit) {
	node.setOnMouseReleased(listener)
  }

  fun setOnMouseMoved(listener: (MouseEvent)->Unit) {
	node.setOnMouseMoved(listener)
  }

  fun setOnMouseExited(listener: (MouseEvent)->Unit) {
	node.setOnMouseExited(listener)
  }

  fun setOnDragEntered(listener: (DragEvent)->Unit) {
	node.setOnDragEntered(listener)
  }

  fun setOnDragOver(listener: (DragEvent)->Unit) {
	node.setOnDragOver(listener)
  }

  fun setOnDragDetected(listener: (MouseEvent)->Unit) {
	node.setOnDragDetected(listener)
  }

  fun setOnDragDone(listener: (DragEvent)->Unit) {
	node.setOnDragDone(listener)
  }

  fun setOnDragExited(listener: (DragEvent)->Unit) {
	node.setOnDragExited(listener)
  }

  fun setOnDragDropped(listener: (DragEvent)->Unit) {
	node.setOnDragDropped(listener)
  }


  fun toFront() = node.toFront()
  fun toBack() = node.toBack()

  fun setOnScroll(listener: (ScrollEvent)->Unit) {
	node.setOnScroll(listener)
  }

  val managedProperty: Var<Boolean>
  val visibleProperty: Var<Boolean>


  fun fireEvent(e: Event) = node.fireEvent(e)

  //  val styleClass get() = node.styleClass


  fun setOnContextMenuRequested(value: EventHandler<ContextMenuEvent>) = node.setOnContextMenuRequested(value)

  var opacity
	get() = node.opacity
	set(value) {
	  node.opacity = value
	}

  fun opacityProperty(): DoubleProperty = node.opacityProperty()
  var rotate
	get() = node.rotate
	set(value) {
	  node.rotate = value
	}
  val rotateProperty: DoubleProperty get() = node.rotateProperty()

  var rotationAxis: Point3D
	get() = node.rotationAxis
	set(value) {
	  node.rotationAxis = value
	}
  val rotationAxisProperty: ObjectProperty<Point3D> get() = node.rotationAxisProperty()


  override val properties: ObservableMap<Any, Any?> get() = node.properties

  var isCache
	get() = node.isCache
	set(value) {
	  node.isCache = value
	}

  fun cacheProperty(): BooleanProperty = node.cacheProperty()

  var cacheHint: CacheHint
	get() = node.cacheHint
	set(value) {
	  node.cacheHint = value
	}

  fun cacheHintProperty(): ObjectProperty<CacheHint> = node.cacheHintProperty()


  fun snapshot(params: SnapshotParameters?, image: WritableImage?): WritableImage = node.snapshot(params, image)

  fun startDragAndDrop(vararg transferModes: TransferMode): Dragboard = node.startDragAndDrop(*transferModes)
  fun startFullDrag() = node.startFullDrag()

  fun lookupAll(selector: String): MutableSet<Node> = node.lookupAll(selector)

  var cursor: Cursor?
	get() = node.cursor
	set(value) {
	  node.cursor = value
	}

  fun cursorProperty(): ObjectProperty<Cursor> = node.cursorProperty()


  var clip: Node?
	get() = node.clip
	set(value) {
	  node.clip = value
	}

  fun clipProperty(): ObjectProperty<Node> = node.clipProperty()

  val effectProperty: Var<Effect?>
  var effect: Effect?
	get() = effectProperty.value
	set(value) {
	  effectProperty.value = value
	}

  var blendMode: BlendMode?
	get() = node.blendMode
	set(value) {
	  node.blendMode = value
	}

  fun blendModeProperty(): ObjectProperty<BlendMode> = node.blendModeProperty()

  fun autosize() = node.autosize()

  fun <T: Event> addEventFilter(eventType: EventType<T>, handler: EventHandler<T>) =
	node.addEventFilter(eventType, handler)

  fun <T: Event> addEventHandler(eventType: EventType<T>, handler: EventHandler<T>) =
	node.addEventHandler(eventType, handler)


  fun <T: Event> removeEventFilter(eventType: EventType<T>, handler: EventHandler<T>) =
	node.removeEventFilter(eventType, handler)

  fun <T: Event> removeEventHandler(eventType: EventType<T>, handler: EventHandler<T>) =
	node.removeEventHandler(eventType, handler)

  fun localToScene(bounds: Bounds): Bounds? = node.localToScene(bounds)
  fun localToScreen(bounds: Bounds): Bounds? = node.localToScreen(bounds)


  val scene: SceneWrapper<*>?
  val sceneProperty: ObsVal<SceneWrapper<*>?>


  operator fun NW.unaryPlus() {
	this@NodeWrapper.add(this)
  }


  var isEnabled
	get() = !isDisable
	set(value) {
	  isDisable = !value
	}
  var isEnable
	get() = !isDisable
	set(value) {
	  isDisable = !value
	}
  var isDisable
	get() = node.isDisable
	set(value) {
	  node.isDisable = value
	}

  val disabledProperty: MObservableValNewAndOld<Boolean>
  val disableProperty: Var<Boolean>

  val enableProperty: Var<Boolean>

  val isFocused: Boolean
  val focusedProperty: ObsVal<Boolean>


  var hGrow: Priority
	get() = HBox.getHgrow(node)
	set(value) {
	  HBox.setHgrow(node, value)
	}
  var vGrow: Priority
	get() = VBox.getVgrow(node)
	set(value) {
	  VBox.setVgrow(node, value)
	}

  fun setOnZoom(op: (ZoomEvent)->Unit) = node.setOnZoom(op)


  var isVisible
	get() = node.isVisible
	set(value) {
	  node.isVisible = value
	}

  var isManaged
	get() = node.isManaged
	set(value) {
	  node.isManaged = value
	}

  var translateX
	get() = node.translateX
	set(value) {
	  node.translateX = value
	}

  fun translateXProperty(): DoubleProperty = node.translateXProperty()

  var translateY
	get() = node.translateY
	set(value) {
	  node.translateY = value
	}

  fun translateYProperty(): DoubleProperty = node.translateYProperty()


  var layoutX
	get() = node.layoutX
	set(value) {
	  node.layoutX = value
	}


  val layoutXProperty: BindableProperty<Double>
	get() = node.layoutXProperty().toNonNullableProp().cast<Double>()


  var layoutY
	get() = node.layoutY
	set(value) {
	  node.layoutY = value
	}

  val layoutYProperty: BindableProperty<Double>
	get() = node.layoutYProperty().toNonNullableProp().cast<Double>()


  var scaleX
	get() = node.scaleX
	set(value) {
	  node.scaleX = value
	}

  val scaleXProperty: BindableProperty<Double>
	get() = node.scaleXProperty().toNonNullableProp().cast()
  var scaleY
	get() = node.scaleY
	set(value) {
	  node.scaleY = value
	}

  val scaleYProperty: BindableProperty<Double>
	get() = node.scaleYProperty().toNonNullableProp().cast()

  fun requestFocus() = node.requestFocus()

  fun setAsLayoutProxyForAndProxiedFrom(other: NodeWrapper) {

	require(this.node != other.node)

	require(hgrow == other.hgrow || (hgrow == null || other.hgrow == null))
	if (hgrow != null) other.hgrow = hgrow
	else if (other.hgrow != null) hgrow = other.hgrow

	require(vgrow == other.vgrow || (vgrow == null || other.vgrow == null))
	if (vgrow != null) other.vgrow = vgrow
	else if (other.vgrow != null) vgrow = other.vgrow

	layoutProxies.add(other)
	other.layoutProxies.add(this)
  }

  val layoutProxies: MutableSet<NodeWrapper>


  var hgrow: Priority?
	get() = HBox.getHgrow(this.node)
	set(value) {
	  val toSet = mutableSetOf<NodeWrapper>(this)
	  var toSearch = layoutProxies.toSet()
	  do {
		toSet += toSearch
		toSearch = toSearch.flatMap { it.layoutProxies }.filter { it !in toSet }.toSet()
	  } while (toSearch.isNotEmpty())
	  toSet.forEach {
		HBox.setHgrow(it.node, value)
	  }
	}
  var vgrow: Priority?
	get() = VBox.getVgrow(this.node)
	set(value) {
	  val toSet = mutableSetOf<NodeWrapper>(this)
	  var toSearch = layoutProxies.toSet()
	  do {
		toSet += toSearch
		toSearch = toSearch.flatMap { it.layoutProxies }.filter { it !in toSet }.toSet()
	  } while (toSearch.isNotEmpty())
	  toSet.forEach {
		VBox.setVgrow(it.node, value)
	  }
	}


  fun saveChoose(
	initialDir: MFile, title: String
  ): MFile? {
	return FileChooser().apply {
	  initialDirectory = initialDir
	  this.title = title
	}.showSaveDialog(stage?.node).toMFile()
  }

  val boundsInScene: Bounds
	get() = localToScene(boundsInLocal)!!

  val boundsInScreen: Bounds
	get() = localToScreen(boundsInLocal)!!


  var visibleAndManaged: Boolean
	get() = node.isVisible && node.isManaged
	set(value) {
	  node.isVisible = value
	  node.isManaged = value
	}


  val stage get() = scene?.window as? StageWrapper

  fun onDoubleClickConsume(action: ()->Unit) {
	node.setOnMouseClicked {
	  if (it.clickCount == 2) {
		action()
		it.consume()
	  }
	}
  }

  infix fun addTo(pane: EventTargetWrapper) = pane.addChild(this)


  fun setAsTopAnchor(offset: Double) = AnchorPane.setTopAnchor(node, offset)
  fun setAsBottomAnchor(offset: Double) = AnchorPane.setBottomAnchor(node, offset)
  fun setAsLeftAnchor(offset: Double) = AnchorPane.setLeftAnchor(node, offset)
  fun setAsRightAnchor(offset: Double) = AnchorPane.setRightAnchor(node, offset)

  override fun removeFromParent() {
	node.parent?.wrapped()?.childList?.remove(node)
  }


}


abstract class NodeWrapperImpl<out N: Node>(
  node: N
): SingularEventTargetWrapper<N>(node),
   StyleableWrapper by object: StyleableWrapperImpl(node) {
	 override fun setTheStyle(value: String) {
	   node.style = value
	   node.style
	 }
   },
   NodeWrapper {


  final override val sceneProperty by lazy {
	node.sceneProperty().toNullableROProp().binding(
	  converter = uncheckedNullableWrapperConverter<Scene, SceneWrapper<*>>()
	)
  }

  override val scene by sceneProperty

  val sceneTemp by provider<Int, ReadOnlyProperty<Any, Int>> {
	//	sceneProperty
	valProp<Any, Int> {
	  1
	  //	  SceneWrapper(Scene(VBox()))
	}
  }

  final override val focusedProperty by lazy { node.focusedProperty().toNonNullableROProp() }
  final override val isFocused by focusedProperty

  override val layoutBoundsProperty by lazy { node.layoutBoundsProperty().toNonNullableROProp() }
  override val hoverProperty by lazy { node.hoverProperty().toNonNullableROProp() }
  override val effectProperty by lazy { node.effectProperty().toNullableProp() }
  override val disabledProperty by lazy { node.disabledProperty().toNonNullableROProp() }
  override val disableProperty by lazy { node.disableProperty().toNonNullableProp() }
  override val enableProperty by lazy {
	val r = BindableProperty(!disableProperty.value)
	val rBlocker = RecursionBlocker()
	disableProperty.onChange {
	  rBlocker.with {
		r.value = !it
	  }
	}
	r.onChange {
	  rBlocker.with {
		disableProperty.value = !it
	  }
	}
	r
  }
  override val managedProperty by lazy { node.managedProperty().toNonNullableProp() }
  override val visibleProperty by lazy { node.visibleProperty().toNonNullableProp() }


  override val layoutProxies = mutableSetOf<NodeWrapper>()

  override fun setTheStyle(value: String) {
	node.style = value
  }

  private val _visibleAndManagedProp by lazy {
	val r = BindableProperty(isVisible && isManaged)
	var changing = false
	r.onChange {
	  changing = true
	  isVisible = it
	  isManaged = it
	  changing = false
	}
	visibleProperty.onChange {
	  if (!changing) r.value = isVisible && isManaged
	}
	managedProperty.onChange {
	  if (!changing) r.value = isVisible && isManaged
	}
	r
  }

  override val visibleAndManagedProp by lazy {
	_visibleAndManagedProp
  }

}


inline fun <T: NodeWrapper> EventTargetWrapper.attach(child: T, op: T.()->Unit = {}): T {
  contract {
	callsInPlace(op, EXACTLY_ONCE)
  }
  addChild(child)
  op(child)
  return child
}

inline fun <T: NodeWrapper> T.attachTo(parent: EventTargetWrapper, op: T.()->Unit = {}): T {
  contract {
	callsInPlace(op, EXACTLY_ONCE)
  }
  parent.addChild(this)
  op(this)
  return this
}


/**
 * Attaches the node to the pane and invokes the node operation.
 * Because the framework sometimes needs to setup the node, another lambda can be provided
 */
inline fun <T: NodeWrapper> T.attachTo(
  parent: EventTargetWrapper,
  after: T.()->Unit,
  before: (T)->Unit
): T {
  contract {
	callsInPlace(before, EXACTLY_ONCE)
	callsInPlace(after, EXACTLY_ONCE)
  }
  return this.also(before).attachTo(parent, after)
}


fun NodeWrapper.setOnDoubleClick(filter: Boolean = false, action: (MouseEvent)->Unit) {
  if (filter) {
	addEventFilter(MouseEvent.MOUSE_CLICKED) {
	  if (it.clickCount == 2)
		action(it)
	}
  } else {
	setOnMouseClicked {
	  if (it.clickCount == 2)
		action(it)
	}
  }

}


fun NodeWrapper.onLeftClick(clickCount: Int = 1, filter: Boolean = false, action: (MouseEvent)->Unit) {
  if (filter) {
	addEventFilter(MouseEvent.MOUSE_CLICKED) {
	  if (it.clickCount == clickCount && it.button === MouseButton.PRIMARY)
		action(it)
	}
  } else {
	setOnMouseClicked {
	  if (it.clickCount == clickCount && it.button === MouseButton.PRIMARY)
		action(it)
	}
  }
}

fun NodeWrapper.onRightClick(clickCount: Int = 1, filter: Boolean = false, action: (MouseEvent)->Unit) {
  if (filter) {
	addEventFilter(MouseEvent.MOUSE_CLICKED) {
	  if (it.clickCount == clickCount && it.button === MouseButton.SECONDARY)
		action(it)
	}
  } else {
	setOnMouseClicked {
	  if (it.clickCount == clickCount && it.button === MouseButton.SECONDARY)
		action(it)
	}
  }
}


private object TFX_TRANSITIONING_PROPERTY

/**
 * Whether this node is currently being used in a [ViewTransition]. Used to determine whether it can be used in a
 * transition. (Nodes can only exist once in the sceneGraph, so it cannot be in two transitions at once.)
 */
internal var NodeWrapper.isTransitioning: Boolean
  get() {
	val x = node.properties[TFX_TRANSITIONING_PROPERTY]
	return x != null && (x !is Boolean || x != false)
  }
  set(value) {
	node.properties[TFX_TRANSITIONING_PROPERTY] = value
  }


fun NodeWrapper.hide() {
  isVisible = false
  isManaged = false
}

fun NodeWrapper.show() {
  isVisible = true
  isManaged = true
}

fun NodeWrapper.whenVisible(runLater: Boolean = true, op: ()->Unit) {
  visibleProperty.onChange {
	if (it) {
	  if (runLater) Platform.runLater(op) else op()
	}
  }
}


fun <T: NodeWrapper> T.managedWhen(expr: ()->ObsB): T = managedWhen(expr())

fun <T: NodeWrapper> T.managedWhen(predicate: ObsB) = apply {
  managedProperty.bind(predicate)
}

fun <T: NodeWrapper> T.visibleWhen(predicate: ObsB) = apply {

  visibleProperty.bind(predicate)
}

fun <T: NodeWrapper> T.visibleWhen(expr: ()->ObsB): T = visibleWhen(expr())

fun <T: NodeWrapper> T.hiddenWhen(expr: ()->ObsB): T = hiddenWhen(expr())

fun <T: NodeWrapper> T.hiddenWhen(predicate: ObsB) = apply {
  val binding = predicate.not()
  visibleProperty.bind(binding)
}

fun <T: NodeWrapper> T.disableWhen(expr: ()->ObsB): T = disableWhen(expr())

fun <T: NodeWrapper> T.disableWhen(predicate: ObsB) = apply {
  disableProperty.bind(predicate)
}

fun <T: NodeWrapper> T.enableWhen(expr: ()->ObsB): T = enableWhen(expr())

fun <T: NodeWrapper> T.enableWhen(predicate: ObsB) = apply {
  enableProperty.bind(predicate.nonBlockingFXWatcher())
}

fun <T: NodeWrapper> T.removeWhen(expr: ()->ValProp<Boolean>): T = removeWhen(expr())

fun <T: NodeWrapper> T.removeWhen(predicate: ValProp<Boolean>) = apply {
  val remove = predicate.not()
  visibleProperty.bind(remove)
  managedProperty.bind(remove)
}


fun NodeWrapper.onHover(onHover: (Boolean)->Unit) = apply {
  hoverProperty.onChange(onHover)
}


fun NW.minYRelativeTo(ancestor: NodeWrapper): Double? {
  var p: ParentWrapper<*>? = parent
  var y = boundsInParent.minY
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

