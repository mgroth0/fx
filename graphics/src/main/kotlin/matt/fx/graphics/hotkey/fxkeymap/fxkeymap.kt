package matt.fx.graphics.hotkey.fxkeymap

import javafx.scene.input.KeyCode
import matt.hotkey.Key
import matt.hotkey.Key.A
import matt.hotkey.Key.B
import matt.hotkey.Key.BACKTICK
import matt.hotkey.Key.BACK_SLASH
import matt.hotkey.Key.BACK_SPACE
import matt.hotkey.Key.C
import matt.hotkey.Key.COMMA
import matt.hotkey.Key.D
import matt.hotkey.Key.DELETE
import matt.hotkey.Key.DIGIT_0
import matt.hotkey.Key.DIGIT_1
import matt.hotkey.Key.DIGIT_2
import matt.hotkey.Key.DIGIT_3
import matt.hotkey.Key.DIGIT_4
import matt.hotkey.Key.DIGIT_5
import matt.hotkey.Key.DIGIT_6
import matt.hotkey.Key.DIGIT_7
import matt.hotkey.Key.DIGIT_8
import matt.hotkey.Key.DIGIT_9
import matt.hotkey.Key.DOWN_ARROW
import matt.hotkey.Key.E
import matt.hotkey.Key.EQUALS
import matt.hotkey.Key.ESCAPE
import matt.hotkey.Key.F
import matt.hotkey.Key.FORWARD_SLASH
import matt.hotkey.Key.G
import matt.hotkey.Key.H
import matt.hotkey.Key.I
import matt.hotkey.Key.J
import matt.hotkey.Key.K
import matt.hotkey.Key.L
import matt.hotkey.Key.LEFT_ARROW
import matt.hotkey.Key.LEFT_BRACKET
import matt.hotkey.Key.M
import matt.hotkey.Key.MINUS
import matt.hotkey.Key.N
import matt.hotkey.Key.O
import matt.hotkey.Key.P
import matt.hotkey.Key.PERIOD
import matt.hotkey.Key.Q
import matt.hotkey.Key.QUOTE
import matt.hotkey.Key.R
import matt.hotkey.Key.RETURN
import matt.hotkey.Key.RIGHT_ARROW
import matt.hotkey.Key.RIGHT_BRACKET
import matt.hotkey.Key.S
import matt.hotkey.Key.SEMICOLON
import matt.hotkey.Key.SHIFT
import matt.hotkey.Key.SPACE
import matt.hotkey.Key.T
import matt.hotkey.Key.TAB
import matt.hotkey.Key.U
import matt.hotkey.Key.UP_ARROW
import matt.hotkey.Key.V
import matt.hotkey.Key.W
import matt.hotkey.Key.X
import matt.hotkey.Key.Y
import matt.hotkey.Key.Z
import matt.hotkey.KeyStrokeProps

val KeyStrokeProps.fxKeyCode get() = key.fxKeyCode
val Key.fxKeyCode
    get() =
        when (this) {
            ESCAPE        -> KeyCode.ESCAPE
            BACKTICK      -> KeyCode.BACK_QUOTE
            DIGIT_1       -> KeyCode.DIGIT1
            DIGIT_2       -> KeyCode.DIGIT2
            DIGIT_3       -> KeyCode.DIGIT3
            DIGIT_4       -> KeyCode.DIGIT4
            DIGIT_5       -> KeyCode.DIGIT5
            DIGIT_6       -> KeyCode.DIGIT6
            DIGIT_7       -> KeyCode.DIGIT7
            DIGIT_8       -> KeyCode.DIGIT8
            DIGIT_9       -> KeyCode.DIGIT9
            DIGIT_0       -> KeyCode.DIGIT0
            MINUS         -> KeyCode.MINUS
            EQUALS        -> KeyCode.EQUALS
            DELETE        -> KeyCode.DELETE
            BACK_SPACE    -> KeyCode.BACK_SPACE
            TAB           -> KeyCode.TAB
            LEFT_BRACKET  -> KeyCode.OPEN_BRACKET
            RIGHT_BRACKET -> KeyCode.CLOSE_BRACKET
            BACK_SLASH    -> KeyCode.BACK_SLASH
            SEMICOLON     -> KeyCode.SEMICOLON
            QUOTE         -> KeyCode.QUOTE
            RETURN        -> KeyCode.ENTER
            SHIFT         -> KeyCode.SHIFT
            COMMA         -> KeyCode.COMMA
            PERIOD        -> KeyCode.PERIOD
            FORWARD_SLASH -> KeyCode.SLASH
            A             -> KeyCode.A
            B             -> KeyCode.B
            C             -> KeyCode.C
            D             -> KeyCode.D
            E             -> KeyCode.E
            F             -> KeyCode.F
            G             -> KeyCode.G
            H             -> KeyCode.H
            I             -> KeyCode.I
            J             -> KeyCode.J
            K             -> KeyCode.K
            L             -> KeyCode.L
            M             -> KeyCode.M
            N             -> KeyCode.N
            O             -> KeyCode.O
            P             -> KeyCode.P
            Q             -> KeyCode.Q
            R             -> KeyCode.R
            S             -> KeyCode.S
            T             -> KeyCode.T
            U             -> KeyCode.U
            V             -> KeyCode.V
            W             -> KeyCode.W
            X             -> KeyCode.X
            Y             -> KeyCode.Y
            Z             -> KeyCode.Z
            SPACE         -> KeyCode.SPACE
            UP_ARROW      -> KeyCode.UP
            LEFT_ARROW    -> KeyCode.LEFT
            DOWN_ARROW    -> KeyCode.DOWN
            RIGHT_ARROW   -> KeyCode.RIGHT
        }


