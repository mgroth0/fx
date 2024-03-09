package matt.fx.graphics.font

import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontPosture.ITALIC
import javafx.scene.text.FontWeight
import javafx.scene.text.FontWeight.BOLD
import matt.lang.common.err
import matt.lang.common.substringAfterSingular
import matt.lang.common.substringBeforeSingular

val fontFamilies: List<String> by lazy { Font.getFamilies() }



fun Font.fixed(): FixedFont {
    var weight: FontWeight? = null
    var posture: FontPosture? = null
    if (style != "Regular") {
        val fields =
            style.trim().substringAfterSingular("Font[").dropLast(1).split(",")
                .associate { it.substringBeforeSingular("=").trim() to it.substringAfterSingular("=").trim() }
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
    fun withFontSizeAdjustedBy(change: Double) = copy(size = size + change)
    fun withFontSizeAdjustedBy(change: Int) = copy(size = size + change)
}




