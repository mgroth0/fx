package matt.fx.media.dsl

import javafx.beans.value.ObservableValue
import javafx.scene.image.Image
import matt.fx.graphics.wrapper.ET
import matt.fx.graphics.wrapper.imageview.ImageViewWrapper
import matt.fx.graphics.wrapper.node.attach
import matt.fx.graphics.wrapper.node.attachTo
import matt.hurricanefx.eye.prop.objectBindingN


fun ET.imageview(url: String? = null, lazyload: Boolean = true, op: ImageViewWrapper.()->Unit = {}) =
  attach(
	if (url == null) ImageViewWrapper() else ImageViewWrapper().apply { this.image = Image(url, lazyload) }, op
  )

fun ET.imageview(
  url: ObservableValue<String>,
  lazyload: Boolean = true,
  op: ImageViewWrapper.()->Unit = {}
) = ImageViewWrapper().attachTo(this, op) { imageView ->
  imageView.imageProperty().bind(url.objectBindingN { it?.let { Image(it, lazyload) } })
}

fun ET.imageview(image: ObservableValue<Image?>, op: ImageViewWrapper.()->Unit = {}) =
  ImageViewWrapper().attachTo(this, op) {
	it.imageProperty().bind(image)
  }

fun ET.imageview(image: Image, op: ImageViewWrapper.()->Unit = {}) =
  ImageViewWrapper().apply { this.image = image }.attachTo(this, op)

