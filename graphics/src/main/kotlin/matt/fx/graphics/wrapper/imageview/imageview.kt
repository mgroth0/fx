package matt.fx.graphics.wrapper.imageview

import javafx.scene.image.Image
import javafx.scene.image.ImageView
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.NodeWrapperImpl
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.node.attachTo
import matt.fx.graphics.wrapper.region.RegionWrapperImpl
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNonNullableProp
import matt.hurricanefx.eye.wrapper.obs.obsval.prop.toNullableProp
import matt.obs.bind.binding
import matt.obs.prop.BindableProperty
import matt.obs.prop.ObsVal


fun ET.imageview(url: String? = null, lazyLoad: Boolean = true, op: ImageViewWrapper.()->Unit = {}) =
  attach(
	if (url == null) ImageViewWrapper() else ImageViewWrapper().apply { this.image = Image(url, lazyLoad) }, op
  )

fun ET.imageview(
  url: ObsVal<String>,
  lazyLoad: Boolean = true,
  op: ImageViewWrapper.()->Unit = {}
) = ImageViewWrapper().attachTo(this, op) { imageView ->
  imageView.imageProperty.bind(url.binding { Image(it, lazyLoad) })
}

fun ET.imageview(image: ObsVal<Image?>, op: ImageViewWrapper.()->Unit = {}) =
  ImageViewWrapper().attachTo(this, op) {
	it.imageProperty.bind(image)
  }

fun ET.imageview(image: Image, op: ImageViewWrapper.()->Unit = {}) =
  ImageViewWrapper().apply { this.image = image }.attachTo(this, op)


class ImageViewWrapper(
  node: ImageView = ImageView(),
): NodeWrapperImpl<ImageView>(node) {

  constructor(image: Image): this(ImageView(image))
  constructor(imageURL: String): this(ImageView(imageURL))


  val imageProperty by lazy { node.imageProperty().toNullableProp() }
  var image by imageProperty


  val preserveRatioProperty by lazy { node.preserveRatioProperty().toNonNullableProp() }
  var isPreserveRatio by preserveRatioProperty


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

  val smoothProperty by lazy { node.smoothProperty().toNonNullableProp() }


  val fitBothProp by lazy {
	BindableProperty(0.0).apply {
	  fitWidthProperty.bind(this)
	  fitWidthProperty.bind(this)
	}
  }

  fun bindFitTo(r: RegionWrapperImpl<*, *>) {
	fitWidthProperty.bind(r.widthProperty)
	fitHeightProperty.bind(r.heightProperty)
  }

  override fun addChild(child: NodeWrapper, index: Int?) {
	TODO("Not yet implemented")
  }

}