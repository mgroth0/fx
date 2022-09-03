package matt.fx.graphics.hotkey

import javafx.application.Platform.runLater
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import matt.hotkey.Hotkey
import matt.hotkey.HotkeyDSL
import matt.hurricanefx.eye.prop.toggle
import matt.hurricanefx.wrapper.target.EventTargetWrapper
import matt.klib.commons.thisMachine
import matt.lang.NEVER
import matt.lang.err
import matt.lang.go
import matt.klib.log.Logger
import matt.klib.log.NOPLogger
import matt.klib.log.SystemOutLogger
import matt.klib.str.joinWithNewLinesAndTabs
import matt.klib.stream.allUnique
import matt.klib.stream.duplicates
import matt.klib.sys.Mac
import matt.stream.applyEach
import java.lang.System.currentTimeMillis
import java.util.WeakHashMap
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

sealed class HotKeyContainer: Hotkey {
  abstract fun getHotkeys(): List<HotKey>

}

data class HotKey(
  val code: KeyCode,
  var isMeta: Boolean = false,
  var isOpt: Boolean = false,
  var isCtrl: Boolean = false,
  var isShift: Boolean = false,

  var previous: HotKey? = null

): HotKeyContainer() {

  override fun getHotkeys() = listOf(this)


  var theOp: (()->Unit)? = null
  var theHandler: ((KeyEvent)->Unit)? = null
  var blocksFXorOSdefault = false

  val meta
	get() = apply { isMeta = true }
  val opt
	get() = apply { isOpt = true }
  val ctrl
	get() = apply { isCtrl = true }
  val shift
	get() = apply { isShift = true }
  val bare get() = this
  infix fun then(h: HotKey) = h.also { it.previous = this }

  var isIgnoreFix = false

  val ignoreFix
	get() = apply { isIgnoreFix = true }

  fun wrapOp(wrapper: (()->Unit)->Unit) {
	require(theOp != null)
	val oldOp = theOp
	theOp = {
	  wrapper(oldOp!!)
	}
  }

  fun wraphandler(wrapper: ((KeyEvent)->Unit)->Unit) {
	require(theHandler != null)
	val oldHandle = theHandler
	theHandler = {
	  wrapper(oldHandle!!)
	}
  }


  fun blockFXorOSdefault() = apply {
	blocksFXorOSdefault = true
  }

}


operator fun KeyCode.plus(other: KeyCode) = HotKeySet(HotKey(this), HotKey(other))
operator fun HotKey.plus(other: HotKey) = HotKeySet(this, other)
operator fun HotKey.plus(other: KeyCode) = HotKeySet(this, HotKey(other))
operator fun KeyCode.plus(other: HotKey) = HotKeySet(HotKey(this), other)


private val KeyCode.meta
  get() = HotKey(this, isMeta = true)
private val KeyCode.opt
  get() = HotKey(this, isOpt = true)
private val KeyCode.ctrl
  get() = HotKey(this, isCtrl = true)
private val KeyCode.shift
  get() = HotKey(this, isShift = true)
private val KeyCode.bare
  get() = HotKey(this)


class HotKeySet(vararg keys: HotKey): HotKeyContainer() {
  val keys = keys.toList()
  override fun getHotkeys() = keys
  val meta
	get() = apply {
	  keys.applyEach { isMeta = true }
	}
  val opt
	get() = apply { keys.applyEach { isOpt = true } }
  val ctrl
	get() = apply { keys.applyEach { isCtrl = true } }
  val shift
	get() = apply { keys.applyEach { isShift = true } }

  val ignoreFix
	get() = apply { keys.applyEach { isIgnoreFix = true } }


  fun blockFXorOSdefault() = apply {
	keys.applyEach {
	  blocksFXorOSdefault = true
	}
  }

}


infix fun KeyEvent.matches(h: HotKey) =
  code == h.code && isMetaDown == h.isMeta && isAltDown == h.isOpt && isControlDown == h.isCtrl && isShiftDown == h.isShift

infix fun HotKey.matches(h: HotKey) =
  code == h.code && isMeta == h.isMeta && isOpt == h.isOpt && isCtrl == h.isCtrl && isShift == h.isShift

