package matt.fx.base.test


import javafx.beans.property.SimpleIntegerProperty
import matt.fx.base.collect.fill
import matt.fx.base.collect.observableListOf
import matt.fx.base.wrapper.obs.FXObservableBackMObservable
import matt.test.Tests
import kotlin.test.Test

class BaseTests : Tests() {
    @Test
    fun obsWrap() {
        FXObservableBackMObservable(SimpleIntegerProperty())
    }


    @Test
    fun obsListStuff() {
        observableListOf<Int>().fill(1)
    }
}
