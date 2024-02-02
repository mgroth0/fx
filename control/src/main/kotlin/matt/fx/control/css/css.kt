package matt.fx.control.css

import javafx.css.CssMetaData
import javafx.css.Styleable
import javafx.css.converter.BooleanConverter


abstract class BooleanCssMetaData<T : Styleable>(
    property: String,
    initialValue: Boolean
) : CssMetaData<T, Boolean>(
        property, BooleanConverter.getInstance(), initialValue
    )
