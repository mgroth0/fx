package matt.fx.graphics.wrapper.region

import javafx.beans.property.ObjectProperty
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.input.DataFormat
import javafx.scene.input.TransferMode
import javafx.scene.layout.Background
import javafx.scene.layout.Border
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import matt.file.MFile
import matt.file.construct.toMFile
import matt.fx.graphics.service.uncheckedWrapperConverter
import matt.fx.graphics.style.background.backgroundFromColor
import matt.fx.graphics.style.border.FXBorder
import matt.fx.graphics.style.copy
import matt.fx.graphics.style.vertical
import matt.fx.graphics.stylelock.toNullableStyleProp
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapperImpl
import matt.fx.graphics.wrapper.node.parent.parent
import matt.fx.graphics.wrapper.region.RegionWrapper.Companion.computePrefWidthFun
import matt.fx.graphics.wrapper.sizeman.SizeManaged
import matt.hurricanefx.eye.wrapper.obs.collect.list.createImmutableWrapper
import matt.hurricanefx.eye.wrapper.obs.collect.list.createMutableWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp
import matt.lang.NEVER
import matt.lang.err
import matt.model.op.convert.Converter
import matt.obs.col.olist.ImmutableObsList
import matt.obs.col.olist.mappedlist.toLazyMappedList
import matt.obs.col.olist.sync.toSyncedList
import matt.obs.prop.BindableProperty
import matt.obs.prop.Var
import matt.obs.prop.VarProp
import matt.obs.prop.proxy.ProxyProp
import matt.reflect.access
import kotlin.reflect.full.declaredMemberFunctions


interface RegionWrapper<C: NodeWrapper>: ParentWrapper<C>, SizeManaged {

  companion object {
	internal val computePrefWidthFun =
	  Region::class.java.getDeclaredMethod("computePrefWidth", Double::class.java).apply {
		isAccessible = true
	  }
  }


  override val node: Region


  fun setMinSize(minWidth: Double, minHeight: Double) = node.setMinSize(minWidth, minHeight)


  val borderProperty: ObjectProperty<Border> get() = node.borderProperty()
  var border: Border?
	get() = node.border
	set(value) = borderProperty.set(value)

  fun yellow() = borderProperty.set(FXBorder.dashed(Color.YELLOW))
  fun blue() = borderProperty.set(FXBorder.dashed(Color.BLUE))
  fun purple() = borderProperty.set(FXBorder.dashed(Color.PURPLE))
  fun green() = borderProperty.set(FXBorder.dashed(Color.GREEN))
  fun red() = borderProperty.set(FXBorder.dashed(Color.RED))
  fun orange() = borderProperty.set(FXBorder.dashed(Color.ORANGE))
  fun white() = borderProperty.set(FXBorder.dashed(Color.WHITE))

  var padding: Insets
  val paddingProperty: Var<Insets>


  val backgroundProperty: Var<Background?>
  var background: Background?

  override val widthProperty get() = node.widthProperty().toNonNullableROProp().cast<Double>()
  override val prefWidthProperty get() = node.prefWidthProperty().toNonNullableProp().cast<Double>()
  override val minWidthProperty get() = node.minWidthProperty().toNonNullableProp().cast<Double>()
  override val maxWidthProperty get() = node.maxWidthProperty().toNonNullableProp().cast<Double>()
  override val heightProperty get() = node.heightProperty().toNonNullableROProp().cast<Double>()
  override val prefHeightProperty get() = node.prefHeightProperty().toNonNullableProp().cast<Double>()
  override val minHeightProperty get() = node.minHeightProperty().toNonNullableProp().cast<Double>()
  override val maxHeightProperty get() = node.maxHeightProperty().toNonNullableProp().cast<Double>()

  fun setOnFilesDropped(op: (List<MFile>)->Unit) {
	node.setOnDragEntered {
	  it.acceptTransferModes(*TransferMode.ANY)
	}
	node.setOnDragOver {
	  it.acceptTransferModes(*TransferMode.ANY)
	}
	node.setOnDragDropped {
	  if (DataFormat.FILES in it.dragboard.contentTypes) {
		op(it.dragboard.files.map { it.toMFile() })
	  }
	  it.consume()
	}
  }

  var backgroundFill: Paint?
	set(value) {
	  if (value == null) {
		this.background = null
	  } else {
		background = backgroundFromColor(value)
	  }

	}
	get() {
	  err("no getter yet")
	}


  fun fitToParentHeight() {
	fitToHeight((parent as RegionWrapper))
  }

