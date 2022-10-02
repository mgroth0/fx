package matt.fx.media.dsl

import javafx.beans.value.ObservableValue
import javafx.scene.image.Image
import matt.hurricanefx.control.dsl.ControlDSL
import matt.hurricanefx.eye.prop.objectBindingN
import matt.fx.graphics.wrapper.imageview.ImageViewWrapper
import matt.hurricanefx.wrapper.node.attach
import matt.hurricanefx.wrapper.node.attachTo

interface MediaDSL: ControlDSL {


  fun imageview(url: String? = null, lazyload: Boolean = true, op: ImageViewWrapper.()->Unit = {}) =
	attach(
	  if (url == null) ImageViewWrapper() else ImageViewWrapper().apply { this.image = Image(url, lazyload) }, op
	)

  fun imageview(
	url: ObservableValue<String>,
	lazyload: Boolean = true,
	op: ImageViewWrapper.()->Unit = {}
  ) = ImageViewWrapper().attachTo(this, op) { imageView ->
	imageView.imageProperty().bind(url.objectBindingN { it?.let { Image(it, lazyload) } })
  }

  fun imageview(image: ObservableValue<Image?>, op: ImageViewWrapper.()->Unit = {}) =
	ImageViewWrapper().attachTo(this, op) {
	  it.imageProperty().bind(image)
	}

  fun imageview(image: Image, op: ImageViewWrapper.()->Unit = {}) =
	ImageViewWrapper().apply { this.image = image }.attachTo(this, op)


}