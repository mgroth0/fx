package matt.fx.graphics.icon.svg

import com.kitfox.svg.SVGRoot
import com.kitfox.svg.SVGUniverse
import com.kitfox.svg.animation.AnimationElement.AT_XML
import matt.file.MFile
import matt.fx.graphics.icon.ICON_WIDTH
import matt.lang.anno.SeeURL
import org.apache.batik.anim.dom.SVGDOMImplementation
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.TranscodingHints
import org.apache.batik.transcoder.image.ImageTranscoder
import org.apache.batik.util.SVGConstants
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.InputStream
import java.net.URI


fun svgToImage(
  svg: MFile,
  width: Int = ICON_WIDTH.toInt(),
  height: Int = ICON_WIDTH.toInt()
) = SVGUniverse().let { svgToImage(it, it.loadSVG(svg.toURI().toURL()), width = width, height = height) }

fun svgToImage(
  svg: InputStream,
  width: Int = ICON_WIDTH.toInt(),
  height: Int = ICON_WIDTH.toInt()
) = SVGUniverse().let { svgToImage(it, it.loadSVG(svg, "name?"), width = width, height = height) }

@SeeURL("https://github.com/blackears/svgSalamander/issues/75#issuecomment-877706802")
fun svgToImage(
  universe: SVGUniverse,
  docURI: URI,
  width: Int = ICON_WIDTH.toInt(),
  height: Int = ICON_WIDTH.toInt()
): BufferedImage {
  val bim = BufferedImage(width, height, TYPE_INT_ARGB)
  val dia = universe.getDiagram(docURI)
  val g = bim.createGraphics()
  dia.apply {
	/*(root as SVGElement).recurse { it.getChildren(null) }.forEach {
	  if (it.attr
	}*/
	root.setOrAddAttribute("width", width)
	root.setOrAddAttribute("height", height)
	root.build()
  }.render(g)
  return bim
}

fun SVGRoot.setOrAddAttribute(name: String, value: Int) = setOrAddAttribute(name, value.toString())
fun SVGRoot.setOrAddAttribute(name: String, value: String) {
  if (hasAttribute(name, AT_XML)) {
	setAttribute(name, AT_XML, value)
  } else {
	addAttribute(name, AT_XML, value)
  }
}


/*fixes black and white issue!?*/
fun svgToImage2(
  svg: MFile,
  width: Int = ICON_WIDTH.toInt(),
  height: Int = ICON_WIDTH.toInt()
) = svgToImage2(svg.inputStream(), width = width, height = height)

/*fixes black and white issue!?*/
fun svgToImage2(
  svg: InputStream,
  width: Int = ICON_WIDTH.toInt(),
  height: Int = ICON_WIDTH.toInt()
) = svgToImage2(TranscoderInput(svg), width = width, height = height)

/*fixes black and white issue!? Nope!*/
@SeeURL("https://stackoverflow.com/questions/42340833/convert-svg-image-to-png-in-java-by-servlet")
@SeeURL("https://stackoverflow.com/questions/11435671/how-to-get-a-bufferedimage-from-a-svg")
@SeeURL("https://stackoverflow.com/questions/21170235/parse-svg-path-style-using-batik")
fun svgToImage2(
  transcoder: TranscoderInput,
  width: Int = ICON_WIDTH.toInt(),
  height: Int = ICON_WIDTH.toInt()
): BufferedImage {

  var bim: BufferedImage? = null

  val t = object: ImageTranscoder() {
	override fun createImage(w: Int, h: Int): BufferedImage {
	  return BufferedImage(w, h, TYPE_INT_ARGB)
	}

	override fun writeImage(image: BufferedImage, out: TranscoderOutput?) {
	  bim = image
	}

  }

  val transcoderHints = TranscodingHints()

  transcoderHints[ImageTranscoder.KEY_XML_PARSER_VALIDATING] = false
  transcoderHints[ImageTranscoder.KEY_DOM_IMPLEMENTATION] = SVGDOMImplementation.getDOMImplementation()
  transcoderHints[ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI] = SVGConstants.SVG_NAMESPACE_URI
  transcoderHints[ImageTranscoder.KEY_DOCUMENT_ELEMENT] = "svg"


  transcoderHints[ImageTranscoder.KEY_HEIGHT] = height.toFloat()
  transcoderHints[ImageTranscoder.KEY_WIDTH] = width.toFloat()


  t.transcodingHints = transcoderHints
  t.transcode(transcoder, null)

  return bim!!
}
