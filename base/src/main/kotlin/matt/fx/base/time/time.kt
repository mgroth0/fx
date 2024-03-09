package matt.fx.base.time

import kotlin.time.Duration

typealias FXDuration = javafx.util.Duration

fun Duration.toFXDuration(): FXDuration = FXDuration.millis(inWholeMilliseconds.toDouble())
