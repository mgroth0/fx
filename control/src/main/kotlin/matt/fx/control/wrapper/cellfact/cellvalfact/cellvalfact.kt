package matt.fx.control.wrapper.cellfact.cellvalfact

import javafx.beans.value.ObservableValue
import javafx.util.Callback

interface CellValueFactory<D, P> {
  var cellValueFactory: Callback<D, ObservableValue<P>>?

  fun setCellValueFact(value: Callback<D, ObservableValue<P>>?) {
	cellValueFactory = value
  }
}