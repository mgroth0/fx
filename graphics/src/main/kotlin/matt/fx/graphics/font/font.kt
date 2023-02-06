package matt.fx.graphics.font

import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontPosture.ITALIC
import javafx.scene.text.FontWeight
import javafx.scene.text.FontWeight.BOLD
import matt.lang.err

val fontFamilies: List<String> by lazy { Font.getFamilies() }

//@Suppress("UNUSED_PARAMETER") fun Font.copy(
//  family: String = this.family,
//  size: Double = this.size,
//  weight: FontWeight? = null,/*this.style.parseFXStyle()[`font-size`]?.let { FontWeight.findByName(it) },*/
//  posture: FontPosture? = null/* this.style.parseFXStyle()[`font-style`]?.let { FontPosture.findByName(it) }*/
//): Font {
//
//
//
//  err("italic/bold=${Font.font("Ariel", BOLD, ITALIC, 10.0)}")
//  @Suppress("UNREACHABLE_CODE")
//  return Font.font(family, weight, posture, size)
//}

fun Font.fixed(): FixedFont {
  var weight: FontWeight? = null
  var posture: FontPosture? = null
  if (style != "Regular") {
	val fields = style.trim().substringAfter("Font[").dropLast(1).split(",")
	  .associate { it.substringBefore("=").trim() to it.substringAfter("=").trim() }
	val weightAndPostureStyle = fields["style"]
	if (weightAndPostureStyle == "Bold Italic") {
	  weight = BOLD
	  posture = ITALIC
	} else err("don't know how to process font style \"${weightAndPostureStyle}\"")
  }
  return FixedFont(family = family, size = size, weight = weight, posture = posture)
}

data class FixedFont internal constructor(
  val family: String,
  val size: Double,
  val weight: FontWeight? = null,
  val posture: FontPosture? = null
) {
  fun fx(): Font = Font.font(family, weight, posture, size)
}




