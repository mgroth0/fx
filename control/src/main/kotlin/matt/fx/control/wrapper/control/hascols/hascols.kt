package matt.fx.control.wrapper.control.hascols

import javafx.collections.ObservableList
import javafx.scene.control.TableColumnBase

interface HasCols<E> {
    val columns: ObservableList<out TableColumnBase<E, *>>
}
