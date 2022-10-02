package matt.fx.graphics.wrapper.sizeman

import matt.obs.bindings.math.ObsD
import matt.obs.prop.Var

interface SizeControlled: HeightControlled, WidthControlled, Sized

interface HeightControlled: HasHeight {
  override var height: Double
}

interface WidthControlled: HasWidth {
  override var width: Double
}

interface SizeManaged: HeightManaged, WidthManaged, Sized {
  infix fun prefBind(other: Sized) {
	prefHeightProperty.bind(other.heightProperty)
	prefWidthProperty.bind(other.widthProperty)
  }


  infix fun minBind(other: Sized) {
	minHeightProperty.bind(other.heightProperty)
	minWidthProperty.bind(other.widthProperty)
  }

  infix fun maxBind(other: Sized) {
	maxHeightProperty.bind(other.heightProperty)
	maxWidthProperty.bind(other.widthProperty)
  }


  infix fun perfectBind(other: Sized) {
	this minBind other
	this maxBind other
  }

}

interface HeightManaged: HasHeight {


  val prefHeightProperty: Var<Double>
  var prefHeight: Double
	get() = prefHeightProperty.value
	set(value) {
	  prefHeightProperty.value = value
	}

  val minHeightProperty: Var<Double>
  var minHeight: Double
	get() = minHeightProperty.value
	set(value) {
	  minHeightProperty.value = value
	}

  val maxHeightProperty: Var<Double>
  var maxHeight: Double
	get() = maxHeightProperty.value
	set(value) {
	  maxHeightProperty.value = value
	}
}

interface WidthManaged: HasWidth {


  val prefWidthProperty: Var<Double>
  var prefWidth: Double
	get() = prefWidthProperty.value
	set(value) {
	  prefWidthProperty.value = value
	}

  val minWidthProperty: Var<Double>
  var minWidth: Double
	get() = minWidthProperty.value
	set(value) {
	  minWidthProperty.value = value
	}

  val maxWidthProperty: Var<Double>
  var maxWidth: Double
	get() = maxWidthProperty.value
	set(value) {
	  maxWidthProperty.value = value
	}

}

interface Sized: HasWidth, HasHeight

interface HasWidth {
  val widthProperty: ObsD

  val width: Double get() = widthProperty.value
}

interface HasHeight {
  val heightProperty: ObsD

  val height: Double get() = heightProperty.value
}