package matt.fx.control.wrapper.cellfact.cellvalfact

import javafx.util.Callback
import matt.lang.anno.Open
import matt.obs.prop.ObsVal

interface CellValueFactory<D, P> {
    var cellValueFactory: Callback<D, ObsVal<P>>?

    @Open
    fun setCellValueFact(value: Callback<D, ObsVal<P>>?) {
        cellValueFactory = value
    }
}
