package matt.fx.graphics.icon.svg

import matt.file.toJioFile
import matt.fx.graphics.icon.ICON_SIZE
import matt.fx.image.toFXImage
import matt.lang.model.file.FsFile
import matt.model.data.rect.IntRectSize
import matt.svg.render.svgToImage2
import java.io.InputStream


/*fixes black and white issue!?*/
fun svgToImage2(
    svg: FsFile,
    size: IntRectSize = ICON_SIZE.toIntSize(),
) = svgToImage2(svg.toJioFile().inputStream(), size = size)


fun svgToFXImage(
    svg: InputStream,
    size: IntRectSize = ICON_SIZE.toIntSize(),
) = svgToImage2(svg, size).toFXImage()

fun svgToFXImage(
    svg: FsFile,
    size: IntRectSize = ICON_SIZE.toIntSize(),
) = svgToImage2(svg, size).toFXImage()

