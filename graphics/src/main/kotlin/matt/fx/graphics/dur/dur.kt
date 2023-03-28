package matt.fx.graphics.dur

import javafx.util.Duration
import matt.fx.base.time.toFXDuration
import matt.time.dur.wrap.DurationWrapper

val YesIUse by lazy { DurationWrapper::class }

fun DurationWrapper.toFXDuration(): Duration = dur.toFXDuration()