package matt.fx.control.wrapper.scroll

import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.geometry.Bounds
import javafx.scene.control.ScrollPane
import javafx.scene.control.ScrollPane.ScrollBarPolicy
import matt.fx.control.control.nodedsl.minYRelativeTo
import matt.fx.control.wrapper.control.ControlWrapperImpl
import matt.fx.control.wrapper.wrapped.wrapped
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.node.NodeWrapperImpl
import matt.hurricanefx.eye.prop.getValue
import matt.hurricanefx.eye.prop.setValue


//fun <C: NodeWrapper> ScrollPaneNoBars(content: C? = null): ScrollPaneWrapper<C> {
//  return (content?.let { ScrollPaneWrapper(it) } ?: ScrollPaneWrapper()).apply {
//	vbarPolicy = NEVER
//	hbarPolicy = NEVER
//  }
//}



open class ScrollPaneWrapper<C: NodeWrapper>(node: ScrollPane = ScrollPane()): ControlWrapperImpl<ScrollPane>(node) {
  constructor (content: C): this(ScrollPane(content.node))

  var viewportBounds: Bounds
	get() = node.viewportBounds
	set(value) {
	  node.viewportBounds = value
	}

  var vmin
	get() = node.vmin
	set(value) {
	  node.vmin = value
	}

  var vmax
	get() = node.vmax
	set(value) {
	  node.vmax = value
	}


  var hmin
	get() = node.hmin
	set(value) {
	  node.hmin = value
	}
  var hmax
	get() = node.hmax
	set(value) {
	  node.hmax = value
	}


  var vbarPolicy: ScrollBarPolicy
	get() = node.vbarPolicy
	set(value) {
	  node.vbarPolicy = value
	}

  var hbarPolicy: ScrollBarPolicy
	get() = node.hbarPolicy
	set(value) {
	  node.hbarPolicy = value
	}

  var isFitToWidth
	get() = node.isFitToWidth
	set(value) {
	  node.isFitToWidth = value
	}

  fun fitToWidthProperty(): BooleanProperty = node.fitToWidthProperty()
  var isFitToHeight
	get() = node.isFitToHeight
	set(value) {
	  node.isFitToHeight = value
	}

  fun fitToHeightProperty(): BooleanProperty = node.fitToHeightProperty()

  var prefViewportWidth
	get() = node.prefViewportWidth
	set(value) {
	  node.prefViewportWidth = value
	}
  var prefViewportHeight
	get() = node.prefViewportHeight
	set(value) {
	  node.prefViewportHeight = value
	}

  val hValueProp: DoubleProperty get() = node.hvalueProperty()
  val vValueProp: DoubleProperty get() = node.vvalueProperty()

  var hvalue by hValueProp
  var vvalue by vValueProp

  @Suppress("UNCHECKED_CAST") var content: C
	get() = node.content.wrapped() as C
	set(value) {
	  node.content = value.node
	}

  fun scrollToMinYOf(node: NodeWrapperImpl<*>): Boolean {/*scrolling values range from 0 to 1*/
	minYRelativeTo(content)?.let {
	  vvalue =
		(it/content.boundsInLocal.height)*1.1 /*IDK why, but y is always coming up a bit short, but this fixes it*/
	  return true
	}
	return false
  }

  val vValueConverted
	get() = vvalue*((content.boundsInParent.height - viewportBounds.height).takeIf { it > 0 } ?: 0.0)

  val vValueConvertedMax get() = vValueConverted + viewportBounds.height
  override fun addChild(child: NodeWrapper, index: Int?) {
	require(index == null)    /*content = node*/ /*TORNADOFX DEFAULT*/
	content.addChild(child) /*MATT'S WAY*/

  }


}