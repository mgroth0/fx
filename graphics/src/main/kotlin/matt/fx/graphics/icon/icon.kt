package matt.fx.graphics.icon

import javafx.scene.image.Image
import matt.collect.map.lazyMap
import matt.file.MFile
import matt.file.commons.ICON_FOLDER
import matt.fx.graphics.effect.INVERSION_EFFECT
import matt.fx.graphics.wrapper.imageview.ImageViewWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.image.toFXImage
import matt.obs.bind.binding
import matt.obs.bindings.str.ObsS
import matt.obs.prop.FakeObsVal
import matt.obs.prop.ObsVal
import matt.fx.graphics.icon.svg.svgToImage

const val ICON_WIDTH = 20.0
const val ICON_HEIGHT = 20.0

fun NodeWrapper.icon(file: MFile, invert: Boolean = false) = add(Icon(file, invert = invert))

fun NodeWrapper.obsFileIcon(file: ObsVal<MFile>, invert: Boolean = false) = add(ObsFileIcon(file, invert = invert))

fun NodeWrapper.icon(image: Image, invert: Boolean = false) = add(Icon(image, invert = invert))
fun NodeWrapper.obsImageIcon(image: ObsVal<Image>, invert: Boolean = false) = add(ObsIcon(image, invert = invert))

fun NodeWrapper.icon(file: String, invert: Boolean = false) = add(Icon(file, invert = invert))
fun NodeWrapper.obsStringIcon(file: ObsS, invert: Boolean = false) = add(ObsStringIcon(file, invert = invert))


private val FALLBACK_FILE by lazy { ICON_FOLDER + "chunk.png" }

fun matt.file.icongen.Icon.view() = Icon(name)

fun Icon(file: String, invert: Boolean = false) = Icon(ICON_FOLDER[file], invert = invert)
fun ObsStringIcon(file: ObsS, invert: Boolean = false) = ObsFileIcon(file.binding { ICON_FOLDER[it] }, invert = invert)

fun IconImage(file: String) = IconImage(ICON_FOLDER[file])
fun ObsIconImage(file: ObsS) = IconImage(file.binding { ICON_FOLDER[it] })


fun Icon(file: MFile, invert: Boolean = false): ImageViewWrapper = Icon(IconImage(file), invert = invert)
fun ObsFileIcon(file: ObsVal<MFile>, invert: Boolean = false): ImageViewWrapper =
  ObsIcon(IconImage(file), invert = invert)


fun Icon(image: Image, invert: Boolean = false): ImageViewWrapper = ObsIcon(FakeObsVal(image), invert = invert)
fun ObsIcon(image: ObsVal<Image>, invert: Boolean = false): ImageViewWrapper = ImageViewWrapper().apply {
  imageProperty.bind(image)
  isPreserveRatio = false
  fitWidth = ICON_WIDTH
  fitHeight = ICON_HEIGHT
  if (invert) effect = INVERSION_EFFECT
}

private val IMAGE_EXTENSIONS = listOf("svg", "png", "jpg", "jpeg")

private val images = lazyMap<MFile, Image> { file ->
  (file.takeIf { it.exists() } ?: if (file.extension.isBlank()) IMAGE_EXTENSIONS.map { file.withExtension(it) }
																  .firstOrNull {
																	it.exists()
																  } ?: FALLBACK_FILE else FALLBACK_FILE).let { f ->
	if (f.extension == "svg") svgToImage(f).toFXImage()
	else Image(f.toPath().toUri().toURL().toString())
  }
}

fun IconImage(file: MFile): Image = IconImage(FakeObsVal(file)).value
fun IconImage(file: ObsVal<MFile>): ObsVal<Image> = file.binding { images[it] }