  fun fitToParentWidth() {
	fitToWidth((parent as RegionWrapper))
  }

  fun fitToParentSize() {
	fitToParentHeight()
	fitToParentWidth()
  }

  fun fitToHeight(region: RegionWrapper<*>) {
	prefHeightProperty.bind(region.heightProperty)
  }

  fun fitToWidth(region: RegionWrapper<*>) {
	prefWidthProperty.bind(region.widthProperty)
  }

  fun fitToSize(region: RegionWrapper<*>) {
	fitToHeight(region)
	fitToWidth(region)
  }


  val paddingVerticalProperty: Var<Double>
  var paddingVertical: Double

  val exactWidthProperty: VarProp<Double>
  val exactHeightProperty: VarProp<Double>


  var exactWidth: Number
	set(value) {
	  exactWidthProperty.bind(BindableProperty(value.toDouble()))
	}
	get() = NEVER
  var exactHeight: Number
	set(value) {
	  exactHeightProperty.bind(BindableProperty(value.toDouble()))
	}
	get() = NEVER


  var useMaxWidth: Boolean
	get() = maxWidth == Double.MAX_VALUE
	set(value) = if (value) maxWidth = Double.MAX_VALUE else Unit

  var useMaxHeight: Boolean
	get() = maxHeight == Double.MAX_VALUE
	set(value) = if (value) maxHeight = Double.MAX_VALUE else Unit

  var useMaxSize: Boolean
	get() = maxWidth == Double.MAX_VALUE && maxHeight == Double.MAX_VALUE
	set(value) = if (value) {
	  useMaxWidth = true; useMaxHeight = true
	} else Unit

  var usePrefWidth: Boolean
	get() = width == prefWidth
	set(value) = if (value) run {
	  minWidth = (Region.USE_PREF_SIZE)
	} else Unit

  var usePrefHeight: Boolean
	get() = height == prefHeight
	set(value) = if (value) run { minHeight = (Region.USE_PREF_SIZE) } else Unit

  var usePrefSize: Boolean
	get() = maxWidth == Double.MAX_VALUE && maxHeight == Double.MAX_VALUE
	set(value) = if (value) setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE) else Unit


  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }


  val children: ImmutableObsList<C>


}

fun RegionWrapper<*>.computePrefWidth(height: Double) = (computePrefWidthFun.invoke(node, height) as Double).also {
  println("computed pref width of $node with height $height is $it")
}


open class RegionWrapperImpl<N: Region, C: NodeWrapper>(node: N): ParentWrapperImpl<N, C>(node), RegionWrapper<C> {
  /*any temporary border changes might want to come back to this after*//*used to be an ugly lazy map that lead to errors. manual is better for this.*/
  var defaultBorder: Border = Border.EMPTY

  @Suppress("UNCHECKED_CAST") protected val regionChildren by lazy {
	(node::class.declaredMemberFunctions.first {
	  it.name == "getChildren"
	}.access {
	  call(this@RegionWrapperImpl.node)
	} as ObservableList<Node>).createMutableWrapper().toSyncedList(
	  uncheckedWrapperConverter()
	)
  }

  override val children: ImmutableObsList<C> by lazy {
	/*trying to avoid initializing wrappers to quickly (and getting the wrong ones as a result)*/
	node.childrenUnmodifiable.createImmutableWrapper()
	  .toLazyMappedList { uncheckedWrapperConverter<Node, C>().convertToB(it) }
  }

  override val paddingProperty by lazy { node.paddingProperty().toNonNullableProp() }
  override var padding by paddingProperty

  final override val paddingVerticalProperty: ProxyProp<Insets, Double> by lazy {
	paddingProperty.proxy(object: Converter<Insets, Double> {
	  override fun convertToB(a: Insets): Double {
		return a.vertical
	  }

	  override fun convertToA(b: Double): Insets {
		return padding.copy(vertical = b)
	  }

	})
  }
  final override var paddingVertical by paddingVerticalProperty

  final override val backgroundProperty by lazy { node.backgroundProperty().toNullableStyleProp() }
  final override var background by backgroundProperty

  final override val exactWidthProperty by lazy {
	BindableProperty<Double>(0.0).also {
	  minWidthProperty.bind(it)
	  maxWidthProperty.bind(it)
	}
  }

  final override val exactHeightProperty by lazy {
	BindableProperty(0.0).also {
	  minHeightProperty.bind(it)
	  maxHeightProperty.bind(it)
	}
  }

}


