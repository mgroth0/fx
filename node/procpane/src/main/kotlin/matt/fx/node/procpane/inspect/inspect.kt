package matt.fx.node.procpane.inspect

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableColumn
import matt.auto.process.ProcessOrHandleWrapper
import matt.file.construct.mFile
import matt.hurricanefx.eye.lib.onChange
import matt.hurricanefx.tornadofx.item.treetableview
import matt.hurricanefx.wrapper.control.treetable.populate
import matt.hurricanefx.wrapper.label.LabelWrapper
import matt.hurricanefx.wrapper.node.NodeWrapper
import matt.hurricanefx.wrapper.pane.hbox.HBoxWrapper
import matt.log.debug
import matt.obs.bind.binding
import matt.obs.bindings.math.div
import matt.obs.bindings.str.ObsS
import matt.obs.bindings.str.plus
import matt.obs.prop.BindableProperty
import matt.obs.prop.MObservableROValBase
import kotlin.contracts.ExperimentalContracts
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty


@ExperimentalContracts
class ProcessInspectPane(initialValue: Process?): HBoxWrapper<NodeWrapper>() {
  val procProp = SimpleObjectProperty<Process>(initialValue).apply {
	addListener { _, oldValue, newValue ->
	  if (newValue != oldValue) {
		update(newValue)
	  }
	}
  }
  private var propLabel: PropListLabel<ProcessOrHandleWrapper> = PropListLabel(
	if (initialValue != null) ProcessOrHandleWrapper(p = initialValue) else null,
	"pid" to ProcessOrHandleWrapper::pid,
	"isAlive" to ProcessOrHandleWrapper::aliveProp,
	"start time" to ProcessOrHandleWrapper::startInstant,
	"command" to ProcessOrHandleWrapper::command,
	"matt.kjlib.shell.top.arguments" to ProcessOrHandleWrapper::arguments,
	"matt.kjlib.shell.top.commandLine" to ProcessOrHandleWrapper::commandLine
  )

  fun refresh() {
	update(procProp.value)
  }

  init {
	refresh()
  }

  private fun update(p: Process?) {
	clear()
	scrollpane(this@ProcessInspectPane.propLabel) {
	  prefWidthProperty.bind(this@ProcessInspectPane.widthProperty/2)
	}
	if (p != null) {
	  @Suppress("RemoveExplicitTypeArguments")
	  treetableview<ProcessOrHandleWrapper> {
		prefWidthProperty.bind(this@ProcessInspectPane.widthProperty/2)
		val col = (column("pid") { cdf: TreeTableColumn.CellDataFeatures<ProcessOrHandleWrapper, String> ->
		  cdf.value?.value?.pid()?.let { pid ->
			SimpleStringProperty(pid.toString())
		  } ?: SimpleStringProperty("null")
		}).apply {
		  prefWidth = 100.0
		}
		sortOrder.setAll(col.node) // not working for children?
		debug("update4")
		(column("main program") { cdf: TreeTableColumn.CellDataFeatures<ProcessOrHandleWrapper, String> ->
		  cdf.value?.value?.command()?.let { com ->
			SimpleStringProperty(mFile(com).name)
		  } ?: SimpleStringProperty("null")
		}).apply {
		  prefWidth = 100.0
		}

		debug("update5")
		selectionModel.selectedItemProperty().onChange {
		  debug("select1")

		  this@ProcessInspectPane.propLabel.oProp.set(it?.value)
		  debug("select2")
		}
		root = TreeItem(ProcessOrHandleWrapper(p))
		root.isExpanded = true
		debug("update6")
		selectionModel.select(root)
		debug("update7")
		isShowRoot = true
		debug("update7.1")
		populate { it.value.obsChildren }
		debug("update8")
	  }
	  debug("update9")
	}
  }
}

private class PropListLabel<T>(
  initialValue: T? = null,
  private vararg val props: Pair<String, KAnnotatedElement>,
  op: LabelWrapper.()->Unit = {}
): LabelWrapper() {
  val oProp = SimpleObjectProperty<T>(initialValue).apply {
	onChange {
	  update(it)
	}
  }

  init {
	refresh()
	op()
  }

  fun refresh() {
	update(oProp.get())
  }

  private fun update(o: T?) {
	debug("update o 1")
	var s: ObsS = BindableProperty("")
	debug("update o 2")
	props.forEach { (name, v) ->
	  debug("update o 3: $name")
	  val value = if (o == null) {
		""
	  } else when (v) {
		is KFunction<*> -> v.call(o)
		is KProperty<*> -> v.getter.call(o)
		else            -> v
	  }
	  s = if (value is MObservableROValBase<*,*,*>) {
		s.plus("$name:\t").plus(value.binding { userString(it) }).plus("\n")
	  } else {
		s.plus("$name:\t${userString(value)}\n")
	  }
	  debug("update o 4: $name")
	}
	debug("update o 5")
	s = s.binding { it.trim() }
	debug("update o 6")
	textProperty.unbind()
	debug("update o 7")
	textProperty.bind(s)
	debug("update o 8")
  }
}

fun userString(o: Any?): String {
  return when (o) {
	null        -> "null"
	is Array<*> -> o.joinToString(
	  prefix = "[",
	  postfix = "]",
	  separator = ","
	)

	else        -> o.toString()
  }
}