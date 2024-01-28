package matt.fx.control.popup.popupcontrol.node.bridge

import com.sun.javafx.css.StyleManager
import com.sun.javafx.scene.ParentHelper
import com.sun.javafx.scene.control.Logging
import com.sun.javafx.scene.layout.PaneHelper
import javafx.css.CssMetaData
import javafx.css.CssParser.ParseError
import javafx.css.Styleable
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Skin
import javafx.scene.layout.Pane
import matt.fx.base.rewrite.ReWrittenFxClass
import matt.fx.control.popup.popupcontrol.node.MyPopupControl

@ReWrittenFxClass(originalName = "PopUpCSSBridge")
open class MyPopUpCSSBridge(open val popupControl: MyPopupControl): Pane() {


  private val helper = MyCSSBridgeHelper(this)


  init {
	// To initialize the class helper at the begining each constructor of this class
	helper.initHelper(this)
  }


  companion object {
	private val prefWidthCache by lazy {
	  MyPopupControl::class.java.getDeclaredField("prefWidthCache").apply {
		isAccessible = true
	  }
	}
	private val prefHeightCache by lazy {
	  MyPopupControl::class.java.getDeclaredField("prefWidthCache").apply {
		isAccessible = true
	  }
	}
	private val minWidthCache by lazy {
	  MyPopupControl::class.java.getDeclaredField("prefWidthCache").apply {
		isAccessible = true
	  }
	}
	private val minHeightCache by lazy {
	  MyPopupControl::class.java.getDeclaredField("prefWidthCache").apply {
		isAccessible = true
	  }
	}
	private val maxWidthCache by lazy {
	  MyPopupControl::class.java.getDeclaredField("prefWidthCache").apply {
		isAccessible = true
	  }
	}
	private val maxHeightCache by lazy {
	  MyPopupControl::class.java.getDeclaredField("prefWidthCache").apply {
		isAccessible = true
	  }
	}
  }

  final /**
   * Requests a layout pass to be performed before the next scene is
   * rendered. This is batched up asynchronously to happen once per
   * "pulse", or frame of animation.
   *
   *
   * If this parent is either a layout root or unmanaged, then it will be
   * added directly to the scene's dirty layout list, otherwise requestLayout
   * will be invoked on its parent.
   */
  override fun requestLayout() {
	prefWidthCache.set(popupControl, -1.0)
	prefHeightCache.set(popupControl, -1.0)
	minWidthCache.set(popupControl, -1.0)
	minHeightCache.set(popupControl, -1.0)
	maxWidthCache.set(popupControl, -1.0)
	maxHeightCache.set(popupControl, -1.0)
	//skinSizeComputed = false; -- RT-33073 disabled this
	super.requestLayout()
  }

  final /**
   * This method should be treated as final and should not be overridden by any subclasses of CSSBridge.
   * @return the styleable parent
   */
  override fun getStyleableParent(): Styleable {
	return popupControl.getStyleableParent()
  }

  final override fun getCssMetaData(): List<CssMetaData<out Styleable, *>> {
	return popupControl.getCssMetaData()
  }

  /*
         * Note: This method MUST only be called via its accessor method.
         */
  internal fun doGetAllParentStylesheets(): List<String>? {
	val styleable = styleableParent
	return if (styleable is Parent) {
	  ParentHelper.getAllParentStylesheets(styleable)
	} else null
  }

  /*
         * Note: This method MUST only be called via its accessor method.
         */
  internal fun doProcessCSS() {
	MyCSSBridgeHelper.superProcessCSS(this)
	if (popupControl.getSkin() == null) {
	  // try to create default skin
	  val defaultSkin: Skin<*>? = popupControl.createDefaultSkinInternal()
	  if (defaultSkin != null) {
		popupControl.skinProperty().set(defaultSkin)
		MyCSSBridgeHelper.superProcessCSS(this)
	  } else {
		val msg = "The -fx-skin property has not been defined in CSS for " + this +
				  " and createDefaultSkin() returned null."
		val errors: MutableList<ParseError>? = StyleManager.getErrors()
		if (errors != null) {
		  val error = ParseError(msg)
		  errors.add(error) // RT-19884
		}
		Logging.getControlsLogger().severe(msg)
	  }
	}
  }
}


class MyCSSBridgeHelper(private val bridge: MyPopUpCSSBridge): PaneHelper() {


  companion object {
	fun superProcessCSS(node: Node?) {
	  (getHelper(node) as MyCSSBridgeHelper).superProcessCSSImpl(node)
	}
  }

  fun superProcessCSSImpl(node: Node?) {
	super.processCSSImpl(node)
  }

  override fun processCSSImpl(node: Node) {
	(node as MyPopUpCSSBridge).doProcessCSS()
  }

  override fun getAllParentStylesheetsImpl(parent: Parent): List<String>? {
	return (parent as MyPopUpCSSBridge).doGetAllParentStylesheets()
  }


  /*companion object {*/
  private val theInstance: MyPopUpCSSBridge? = null

  /*	init {
		MyTooltipCSSBridge.theInstance = MyTooltipCSSBridge()
	  }*/

  private val instance: MyPopUpCSSBridge get() = bridge

  fun initHelper(cssBridge: MyPopUpCSSBridge) {
	setHelper(cssBridge, this)
  }


  /*}*/
}
