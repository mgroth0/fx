package matt.fx.control.wrapper.control.tablelike

import matt.fx.control.wrapper.control.hascols.HasCols
import matt.fx.control.wrapper.selects.SelectingControl


interface TableLikeWrapper<E: Any>: /*ControlWrapperInterface,*/ SelectingControl<E>, HasCols<E>