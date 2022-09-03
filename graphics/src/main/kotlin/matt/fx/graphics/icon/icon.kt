package matt.fx.graphics.icon

//import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import com.kitfox.svg.SVGUniverse
import com.kitfox.svg.animation.AnimationElement.AT_XML
import javafx.scene.image.Image
import matt.file.MFile
import matt.file.commons.ICON_FOLDER
import matt.fx.image.toFXImage
import matt.hurricanefx.wrapper.imageview.ImageViewWrapper
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.stream.map.lazyMap
import java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.KEY_COLOR_RENDERING
import java.awt.RenderingHints.KEY_DITHERING
import java.awt.RenderingHints.KEY_FRACTIONALMETRICS
import java.awt.RenderingHints.KEY_INTERPOLATION
import java.awt.RenderingHints.KEY_RENDERING
import java.awt.RenderingHints.KEY_STROKE_CONTROL
import java.awt.RenderingHints.KEY_TEXT_ANTIALIASING
import java.awt.RenderingHints.Key
import java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON
import java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY
import java.awt.RenderingHints.VALUE_DITHER_DISABLE
import java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON
import java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC
import java.awt.RenderingHints.VALUE_RENDER_QUALITY
import java.awt.RenderingHints.VALUE_STROKE_PURE
import java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB

const val ICON_WIDTH = 20.0
const val ICON_HEIGHT = 20.0

fun NodeWrapper.icon(file: MFile) = add(Icon(file))

fun NodeWrapper.icon(image: Image) = add(Icon(image))

fun NodeWrapper.icon(file: String) = add(Icon(file))


private val FALLBACK_FILE = (ICON_FOLDER + "chunk.png").apply { //  SvgImageLoaderFactory.install();
}

fun matt.file.icongen.Icon.view() = Icon(name)

fun Icon(file: String) = Icon(ICON_FOLDER[file])
fun IconImage(file: String) = IconImage(ICON_FOLDER[file])
fun Icon(file: MFile): ImageViewWrapper = Icon(IconImage(file))

/*private val SVG_PARAMS = LoaderParameters().apply {
  this.width = ICON_WIDTH
  this.
}*/

fun Icon(image: Image): ImageViewWrapper = ImageViewWrapper(image).apply {
  isPreserveRatio = false
  fitWidth = ICON_WIDTH
  fitHeight = ICON_HEIGHT
}


/* when (image) {
is Image  ->*//* ImageView(image).apply {
	isPreserveRatio = false
	fitWidth = ICON_WIDTH
	fitHeight = ICON_HEIGHT
  }*/

/*  is String -> SVGLoader.load(image*//*, ICON_HEIGHT*//*, SVG_PARAMS).apply {
	*//*prefWidth(ICON_WIDTH)
	prefHeight(ICON_HEIGHT)*//*
  }

  else      -> Icon(FALLBACK_FILE)*//*}*/

private val IMAGE_EXTENSIONS = listOf("svg", "png", "jpg", "jpeg")

private val images = lazyMap<MFile, Image> { file ->
  (file.takeIf { it.exists() } ?: if (file.extension.isBlank()) IMAGE_EXTENSIONS.map { file.withExtension(it) }
	.firstOrNull {
	  it.exists()
	} ?: FALLBACK_FILE else FALLBACK_FILE).let { f ->
	if (f.extension == "svg") SVGUniverse().let {
	  val docURI = it.loadSVG(f.toURI().toURL())
	  val bim = BufferedImage(ICON_WIDTH.toInt(), ICON_HEIGHT.toInt(), TYPE_INT_ARGB)
	  val dia = it.getDiagram(docURI)
	  val g = bim.createGraphics().apply {

		//		transform.setToScale(ICON_WIDTH/dia.width, ICON_HEIGHT/dia.height)

		/*renderingHints = RenderingHints(RENDERING_HINTS)*/            //		renderingHints.putAll(RENDERING_HINTS)
	  }
	  dia.apply {

		if (root.hasAttribute("width", AT_XML)) {
		  root.setAttribute("width", AT_XML, ICON_WIDTH.toInt().toString())
		} else {
		  root.addAttribute("width", AT_XML, ICON_WIDTH.toInt().toString())
		}

		if (root.hasAttribute("height", AT_XML)) {
		  root.setAttribute("height", AT_XML, ICON_HEIGHT.toInt().toString())
		} else {
		  root.addAttribute("height", AT_XML, ICON_HEIGHT.toInt().toString())
		}


		//		this.deviceViewport = Rectangle(ICON_WIDTH.toInt(), ICON_HEIGHT.toInt())
		//		this.
		//		setIgnoringClipHeuristic(true)
		root.build()            //		  println("svg string = ${this.toString()}")
		//		  println("svg root string = ${this.root.toString()}")

	  }.render(g)

	  bim.toFXImage()
	} else Image(
	  f.toPath().toUri().toURL().toString()
	)
  }
}

fun IconImage(file: MFile): Image = images[file]

private val RENDERING_HINTS: MutableMap<Key, Any> = java.util.Map.of(
  KEY_ANTIALIASING, VALUE_ANTIALIAS_ON, KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY, KEY_COLOR_RENDERING,
  VALUE_COLOR_RENDER_QUALITY, KEY_DITHERING, VALUE_DITHER_DISABLE, KEY_FRACTIONALMETRICS, VALUE_FRACTIONALMETRICS_ON,
  KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC, KEY_RENDERING, VALUE_RENDER_QUALITY, KEY_STROKE_CONTROL,
  VALUE_STROKE_PURE, KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON
)










