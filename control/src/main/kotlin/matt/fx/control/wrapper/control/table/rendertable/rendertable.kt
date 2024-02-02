package matt.fx.control.wrapper.control.table.rendertable

import matt.fig.render.FormattedTableRenderer
import matt.fig.render.tabular.FormattedTabularData
import matt.fx.control.wrapper.control.table.tableview
import matt.fx.control.wrapper.label.label
import matt.fx.graphics.wrapper.getterdsl.buildNode
import matt.fx.graphics.wrapper.pane.vbox.VBoxWrapper
import matt.fx.graphics.wrapper.pane.vbox.v
import matt.lang.go
import matt.obs.col.olist.toBasicObservableList
import matt.obs.prop.toVarProp


class FXTableRenderer : FormattedTableRenderer<VBoxWrapper<*>> {


    override fun render(figData: FormattedTabularData<*, *, *>): VBoxWrapper<*> = buildNode {
        v {
            figData.title?.go {
                label(it)
            }
            tableview(items = figData.rows().map { it.value }.toBasicObservableList()) {

                column(
                    title = "row name",
                ) {
                    it.value.toVarProp()
                }

                figData.columns().forEach { col ->
                    column(
                        title = col.value
                    ) {
                        col[it.value].toVarProp()
                    }
                }
            }
        }

    }

}
