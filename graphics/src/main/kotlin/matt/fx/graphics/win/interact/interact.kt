package matt.fx.graphics.win.interact

import javafx.application.Platform.runLater
import javafx.beans.binding.DoubleBinding
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.TextInputDialog
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import matt.file.MFile
import matt.file.construct.mFile
import matt.fx.graphics.core.scene.MScene
import matt.fx.graphics.lang.actionbutton
import matt.fx.graphics.win.bindgeom.bindGeometry
import matt.fx.graphics.win.interact.WinGeom.Centered
import matt.fx.graphics.win.interact.WinOwn.Auto
import matt.fx.graphics.win.stage.MStage
import matt.fx.graphics.win.stage.ShowMode
import matt.fx.graphics.win.stage.ShowMode.DO_NOT_SHOW
import matt.fx.graphics.win.stage.ShowMode.SHOW
import matt.fx.graphics.win.stage.ShowMode.SHOW_AND_WAIT
import matt.fx.graphics.win.stage.WMode
import matt.fx.graphics.win.stage.WMode.CLOSE
import matt.fx.graphics.win.stage.WMode.NOTHING
import matt.hurricanefx.eye.lang.BProp
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.eye.prop.booleanBinding
import matt.hurricanefx.eye.prop.doubleBinding
import matt.hurricanefx.tornadofx.control.button
import matt.hurricanefx.tornadofx.control.textarea
import matt.hurricanefx.tornadofx.dialog.alert
import matt.hurricanefx.wrapper.control.button.ButtonWrapper
import matt.hurricanefx.wrapper.imageview.ImageViewWrapper
import matt.hurricanefx.wrapper.node.NodeWrapperImpl
import matt.hurricanefx.wrapper.node.disableWhen
import matt.hurricanefx.wrapper.node.setOnDoubleClick
import matt.hurricanefx.wrapper.pane.anchor.AnchorPaneWrapper
import matt.hurricanefx.wrapper.stage.StageWrapper
import matt.hurricanefx.wrapper.pane.vbox.VBoxWrapper
import matt.hurricanefx.wrapper.parent.ParentWrapper
import matt.hurricanefx.wrapper.region.RegionWrapper
import matt.hurricanefx.wrapper.wrapped
import matt.json.prim.isValidJson
import matt.klib.lang.noExceptions
import matt.klib.lang.nullIfExceptions
import java.net.URI
import java.util.WeakHashMap
import kotlin.jvm.optionals.getOrNull

fun safe(s: String, op: ()->Unit): Boolean {
  var r = false
  alert(
	Alert.AlertType.CONFIRMATION,
	header = s,
	content = s,
	owner = Stage.getWindows().firstOrNull {
	  it.isFocused
	}
  ) {
	if (it.buttonData.isDefaultButton) {
	  op()
	  r = true
	}
  }
  return r
}

class MDialog<R> internal constructor(): VBoxWrapper() {
  val stg = MStage(wMode = CLOSE, EscClosable = true).apply {
	scene = MScene(this@MDialog).node
	width = 400.0
	height = 400.0
  }
  lateinit var confirmButton: ButtonWrapper
  fun confirm() = confirmButton.fire()
  val window get() = stg
  var x: Double? = null
  var y: Double? = null
  var owner: Window? = null
  var autoOwner: Boolean = true
  private var resultConverter: ()->R? = { null }
  fun getResult() = resultConverter()
  fun setResultConverter(op: ()->R?) {
	resultConverter = op
  }

  val readyProperty = BProp(true)

  fun readyWhen(o: ObservableValue<Boolean>) {
	readyProperty.bind(o)
  }

  init {
	exactHeightProperty().bind(stg.heightProperty)
	borderFill = Color.DARKBLUE
  }
}

