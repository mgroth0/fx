package matt.fx.graphics.wrapper.region

import javafx.beans.property.DoubleProperty
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
import matt.fx.graphics.style.all
import matt.fx.graphics.style.backgroundColor
import matt.fx.graphics.style.copy
import matt.fx.graphics.style.horizontal
import matt.fx.graphics.style.insets
import matt.fx.graphics.style.vertical
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.hurricanefx.eye.lib.proxypropDouble
import matt.hurricanefx.eye.wrapper.obs.collect.createMutableWrapper
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.toNonNullableROProp
import matt.fx.graphics.wrapper.node.parent.ParentWrapper
import matt.fx.graphics.wrapper.node.parent.ParentWrapperImpl
import matt.fx.graphics.wrapper.node.parent.parent
import matt.fx.graphics.wrapper.region.border.FXBorder
import matt.fx.graphics.wrapper.sizeman.SizeManaged
import matt.hurricanefx.wrapper.pane.scroll.ScrollPaneWrapper
import matt.lang.NEVER
import matt.lang.err
import matt.obs.col.olist.mappedlist.toSyncedList
import matt.obs.prop.BindableProperty
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
	get() = node.padding
	set(value) {
	  node.padding = value
	}
  val paddingProperty: ObjectProperty<Insets> get() = node.paddingProperty()


  var background: Background?
	get() = node.background
	set(value) {
	  node.background = value
	}
  val backgroundProperty: ObjectProperty<Background> get() = node.backgroundProperty()


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

  @Deprecated("Use the paddingRight property instead", ReplaceWith("paddingRight = p")) fun paddingRight(p: Double) {
	paddingRight = p
  }

  var paddingRight: Number
	get() = padding.right
	set(value) {
	  padding = padding.copy(right = value.toDouble())
	}

  @Deprecated("Use the paddingLeft property instead", ReplaceWith("paddingLeft = p")) fun paddingLeft(p: Double) {
	paddingLeft = p
  }

  var paddingLeft: Number
	get() = padding.left
	set(value) {
	  padding = padding.copy(left = value)
	}

  @Deprecated("Use the paddingTop property instead", ReplaceWith("paddingTop = p")) fun paddingTop(p: Double) {
	paddingTop = p
  }

  var paddingTop: Number
	get() = padding.top
	set(value) {
	  padding = padding.copy(top = value)
	}

  @Deprecated("Use the paddingBottom property instead", ReplaceWith("paddingBottom = p")) fun paddingBottom(p: Double) {
	paddingBottom = p
  }

  var paddingBottom: Number
	get() = padding.bottom
	set(value) {
	  padding = padding.copy(bottom = value)
	}

  @Deprecated("Use the paddingVertical property instead", ReplaceWith("paddingVertical = p"))
  fun paddingVertical(p: Double) {
	paddingVertical = p
  }

  var paddingVertical: Number
	get() = padding.vertical*2
	set(value) {
	  val half = value.toDouble()/2.0
	  padding = padding.copy(vertical = half)
	}

  @Deprecated("Use the paddingHorizontal property instead", ReplaceWith("paddingHorizontal = p")) fun paddingHorizontal(
	p: Double
  ) {
	paddingHorizontal = p
  }

  var paddingHorizontal: Number
	get() = padding.horizontal*2
	set(value) {
	  val half = value.toDouble()/2.0
	  padding = padding.copy(horizontal = half)
	}

  @Deprecated("Use the paddingAll property instead", ReplaceWith("paddingAll = p")) fun paddingAll(p: Double) {
	paddingAll = p
  }

  var paddingAll: Number
	get() = padding.all
	set(value) {
	  padding = insets(value)
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


  val paddingVerticalProperty: DoubleProperty
	get() = node.properties.getOrPut("paddingVerticalProperty") {
	  proxypropDouble(paddingProperty, { paddingVertical.toDouble() }) {

		val half = it/2.0
		Insets(half, value.right, half, value.left)
	  }
	} as DoubleProperty

  val paddingHorizontalProperty: DoubleProperty
	get() = node.properties.getOrPut("paddingHorizontalProperty") {
	  proxypropDouble(paddingProperty, { paddingHorizontal.toDouble() }) {
		val half = it/2.0
		Insets(value.top, half, value.bottom, half)
	  }
	} as DoubleProperty

  val paddingAllProperty: DoubleProperty
	get() = node.properties.getOrPut("paddingAllProperty") {
	  proxypropDouble(paddingProperty, { paddingAll.toDouble() }) {
		Insets(it, it, it, it)
	  }
	} as DoubleProperty


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


  val paddingTopProperty: DoubleProperty
	get() = node.properties.getOrPut("paddingTopProperty") {
	  proxypropDouble(paddingProperty, { value.top }) {
		Insets(it, value.right, value.bottom, value.left)
	  }
	} as DoubleProperty

  val paddingBottomProperty: DoubleProperty
	get() = node.properties.getOrPut("paddingBottomProperty") {
	  proxypropDouble(paddingProperty, { value.bottom }) {
		Insets(value.top, value.right, it, value.left)
	  }
	} as DoubleProperty

  val paddingLeftProperty: DoubleProperty
	get() = node.properties.getOrPut("paddingLeftProperty") {
	  proxypropDouble(paddingProperty, { value.left }) {
		Insets(value.top, value.right, value.bottom, it)
	  }
	} as DoubleProperty

  val paddingRightProperty: DoubleProperty
	get() = node.properties.getOrPut("paddingRightProperty") {
	  proxypropDouble(paddingProperty, { value.right }) {
		Insets(value.top, it, value.bottom, value.left)
	  }
	} as DoubleProperty




}


abstract class RegionWrapperImpl<N: Region, C: NodeWrapper>(node: N): ParentWrapperImpl<N, C>(node), RegionWrapper<C> {
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
}
