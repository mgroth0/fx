package matt.fx.graphics.laterprops

import javafx.application.Platform.runLater
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import matt.klib.lang.jDefault

fun BooleanProperty.later() = LaterBoolean().apply {
  bindBidirectional(this@later)
}
fun DoubleProperty.later() = LaterDouble().apply {
  bindBidirectional(this@later)
}

class LaterBoolean(b: Boolean = Boolean::class.jDefault!!): SimpleBooleanProperty(b) {
  override fun setValue(v: Boolean?) {
	runLater {
	  value = v
	}
  }

  override fun set(v: Boolean) {
	runLater {
	  set(v)
	}
  }
}

class LaterDouble(b: Double? = null): SimpleDoubleProperty(b ?: Double::class.jDefault!!) {
  override fun setValue(v: Number?) {
	runLater {
	  super.setValue(v)
	}
  }

  override fun set(v: Double) {
	runLater {
	  set(v)
	}
  }
}