infix fun KeyEvent.matches(h: KeyEvent) =
  code == h.code && isMetaDown == h.isMetaDown && isAltDown == h.isAltDown && isControlDown == h.isControlDown && isShiftDown == h.isShiftDown


const val DOUBLE_HOTKEY_WINDOW_MS = 500L
var lastHotKey: Pair<HotKey, Long>? = null


fun KeyEvent.runAgainst(
  hotkeys: Iterable<HotKeyContainer>, last: KeyEvent? = null, fixer: HotKeyEventHandler, log: Logger
) {

  log += "got hotkey: ${this}"

  val pressTime = currentTimeMillis()

  if (this.code.isModifierKey) {
	return consume()
  }

  log += "$this is running against"

  hotkeys.forEach {
	log += it
  }


  /*https://stackoverflow.com/questions/47797980/javafx-keyevent-triggers-twice*//*the solution apparently is to use key released*//*but that feels less responsive to me. heres my custom solution... here goes...*//*potential issue: if I'm typing and I need to type same key multiple matt.hurricanefx.eye.prop.math.times and hjavafx is laggy, it might not clear this and my keys might not go through. So I'm only doing this with keys that are actual hotkeys*/
  if (last != null) {
	if (this matches last) {
	  if (this.target == last.target) { //this one never passes

		/*

		here I assume there will be at most one duplicate...

		but what if there is no duplicate?

		I guess my best option is to assume there will be a duplicate always... if there isnt at least
		i can use that to start tracking down the root cause



		*/
		fixer.last = null

		//                println("consuming2 ${this}")
		return consume()

	  }
	} else {
	}
  } //  runLater { fixer.last = null }
  /*only possible race condition now is with:
  * 1. javafx lag
  * 2. same hotkey twice really fast, faster than this runLater can run
  * 3. or not even so fast, of there's lots of lag (but who cares then there shouldnt be lots of lag)
  * more concerned with if there's little lag but I just am trying to do lots of things fast...
  * guess there's little I can do for now. Would need to file a bug report with a minimal working example...
  * */

  /*UGH! I CANNOT AVOID THIS PROBLEM!!!! I must use eventHandlers wherever this occurs. Its fine, they are the proper HANDLERS anyway (filters are FILTERS)
  *
  * UPDATE: EVEN HANDLERS ARENT WORKING
  * */


  var ensureConsume = false
  hotkeys.asSequence().flatMap { it.getHotkeys() }.flatMap {
	sequence {
	  yield(it)
	  it.previous?.go { yield(it) }
	}
  }.filter { h ->
	log += "h(${this matches h}): $h"
	this matches h && (h.previous == null || (lastHotKey?.let {
	  h.previous!!.matches(it.first) && (pressTime - it.second) <= DOUBLE_HOTKEY_WINDOW_MS
	} ?: false))
  }.onEach {
	ensureConsume = ensureConsume || it.blocksFXorOSdefault
  }.forEach { h ->
	lastHotKey = h to currentTimeMillis()
	if (!h.isIgnoreFix) {
	  fixer.last = this
	}
	if (isConsumed) return
	h.theOp?.go {
	  it()
	  runLater {
		fixer.last = null
	  } /*... finally got it. not to early not too late. wow.*/        //                println("consume 3")
	  return consume()
	}
	h.theHandler?.invoke(this)
  }
  if (ensureConsume && !isConsumed) {    //        println("consume 4")
	consume()
  }
}

class HotKeyEventHandler(
  var quickPassForNormalTyping: Boolean
): EventHandler<KeyEvent> {

  var debug: Boolean = false

  val hotkeys = mutableListOf<HotKeyContainer>()

  init {    //        println("making handler ${this.hashCode()} with")
	//        hotkeys.forEach {
	//            tab(it)
	//        }
	hotkeys.flatMap { it.getHotkeys() }.forEach {
	  if (it.code == KeyCode.H && it.isMeta && !it.isCtrl && !it.isOpt && !it.isShift) {
		err("meta H is blocked by KM to prevent OS window hiding")
	  }
	  if (it.isOpt && !it.code.isArrowKey && !it.isMeta && !it.isCtrl && !it.isShift && (thisMachine is Mac)
	  ) {
		err(
		  "I think MacOS has problems with opt where it sends an extra key typed event. stupid. also seen in intellij"
		)
	  }
	}
  }

  var last: KeyEvent? = null
  override fun handle(event: KeyEvent) {    //        println("handling with ${this.hashCode()}")
	if (quickPassForNormalTyping && (event.code.isDigitKey || event.code.isLetterKey) && !event.isMetaDown && !event.isControlDown && !event.isAltDown) {
	  return
	}    //    println("event.code: ${event.code}")
	//    println("hotkeys length: ${hotkeys.size}")
	event.runAgainst(hotkeys, last = last, fixer = this, log = if (debug) SystemOutLogger else NOPLogger)
  }
}

