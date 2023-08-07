package matt.fx.base.wrapper.obs

import javafx.beans.Observable
import matt.obs.oobj.MObservableObject
import matt.obs.oobj.ObservableObject

fun Observable.toMObservable(): MObservableObject<out Any> = FXObservableBackMObservable(this)

open class FXObservableBackMObservable(o: Observable) : ObservableObject<FXObservableBackMObservable>() {
    init {
        o.addListener {
            markInvalid()
        }
    }
}
