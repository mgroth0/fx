package matt.fx.graphics.hotkey

import javafx.application.Platform.runLater
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import matt.collect.itr.areAllUnique
import matt.collect.itr.duplicates
import matt.collect.mapToSet
import matt.file.thismachine.thisMachine
import matt.fx.graphics.hotkey.fxkeymap.fxKeyCode
import matt.fx.graphics.hotkey.model.FxHotKey
import matt.fx.graphics.hotkey.model.FxHotKeyLike
import matt.fx.graphics.hotkey.model.FxPrevHotKey
import matt.fx.graphics.wrapper.EventTargetWrapper
import matt.fx.graphics.wrapper.node.impl.NodeWrapperImpl
import matt.fx.graphics.wrapper.scene.SceneWrapper
import matt.fx.graphics.wrapper.stage.StageWrapper
import matt.hotkey.ConsumeInstruction
import matt.hotkey.ConsumeInstruction.Consume
import matt.hotkey.ConsumeInstruction.DoNotConsume
import matt.hotkey.Hotkey
import matt.hotkey.HotkeyDsl
import matt.hotkey.KeyStroke
import matt.hotkey.KeyStrokeProps
import matt.lang.NEVER
import matt.lang.anno.SeeURL
import matt.lang.err
import matt.lang.go
import matt.log.NOPLogger
import matt.log.SystemOutLogger
import matt.log.logger.Logger
import matt.model.code.sys.Mac
import matt.model.op.prints.plusAssign
import matt.obs.prop.BindableProperty
import matt.obs.prop.toggle
import matt.prim.str.joinWithNewLinesAndTabs
import java.lang.System.currentTimeMillis
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract


infix fun FxHotKeyLike.matches(h: FxHotKeyLike) = this.keyStroke == h.keyStroke

infix fun KeyEvent.matches(h: KeyStrokeProps) =
    code == h.fxKeyCode && isMetaDown == h.isMeta && isAltDown == h.isOpt && isControlDown == h.isCtrl && isShiftDown == h.isShift

infix fun KeyEvent.matches(h: KeyEvent) =
    code == h.code && isMetaDown == h.isMetaDown && isAltDown == h.isAltDown && isControlDown == h.isControlDown && isShiftDown == h.isShiftDown


const val DOUBLE_HOTKEY_WINDOW_MS = 500L
var lastHotKey: Pair<FxHotKeyLike, Long>? = null


fun KeyEvent.runAgainst(
    hotkeys: Iterable<FxHotKeyLike>,
    last: KeyEvent? = null,
    fixer: HotKeyEventHandler,
    log: Logger
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


    /*https://stackoverflow.com/questions/47797980/javafx-keyevent-triggers-twice*//*the solution apparently is to use key released*//*but that feels less responsive to me. heres my custom solution... here goes...*//*potential issue: if I'm typing and I need to type same key multiple matt.hurricanefx.eye.prop.math.matt.math.op.matt.obs.math.op.matt.obs.math.double.op.times and hjavafx is laggy, it might not clear this and my keys might not go through. So I'm only doing this with keys that are actual hotkeys*/
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

                return consume()

            }
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


    hotkeys.asSequence().flatMap {
        sequence {
            yield(it)
            it.previous?.go { yield(it) }
        }
    }.filter { h ->


        log += "h(${this matches h}): $h"


        this matches h && (h.previous == null || (lastHotKey?.let {
            h.previous!!.matches(it.first) && (pressTime - it.second) <= DOUBLE_HOTKEY_WINDOW_MS
        } ?: false))


    }.forEach { h ->
        lastHotKey = h to currentTimeMillis()
        if (!h.isIgnoreFix) {
            /*And yet I still set fixer.last = null below? confused.*/
            fixer.last = this
        }
        if (isConsumed) return
        when (h) {
            is FxHotKey     -> {
                val consumeInstruction = h.theOp()
                runLater {
                    fixer.last = null
                } /*... finally got it. not to early not too late. wow.*/

                when (consumeInstruction) {
                    Consume      -> return consume()
                    DoNotConsume -> Unit
                }
            }

            is FxPrevHotKey -> Unit
        }
    }
}