fun <K, V> Map<K, V>.invert(): Map<V, K> {
  require(values.allUnique())
  return this.map { it.value to it.key }.associate { it.first to it.second }
}

val handlers = WeakHashMap<EventTarget, HotKeyEventHandler>()
val filters = WeakHashMap<EventTarget, HotKeyEventHandler>()

fun EventTarget.register(
  inFilter: Boolean,
  hotkeys: Iterable<HotKeyContainer>,
  quickPassForNormalTyping: Boolean = false,
  debug: Boolean = false,
) {
  val map = if (inFilter) filters else handlers
  val handler = map[this] ?: HotKeyEventHandler(quickPassForNormalTyping).also {
	map[this] = it
	if (inFilter) {
	  when (this) {
		is Node  -> addEventFilter(KeyEvent.KEY_PRESSED, it)
		is Scene -> addEventFilter(KeyEvent.KEY_PRESSED, it)
		is Stage -> addEventFilter(KeyEvent.KEY_PRESSED, it)
		else     -> NEVER
	  }
	} else {
	  when (this) {
		is Node  -> addEventHandler(KeyEvent.KEY_PRESSED, it)
		is Scene -> addEventHandler(KeyEvent.KEY_PRESSED, it)
		is Stage -> addEventHandler(KeyEvent.KEY_PRESSED, it)
		else     -> NEVER
	  }
	}
  }
  if (quickPassForNormalTyping) handler.quickPassForNormalTyping = true
  handler.hotkeys.addAll(hotkeys.toList())
  if (debug) handler.debug = true
  val dups = handler.hotkeys.duplicates()
  if (dups.isNotEmpty()) { /*doesnt check handlers and filters together, and doesnt check from multiple nodes together!*/
	err(
	  """
	  hotkey duplicates!:
	  ${dups.joinWithNewLinesAndTabs()}
	""".trimIndent()
	)
  }
}

@Suppress("PropertyName") class FXHotkeyDSL: HotkeyDSL<HotKeyContainer>() {

  /*fun keyCode(name: String) = KeyCode.getKeyCode(name).bare*/
  fun keyCode(name: String) = KeyCode.valueOf(name).bare

  val hotkeys = mutableSetOf<HotKeyContainer>()


  override val A get() = KeyCode.A.bare
  override val B get() = KeyCode.B.bare
  override val C get() = KeyCode.C.bare
  override val D get() = KeyCode.D.bare
  override val E get() = KeyCode.E.bare
  override val F get() = KeyCode.F.bare
  override val G get() = KeyCode.G.bare
  override val H get() = KeyCode.H.bare
  override val I get() = KeyCode.I.bare
  override val J get() = KeyCode.J.bare
  override val K get() = KeyCode.K.bare
  override val L get() = KeyCode.L.bare
  override val M get() = KeyCode.M.bare
  override val N get() = KeyCode.N.bare
  override val O get() = KeyCode.O.bare
  override val P get() = KeyCode.P.bare
  override val Q get() = KeyCode.Q.bare
  override val R get() = KeyCode.R.bare
  override val S get() = KeyCode.S.bare
  override val T get() = KeyCode.T.bare
  override val U get() = KeyCode.U.bare
  override val V get() = KeyCode.V.bare
  override val W get() = KeyCode.W.bare
  override val X get() = KeyCode.X.bare
  override val Y get() = KeyCode.Y.bare
  override val Z get() = KeyCode.Z.bare
  override val SPACE get() = KeyCode.SPACE.bare
  val ENTER get() = KeyCode.ENTER.bare
  val DELETE get() = KeyCode.DELETE.bare
  val BACK_SPACE get() = KeyCode.BACK_SPACE.bare
  val CLOSE_BRACKET get() = KeyCode.CLOSE_BRACKET.bare
  val RIGHT_BRACKET get() = CLOSE_BRACKET
  val OPEN_BRACKET get() = KeyCode.OPEN_BRACKET.bare
  val LEFT_BRACKET get() = OPEN_BRACKET
  val ESCAPE get() = KeyCode.ESCAPE.bare
  val TAB get() = KeyCode.TAB.bare
  val COMMA get() = KeyCode.COMMA.bare

