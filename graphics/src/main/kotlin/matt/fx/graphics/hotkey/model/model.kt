package matt.fx.graphics.hotkey.model

import matt.hotkey.Hotkey
import matt.hotkey.KeyStroke
import matt.hotkey.KeyStrokeProps

const val DEFAULT_IGNORE_FIX = false


data class FxHotKey(
    val hotKey: Hotkey,
    override val previous: FxPrevHotKey? = null,
    override val isIgnoreFix: Boolean = DEFAULT_IGNORE_FIX,
) : FxHotKeyLike(), KeyStrokeProps by hotKey {

    val theOp get() = hotKey.handler
    override val keyStroke = hotKey.keyStroke
}

data class FxPrevHotKey(
    override val keyStroke: KeyStroke,
    override val previous: FxPrevHotKey? = null,
    override val isIgnoreFix: Boolean = DEFAULT_IGNORE_FIX,
) : FxHotKeyLike(), KeyStrokeProps by keyStroke

sealed class FxHotKeyLike : KeyStrokeProps {
    abstract val isIgnoreFix: Boolean
    abstract val previous: FxPrevHotKey?

    abstract val keyStroke: KeyStroke

}

