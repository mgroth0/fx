package matt.fx.graphics.node

import matt.fx.graphics.wrapper.pane.PaneWrapperImpl

interface Inspectable<N: PaneWrapperImpl<*, *>> {
    fun inspect(): N
}