  override val LEFT get() = KeyCode.LEFT.bare
  override val RIGHT get() = KeyCode.RIGHT.bare
  override val UP get() = KeyCode.UP.bare
  override val DOWN get() = KeyCode.DOWN.bare

  val DIGIT1 get() = KeyCode.DIGIT1.bare
  val DIGIT2 get() = KeyCode.DIGIT2.bare
  val DIGIT3 get() = KeyCode.DIGIT3.bare
  val DIGIT4 get() = KeyCode.DIGIT4.bare
  val DIGIT5 get() = KeyCode.DIGIT5.bare
  val DIGIT6 get() = KeyCode.DIGIT6.bare
  val DIGIT7 get() = KeyCode.DIGIT7.bare
  val DIGIT8 get() = KeyCode.DIGIT8.bare
  val DIGIT9 get() = KeyCode.DIGIT9.bare
  val DIGIT0 get() = KeyCode.DIGIT0.bare

  val BRACELEFT get() = KeyCode.BRACELEFT.bare
  val BRACERIGHT get() = KeyCode.BRACERIGHT.bare

  val PLUS get() = KeyCode.PLUS.bare
  val EQUALS get() = KeyCode.EQUALS.bare
  val MINUS get() = KeyCode.MINUS.bare

  fun HotKey.meta(h: ()->Unit) = apply { hotkeys.add(this.meta op { h() }) }
  fun HotKey.opt(h: ()->Unit) = apply { hotkeys.add(this.opt op { h() }) }
  fun HotKey.ctrl(h: ()->Unit) = apply { hotkeys.add(this.ctrl op { h() }) }
  fun HotKey.shift(h: ()->Unit) = apply { hotkeys.add(this.shift op { h() }) }
  fun HotKey.bare(h: ()->Unit) = apply { hotkeys.add(this op { h() }) }

  fun HotKeySet.meta(h: ()->Unit) = hotkeys.add(this.meta op { h() })
  fun HotKeySet.opt(h: ()->Unit) = hotkeys.add(this.opt op { h() })
  fun HotKeySet.ctrl(h: ()->Unit) = hotkeys.add(this.ctrl op { h() })
  fun HotKeySet.shift(h: ()->Unit) = hotkeys.add(this.shift op { h() })
  fun HotKeySet.bare(h: ()->Unit) = hotkeys.add(this op { h() })


  infix fun HotKey.op(setOp: ()->Unit) = apply {
	require(theHandler == null)
	theOp = setOp
	hotkeys.add(this)
  }


  infix fun HotKey.toggles(b: SimpleBooleanProperty) = op { b.toggle() }

  infix fun HotKey.handle(setHandler: (KeyEvent)->Unit) = apply {
	require(theOp == null)
	theHandler = setHandler
	hotkeys.add(this)
  }

  infix fun HotKeySet.op(setOp: ()->Unit) = apply {
	keys.applyEach {
	  require(theHandler == null)
	  theOp = setOp
	}
	hotkeys.add(this)
  }

  infix fun HotKeySet.toggles(b: SimpleBooleanProperty) = op { b.toggle() }

  infix fun HotKeySet.handle(setHandler: (KeyEvent)->Unit) = apply {
	keys.applyEach {
	  require(theOp == null)
	  theHandler = setHandler
	}
	hotkeys.add(this)
  }
}

inline fun EventTargetWrapper.hotkeys(
  filter: Boolean = false,
  quickPassForNormalTyping: Boolean = false,
  debug: Boolean = false,
  op: FXHotkeyDSL.()->Unit,
  ) {
  contract {
	callsInPlace(op, EXACTLY_ONCE)
  }
  FXHotkeyDSL().apply(op).hotkeys.go {
	if (filter) this.node.register(inFilter = true, it, quickPassForNormalTyping, debug)
	else this.node.register(inFilter = false, it, quickPassForNormalTyping, debug)
  }
}
