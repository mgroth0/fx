package matt.fx.graphics.style.inset

import javafx.geometry.Insets


abstract class MarginableConstraints {
    abstract var margin: Insets?
    var marginTop: Double
        get() = margin?.top ?: 0.0
        set(value) {
            margin = Insets(value, margin?.right ?: 0.0, margin?.bottom ?: 0.0, margin?.left ?: 0.0)
        }

    var marginRight: Double
        get() = margin?.right ?: 0.0
        set(value) {
            margin = Insets(margin?.top ?: 0.0, value, margin?.bottom ?: 0.0, margin?.left ?: 0.0)
        }

    var marginBottom: Double
        get() = margin?.bottom ?: 0.0
        set(value) {
            margin = Insets(margin?.top ?: 0.0, margin?.right ?: 0.0, value, margin?.left ?: 0.0)
        }

    var marginLeft: Double
        get() = margin?.left ?: 0.0
        set(value) {
            margin = Insets(margin?.top ?: 0.0, margin?.right ?: 0.0, margin?.bottom ?: 0.0, value)
        }

    fun marginTopBottom(value: Double) {
        marginTop = value
        marginBottom = value
    }

    fun marginLeftRight(value: Double) {
        marginLeft = value
        marginRight = value
    }
}