val aXBindingStrengthener = WeakHashMap<Stage, DoubleBinding>()
val aYBindingStrengthener = WeakHashMap<Stage, DoubleBinding>()
fun StageWrapper.bindXYToOwnerCenter() {
  require(owner != null) {
	"must use initOwner before bindXYToOwnerCenter"
  }
  val xBinding = owner.xProperty().doubleBinding(owner.widthProperty(), this.widthProperty) {
	(owner.x + (owner.width/2)) - width/2
  }
  val yBinding = owner.yProperty().doubleBinding(owner.heightProperty(), this.heightProperty) {
	(owner.y + (owner.height/2)) - height/2
  }
  aXBindingStrengthener[this.node] = xBinding
  aYBindingStrengthener[this.node] = yBinding
  x = xBinding.value
  xBinding.onChange {
	x = it
  }
  y = yBinding.value
  yBinding.onChange {
	y = it
  }
}

fun StageWrapper.bindHWToOwner() {
  require(owner != null) {
	"must use initOwner before bindXYToOwnerCenter"
  }
  width = owner.width
  owner.widthProperty().onChange {
	width = it
  }
  height = owner.height
  owner.heightProperty().onChange {
	y = it
  }
}


inline fun <reified T> jsonEditor(json: String? = null) = dialog<T?> {
  val ta = textarea(json ?: "")
  val goodBind = ta.textProperty().booleanBinding {
	it != null
		&& it.isValidJson()
		&& noExceptions { Json.decodeFromString<T>(it) }
  }
  readyWhen(goodBind)
  ta.borderFill = Color.BLACK /*so it does not jitter*/
  goodBind.onChange {
	ta.borderFill = if (it) Color.BLACK else Color.RED
  }
  setResultConverter {
	ta.text.takeIf { it.isValidJson() }?.let { nullIfExceptions { Json.decodeFromString<T>(it) } }
  }
}


fun <R> dialog(
  cfg: MDialog<R>.()->Unit
): R? {
  val d = MDialog<R>()
  d.apply(cfg)
  d.stg.initOwner(d.owner ?: if (d.autoOwner) Window.getWindows().firstOrNull() else null)
  if (d.stg.owner != null) {
	Centered().applyTo(d.stg)
  } // d.stage.initAndCenterToOwner(own)
  var r: R? = null
  d.hbox {
	prefWidthProperty.bind(d.widthProperty)
	alignment = Pos.CENTER
	actionbutton("matt.hurricanefx.wrapper.control.column.matt.hurricanefx.wrapper.control.colbase.cancel") {
	  styleClass += "CancelButton"
	  d.stg.close()
	}
	d.confirmButton = button("confirm") {
	  styleClass += "ConfirmButton"
	  disableWhen { d.readyProperty.not() }
	  setOnAction {
		r = d.getResult()
		d.stg.close()
	  }
	}
	d.scene!!.addEventFilter(KeyEvent.KEY_PRESSED) {
	  if (it.code == KeyCode.ENTER) {
		if (d.readyProperty.value) {
		  d.confirmButton.fire()
		}
	  }
	}
  }
  d.window.showAndWait()
  return r
}


sealed class WinGeom {


  class Bound(val key: String): WinGeom() {
	override fun applyTo(win: StageWrapper) {
	  win.bindGeometry(key)
	}
  }

  class ManualOr0(
	val x: Double = 0.0,
	val y: Double = 0.0,
	val width: Double = 0.0,
	val height: Double = 0.0
  ): WinGeom() {
	override fun applyTo(win: StageWrapper) {
	  win.x = x
	  win.y = y
	  win.height = height
	  win.width = width
	}
  }

  class ManualOrOwner(
	val x: Double? = null,
	val y: Double? = null,
	val width: Double? = null,
	val height: Double? = null
  ): WinGeom() {
	override fun applyTo(win: StageWrapper) {
	  require(win.owner != null) { "use initOwner first" }
	  win.x = x ?: win.owner.x
	  win.y = y ?: win.owner.y
	  win.height = height ?: win.owner.height
	  win.width = width ?: win.owner.width
	}
  }

  class Centered(
	val width: Double = 400.0,
	val height: Double = 400.0
  ): WinGeom() {
	override fun applyTo(win: StageWrapper) {
	  win.width = width
	  win.height = height
	  if (win.owner == null) {
		win.centerOnScreen()
	  } else {
		win.bindXYToOwnerCenter()
	  }
	  /*require(win.owner != null) { "use initOwner first" }*/


	}
  }