class HotKeyEventHandler(
    var quickPassForNormalTyping: Boolean
) : EventHandler<KeyEvent> {

    var debug: Boolean = false

    val hotkeys = mutableListOf<FxHotKeyLike>()

    init {
        hotkeys.forEach {
            if (it.fxKeyCode == KeyCode.H && it.isMeta && !it.isCtrl && !it.isOpt && !it.isShift) {
                error("meta H is blocked by KM to prevent OS window hiding")
            }
            if (it.isOpt && !it.fxKeyCode.isArrowKey && !it.isMeta && !it.isCtrl && !it.isShift && (thisMachine is Mac)
            ) {
                error(
                    "I think MacOS has problems with opt where it sends an extra key typed event. stupid. also seen in intellij"
                )
            }
        }
    }

    var last: KeyEvent? = null
    override fun handle(event: KeyEvent) {
        if (quickPassForNormalTyping && (event.code.isDigitKey || event.code.isLetterKey) && !event.isMetaDown && !event.isControlDown && !event.isAltDown) {
            return
        }
        event.runAgainst(hotkeys, last = last, fixer = this, log = if (debug) SystemOutLogger else NOPLogger)
    }
}

fun <K, V> Map<K, V>.invert(): Map<V, K> {
    require(values.areAllUnique())
    return this.map { it.value to it.key }.associate { it.first to it.second }
}


@Synchronized
fun EventTargetWrapper.register(
    inFilter: Boolean,
    hotkeys: Iterable<FxHotKeyLike>,
    quickPassForNormalTyping: Boolean = false,
    debug: Boolean = false,
) {
    val handler = (if (inFilter) hotKeyFilter else hotKeyHandler) ?: HotKeyEventHandler(quickPassForNormalTyping).also {
        if (inFilter) {
            hotKeyFilter = it
        } else {
            hotKeyHandler = it
        }
        if (inFilter) {
            when (this) {
                is NodeWrapperImpl<*> -> addEventFilter(KeyEvent.KEY_PRESSED, it)
                is SceneWrapper<*>    -> addEventFilter(KeyEvent.KEY_PRESSED, it)
                is StageWrapper       -> addEventFilter(KeyEvent.KEY_PRESSED, it)
                else                  -> NEVER
            }
        } else {
            when (this) {
                is NodeWrapperImpl<*> -> addEventHandler(KeyEvent.KEY_PRESSED, it)
                is SceneWrapper<*>    -> addEventHandler(KeyEvent.KEY_PRESSED, it)
                is StageWrapper       -> addEventHandler(KeyEvent.KEY_PRESSED, it)
                else                  -> NEVER
            }
        }
    }
    if (quickPassForNormalTyping) handler.quickPassForNormalTyping = true
    handler.hotkeys.addAll(hotkeys.toList())
    if (debug) handler.debug = true
    val dups = handler.hotkeys.duplicates()
    if (dups.isNotEmpty()) { /*doesnt check handlers and filters together, and doesn't check from multiple nodes together!*/
        err(
            """
	  hotkey duplicates!:
	  ${dups.joinWithNewLinesAndTabs()}
	""".trimIndent()
        )
    }
}


context(HotkeyDsl)
infix fun KeyStroke.toggles(b: BindableProperty<Boolean>) = op { b.toggle() }

@Suppress("PropertyName")
class FXHotkeyDSL : HotkeyDsl() {


    private val fxSpecialHotKeys = mutableSetOf<FxHotKeyLike>()

    fun buildFxHotkeys() = setOf(
        *fxSpecialHotKeys.toTypedArray(),
        *buildHotkeys().mapToSet { FxHotKey(it) }.toTypedArray()
    )

    fun KeyStroke.op(
        ignoreFix: Boolean,
        setOp: () -> Unit
    ) = apply {
        fxSpecialHotKeys.add(FxHotKey(Hotkey(this) {
            setOp()
            ConsumeInstruction.Consume
        }, isIgnoreFix = ignoreFix))
    }

    fun KeyStroke.conditionalOp(
        ignoreFix: Boolean,
        setOp: () -> ConsumeInstruction
    ) = apply {
        fxSpecialHotKeys.add(FxHotKey(Hotkey(this) {
            setOp()
        }, isIgnoreFix = ignoreFix))
    }

    infix fun KeyStroke.then(
        other: Hotkey
    ) = apply {
        fxSpecialHotKeys.add(FxHotKey(other, previous = FxPrevHotKey(keyStroke = this)))
    }


}

@SeeURL("https://youtrack.jetbrains.com/issue/KT-65158/K2-Contracts-False-positive-WRONGINVOCATIONKIND-with-unrelated-higher-order-function-call")
@Suppress("WRONG_INVOCATION_KIND")
inline fun EventTargetWrapper.hotkeys(
    filter: Boolean = false,
    quickPassForNormalTyping: Boolean = false,
    debug: Boolean = false,
    op: FXHotkeyDSL.() -> Unit,
) {
    contract {
        callsInPlace(op, EXACTLY_ONCE)
    }
    FXHotkeyDSL().apply(op).buildFxHotkeys().go {
        if (filter) this.register(inFilter = true, it, quickPassForNormalTyping, debug)
        else this.register(inFilter = false, it, quickPassForNormalTyping, debug)
    }
}
