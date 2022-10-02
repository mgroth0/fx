package matt.fx.control.wrapper.control.tablelike

import matt.hurricanefx.wrapper.control.hascols.HasCols
import matt.hurricanefx.wrapper.selects.SelectingControl

interface TableLikeWrapper<E>: /*ControlWrapperInterface,*/ SelectingControl<E>, HasCols<E>