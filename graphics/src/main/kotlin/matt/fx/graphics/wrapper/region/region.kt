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
import matt.fx.graphics.style.backgroundColor
import matt.fx.graphics.style.copy
import matt.fx.graphics.style.vertical
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapperImpl
import matt.fx.graphics.wrapper.node.parent.parent
import matt.fx.graphics.wrapper.region.border.FXBorder
import matt.fx.graphics.wrapper.sizeman.SizeManaged
import matt.hurricanefx.eye.wrapper.obs.collect.createMutableWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp
import matt.lang.NEVER
import matt.lang.err
import matt.model.convert.Converter
import matt.obs.col.olist.mappedlist.toSyncedList
import matt.obs.prop.BindableProperty
import matt.obs.prop.Var
import matt.reflect.access
import kotlin.reflect.full.declaredMemberFunctions

interface RegionWrapper<C: NodeWrapper>: ParentWrapper<C>, SizeManaged {


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
		background = backgroundColor(value)
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

  val exactWidthProperty
	get() = BindableProperty<Double>(0.0).also {
	  minWidthProperty.bind(it)
	  maxWidthProperty.bind(it)
	}

  val exactHeightProperty
	get() = BindableProperty<Double>(0.0).also {
	  minHeightProperty.bind(it)
	  maxHeightProperty.bind(it)
	}


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

  override val paddingProperty by lazy {node.paddingProperty().toNonNullableProp()}
  override var padding by paddingProperty

  override val paddingVerticalProperty by lazy {
	paddingProperty.proxy(object: Converter<Insets, Double> {
	  override fun convertToB(a: Insets): Double {
		return a.vertical
	  }

	  override fun convertToA(b: Double): Insets {
		return padding.copy(vertical = b)
	  }

	})
  }
  override var paddingVertical by paddingVerticalProperty

  override val backgroundProperty by lazy { node.backgroundProperty().toNullableProp() }
  override var background by backgroundProperty

}
