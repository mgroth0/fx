package matt.fx.node.procpane.inspect

import javafx.scene.control.TreeTableColumn
import matt.auto.process.ProcessOrHandleWrapper
import matt.file.construct.mFile
import matt.fx.control.wrapper.control.treetable.populate
import matt.fx.control.wrapper.control.treetable.treetableview
import matt.fx.control.wrapper.label.LabelWrapper
import matt.fx.control.wrapper.scroll.scrollpane
import matt.fx.control.wrapper.treeitem.TreeItemWrapper
import matt.fx.graphics.fxthread.ts.nonBlockingFXWatcher
import matt.fx.graphics.wrapper.node.NodeWrapper
import matt.fx.graphics.wrapper.pane.hbox.HBoxWrapperImpl
import matt.obs.bind.binding
import matt.obs.bindings.str.ObsS
import matt.obs.bindings.str.plus
import matt.obs.listen.OldAndNewListenerImpl
import matt.obs.math.double.op.div
import matt.obs.prop.BindableProperty
import matt.obs.prop.MObservableROValBase
import matt.obs.prop.VarProp
import kotlin.contracts.ExperimentalContracts
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty


@ExperimentalContracts
class ProcessInspectPane(initialValue: Process?) : HBoxWrapperImpl<NodeWrapper>() {
    val procProp = VarProp<Process?>(initialValue).apply {
        addListener(OldAndNewListenerImpl { oldValue, newValue ->
            if (newValue != oldValue) {
                update(newValue)
            }

        })
    }
    private val propLabel: PropListLabel<ProcessOrHandleWrapper> by lazy {



        PropListLabel(
            if (initialValue != null) ProcessOrHandleWrapper(p = initialValue) else null,
            "pid" to ProcessOrHandleWrapper::pid,
            "isAlive" to ProcessOrHandleWrapper::aliveProp,
            "start time" to ProcessOrHandleWrapper::startInstant,
            "command" to ProcessOrHandleWrapper::command,
            "matt.kjlib.shell.top.arguments" to ProcessOrHandleWrapper::arguments,
            "matt.kjlib.shell.top.commandLine" to ProcessOrHandleWrapper::commandLine
        )
    }

    fun refresh() {
        update(procProp.value)
    }

    init {
        refresh()
    }

    private fun update(p: Process?) {
        clear()
        scrollpane(this@ProcessInspectPane.propLabel) {
            prefWidthProperty.bind(this@ProcessInspectPane.widthProperty / 2.0)
        }
        if (p != null) {
            treetableview<ProcessOrHandleWrapper> {
                prefWidthProperty.bind(this@ProcessInspectPane.widthProperty / 2.0)
                val col = (column("pid") { cdf: TreeTableColumn.CellDataFeatures<ProcessOrHandleWrapper, String> ->
                    cdf.value?.value?.pid()?.let { pid ->
                        BindableProperty(pid.toString())
                    } ?: BindableProperty("null")
                }).apply {
                    prefWidth = 100.0
                }
                sortOrder.setAll(col.node) // not working for matt.fx.control.layout.children?
                (column("main program") { cdf: TreeTableColumn.CellDataFeatures<ProcessOrHandleWrapper, String> ->
                    cdf.value?.value?.command()?.let { com ->
                        BindableProperty(mFile(com).name)
                    } ?: BindableProperty("null")
                }).apply {
                    prefWidth = 100.0
                }

                selectionModel.selectedItemProperty.onChange {

                    this@ProcessInspectPane.propLabel.oProp v (it?.value)
                }
                root = TreeItemWrapper(ProcessOrHandleWrapper(p))
                root!!.isExpanded = true
                selectionModel.select(root!!.node)
                isShowRoot = true
                populate { it.value.obsChildren }
            }
        }
    }
}

private class PropListLabel<T>(
    initialValue: T? = null,
    private vararg val props: Pair<String, KAnnotatedElement>,
    op: LabelWrapper.() -> Unit = {}
) : LabelWrapper() {
    val oProp = VarProp<T?>(initialValue).apply {
        onChange {
            update(it)
        }
    }

    init {
        refresh()
        op()
    }

    fun refresh() {
        update(oProp.value)
    }

    private fun update(o: T?) {
        var s: ObsS = BindableProperty("")
        props.forEach { (name, v) ->
            val value = if (o == null) {
                ""
            } else when (v) {
                is KFunction<*> -> v.call(o)
                is KProperty<*> -> v.getter.call(o)
                else            -> v
            }
            s = if (value is MObservableROValBase<*, *, *>) {
                s.plus("$name:\t").plus(value.nonBlockingFXWatcher().binding { userString(it) }).plus("\n")
            } else {
                s.plus("$name:\t${userString(value)}\n")
            }
        }
        s = s.binding { it.trim() }
        textProperty.unbind()
        textProperty.bind(s)
    }
}

fun userString(o: Any?): String {
    return when (o) {
        null        -> "null"
        is Array<*> -> o.joinToString(
            prefix = "[", postfix = "]", separator = ","
        )

        else        -> o.toString()
    }
}