package matt.fx.graphics.style.background

import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.paint.Paint
import matt.obs.prop.Var


fun backgroundFill(c: Paint) = BackgroundFill(c, null, null)
fun backgroundFromColor(c: Paint) = Background(backgroundFill(c))


fun Var<Background?>.ensureLastFillIsIfPresent(paint: Paint) {
    val currentBG = value
    if (currentBG != null) {
        val currentBGFills = currentBG.fills
        if (currentBGFills.last().fill != paint) {
            val standards = currentBGFills.filter { it.fill != paint }
            val example = standards.first()
            val cool = BackgroundFill(paint, example.radii, example.insets)
            value = Background(*standards.toTypedArray(), cool)
        }
    }
}


//
//
///**
// * Since Java's is package private
// */
// class MyBackgroundConverter: StyleConverter<Array<ParsedValue<*, *>>, Background>() {
//  companion object {
//	val INSTANCE: StyleConverter<Array<ParsedValue<*, *>>, Background> = MyBackgroundConverter()
//	val BACKGROUND_COLOR:  CssMetaData<Node, Array<Paint>> by lazy {
//	  SubCssMetaData(
//		"-fx-background-color",
//		SequenceConverter.getInstance(), arrayOf(
//		  Color.TRANSPARENT
//		)
//	  )
//	}
//
//
//	val BACKGROUND_RADIUS: CssMetaData<Node, Array<CornerRadii>> = SubCssMetaData(
//	  "-fx-background-radius",
//	  CornerRadiiConverter.getInstance(), arrayOf(CornerRadii.EMPTY)
//	)
//
//	val BACKGROUND_INSETS: CssMetaData<Node, Array<Insets>> = SubCssMetaData(
//	  "-fx-background-insets",
//	  InsetsConverter.SequenceConverter.getInstance(), arrayOf(
//		Insets.EMPTY
//	  )
//	)
//
//	val BACKGROUND_IMAGE: CssMetaData<Node, Array<Image>> = SubCssMetaData(
//	  "-fx-background-image",
//	  URLConverter.SequenceConverter.getInstance()
//	)
//
//	val BACKGROUND_REPEAT: CssMetaData<Node, Array<RepeatStruct>> = SubCssMetaData(
//	  "-fx-background-repeat",
//	  RepeatStructConverter.getInstance(), arrayOf(
//		RepeatStruct(
//		  REPEAT,
//		  REPEAT
//		)
//	  )
//	)
//
//	val BACKGROUND_POSITION: CssMetaData<Node, Array<BackgroundPosition>> = SubCssMetaData(
//	  "-fx-background-position",
//	  LayeredBackgroundPositionConverter.getInstance(),
//	  arrayOf(
//		BackgroundPosition.DEFAULT
//	  )
//	)
//
//	val BACKGROUND_SIZE: CssMetaData<Node, Array<BackgroundSize>> = SubCssMetaData(
//	  "-fx-background-size",
//	  LayeredBackgroundSizeConverter.getInstance(), arrayOf(
//		BackgroundSize.DEFAULT
//	  )
//	)
//
//  }
//
//  @Suppress("UNCHECKED_CAST")
//  override fun convert(convertedValues: Map<CssMetaData<out Styleable?, *>?, Any?>): Background? {
//	val fills = convertedValues[BACKGROUND_COLOR] as Array<Paint>?
//	val imageUrls = convertedValues[BACKGROUND_IMAGE] as Array<String?>
//	val hasFills = fills != null && fills.size > 0
//	val hasImages = imageUrls != null && imageUrls.size > 0
//
//	// If there are neither background fills nor images, then there is nothing for us to construct.
//	if (!hasFills && !hasImages) return null
//
//	// Iterate over all of the fills, and create BackgroundFill objects for each.
//	var backgroundFills: Array<BackgroundFill?>? = null
//	if (hasFills) {
//	  backgroundFills = arrayOfNulls(fills!!.size)
//	  var tmp = convertedValues[BACKGROUND_INSETS]
//	  val insets = if (tmp == null) arrayOfNulls(0) else (tmp as Array<Insets?>)
//	  tmp = convertedValues[BACKGROUND_RADIUS]
//	  val radii = if (tmp == null) arrayOfNulls(0) else (tmp as Array<CornerRadii?>)
//	  val lastInsetsIndex = insets.size - 1
//	  val lastRadiiIndex = radii.size - 1
//	  for (i in fills.indices) {
//		val `in` = if (insets.size > 0) insets[if (i <= lastInsetsIndex) i else lastInsetsIndex] else Insets.EMPTY
//		val ra = if (radii.size > 0) radii[if (i <= lastRadiiIndex) i else lastRadiiIndex] else CornerRadii.EMPTY
//		backgroundFills[i] = BackgroundFill(fills[i], ra, `in`)
//	  }
//	}
//
//	// Iterate over all of the image, and create BackgroundImage objects for each.
//	var backgroundImages: Array<BackgroundImage?>? = null
//	if (hasImages) {
//	  // TODO convert image urls into image objects!
//	  backgroundImages = arrayOfNulls(imageUrls.size)
//	  var tmp = convertedValues[BACKGROUND_REPEAT]
//	  val repeats = if (tmp == null) arrayOfNulls(0) else (tmp as Array<RepeatStruct?>)
//	  tmp = convertedValues[BACKGROUND_POSITION]
//	  val positions = if (tmp == null) arrayOfNulls(0) else (tmp as Array<BackgroundPosition?>)
//	  tmp = convertedValues[BACKGROUND_SIZE]
//	  val sizes = if (tmp == null) arrayOfNulls(0) else (tmp as Array<BackgroundSize?>)
//	  val lastRepeatIndex = repeats.size - 1
//	  val lastPositionIndex = positions.size - 1
//	  val lastSizeIndex = sizes.size - 1
//	  for (i in imageUrls.indices) {
//		// RT-21335: skip background and border images whose image url is null
//		if (imageUrls[i] == null) continue
//		val image = StyleManager.getInstance().getCachedImage(
//		  imageUrls[i]
//		) ?: continue
//		val repeat = if (repeats.size > 0) repeats[if (i <= lastRepeatIndex) i else lastRepeatIndex] else null // min
//		val position = if (positions.size > 0) positions[if (i <= lastPositionIndex) i else lastPositionIndex] else null // min
//		val size = if (sizes.size > 0) sizes[if (i <= lastSizeIndex) i else lastSizeIndex] else null // min
//		backgroundImages[i] = BackgroundImage(
//		  image,
//		  repeat?.repeatX,
//		  repeat?.repeatY,
//		  position, size
//		)
//	  }
//	}
//
//	// Give the background fills and background images to a newly constructed BackgroundConverter,
//	// and return it.
//	return Background(backgroundFills, backgroundImages)
//  }
//
//
//}
