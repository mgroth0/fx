package matt.fx.graphics.icon

import javafx.scene.image.Image
import matt.collect.map.lazyMap
import matt.file.JioFile
import matt.file.commons.reg.ICON_FOLDER
import matt.file.ext.FileExtension.Companion.IMAGE_EXTENSIONS
import matt.file.ext.FileExtension.Companion.SVG
import matt.file.ext.j.hasExtension
import matt.file.ext.j.withExtension
import matt.file.toJioFile
import matt.fx.graphics.effect.INVERSION_EFFECT
import matt.fx.graphics.wrapper.imageview.ImageViewWrapper
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.image.toFXImage
import matt.lang.model.file.FsFile
import matt.model.data.rect.DoubleRectSize
import matt.obs.bind.binding
import matt.obs.bindings.str.ObsS
import matt.obs.prop.ObsVal
import matt.svg.render.svgToImage
import kotlin.io.path.extension

val ICON_SIZE =
    DoubleRectSize(
        width = 20.0,
        height = 20.0
    )

fun NodeWrapper.icon(
    file: FsFile,
    invert: Boolean = false
) = add(Icon(file, invert = invert))

fun NodeWrapper.obsFileIcon(
    file: ObsVal<FsFile>,
    invert: Boolean = false
) = add(ObsFileIcon(file, invert = invert))

fun NodeWrapper.icon(
    image: Image,
    invert: Boolean = false
) = add(Icon(image, invert = invert))

fun NodeWrapper.obsImageIcon(
    image: ObsVal<Image>,
    invert: Boolean = false
) = add(ObsIcon(image, invert = invert))

fun NodeWrapper.icon(
    file: String,
    invert: Boolean = false
) = add(Icon(file, invert = invert))

fun NodeWrapper.obsStringIcon(
    file: ObsS,
    invert: Boolean = false
) = add(ObsStringIcon(file, invert = invert))


private val FALLBACK_FILE by lazy { ICON_FOLDER + "white/chunk.png" }

fun matt.file.icongen.Icon.view() = Icon(name)

fun Icon(
    file: String,
    invert: Boolean = false
) = Icon(ICON_FOLDER[file], invert = invert)

fun ObsStringIcon(
    file: ObsS,
    invert: Boolean = false
) = ObsFileIcon(file.binding { ICON_FOLDER[it] }, invert = invert)

fun IconImage(file: String) = IconImage(ICON_FOLDER[file])
fun ObsIconImage(file: ObsS) = IconImage(file.binding { ICON_FOLDER[it] })


fun Icon(
    file: FsFile,
    invert: Boolean = false
): ImageViewWrapper = Icon(IconImage(file), invert = invert)

fun ObsFileIcon(
    file: ObsVal<FsFile>,
    invert: Boolean = false
): ImageViewWrapper =
    ObsIcon(IconImage(file), invert = invert)


fun Icon(
    image: Image,
    invert: Boolean = false
): ImageViewWrapper = NotObsIcon(image, invert = invert)

fun ObsIcon(
    image: ObsVal<Image>,
    invert: Boolean = false
): ImageViewWrapper =
    ImageViewWrapper().apply {
        imageProperty.bind(image)
        configIcon(invert = invert)
    }

fun NotObsIcon(
    image: Image,
    invert: Boolean = false
): ImageViewWrapper =
    ImageViewWrapper().apply {
        this.image = image
        configIcon(invert = invert)
    }

private fun ImageViewWrapper.configIcon(
    invert: Boolean = false
) {
    isPreserveRatio = false
    fitWidth = ICON_SIZE.width
    fitHeight = ICON_SIZE.height
    if (invert) effect = INVERSION_EFFECT
}


private val images =
    lazyMap<JioFile, Image> { file ->
        (
            file.takeIf { it.exists() }
                ?: if (file.extension.isNotBlank()) IMAGE_EXTENSIONS.map { file.withExtension(it) }
                    .firstOrNull {
                        it.exists()
                    } ?: FALLBACK_FILE else FALLBACK_FILE
        ).let { f ->
            if (f.hasExtension(SVG)) svgToImage(f, size = ICON_SIZE.toIntSize()).toFXImage()
            else Image(f.toUri().toURL().toString())
        }
    }


fun IconImage(file: ObsVal<FsFile>): ObsVal<Image> = file.binding { IconImage(it) }
fun IconImage(file: FsFile): Image = images[file.toJioFile()]








