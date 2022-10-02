package matt.fx.graphics.wrapper.imageview

import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import matt.collect.dmap.DefaultStoringMap
import matt.collect.dmap.withStoringDefault
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.NodeWrapperImpl
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import java.util.WeakHashMap

class ImageViewWrapper(
   node: ImageView = ImageView(),
): NodeWrapperImpl<ImageView>(node) {
  companion object {
	fun ImageView.wrapped() = ImageViewWrapper(this)
  }

  constructor(image: Image): this(ImageView(image))
  constructor(imageURL: String): this(ImageView(imageURL))




  var image: Image?
	get() = node.image
	set(value) {
	  node.image = value
	}

  fun imageProperty(): ObjectProperty<Image> = node.imageProperty()


  var isPreserveRatio
	get() = node.isPreserveRatio
	set(value) {
	  node.isPreserveRatio = value
	}

  fun preserveRatioProperty(): BooleanProperty = node.preserveRatioProperty()


  var fitWidth
	get() = node.fitWidth
	set(value) {
	  node.fitWidth = value
	}

  val fitWidthProperty = node.fitWidthProperty().toNonNullableProp().cast<Double>()

  var fitHeight
	get() = node.fitHeight
	set(value) {
	  node.fitHeight = value
	}

  val fitHeightProperty = node.fitHeightProperty().toNonNullableProp().cast<Double>()


  var isSmooth
	get() = node.isSmooth
	set(value) {
	  node.isSmooth = value
	}

  fun smoothProperty(): BooleanProperty = node.smoothProperty()



  fun fitBothProp(): DoubleProperty = fitBothProps[this.node]
  fun bindFitTo(r: RegionWrapperImpl<*, *>) {
	fitWidthProperty.bind(r.widthProperty)
	fitHeightProperty.bind(r.heightProperty)
  }

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }

}

val fitBothProps: DefaultStoringMap<ImageView, DoubleProperty> =
  WeakHashMap<ImageView, DoubleProperty>().withStoringDefault {
	SimpleDoubleProperty().apply {
	  it.fitWidthProperty().bind(this)
	  it.fitWidthProperty().bind(this)
	}
  }