  object Max: WinGeom() {
	override fun applyTo(win: StageWrapper) {
	  win.isMaximized = true
	  //	  win.width = width
	  //	  win.height = height
	  //	  if (win.owner == null) {
	  //		win.centerOnScreen()
	  //	  } else {
	  //		win.bindXYToOwnerCenter()
	  //	  }
	  /*require(win.owner != null) { "use initOwner first" }*/


	}
  }

  object CenteredMinWrapContent: WinGeom() {
	override fun applyTo(win: StageWrapper) {
	  require(win.owner != null) { "use initOwner first" }

	  win.bindXYToOwnerCenter()
	}
  }

  class MatchOwner: WinGeom() {
	override fun applyTo(win: StageWrapper) {
	  require(win.owner != null) { "use initOwner first" }
	  win.bindXYToOwnerCenter()
	  win.bindHWToOwner()
	}
  }

  abstract fun applyTo(win: StageWrapper)
}

sealed class WinOwn {
  object None: WinOwn() {
	override fun applyTo(win: StageWrapper) {
	  /*do nothing*/
	}
  }

  class Owner(val owner: Window): WinOwn() {
	override fun applyTo(win: StageWrapper) {
	  win.initOwner(owner)
	}
  }

  object Auto: WinOwn() {
	override fun applyTo(win: StageWrapper) {
	  win.initOwner(Window.getWindows().firstOrNull())
	}
  }

  abstract fun applyTo(win: StageWrapper)
}

//fun NodeWrapperImpl<Parent>.openInNewWindow(
//  showMode: ShowMode = SHOW,
//  wMode: WMode = NOTHING,
//  EscClosable: Boolean = false,
//  EnterClosable: Boolean = false,
//  own: WinOwn = Auto,
//  geom: WinGeom = Centered(),
//  mScene: Boolean = true,
//  beforeShowing: StageWrapper.()->Unit = {},
//  border: Boolean = true,
//  decorated: Boolean = false
//) = node.openInNewWindow(
//  showMode, wMode, EscClosable, EnterClosable, own, geom, mScene, beforeShowing, border, decorated
//)

fun ParentWrapper.openInNewWindow(
  showMode: ShowMode = SHOW,
  wMode: WMode = NOTHING,
  EscClosable: Boolean = false,
  EnterClosable: Boolean = false,
  own: WinOwn = Auto,
  geom: WinGeom = Centered(),
  mScene: Boolean = true,
  beforeShowing: StageWrapper.()->Unit = {},
  border: Boolean = true,
  decorated: Boolean = false
): MStage {
  return MStage(
	wMode = wMode,
	EscClosable = EscClosable,
	EnterClosable = EnterClosable,
	decorated = decorated
  ).apply {
	scene = if (mScene) MScene(this@openInNewWindow).node else Scene(this@openInNewWindow.node)
	own.applyTo(this)
	geom.applyTo(this)
	if (border) {
	  (this@openInNewWindow as RegionWrapper).borderFill = Color.DARKBLUE
	}
	beforeShowing()
	when (showMode) {
	  SHOW -> show()
	  SHOW_AND_WAIT -> showAndWait()
	  DO_NOT_SHOW -> Unit
	}
  }
}

fun MFile.openImageInWindow() {
  AnchorPaneWrapper(ImageViewWrapper(this@openImageInWindow.toURI().toString()).apply {
	isPreserveRatio = true
	runLater {
	  fitHeightProperty().bind(scene.window.heightProperty())
	  fitWidthProperty().bind(scene.window.widthProperty())
	  this.setOnDoubleClick { (scene.window as Stage).close() }
	}
  }).openInNewWindow()
}

fun ImageViewWrapper.doubleClickToOpenInWindow() {
  this.setOnDoubleClick { mFile(URI(this.image.url)).openImageInWindow() }
}

@OptIn(ExperimentalStdlibApi::class)
fun Node.textInput(
  default: String = "insert default here",
  prompt: String = "insert prompt here"
): String? = TextInputDialog(default).apply {
  initOwner(wrapped().stage)
  contentText = prompt
  initStyle(StageStyle.UTILITY)
}.showAndWait().getOrNull()

fun NodeWrapperImpl<*>.textInput(
  default: String = "insert default here",
  prompt: String = "insert prompt here"
): String? = node.textInput(default, prompt)

